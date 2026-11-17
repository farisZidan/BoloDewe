package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import com.example.data.model.Product
import com.example.data.model.SalesTransaction
import com.example.data.model.ForecastItem
import com.example.data.remote.BoloDeweApi
import com.example.data.remote.LoginRequest
import com.example.data.remote.RemoteSaleItem
import com.example.data.remote.SalesRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class BoloDeweRepository(
    private val context: Context
) {
    companion object {
        private const val TAG = "BoloDeweRepo"
        private const val PREFS_NAME = "BoloDewePrefs"
        private const val TOKEN_KEY = "jwt_token"
        private const val LOGIN_PATH = "/api/login"
        private const val BASE_URL = "https://home-lab-server.web.id/"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Volatile
    private var token: String? = sharedPrefs.getString(TOKEN_KEY, null)

    init {
        // Remove an expired or malformed JWT left over from an earlier app session.
        token?.let { savedToken ->
            if (!isJwtLocallyValid(savedToken)) {
                clearTokenIfUnchanged(savedToken)
            }
        }
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // BASIC deliberately avoids logging request/response bodies, which may
        // contain credentials. Authorization is redacted as an extra precaution.
        level = HttpLoggingInterceptor.Level.BASIC
        redactHeader("Authorization")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val isLoginRequest = originalRequest.url.encodedPath.endsWith(LOGIN_PATH)

            // Capture the token used by this request. If another request logs in
            // while this one is running, its 401 must not erase the newer token.
            val tokenUsed = if (isLoginRequest) null else token

            val requestBuilder = originalRequest.newBuilder()
                .removeHeader("Authorization")

            if (!tokenUsed.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $tokenUsed")
            }

            val response = chain.proceed(requestBuilder.build())

            // This backend currently exposes login but no refresh-token endpoint.
            // Invalidate the rejected token so the UI can ask the user to log in.
            if (response.code == 401 && tokenUsed != null) {
                clearTokenIfUnchanged(tokenUsed)
            }

            response
        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api = retrofit.create(BoloDeweApi::class.java)

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: Flow<List<Product>> = _allProducts.asStateFlow()
    val completeCatalog: Flow<List<Product>> = _allProducts.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<SalesTransaction>>(emptyList())
    val allTransactions: Flow<List<SalesTransaction>> = _allTransactions.asStateFlow()

    fun getJwtToken(): String? = token

    /**
     * Checks the JWT expiry locally for UI/session convenience.
     * This does NOT verify the JWT signature; the server remains authoritative.
     */
    fun isLoggedIn(): Boolean {
        val currentToken = token ?: return false

        if (!isJwtLocallyValid(currentToken)) {
            clearTokenIfUnchanged(currentToken)
            return false
        }

        return true
    }

    private fun isJwtLocallyValid(jwt: String): Boolean {
        return try {
            val parts = jwt.split(".")
            if (parts.size != 3) return false

            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
            val payload = JSONObject(String(payloadBytes, Charsets.UTF_8))
            val expirySeconds = payload.optLong("exp", 0L)

            // Treat missing/invalid exp as an unusable token.
            expirySeconds > (System.currentTimeMillis() / 1000L)
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    private fun clearTokenIfUnchanged(rejectedToken: String) {
        if (token == rejectedToken) {
            token = null
            sharedPrefs.edit().remove(TOKEN_KEY).apply()
        }
    }

    private fun saveToken(newToken: String) {
        synchronized(this) {
            token = newToken
            sharedPrefs.edit().putString(TOKEN_KEY, newToken).apply()
        }
    }

    fun logout() {
        synchronized(this) {
            token = null
            sharedPrefs.edit().remove(TOKEN_KEY).apply()
        }
    }

    fun saveUriToLocalFile(uriString: String): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "product_${System.currentTimeMillis()}.jpg")

            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save uri to local file", e)
            null
        }
    }

    suspend fun login(username: String, passwordHash: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // The login endpoint must be called without an old Bearer token.
                val response = api.login(LoginRequest(username, passwordHash))

                if (!response.isSuccessful) {
                    Log.e(TAG, "Login failed: HTTP ${response.code()}")
                    return@withContext false
                }

                val newToken = response.body()?.token
                if (newToken.isNullOrBlank() || !isJwtLocallyValid(newToken)) {
                    Log.e(TAG, "Login response did not contain a valid, unexpired JWT")
                    return@withContext false
                }

                saveToken(newToken)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Login network error", e)
                false
            }
        }

    suspend fun getAttributes(): com.example.data.model.AttributesResponse? =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAttributes()
                if (response.isSuccessful) response.body() else {
                    if (response.code() == 401) Log.w(TAG, "Session expired while fetching attributes")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed fetching attributes from server", e)
                null
            }
        }

    suspend fun refreshProducts() = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts()
            if (response.isSuccessful) {
                response.body()?.let { _allProducts.value = it }
            } else {
                throw Exception(
                    if (response.code() == 401) {
                        "Sesi login telah berakhir. Silakan login kembali."
                    } else {
                        "Masalah Jaringan/Server: HTTP ${response.code()}"
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed refreshing products from server", e)
            throw Exception("Gagal mengambil produk: ${e.localizedMessage}", e)
        }
    }

    suspend fun refreshSales() = withContext(Dispatchers.IO) {
        try {
            val response = api.getSales()
            if (response.isSuccessful) {
                response.body()?.let { _allTransactions.value = it }
            } else {
                if (response.code() == 401) {
                    Log.w(TAG, "Session expired while fetching sales")
                } else {
                    Log.e(TAG, "Gagal memuat transaksi: HTTP ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error jaringan saat memuat transaksi", e)
        }
    }

    suspend fun addProduct(
        product: Product,
        imageBytes: ByteArray? = null,
        contentType: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("kode_sku", product.kode_sku)
                .addFormDataPart("nama_produk", product.nama_produk)
                .addFormDataPart("category_id", product.category_id.toString())
                .addFormDataPart("brand_id", product.brand_id.toString())
                .addFormDataPart("gender_id", product.gender_id.toString())
                .addFormDataPart("age_group_id", product.age_group_id.toString())
                .addFormDataPart("material_id", product.material_id.toString())
                .addFormDataPart("cutting_id", product.cutting_id.toString())
                .addFormDataPart("fit_id", product.fit_id.toString())
                .addFormDataPart("color_id", product.color_id.toString())
                .addFormDataPart("size_group_id", product.size_group_id.toString())

            if (imageBytes != null && contentType != null) {
                val requestBody = imageBytes.toRequestBody(
                    contentType.toMediaTypeOrNull(),
                    0,
                    imageBytes.size
                )
                builder.addFormDataPart(
                    "gambar",
                    "upload_img_${System.currentTimeMillis()}.jpg",
                    requestBody
                )
            }

            val response = api.createProduct(builder.build())
            if (response.isSuccessful) {
                refreshProducts()
                true
            } else {
                throw Exception("Server menolak penambahan produk (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Server sync failed, product not created", e)
            throw Exception("Gagal menambahkan produk: ${e.localizedMessage}", e)
        }
    }

    suspend fun updateProduct(
        product: Product,
        imageBytes: ByteArray? = null,
        contentType: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("kode_sku", product.kode_sku)
                .addFormDataPart("nama_produk", product.nama_produk)
                .addFormDataPart("category_id", product.category_id.toString())
                .addFormDataPart("brand_id", product.brand_id.toString())
                .addFormDataPart("gender_id", product.gender_id.toString())
                .addFormDataPart("age_group_id", product.age_group_id.toString())
                .addFormDataPart("material_id", product.material_id.toString())
                .addFormDataPart("cutting_id", product.cutting_id.toString())
                .addFormDataPart("fit_id", product.fit_id.toString())
                .addFormDataPart("color_id", product.color_id.toString())
                .addFormDataPart("size_group_id", product.size_group_id.toString())

            if (imageBytes != null && contentType != null) {
                val requestBody = imageBytes.toRequestBody(
                    contentType.toMediaTypeOrNull(),
                    0,
                    imageBytes.size
                )
                builder.addFormDataPart(
                    "gambar",
                    "upload_img_${System.currentTimeMillis()}.jpg",
                    requestBody
                )
            }

            val response = api.updateProduct(product.id, builder.build())
            if (response.isSuccessful) {
                refreshProducts()
                true
            } else {
                throw Exception("Gagal memperbarui produk (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Server edit failed", e)
            throw Exception("Gagal memperbarui produk: ${e.localizedMessage}", e)
        }
    }

    suspend fun deleteProduct(id: Int, sku: String) = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProduct(id)
            val responseBody = response.body()?.string()
                ?: response.errorBody()?.string()
                ?: ""

            if (response.isSuccessful &&
                !responseBody.contains("error", ignoreCase = true) &&
                !responseBody.contains("gagal", ignoreCase = true) &&
                !responseBody.contains("failed", ignoreCase = true)
            ) {
                refreshProducts()
            } else {
                val detail = if (responseBody.length in 1..200) " ($responseBody)" else ""
                throw Exception("Gagal menghapus produk (HTTP ${response.code()})$detail")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Server delete failed for SKU $sku", e)
            throw Exception("Terjadi masalah saat menghapus produk: ${e.localizedMessage}", e)
        }
    }

    suspend fun deleteTransaction(transactionId: Int) = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteSale(transactionId)
            if (response.isSuccessful) {
                refreshSales()
            } else {
                throw Exception("Gagal menghapus transaksi (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Server delete sale failed", e)
            throw Exception("Terjadi kesalahan saat menghapus transaksi: ${e.localizedMessage}", e)
        }
    }

    suspend fun updateSale(
        transactionId: Int,
        productId: Int,
        date: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val formattedDate = if (date.length == 10) "$date 00:00:00" else date
            val response = api.updateSale(
                transactionId,
                SalesRequest(
                    id_transaksi = transactionId,
                    product_id = productId,
                    tanggal = formattedDate
                )
            )
            if (response.isSuccessful) {
                refreshSales()
                true
            } else {
                Log.e(TAG, "Gagal update transaksi: HTTP ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Server edit sale failed", e)
            false
        }
    }

    suspend fun getForecast(): List<ForecastItem> = withContext(Dispatchers.IO) {
        try {
            val response = api.getForecast()
            if (response.isSuccessful) response.body().orEmpty() else {
                if (response.code() == 401) Log.w(TAG, "Session expired while fetching forecast")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch forecast from server", e)
            emptyList()
        }
    }

    suspend fun addSale(
        productId: Int,
        date: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val formattedDate = if (date.length == 10) "$date 00:00:00" else date
            val response = api.createSale(
                SalesRequest(
                    id_transaksi = 0,
                    product_id = productId,
                    tanggal = formattedDate
                )
            )
            if (response.isSuccessful) {
                refreshProducts()
                refreshSales()
                true
            } else {
                throw Exception("Server menolak rekaman penjualan (HTTP ${response.code()})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed posting transaction online", e)
            throw Exception("Gagal menyinkronkan transaksi penjualan: ${e.localizedMessage}", e)
        }
    }

    suspend fun fetchSalesChart(): Map<String, Map<String, Int>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getSalesChart()
                if (response.isSuccessful) {
                    response.body() ?: emptyMap()
                } else {
                    Log.w(TAG, "Sales chart request failed: HTTP ${response.code()}")
                    computeChartFromLocalSales()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server charts unreachable; using local fallback", e)
                computeChartFromLocalSales()
            }
        }

    private suspend fun computeChartFromLocalSales(): Map<String, Map<String, Int>> {
        return emptyMap()
    }

    private suspend fun seedInitialLocalProductsIfEmpty() {
        // Dummy data removed.
    }

    private suspend fun seedInitialLocalTransactionsIfEmpty() {
        // Dummy data removed.
    }
}

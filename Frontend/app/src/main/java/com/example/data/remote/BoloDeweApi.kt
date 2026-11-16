package com.example.data.remote

import com.example.data.model.Product
import com.example.data.model.SalesTransaction
import com.example.data.model.ForecastItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    @Json(name = "password_hash")
    val passwordHash: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String
)

@JsonClass(generateAdapter = true)
data class SalesRequest(
    val product_id: Int,
    val tanggal: String
)

@JsonClass(generateAdapter = true)
data class RemoteSaleItem(
    @Json(name = "kode_sku") val kode_sku: String,
    @Json(name = "nama_produk") val nama_produk: String
)

interface BoloDeweApi {

    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/products")
    suspend fun getProducts(): Response<List<Product>>

    @GET("api/attributes")
    suspend fun getAttributes(): Response<com.example.data.model.AttributesResponse>

    @POST("api/products")
    suspend fun createProduct(
        @Body body: okhttp3.MultipartBody
    ): Response<okhttp3.ResponseBody>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body body: okhttp3.MultipartBody
    ): Response<okhttp3.ResponseBody>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int
    ): Response<okhttp3.ResponseBody>

    @GET("api/sales")
    suspend fun getSales(): Response<Map<String, List<RemoteSaleItem>>>

    @POST("api/sales")
    suspend fun createSale(
        @Body request: SalesRequest
    ): Response<okhttp3.ResponseBody>

    @PUT("api/sales/{id}")
    suspend fun updateSale(
        @Path("id") id: Int,
        @Body request: SalesRequest
    ): Response<okhttp3.ResponseBody>

    @DELETE("api/sales/{id}")
    suspend fun deleteSale(
        @Path("id") id: Int
    ): Response<okhttp3.ResponseBody>

    @GET("api/sales/chart")
    suspend fun getSalesChart(): Response<Map<String, Map<String, Int>>>

    @GET("api/forecast")
    suspend fun getForecast(): Response<List<ForecastItem>>
}

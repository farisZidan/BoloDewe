package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Product
import com.example.data.model.SalesTransaction
import com.example.data.model.ForecastItem
import com.example.data.repository.BoloDeweRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class BoloDeweViewModel(private val repository: BoloDeweRepository) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = flow {
        emit(repository.isLoggedIn())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.isLoggedIn())

    private val _isLoginSuccess = MutableSharedFlow<Boolean>()
    val isLoginSuccess = _isLoginSuccess.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    data class AppNotification(val isSuccess: Boolean, val title: String, val message: String)

    private val _appNotification = MutableStateFlow<AppNotification?>(null)
    val appNotification = _appNotification.asStateFlow()

    private var notificationJob: kotlinx.coroutines.Job? = null

    fun clearNotification() {
        notificationJob?.cancel()
        _appNotification.value = null
    }

    fun showSuccess(title: String, message: String) {
        notificationJob?.cancel()
        _appNotification.value = AppNotification(true, title, message)
        notificationJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _appNotification.value = null
        }
    }

    fun showErrorInfo(title: String, message: String) {
        notificationJob?.cancel()
        _appNotification.value = AppNotification(false, title, message)
        notificationJob = viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _appNotification.value = null
        }
    }

    // Banner state removed

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completeCatalog: StateFlow<List<Product>> = repository.completeCatalog
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<SalesTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _salesChartData = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val salesChartData = _salesChartData.asStateFlow()

    private val _forecastList = MutableStateFlow<List<ForecastItem>>(emptyList())
    val forecastList = _forecastList.asStateFlow()

    private val _attributes = MutableStateFlow<com.example.data.model.AttributesResponse?>(null)
    val attributes = _attributes.asStateFlow()

    private val _selectedTab = MutableStateFlow(1)
    val selectedTab = _selectedTab.asStateFlow()

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    init {
        if (repository.isLoggedIn()) {
            refreshData()
            startPeriodicSync()
        }
    }

    private var syncJob: kotlinx.coroutines.Job? = null

    fun startPeriodicSync() {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(10000) // Sync every 10 seconds
                try {
                    repository.refreshProducts()
                    repository.refreshSales()
                } catch (e: Exception) {
                    Log.e("BoloDeweViewModel", "Periodic sync failed", e)
                }
            }
        }
    }

    fun login(username: String, passwordHash: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val success = repository.login(username, passwordHash)
            _isLoading.value = false
            if (success) {
                _isLoginSuccess.emit(true)
                showSuccess("SUKSES", "Login Berhasil")
                refreshData()
                startPeriodicSync()
            } else {
                _errorMessage.value = "Username atau Password salah!"
                showErrorInfo("GAGAL", "Username atau Password salah!")
            }
        }
    }

    fun logout() {
        repository.logout()
        syncJob?.cancel()
        _salesChartData.value = emptyMap()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _attributes.value = repository.getAttributes()
                repository.refreshProducts()
                repository.refreshSales()
                val chart = repository.fetchSalesChart()
                _salesChartData.value = chart
                fetchForecast()
            } catch (e: Exception) {
                Log.e("BoloDeweViewModel", "Background refresh failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchForecast() {
        viewModelScope.launch {
            try {
                val netForecast = repository.getForecast()
                if (netForecast.isNotEmpty()) {
                    _forecastList.value = netForecast
                } else {
                    val dbProducts = allProducts.value
                    val dbTransactions = allTransactions.value
                    val computed = dbProducts.map { prod ->
                        val totalQty = dbTransactions.filter { it.product_id == prod.id }.count()
                        val ramalan = if (totalQty > 0) (totalQty * 1.5).toInt().coerceAtMost(50) else 0
                        val statusText = if (dbTransactions.isNotEmpty()) "Sukses" else "Data kurang dari 2 bulan"
                        ForecastItem(
                            kode_sku = prod.kode_sku,
                            nama_produk = prod.nama_produk,
                            ramalan_final = ramalan,
                            status = statusText
                        )
                    }
                    _forecastList.value = computed
                }
            } catch (e: java.lang.Exception) {
                // ignore
            }
        }
    }

    fun addProduct(
        product: Product,
        gambarUri: String? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val finalGambar = if (gambarUri != null) {
                repository.saveUriToLocalFile(gambarUri)
            } else {
                null
            }
            val imageBytes = if (finalGambar != null) {
                try {
                    java.io.File(finalGambar).readBytes()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            val productToSave = product.copy(gambar = finalGambar)
            val success = try {
                repository.addProduct(productToSave, imageBytes, "image/jpeg")
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal menambah produk."
                _errorMessage.value = msg
                showErrorInfo("GAGAL", msg)
                false
            }
            _isLoading.value = false
            if (success) {
                showSuccess("SUKSES", "Barang Telah Berhasil Di Tambahkan")
                onComplete()
                refreshData()
            }
        }
    }

    fun updateProduct(
        product: Product,
        newGambarUri: String? = null,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            var finalGambar = product.gambar
            var imageBytes: ByteArray? = null
            if (newGambarUri != null) {
                val savedPath = repository.saveUriToLocalFile(newGambarUri)
                if (savedPath != null) {
                    finalGambar = savedPath
                    try {
                        imageBytes = java.io.File(savedPath).readBytes()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }

            val updatedProduct = product.copy(gambar = finalGambar)
            val success = try {
                repository.updateProduct(updatedProduct, imageBytes, "image/jpeg")
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal memperbaharui produk."
                _errorMessage.value = msg
                showErrorInfo("GAGAL", msg)
                false
            }
            _isLoading.value = false
            if (success) {
                showSuccess("SUKSES", "Barang Telah Berhasil Diperbarui")
                onComplete()
                refreshData()
            }
        }
    }

    fun deleteProduct(id: Int, sku: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(id, sku)
                showSuccess("SUKSES", "Produk Berhasil Dihapus")
                refreshData()
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal menghapus produk."
                _errorMessage.value = msg
                showErrorInfo("GAGAL", msg)
            }
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(id)
                showSuccess("SUKSES", "Transaksi Berhasil Dihapus")
                refreshData()
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal menghapus transaksi."
                _errorMessage.value = msg
                showErrorInfo("GAGAL", msg)
            }
        }
    }

    fun addSaleTransaction(
        productId: Int,
        dateString: String // format "yyyy-MM-dd" or will fallback
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            // Format dynamic time
            val sdfTime = SimpleDateFormat("HH:mm", Locale.US)
            val currentTime = sdfTime.format(Date())

            val finalDate = if (dateString.isEmpty() || dateString.contains("yyyy")) {
                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val str = sdfDate.format(Date())
                str
            } else {
                dateString
            }

            val success = try {
                repository.addSale(
                    productId = productId,
                    date = finalDate
                )
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal menyimpan transaksi."
                _errorMessage.value = msg
                showErrorInfo("GAGAL", msg)
                false
            }
            _isLoading.value = false
            if (success) {
                showSuccess("SUKSES", "Transaksi Berhasil Ditambahkan")
            }
        }
    }

    fun updateSaleTransaction(
        transactionId: Int,
        productId: Int,
        dateString: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = try {
                repository.updateSale(
                    transactionId = transactionId,
                    productId = productId,
                    date = dateString
                )
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Gagal memperbaharui transaksi."
                _errorMessage.value = msg
                showErrorInfo("GAGAL", msg)
                false
            }
            _isLoading.value = false
            if (success) {
                showSuccess("SUKSES", "Data Berhasil Disimpan")
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(private val repository: BoloDeweRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BoloDeweViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BoloDeweViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

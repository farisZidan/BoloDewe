package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SalesTransaction(
    @Json(name = "id_transaksi")
    val id_transaksi: Int = 0,
    @Json(name = "product_id")
    val product_id: Int,
    @Json(name = "tanggal")
    val tanggal: String // String representation format "yyyy-MM-dd"
)

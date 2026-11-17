package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Product(
    @Json(name = "id_produk")
    val id: Int = 0,
    @Json(name = "kode_sku")
    val kode_sku: String,
    @Json(name = "nama_produk")
    val nama_produk: String,
    @Json(name = "gambar")
    val gambar: String?,
    @Json(name = "category_id")
    val category_id: Int = 1,
    @Json(name = "brand_id")
    val brand_id: Int = 1,
    @Json(name = "gender_id")
    val gender_id: Int = 1,
    @Json(name = "age_group_id")
    val age_group_id: Int = 1,
    @Json(name = "material_id")
    val material_id: Int = 1,
    @Json(name = "cutting_id")
    val cutting_id: Int = 1,
    @Json(name = "fit_id")
    val fit_id: Int = 1,
    @Json(name = "color_id")
    val color_id: Int = 1,
    @Json(name = "size_group_id")
    val size_group_id: Int = 1
)

fun resolveProductImageUrl(gambar: String?): String? {
    val img = gambar ?: return null
    if (img.isEmpty()) return null
    if (img.startsWith("/") || img.startsWith("content:") || img.startsWith("file:") || img.startsWith("http")) {
        return img
    }
    return "http://home-lab-server.web.id/uploads/$img"
}

@JsonClass(generateAdapter = true)
data class ForecastItem(
    @Json(name = "sku") val sku: String? = "",
    @Json(name = "nama_produk") val nama_produk: String? = "",
    @Json(name = "prediksi_bulan_depan") val prediksi_bulan_depan: Int? = 0,
    val status: String? = ""
) {
    val kode_sku: String? get() = sku
    val ramalan_final: Int? get() = prediksi_bulan_depan
}



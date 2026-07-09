package com.bolodewe

import kotlinx.serialization.Serializable

@Serializable
data class UserLogin(val username: String, val password_hash: String)

@Serializable
data class LoginResponse(val token: String)

@Serializable
data class ProductDTO(
    val id_produk: Int, val kode_sku: String,
    val nama_produk: String, val gambar: String? = null,
    val category_id: Int, val brand_id: Int, val gender_id: Int,
    val age_group_id: Int, val material_id: Int, val cutting_id: Int, val fit_id: Int,
    val color_id: Int, val size_group_id: Int
)

@Serializable
data class AttributeItemDTO(
    val id: Int,
    val name: String
)

@Serializable
data class AllAttributesDTO(
    val categories: List<AttributeItemDTO>,
    val materials: List<AttributeItemDTO>,
    val brands: List<AttributeItemDTO>,
    val size_groups: List<AttributeItemDTO>,
    val genders: List<AttributeItemDTO>,
    val cuttings: List<AttributeItemDTO>,
    val fits: List<AttributeItemDTO>,
    val colors: List<AttributeItemDTO>,
    val age_groups: List<AttributeItemDTO>
)
@Serializable
data class SalesDTO(val id_transaksi: Int, val product_id: Int, val tanggal: String)

@Serializable
data class SalesTransactionResponse(
    val id_transaksi: Int,
    val product_id: Int,
    val tanggal: String
)

@Serializable
data class SalesItemResponse(
    val id_transaksi: Int,
    val product_id: Int,
    val nama_produk: String
)

@Serializable
data class ForecastDTO(
    val sku: String,
    val nama_produk: String,
    val prediksi_bulan_depan: Int,
    val status: String
)

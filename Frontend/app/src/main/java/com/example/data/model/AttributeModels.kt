package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttributeOption(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class AttributesResponse(
    val categories: List<AttributeOption> = emptyList(),
    val brands: List<AttributeOption> = emptyList(),
    val genders: List<AttributeOption> = emptyList(),
    val age_groups: List<AttributeOption> = emptyList(),
    val materials: List<AttributeOption> = emptyList(),
    val cuttings: List<AttributeOption> = emptyList(),
    val fits: List<AttributeOption> = emptyList(),
    val colors: List<AttributeOption> = emptyList(),
    val size_groups: List<AttributeOption> = emptyList()
)

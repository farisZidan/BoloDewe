package com.bolodewe

import org.jetbrains.exposed.sql.Table

// --- TABEL MASTER ---
object CategoriesTable : Table("M_CATEGORIES") {
    val idCategory = integer("id_category").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idCategory)
}

object MaterialsTable : Table("M_MATERIALS") {
    val idMaterial = integer("id_material").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idMaterial)
}

object BrandsTable : Table("M_BRANDS") {
    val idBrand = integer("id_brand").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idBrand)
}

object SizeGroupsTable : Table("M_SIZE_GROUPS") {
    val idSize = integer("id_size").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idSize)
}

object GendersTable : Table("M_GENDERS") {
    val idGender = integer("id_gender").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idGender)
}

object CuttingsTable : Table("M_CUTTINGS") {
    val idCutting = integer("id_cutting").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idCutting)
}

object FitsTable : Table("M_FITS") {
    val idFit = integer("id_fit").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idFit)
}

object ColorsTable : Table("M_COLORS") {
    val idColor = integer("id_color").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idColor)
}

object AgeGroupsTable : Table("M_AGE_GROUPS") {
    val idAge = integer("id_age").autoIncrement()
    val name = varchar("name", 100)
    override val primaryKey = PrimaryKey(idAge)
}

// --- TABEL UTAMA ---
object ProductsTable : Table("PRODUCTS") {
    val idProduk = integer("id_produk").autoIncrement()
    val kodeSku = varchar("kode_sku", 100).uniqueIndex()

    val gambar = varchar("gambar", 255).nullable()
    val namaProduk = varchar("nama_produk", 255)
    // 9 Foreign Keys sesuai ERD PDF
    val categoryId = reference("category_id", CategoriesTable.idCategory)
    val brandId = reference("brand_id", BrandsTable.idBrand)
    val genderId = reference("gender_id", GendersTable.idGender)
    val ageGroupId = reference("age_group_id", AgeGroupsTable.idAge)
    val materialId = reference("material_id", MaterialsTable.idMaterial)
    val cuttingId = reference("cutting_id", CuttingsTable.idCutting)
    val fitId = reference("fit_id", FitsTable.idFit)
    val colorId = reference("color_id", ColorsTable.idColor)
    val sizeGroupId = reference("size_group_id", SizeGroupsTable.idSize)

    val createdAt = varchar("created_at", 50) // Disimpan sebagai string ISO 8601 untuk kemudahan MVP

    override val primaryKey = PrimaryKey(idProduk)
}

// --- TABEL TRANSAKSI (SALES) ---
object SalesTable : Table("SALES") {
    val idTransaksi = integer("id_transaksi").autoIncrement()
    val productId = reference("product_id", ProductsTable.idProduk)
    val tanggal = varchar("tanggal", 50) // Disimpan format teks (misal: "2026-06-04 18:22:00") agar aman saat dikirim via JSON

    override val primaryKey = PrimaryKey(idTransaksi)
}

// --- TABEL KASIR (USERS) ---
object UsersTable : Table("M_USERS") {
    val idUser = integer("id_user").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255) // Di dunia nyata, ini wajib dienkripsi
    val role = varchar("role", 20) // Contoh: "kasir" atau "admin"

    override val primaryKey = PrimaryKey(idUser)
}
package com.bolodewe

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.io.File

object ProductController {

    suspend fun getAll(call: ApplicationCall) {
        val products = transaction {
            ProductsTable.selectAll().map {
                ProductDTO(
                    id_produk = it[ProductsTable.idProduk],
                    kode_sku = it[ProductsTable.kodeSku],
                    nama_produk = it[ProductsTable.namaProduk],
                    gambar = it[ProductsTable.gambar],
                    category_id = it[ProductsTable.categoryId],
                    brand_id = it[ProductsTable.brandId],
                    gender_id = it[ProductsTable.genderId],
                    age_group_id = it[ProductsTable.ageGroupId],
                    material_id = it[ProductsTable.materialId],
                    cutting_id = it[ProductsTable.cuttingId],
                    fit_id = it[ProductsTable.fitId],
                    color_id = it[ProductsTable.colorId],
                    size_group_id = it[ProductsTable.sizeGroupId]
                )
            }
        }
        call.respond(HttpStatusCode.OK,products)
    }

    suspend fun getById(call: ApplicationCall) {
        val idProduk = call.parameters["id"]?.toIntOrNull()

        if (idProduk != null) {
            val product = transaction {
                // Mencari produk spesifik berdasarkan ID
                ProductsTable.select { ProductsTable.idProduk eq idProduk }
                    .map {
                        ProductDTO(
                            id_produk = it[ProductsTable.idProduk],
                            kode_sku = it[ProductsTable.kodeSku],
                            nama_produk = it[ProductsTable.namaProduk],
                            gambar = it[ProductsTable.gambar],
                            category_id = it[ProductsTable.categoryId],
                            brand_id = it[ProductsTable.brandId],
                            gender_id = it[ProductsTable.genderId],
                            age_group_id = it[ProductsTable.ageGroupId],
                            material_id = it[ProductsTable.materialId],
                            cutting_id = it[ProductsTable.cuttingId],
                            fit_id = it[ProductsTable.fitId],
                            color_id = it[ProductsTable.colorId],
                            size_group_id = it[ProductsTable.sizeGroupId]
                        )
                    }
                    .singleOrNull() // Mengambil satu data saja, atau null jika tidak ada
            }

            if (product != null) {
                // Jika barang ketemu, kirim datanya
                call.respond(HttpStatusCode.OK, product)
            } else {
                // Jika ID tidak ada di database
                call.respond(HttpStatusCode.NotFound, mapOf("pesan" to "Produk dengan ID $idProduk tidak ditemukan"))
            }
        } else {
            // Jika yang diketik di URL bukan angka
            call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "ID Produk tidak valid"))
        }
    }

    suspend fun create(call: ApplicationCall) {
        try {
            val multipart = call.receiveMultipart()

            var kodeSku = ""
            var namaProduk = ""
            var categoryId: Int? = null
            var brandId: Int? = null
            var genderId: Int? = null
            var ageGroupId: Int? = null
            var materialId: Int? = null
            var cuttingId: Int? = null
            var fitId: Int? = null
            var colorId: Int? = null
            var sizeGroupId: Int? = null
            var namaFileGambar: String? = null

            // Iterasi membaca kiriman Form-Data dari Postman/Android
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "kode_sku" -> kodeSku = part.value
                            "nama_produk" -> namaProduk = part.value
                            "category_id" -> categoryId = part.value.toIntOrNull()
                            "brand_id" -> brandId = part.value.toIntOrNull()
                            "gender_id" -> genderId = part.value.toIntOrNull()
                            "age_group_id" -> ageGroupId = part.value.toIntOrNull()
                            "material_id" -> materialId = part.value.toIntOrNull()
                            "cutting_id" -> cuttingId = part.value.toIntOrNull()
                            "fit_id" -> fitId = part.value.toIntOrNull()
                            "color_id" -> colorId = part.value.toIntOrNull()
                            "size_group_id" -> sizeGroupId = part.value.toIntOrNull()
                        }
                    }
                    is PartData.FileItem -> {
                        if (part.originalFileName != null && part.originalFileName!!.isNotEmpty()) {
                            val originalName = part.originalFileName ?: "produk.jpg"
                            // Amankan nama file unik dengan Timestamp
                            namaFileGambar = "${System.currentTimeMillis()}_$originalName"

                            val folderUploads = File("/app/uploads")
                            if (!folderUploads.exists()) folderUploads.mkdirs()

                            val file = File(folderUploads, namaFileGambar!!)
                            part.streamProvider().use { input ->
                                file.outputStream().use { output -> input.copyTo(output) }
                            }
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (categoryId == null || brandId == null || genderId == null || ageGroupId == null || materialId == null || cuttingId == null || fitId == null || colorId == null || sizeGroupId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "Gagal: ID Master (Category, Brand, Gender) wajib diisi!"))
                return
            }

            // Simpan data terkumpul ke MariaDB
            transaction {
                ProductsTable.insert {
                    it[this.kodeSku] = kodeSku
                    it[this.namaProduk] = namaProduk
                    it[this.categoryId] = categoryId!!
                    it[this.brandId] = brandId!!
                    it[this.genderId] = genderId!!
                    it[this.ageGroupId] = ageGroupId!!
                    it[this.materialId] = materialId!!
                    it[this.cuttingId] = cuttingId!!
                    it[this.fitId] = fitId!!
                    it[this.colorId] = colorId!!
                    it[this.sizeGroupId] = sizeGroupId!!
                    it[this.gambar] = namaFileGambar // Teks nama file masuk DB
                    it[this.createdAt] = LocalDateTime.now().toString()
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("pesan" to "Sukses menambahkan $namaProduk"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "Gagal: ${e.localizedMessage}"))
        }
    }

    // 3. UPDATE DATA PRODUK & OPTIONAL UPDATE GAMBAR BARU
    suspend fun update(call: ApplicationCall) {
        val idProduk = call.parameters["id"]?.toIntOrNull()
        if (idProduk != null) {
            try {
                val multipart = call.receiveMultipart()

                var kodeSku = ""
                var namaProduk = ""
                var categoryId: Int? = null
                var brandId: Int? = null
                var genderId: Int? = null
                var ageGroupId: Int? = null
                var materialId: Int? = null
                var cuttingId: Int? = null
                var fitId: Int? = null
                var colorId: Int? = null
                var sizeGroupId: Int? = null
                var namaFileGambar: String? = null
                var adaGambarBaru = false

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "kode_sku" -> kodeSku = part.value
                                "nama_produk" -> namaProduk = part.value
                                "category_id" -> categoryId = part.value.toIntOrNull()
                                "brand_id" -> brandId = part.value.toIntOrNull()
                                "gender_id" -> genderId = part.value.toIntOrNull()
                                "age_group_id" -> ageGroupId = part.value.toIntOrNull()
                                "material_id" -> materialId = part.value.toIntOrNull()
                                "cutting_id" -> cuttingId = part.value.toIntOrNull()
                                "fit_id" -> fitId = part.value.toIntOrNull()
                                "color_id" -> colorId = part.value.toIntOrNull()
                                "size_group_id" -> sizeGroupId = part.value.toIntOrNull()
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.originalFileName != null && part.originalFileName!!.isNotEmpty()) {
                                val originalName = part.originalFileName ?: "produk.jpg"
                                namaFileGambar = "${System.currentTimeMillis()}_$originalName"
                                adaGambarBaru = true

                                val folderUploads = File("/app/uploads")
                                if (!folderUploads.exists()) folderUploads.mkdirs()

                                val file = File(folderUploads, namaFileGambar!!)
                                part.streamProvider().use { input ->
                                    file.outputStream().use { output -> input.copyTo(output) }
                                }
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (categoryId == null || brandId == null || genderId == null || ageGroupId == null || materialId == null || cuttingId == null || fitId == null || colorId == null || sizeGroupId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "Gagal: ID Master (Category, Brand, Gender) wajib diisi!"))
                    return }

                transaction {
                    ProductsTable.update({ ProductsTable.idProduk eq idProduk }) {
                        it[this.kodeSku] = kodeSku
                        it[this.namaProduk] = namaProduk
                        it[this.categoryId] = categoryId!!
                        it[this.brandId] = brandId!!
                        it[this.genderId] = genderId!!
                        it[this.ageGroupId] = ageGroupId!!
                        it[this.materialId] = materialId!!
                        it[this.cuttingId] = cuttingId!!
                        it[this.fitId] = fitId!!
                        it[this.colorId] = colorId!!
                        it[this.sizeGroupId] = sizeGroupId!!
                        // Hanya timpa kolom gambar jika user memasukkan file baru saat update
                        if (adaGambarBaru) {
                            it[this.gambar] = namaFileGambar
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("pesan" to "Produk ID $idProduk berhasil diupdate"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "Gagal update: ${e.localizedMessage}"))
            }
        } else {
            call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "ID Produk tidak valid"))
        }
    }

    suspend fun delete(call: ApplicationCall) {
        val idProduk = call.parameters["id"]?.toIntOrNull()
        if (idProduk != null) {
            transaction { ProductsTable.deleteWhere { ProductsTable.idProduk eq idProduk } }
            call.respond(HttpStatusCode.OK, mapOf("pesan" to "Barang dengan ID $idProduk dihapus"))
        } else call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "ID Produk tidak valid"))
    }
}
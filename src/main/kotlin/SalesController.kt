package com.bolodewe

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object SalesController {

    suspend fun getAll(call: ApplicationCall) {
        val transactions = transaction {
            SalesTable.selectAll().map {
                SalesTransactionResponse(
                    id_transaksi = it[SalesTable.idTransaksi],
                    product_id = it[SalesTable.productId],
                    // Potong tanggal jadi YYYY-MM-DD agar UI Android rapi
                    tanggal = it[SalesTable.tanggal].take(10)
                )
            }
        }
        call.respond(HttpStatusCode.OK, transactions)
    }

    suspend fun create(call: ApplicationCall) {
        try {
            val newSale = call.receive<SalesDTO>()
            transaction {
                SalesTable.insert {
                    it[productId] = newSale.product_id
                    it[tanggal] = newSale.tanggal
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("pesan" to "Transaksi berhasil dicatat"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "ID Produk tidak ditemukan"))
        }
    }

    suspend fun update(call: ApplicationCall) {
        val idTransaksi = call.parameters["id"]?.toIntOrNull()
        if (idTransaksi != null) {
            val updatedSale = call.receive<SalesDTO>()
            transaction {
                SalesTable.update({ SalesTable.idTransaksi eq idTransaksi }) {
                    it[productId] = updatedSale.product_id
                    it[tanggal] = updatedSale.tanggal
                }
            }
            call.respond(HttpStatusCode.OK, mapOf("pesan" to "Transaksi ID $idTransaksi diupdate"))
        } else call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "ID Transaksi tidak valid"))
    }

    suspend fun delete(call: ApplicationCall) {
        val idTransaksi = call.parameters["id"]?.toIntOrNull()
        if (idTransaksi != null) {
            transaction { SalesTable.deleteWhere { SalesTable.idTransaksi eq idTransaksi } }
            call.respond(HttpStatusCode.OK, mapOf("pesan" to "Transaksi ID $idTransaksi dihapus (Void)"))
        } else call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "ID Transaksi tidak valid"))
    }

    suspend fun getChartReport(call: ApplicationCall) {
        // 1. Tangkap query parameter dari frontend (default ke 'monthly' jika kosong)
        val filter = call.request.queryParameters["filter"] ?: "monthly"

        // Ambil tanggal hari ini (Patokan mundur ke belakang)
        val hariIni = java.time.LocalDate.now()

        // 2. Tentukan batas tanggal mundur berdasarkan filter
        val tanggalBatas = when (filter) {
            "weekly" -> hariIni.minusDays(7).toString()   // Contoh hasil: "2026-06-07"
            "yearly" -> hariIni.minusYears(1).toString()  // Contoh hasil: "2025-06-14"
            else -> hariIni.minusDays(30).toString()      // Default 'monthly' (30 hari terakhir)
        }

        // 3. Tarik data dari database yang nilainya >= tanggalBatas
        val rawSales = transaction {
            (SalesTable innerJoin ProductsTable)
                .slice(ProductsTable.namaProduk, SalesTable.tanggal)
                .select { SalesTable.tanggal greaterEq tanggalBatas }
                .map {
                    Pair(it[ProductsTable.namaProduk], it[SalesTable.tanggal])
                }
        }

        // 4. Kelompokkan data secara dinamis berdasarkan kebutuhan grafik
        val chartData = rawSales.groupBy { it.first } // Group berdasarkan Nama Produk
            .mapValues { (_, transaksi) ->
                transaksi.map { item ->
                    val tanggalLengkap = item.second // Format asli: "YYYY-MM-DD HH:mm:ss"

                    // Jika filternya tahunan, kelompokkan per BULAN ("YYYY-MM")
                    // Jika mingguan/bulanan, kelompokkan per TANGGAL HARI ("YYYY-MM-DD")
                    if (filter == "yearly") tanggalLengkap.take(7) else tanggalLengkap.take(10)
                }
                    .groupingBy { it }
                    .eachCount()
                    .toSortedMap() // Urutkan dari tanggal/bulan terlama ke terbaru
                    .toMap()
            }

        // 5. Kirim data yang sudah matang ke Android
        call.respond(HttpStatusCode.OK, chartData)
    }
}
package com.bolodewe

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ForecastController {

    // 1. Data class internal untuk menampung hasil query mentah dari Join Table
    private data class SalesDataRaw(
        val categoryId: Int,
        val brandId: Int,
        val cuttingId: Int,
        val fitId: Int,
        val materialId: Int,
        val ageGroupId: Int,
        val namaKategori: String,
        val namaBrand: String,
        val namaCutting: String,
        val namaFit: String,
        val namaMaterial: String,
        val tanggal: String
    )

    suspend fun hitungSemuaTrendMoment(call: ApplicationCall) {
        // 2. Ambil data penjualan beserta nama asli dari tabel master
        val allSales = transaction {
            (SalesTable innerJoin ProductsTable
                    innerJoin CategoriesTable
                    innerJoin BrandsTable
                    innerJoin CuttingsTable
                    innerJoin FitsTable
                    innerJoin MaterialsTable)
                .slice(
                    ProductsTable.categoryId,
                    ProductsTable.brandId,
                    ProductsTable.cuttingId,
                    ProductsTable.fitId,
                    ProductsTable.materialId,
                    ProductsTable.ageGroupId,
                    CategoriesTable.name,
                    BrandsTable.name,
                    CuttingsTable.name,
                    FitsTable.name,
                    MaterialsTable.name,
                    SalesTable.tanggal
                )
                .selectAll()
                .map {
                    SalesDataRaw(
                        categoryId = it[ProductsTable.categoryId],
                        brandId = it[ProductsTable.brandId],
                        cuttingId = it[ProductsTable.cuttingId],
                        fitId = it[ProductsTable.fitId],
                        materialId = it[ProductsTable.materialId],
                        ageGroupId = it[ProductsTable.ageGroupId],
                        namaKategori = it[CategoriesTable.name],
                        namaBrand = it[BrandsTable.name],
                        namaCutting = it[CuttingsTable.name],
                        namaFit = it[FitsTable.name],
                        namaMaterial = it[MaterialsTable.name],
                        tanggal = it[SalesTable.tanggal]
                    )
                }
        }

        if (allSales.isEmpty()) {
            call.respond(HttpStatusCode.OK, emptyList<ForecastDTO>())
            return
        }

        // 3. Konfigurasi ID Usia Anak
        val idKategoriAnak = 3

        // 4. Kelompokkan Data (Merger Remaja & Dewasa jadi satu)
        val salesByGroup = allSales.groupBy { data ->
            val kelompokUsia = if (data.ageGroupId == idKategoriAnak) "ANAK" else "UMUM"
            "${data.categoryId}-${data.brandId}-${data.cuttingId}-${data.fitId}-${data.materialId}-$kelompokUsia"
        }

        val hasilRamalanList = mutableListOf<ForecastDTO>()

        // 5. Looping perhitungan Trend Moment untuk setiap kelompok
        for ((groupKey, transaksiBerdasarkanGrup) in salesByGroup) {

            // Ambil sampel data pertama untuk merakit nama
            val dataPertama = transaksiBerdasarkanGrup.first()

            // Tentukan label usia untuk ditampilkan di nama
            val labelUsia = if (dataPertama.ageGroupId == idKategoriAnak) " Anak" else ""

            // Bersihkan nama Brand (Jika "-" atau kosong, buang agar tidak muncul di teks)
            val brandBersih = if (dataPertama.namaBrand == "-" || dataPertama.namaBrand.isBlank()) "" else "${dataPertama.namaBrand} "
            // Tambahkan kondisi untuk menyembunyikan kata "Straight"
            val cuttingBersih = if (dataPertama.namaCutting == "-" ||
                dataPertama.namaCutting.isBlank() ||
                dataPertama.namaCutting.equals("Straight", ignoreCase = true)) {
                ""
            } else {
                "${dataPertama.namaCutting} "
            }
            val materialBersih = if (dataPertama.namaMaterial == "-" || dataPertama.namaMaterial.isBlank()) "" else "${dataPertama.namaMaterial} "
            val fitBersih = if (dataPertama.namaFit == "-" || dataPertama.namaFit.isBlank()) "" else "${dataPertama.namaFit} "
            val kategoriSingkat = when (dataPertama.namaKategori.trim().lowercase()) {
                "celana panjang" -> "CPJ "
                "celana pendek" -> "CP "
                // Singkatan
                else -> dataPertama.namaKategori // Jika tidak ada match, biarkan nama aslinya
            }

            // Hasil RAKITAN NAMA: "Celana Panjang XPD Straight (Umum)"
            val namaMentah = "$kategoriSingkat$brandBersih$cuttingBersih$materialBersih$fitBersih$labelUsia"
            val namaGrupBeres = namaMentah.replace("\\s+".toRegex(), " ").trim()

            // Buat Pseudo-SKU untuk UI Android agar tidak error
            val pseudoSku = "GRUP-$groupKey"

            // Kelompokkan transaksi per bulan ("YYYY-MM")
            val monthlySales = transaksiBerdasarkanGrup
                .map { it.tanggal.take(7) }
                .groupingBy { it }
                .eachCount()
                .toSortedMap()

            val n = monthlySales.size

            // Evaluasi syarat minimal Trend Moment (Wajib >= 2 Bulan)
            if (n < 2) {
                hasilRamalanList.add(ForecastDTO(pseudoSku, namaGrupBeres, 0, "Data kurang dari 2 bulan"))
                continue
            }

            // --- Mulai Algoritma Trend Moment ---
            var sigmaX = 0.0
            var sigmaY = 0.0
            var sigmaXY = 0.0
            var sigmaX2 = 0.0
            var x = 0

            for ((_, jumlahTerjual) in monthlySales) {
                val y = jumlahTerjual.toDouble()
                sigmaX += x
                sigmaY += y
                sigmaXY += (x * y)
                sigmaX2 += (x * x)
                x++
            }

            val penyebut = (n * sigmaX2) - (sigmaX * sigmaX)
            val b = if (penyebut == 0.0) 0.0 else ((n * sigmaXY) - (sigmaX * sigmaY)) / penyebut
            val a = (sigmaY - (b * sigmaX)) / n

            // Rumus y = a + bx (n adalah indeks bulan ke depan yang diprediksi)
            val prediksiY = a + (b * n)

            // Pastikan tidak ada prediksi minus (dibulatkan ke atas, batas bawah 0)
            val ramalanFinal = Math.max(0.0, Math.ceil(prediksiY)).toInt()

            hasilRamalanList.add(ForecastDTO(pseudoSku, namaGrupBeres, ramalanFinal, "Sukses"))
        }

        val hasilAkhir = hasilRamalanList.sortedByDescending { it.prediksi_bulan_depan }
        // 6. Kirim JSON ke Android
        call.respond(HttpStatusCode.OK, hasilAkhir)
    }
}
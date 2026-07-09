package com.bolodewe

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase(ipAddress: String) {
    val config = HikariConfig().apply {
        // Masukkan nama database pada "namaDatabase"
        jdbcUrl = "jdbc:mysql://$ipAddress:3306/namaDatabase"
        driverClassName = "com.mysql.cj.jdbc.Driver"

        // Silahkan masukkan username dan sandi database
        username = ""
        password = ""
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    // Perintah untuk membuat tabel otomatis jika belum ada di MariaDB
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            CategoriesTable, MaterialsTable, BrandsTable, SizeGroupsTable,
            GendersTable, CuttingsTable, FitsTable, ColorsTable, AgeGroupsTable,
            ProductsTable, SalesTable, UsersTable
        )
    }

    println("Berhasil terhubung ke database di $ipAddress, dan tabel siap digunakan!")
}
package com.bolodewe

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import java.io.File

fun Application.configureRouting() {
    routing {

        // --- AREA PUBLIK ---
        get("/") { call.respondText("API Toko Bolo Dewe Aktif!") }
        staticFiles("/uploads", File("/app/uploads"))
        post("/api/register") { AuthController.register(call) }
        post("/api/login") { AuthController.login(call) }

        // --- AREA RAHASIA (Butuh Token JWT) ---
        authenticate("auth-jwt") {

            // Attribute produk
            route("/api/attributes") {
                get { AttributeController.getAllAttributes(call) }
            }

            // Produk (Master Barang)
            route("/api/products") {
                get { ProductController.getAll(call) }
                get("/{id}") { ProductController.getById(call)}
                post { ProductController.create(call) }
                put("/{id}") { ProductController.update(call) }
                delete("/{id}") { ProductController.delete(call) }
            }

            // Sales (Transaksi Penjualan)
            route("/api/sales") {
                get { SalesController.getAll(call) }
                post { SalesController.create(call) }
                put("/{id}") { SalesController.update(call) }
                delete("/{id}") { SalesController.delete(call) }

                get("/chart") { SalesController.getChartReport(call) }
            }

            // Peramalan (Trend Moment)
            route("/api/forecast") {
                get { ForecastController.hitungSemuaTrendMoment(call) }
            }
        }
    }
}
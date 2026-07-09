package com.bolodewe

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    // Deploy
    initDatabase("127.0.0.1")

    // Testing
    //initDatabase("192.168.18.201")
    val rahasiaJWT = "kunci-rahasia-expo-2026" // Kunci ini digunakan untuk menyegel tiket

    // Konfigurasi Satpam JWT
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWT.require(Algorithm.HMAC256(rahasiaJWT)).build())
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }

    configureSerialization()
    configureRouting()
}
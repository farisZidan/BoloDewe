package com.bolodewe

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

object AuthController {
    // Masukkan kunci JWT
    private const val rahasiaJWT = ""

    suspend fun register(call: ApplicationCall) {
        try {
            val newUser = call.receive<UserLogin>()

            // 1. Acak password menggunakan BCrypt
            val hashedPassword = BCrypt.hashpw(newUser.password_hash, BCrypt.gensalt())

            transaction {
                UsersTable.insert {
                    it[username] = newUser.username
                    it[passwordHash] = hashedPassword // Simpan versi acaknya
                    it[role] = "kasir"
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("pesan" to "Akun kasir ${newUser.username} berhasil dibuat!"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("pesan" to "Gagal membuat akun. Username mungkin sudah terpakai."))
        }
    }

    suspend fun login(call: ApplicationCall) {
        val credentials = call.receive<UserLogin>()

        // 1. Cari user berdasarkan username saja
        val userRow = transaction {
            UsersTable.selectAll().where { UsersTable.username eq credentials.username }.singleOrNull()
        }

        // 2. Jika user ketemu, verifikasi password-nya menggunakan BCrypt
        if (userRow != null && BCrypt.checkpw(credentials.password_hash, userRow[UsersTable.passwordHash])) {

            // Password cocok, buatkan token
            val token = JWT.create()
                .withClaim("username", credentials.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 86400000))
                .sign(Algorithm.HMAC256(rahasiaJWT))

            call.respond(HttpStatusCode.OK, LoginResponse(token))
        } else {
            // Password salah atau username tidak ada
            call.respond(HttpStatusCode.Unauthorized, mapOf("pesan" to "Username atau Password salah!"))
        }
    }
}
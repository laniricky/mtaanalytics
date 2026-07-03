package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.auth.JwtConfig
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.AuthToken
import com.mtaanimation.growthos.shared.models.LoginRequest
import com.mtaanimation.growthos.shared.models.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(userRepository: UserRepository) {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            
            // Check if user already exists
            if (userRepository.getUserByUsername(request.username) != null) {
                call.respond(HttpStatusCode.Conflict, "User already exists")
                return@post
            }

            val user = userRepository.createUser(request.username, request.email, request.passwordHash)
            if (user != null) {
                val token = JwtConfig.generateToken(user.username)
                call.respond(HttpStatusCode.Created, AuthToken(token))
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create user")
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            
            val storedHash = userRepository.getPasswordHashByUsername(request.username)
            if (storedHash != null && storedHash == request.passwordHash) {
                val token = JwtConfig.generateToken(request.username)
                call.respond(HttpStatusCode.OK, AuthToken(token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
    }
}

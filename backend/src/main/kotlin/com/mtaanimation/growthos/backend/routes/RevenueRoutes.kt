package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.RevenueRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.revenue.RecordRevenueRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.revenueRoutes(revenueRepository: RevenueRepository, userRepository: UserRepository) {
    route("/api/revenue") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<RecordRevenueRequest>()
                
                val revenueEntry = revenueRepository.recordRevenue(UUID.fromString(user.id), request)
                
                if (revenueEntry != null) {
                    call.respond(HttpStatusCode.Created, revenueEntry)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to record revenue")
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val revenueEntries = revenueRepository.getAllRevenue(UUID.fromString(user.id))
                call.respond(HttpStatusCode.OK, revenueEntries)
            }
        }
    }
}

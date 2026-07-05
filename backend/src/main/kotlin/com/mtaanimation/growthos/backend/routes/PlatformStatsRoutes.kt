package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.PlatformStatsRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.RecordStatsRequest
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
import java.time.Instant
import java.util.UUID

fun Route.platformStatsRoutes(platformStatsRepository: PlatformStatsRepository, userRepository: UserRepository) {
    route("/api/stats") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<RecordStatsRequest>()
                val dateRecorded = Instant.ofEpochMilli(request.dateRecordedEpochMillis)
                
                val stats = platformStatsRepository.recordStats(
                    userId = UUID.fromString(user.id),
                    platformType = request.platformType,
                    currentFollowers = request.currentFollowers,
                    dateRecorded = dateRecorded
                )
                
                if (stats != null) {
                    call.respond(HttpStatusCode.Created, stats)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to record stats")
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val stats = platformStatsRepository.getStatsForUser(UUID.fromString(user.id))
                call.respond(HttpStatusCode.OK, stats)
            }
        }
    }
}

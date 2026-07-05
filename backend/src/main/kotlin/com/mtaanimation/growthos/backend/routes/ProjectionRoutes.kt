package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.backend.services.ProjectionService
import com.mtaanimation.growthos.shared.projection.ProjectionRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.time.Instant
import java.util.UUID

fun Route.projectionRoutes(projectionService: ProjectionService, userRepository: UserRepository) {
    route("/api/projections") {
        authenticate("auth-jwt") {

            /**
             * POST /api/projections/dashboard
             * Returns the full dashboard projection snapshot.
             * Accepts an optional JSON body { "deadlineEpochMillis": ... } to override the deadline.
             */
            post("/dashboard") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val user = userRepository.getUserByUsername(username)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                // Allow client to optionally override the deadline
                val request = call.receiveNullable<ProjectionRequest>()
                val deadline = request?.deadlineEpochMillis?.let { Instant.ofEpochMilli(it) }

                try {
                    val dashboard = if (deadline != null) {
                        projectionService.computeDashboard(UUID.fromString(user.id), deadline)
                    } else {
                        projectionService.computeDashboard(UUID.fromString(user.id))
                    }

                    call.respond(HttpStatusCode.OK, dashboard)
                } catch (e: Exception) {
                    println("ERROR computing dashboard: ${e.message}")
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Failed to compute dashboard: ${e.message}")
                }
            }
        }
    }
}

package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.backend.services.ProjectionService
import com.mtaanimation.growthos.backend.services.RevenueProjectionService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.projectionRoutes(
    projectionService: ProjectionService, 
    revenueProjectionService: RevenueProjectionService,
    userRepository: UserRepository
) {
    route("/api/projections") {
        authenticate("auth-jwt") {

            /**
             * POST /api/projections/dashboard
             * Returns the full dashboard projection snapshot.
             * Accepts an optional JSON body { "deadlineEpochMillis": ... } to override the deadline.
             */
            post("/dashboard") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val username = principal?.payload?.getClaim("username")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val user = userRepository.getUserByUsername(username)
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val dashboard = projectionService.computeDashboard(UUID.fromString(user.id))

                    call.respond(HttpStatusCode.OK, dashboard)
                } catch (e: Throwable) {
                    println("CRITICAL ERROR computing dashboard: ${e.message}")
                    e.printStackTrace()
                    call.respondText("Failed: ${e.message} - ${e.javaClass.name}", ContentType.Text.Plain, HttpStatusCode.InternalServerError)
                }
            }

            /**
             * POST /api/projections/revenue
             * Returns the revenue projection snapshot.
             */
            post("/revenue") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val username = principal?.payload?.getClaim("username")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val user = userRepository.getUserByUsername(username)
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val revenueProjection = revenueProjectionService.computeRevenueProjection(UUID.fromString(user.id))

                    call.respond(HttpStatusCode.OK, revenueProjection)
                } catch (e: Throwable) {
                    println("CRITICAL ERROR computing revenue projection: ${e.message}")
                    e.printStackTrace()
                    call.respondText("Failed: ${e.message} - ${e.javaClass.name}", ContentType.Text.Plain, HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

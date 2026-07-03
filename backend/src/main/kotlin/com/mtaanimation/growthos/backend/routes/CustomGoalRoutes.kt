package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.CustomGoalRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.customgoals.CreateCustomGoalRequest
import com.mtaanimation.growthos.shared.models.customgoals.UpdateCustomGoalProgressRequest
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
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

fun Route.customGoalRoutes(customGoalRepository: CustomGoalRepository, userRepository: UserRepository) {
    route("/api/custom_goals") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<CreateCustomGoalRequest>()
                
                val goal = customGoalRepository.createGoal(UUID.fromString(user.id), request)
                
                if (goal != null) {
                    call.respond(HttpStatusCode.Created, goal)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create custom goal")
                }
            }

            put("/progress") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<UpdateCustomGoalProgressRequest>()
                
                val goal = customGoalRepository.updateProgress(UUID.fromString(user.id), request)
                
                if (goal != null) {
                    call.respond(HttpStatusCode.OK, goal)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to update progress")
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val goals = customGoalRepository.getAllGoals(UUID.fromString(user.id))
                call.respond(HttpStatusCode.OK, goals)
            }
        }
    }
}

package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.GoalRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.CreateGoalRequest
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

fun Route.goalRoutes(goalRepository: GoalRepository, userRepository: UserRepository) {
    route("/api/goals") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                
                if (username == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                
                val user = userRepository.getUserByUsername(username)
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val request = call.receive<CreateGoalRequest>()
                val targetDate = Instant.ofEpochMilli(request.targetDateEpochMillis)
                
                val goal = goalRepository.createGoal(
                    userId = UUID.fromString(user.id),
                    name = request.name,
                    targetValue = request.targetValue,
                    targetDate = targetDate,
                    type = request.type
                )
                
                if (goal != null) {
                    call.respond(HttpStatusCode.Created, goal)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create goal")
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val user = userRepository.getUserByUsername(username) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val goals = goalRepository.getGoalsForUser(UUID.fromString(user.id))
                call.respond(HttpStatusCode.OK, goals)
            }
        }
    }
}

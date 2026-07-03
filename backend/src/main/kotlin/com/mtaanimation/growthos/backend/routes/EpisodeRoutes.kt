package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.EpisodeRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.episodes.RecordEpisodeRequest
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

fun Route.episodeRoutes(episodeRepository: EpisodeRepository, userRepository: UserRepository) {
    route("/api/episodes") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<RecordEpisodeRequest>()
                
                val episodeEntry = episodeRepository.recordEpisode(UUID.fromString(user.id), request)
                
                if (episodeEntry != null) {
                    call.respond(HttpStatusCode.Created, episodeEntry)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to record episode")
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val episodes = episodeRepository.getAllEpisodes(UUID.fromString(user.id))
                call.respond(HttpStatusCode.OK, episodes)
            }
        }
    }
}

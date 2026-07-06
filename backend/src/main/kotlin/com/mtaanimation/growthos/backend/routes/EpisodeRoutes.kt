package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.EpisodeRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.episodes.CreateEpisodeRequest
import com.mtaanimation.growthos.shared.models.episodes.UpsertEpisodeLinkRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import java.util.UUID

fun Route.episodeRoutes(episodeRepository: EpisodeRepository, userRepository: UserRepository) {
    route("/api/episodes") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<CreateEpisodeRequest>()
                val episodeEntry = episodeRepository.createEpisode(UUID.fromString(user.id), request)
                
                if (episodeEntry != null) {
                    call.respond(HttpStatusCode.Created, episodeEntry)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create episode")
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val episodes = episodeRepository.getEpisodesForUser(UUID.fromString(user.id))
                call.respond(HttpStatusCode.OK, episodes)
            }

            post("/{id}/links") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val episodeIdStr = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing id")
                val episodeId = UUID.fromString(episodeIdStr)

                val request = call.receive<UpsertEpisodeLinkRequest>()
                val linkEntry = episodeRepository.upsertLink(episodeId, request)
                
                if (linkEntry != null) {
                    call.respond(HttpStatusCode.OK, linkEntry)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to upsert link")
                }
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val episodeIdStr = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
                val episodeId = UUID.fromString(episodeIdStr)

                val success = episodeRepository.deleteEpisode(episodeId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to delete episode")
                }
            }
        }
    }
}

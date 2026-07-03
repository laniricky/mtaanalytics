package com.mtaanimation.growthos.backend.routes

import com.mtaanimation.growthos.backend.repositories.UploadRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.shared.models.uploads.RecordUploadsRequest
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

fun Route.uploadRoutes(uploadRepository: UploadRepository, userRepository: UserRepository) {
    route("/api/uploads") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val request = call.receive<RecordUploadsRequest>()
                
                val uploadsEntry = uploadRepository.recordUploads(UUID.fromString(user.id), request)
                
                if (uploadsEntry != null) {
                    call.respond(HttpStatusCode.Created, uploadsEntry)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to record uploads")
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = userRepository.getUserByUsername(username) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                
                val uploads = uploadRepository.getAllUploads(UUID.fromString(user.id))
                call.respond(HttpStatusCode.OK, uploads)
            }
        }
    }
}

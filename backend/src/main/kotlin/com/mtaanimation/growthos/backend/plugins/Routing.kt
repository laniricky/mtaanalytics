package com.mtaanimation.growthos.backend.plugins

import com.mtaanimation.growthos.backend.repositories.GoalRepository
import com.mtaanimation.growthos.backend.repositories.PlatformStatsRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.backend.routes.authRoutes
import com.mtaanimation.growthos.backend.routes.goalRoutes
import com.mtaanimation.growthos.backend.routes.platformStatsRoutes
import com.mtaanimation.growthos.backend.routes.projectionRoutes
import com.mtaanimation.growthos.backend.routes.revenueRoutes
import com.mtaanimation.growthos.backend.routes.episodeRoutes
import com.mtaanimation.growthos.backend.routes.uploadRoutes
import com.mtaanimation.growthos.backend.routes.customGoalRoutes
import com.mtaanimation.growthos.backend.services.ProjectionService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.routing.get
import io.ktor.server.html.respondHtml
import kotlinx.html.*

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val goalRepository = GoalRepository()
    val platformStatsRepository = PlatformStatsRepository()
    val projectionService = ProjectionService(platformStatsRepository)
    val revenueRepository = com.mtaanimation.growthos.backend.repositories.RevenueRepository()
    val episodeRepository = com.mtaanimation.growthos.backend.repositories.EpisodeRepository()
    val uploadRepository = com.mtaanimation.growthos.backend.repositories.UploadRepository()
    val customGoalRepository = com.mtaanimation.growthos.backend.repositories.CustomGoalRepository()

    routing {
        // Status page visible in browser
        get("/") {
            call.respondHtml {
                head {
                    title { +"Growth OS — API Server" }
                    style {
                        +"""
                        body { background: #1a1a1a; color: #f0f0f0; font-family: sans-serif;
                               display: flex; align-items: center; justify-content: center;
                               height: 100vh; margin: 0; flex-direction: column; gap: 12px; }
                        h1 { color: #FF8C00; font-size: 2.5rem; margin: 0; }
                        p  { color: #aaa; margin: 0; }
                        .badge { background: #2a2a2a; border: 1px solid #FF8C00;
                                 color: #FF8C00; padding: 6px 18px; border-radius: 999px;
                                 font-size: 0.85rem; }
                        """.trimIndent()
                    }
                }
                body {
                    h1 { +"Growth OS" }
                    p { +"Mtaanimation Analytics API Server" }
                    span(classes = "badge") { +"✓ Running" }
                    p { +"All API endpoints are available at /api/*" }
                }
            }
        }

        authRoutes(userRepository)
        goalRoutes(goalRepository, userRepository)
        platformStatsRoutes(platformStatsRepository, userRepository)
        projectionRoutes(projectionService, userRepository)
        revenueRoutes(revenueRepository, userRepository)
        episodeRoutes(episodeRepository, userRepository)
        uploadRoutes(uploadRepository, userRepository)
        customGoalRoutes(customGoalRepository, userRepository)
    }
}

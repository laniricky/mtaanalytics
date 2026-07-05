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
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

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
        // Status page — confirms the server is running when visited in a browser
        get("/") {
            call.respondText(
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>Growth OS — API Server</title>
                  <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                      background: #111111;
                      color: #f0f0f0;
                      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      height: 100vh;
                      flex-direction: column;
                      gap: 16px;
                    }
                    h1 { font-size: 2.8rem; font-weight: 800; }
                    h1 span { color: #FF8C00; }
                    .subtitle { color: #888; font-size: 1rem; }
                    .badge {
                      display: inline-flex;
                      align-items: center;
                      gap: 8px;
                      background: #1e1e1e;
                      border: 1px solid #FF8C00;
                      color: #FF8C00;
                      padding: 8px 22px;
                      border-radius: 999px;
                      font-size: 0.9rem;
                      font-weight: 600;
                    }
                    .dot {
                      width: 8px; height: 8px;
                      background: #4CAF50;
                      border-radius: 50%;
                      animation: pulse 1.5s infinite;
                    }
                    @keyframes pulse {
                      0%, 100% { opacity: 1; }
                      50% { opacity: 0.4; }
                    }
                    .info { color: #555; font-size: 0.85rem; margin-top: 8px; }
                  </style>
                </head>
                <body>
                  <h1>Growth <span>OS</span></h1>
                  <p class="subtitle">Mtaanimation Analytics — Backend API</p>
                  <div class="badge">
                    <span class="dot"></span>
                    Server Running
                  </div>
                  <p class="info">API endpoints available at /api/*</p>
                </body>
                </html>
                """.trimIndent(),
                ContentType.Text.Html
            )
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

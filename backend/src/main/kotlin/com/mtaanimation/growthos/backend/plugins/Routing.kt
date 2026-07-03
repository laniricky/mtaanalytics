package com.mtaanimation.growthos.backend.plugins

import com.mtaanimation.growthos.backend.repositories.GoalRepository
import com.mtaanimation.growthos.backend.repositories.PlatformStatsRepository
import com.mtaanimation.growthos.backend.repositories.UserRepository
import com.mtaanimation.growthos.backend.routes.authRoutes
import com.mtaanimation.growthos.backend.routes.goalRoutes
import com.mtaanimation.growthos.backend.routes.platformStatsRoutes
import com.mtaanimation.growthos.backend.routes.projectionRoutes
import com.mtaanimation.growthos.backend.services.ProjectionService
import io.ktor.server.application.Application
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
        authRoutes(userRepository)
        goalRoutes(goalRepository, userRepository)
        platformStatsRoutes(platformStatsRepository, userRepository)
        projectionRoutes(projectionService, userRepository)
        com.mtaanimation.growthos.backend.routes.revenueRoutes(revenueRepository, userRepository)
        com.mtaanimation.growthos.backend.routes.episodeRoutes(episodeRepository, userRepository)
        com.mtaanimation.growthos.backend.routes.uploadRoutes(uploadRepository, userRepository)
        com.mtaanimation.growthos.backend.routes.customGoalRoutes(customGoalRepository, userRepository)
    }
}

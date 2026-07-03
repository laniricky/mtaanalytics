package com.mtaanimation.growthos.android.domain.repository

import com.mtaanimation.growthos.android.data.network.CustomGoalsApiService
import com.mtaanimation.growthos.shared.models.customgoals.CreateCustomGoalRequest
import com.mtaanimation.growthos.shared.models.customgoals.CustomGoalDto
import com.mtaanimation.growthos.shared.models.customgoals.UpdateCustomGoalProgressRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomGoalsRepository @Inject constructor(
    private val apiService: CustomGoalsApiService
) {
    suspend fun getAllGoals(): Result<List<CustomGoalDto>> = apiService.getAllGoals()

    suspend fun createGoal(request: CreateCustomGoalRequest): Result<CustomGoalDto> =
        apiService.createGoal(request)

    suspend fun updateProgress(request: UpdateCustomGoalProgressRequest): Result<CustomGoalDto> =
        apiService.updateProgress(request)
}

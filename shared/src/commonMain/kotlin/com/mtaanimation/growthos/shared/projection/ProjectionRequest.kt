package com.mtaanimation.growthos.shared.projection

import kotlinx.serialization.Serializable

/**
 * Query parameters sent by the Android client to request projections.
 * Deadline defaults to the primary goal's date if omitted.
 */
@Serializable
data class ProjectionRequest(
    /** Override deadline (epoch ms). If null, backend uses the user's goal deadline. */
    val deadlineEpochMillis: Long? = null
)

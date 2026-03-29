package me.repeater64.mpkseedfilter.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SeedsRequest(
    val seedType: SeedType,
    val userID: String,
    val additionalRequestInfo: SeedRequestInfo? = null,
    val fortOpennessBounds: Pair<Double, Double>? = null
)

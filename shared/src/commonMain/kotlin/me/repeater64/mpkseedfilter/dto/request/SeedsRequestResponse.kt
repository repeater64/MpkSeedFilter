package me.repeater64.mpkseedfilter.dto.request

import kotlinx.serialization.Serializable
import me.repeater64.mpkseedfilter.dto.SavedSeedInfo

@Serializable
data class SeedsRequestResponse(
    val seeds: List<SavedSeedInfo>
)

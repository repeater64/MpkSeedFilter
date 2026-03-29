package me.repeater64.mpkseedfilter.dto.request

import kotlinx.serialization.Serializable
import me.repeater64.mpkseedfilter.dto.end.EndInfo

@Serializable
data class EndRequestInfo(
    val endInfo : EndInfo
) : SeedRequestInfo()

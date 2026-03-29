package me.repeater64.mpkseedfilter.dto.request

import kotlinx.serialization.Serializable
import me.repeater64.mpkseedfilter.dto.bastion.BastionIndexedByInfo

@Serializable
data class BastionRequestInfo(
    val bastionInfo : BastionIndexedByInfo
) : SeedRequestInfo()

package me.repeater64.mpkseedfilter.dto.bastion.ramparts

import kotlinx.serialization.Serializable

@Serializable
data class BridgeRamparts(
    val leftSide: BridgeRampart,
    val rightSide: BridgeRampart,
) : BastionRamparts()

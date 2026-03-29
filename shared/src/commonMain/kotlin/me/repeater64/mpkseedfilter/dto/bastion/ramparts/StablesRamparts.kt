package me.repeater64.mpkseedfilter.dto.bastion.ramparts

import kotlinx.serialization.Serializable

@Serializable
data class StablesRamparts(
    val leftRampart: StablesRampart,
    val middleRampart: StablesRampart,
    val rightRampart: StablesRampart,
    val leftGap: StablesGap,
    val rightGap: StablesGap
) : BastionRamparts()

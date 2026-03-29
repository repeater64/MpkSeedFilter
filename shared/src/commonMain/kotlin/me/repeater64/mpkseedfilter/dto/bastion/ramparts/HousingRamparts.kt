package me.repeater64.mpkseedfilter.dto.bastion.ramparts

import kotlinx.serialization.Serializable

@Serializable
data class HousingRamparts(val leftRampart: HousingRampart) : BastionRamparts()
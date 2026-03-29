package me.repeater64.mpkseedfilter.filtering.bastion

import Xinyuiii.enumType.BastionType
import com.seedfinding.mccore.util.pos.CPos
import me.repeater64.mpkseedfilter.dto.bastion.BastionIndexedByInfo
import me.repeater64.mpkseedfilter.dto.bastion.SavedBastionInfo
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.BastionRamparts
import me.repeater64.mpkseedfilter.filtering.toDTO

data class BastionInfo(
    val quadrantX: Int,
    val quadrantZ: Int,
    val pos: CPos,
    val indexedByInfo: BastionIndexedByInfo,
    val hasObbyChest: Boolean,
    val wetManhunt: Boolean,
) {
    constructor(quadrantX: Int, quadrantZ: Int, pos: CPos, type: BastionType, ramparts: BastionRamparts, hasObbyChest: Boolean, wetManhunt: Boolean)
            : this(quadrantX, quadrantZ, pos, BastionIndexedByInfo(type.toDTO(), ramparts), hasObbyChest, wetManhunt)

    val type: me.repeater64.mpkseedfilter.dto.bastion.BastionType get() = indexedByInfo.type
    val ramparts: BastionRamparts get() = indexedByInfo.ramparts

    fun toSavedBastionInfo(): SavedBastionInfo {
        return SavedBastionInfo(this.pos.toDTO(), this.indexedByInfo, this.hasObbyChest, this.wetManhunt)
    }
}
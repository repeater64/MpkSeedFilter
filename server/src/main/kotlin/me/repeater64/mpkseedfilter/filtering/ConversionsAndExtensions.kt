package me.repeater64.mpkseedfilter.filtering

import com.seedfinding.mccore.util.pos.BPos
import me.repeater64.mpkseedfilter.dto.SpawnLocation
import me.repeater64.mpkseedfilter.dto.bastion.BastionType
import me.repeater64.mpkseedfilter.dto.util.CPos
import kotlin.math.roundToInt

fun com.seedfinding.mccore.util.pos.CPos.toDTO() : CPos {
    return CPos(this.x, this.z)
}

fun Xinyuiii.enumType.BastionType.toDTO() : BastionType {
    return when (this) {
        Xinyuiii.enumType.BastionType.HOUSING -> BastionType.HOUSING
        Xinyuiii.enumType.BastionType.STABLES -> BastionType.STABLES
        Xinyuiii.enumType.BastionType.BRIDGE -> BastionType.BRIDGE
        Xinyuiii.enumType.BastionType.TREASURE -> BastionType.TREASURE
    }
}

fun SpawnLocation.getBlockPos(): BPos = BPos(x.roundToInt(), y.roundToInt(), z.roundToInt())
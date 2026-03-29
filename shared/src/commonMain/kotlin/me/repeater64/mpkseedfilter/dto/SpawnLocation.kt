package me.repeater64.mpkseedfilter.dto

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

data class SpawnLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Double
) {
    fun getTpCommand() = "execute in minecraft:the_nether run tp @p $x $y $z $yaw 0"

    fun toInt(): Int {
        if (abs(x) >= 1024 || y > 127 || abs(z) >= 1024) {
            throw RuntimeException("Failed to pack a SpawnLocation into an int because its coordinates were too big! ($x $y $z)")
        }

        // 2 bits
        val yawBits = if (yaw == 0.0) 0 else if (yaw == 90.0) 1 else if (yaw == -90.0) 2 else if (yaw==180.0) 3 else throw RuntimeException("Failed to pack a SpawnLocation into an int because it had an unexpected yaw ($yaw)!")

        val intX = (floor(x).toInt() + 1024).coerceAtMost(2047) // 11 bits
        val intY = y.roundToInt().coerceAtMost(127) // 7 bits
        val intZ = (floor(z).toInt() + 1024).coerceAtMost(2047) // 11 bits

        return intX or (intY shl 11) or (intZ shl 18) or (yawBits shl 29) // 31 bits total, fitting into a 32 bit integer
    }

    companion object {
        fun fromInt(int: Int): SpawnLocation {
            val intX = int and 0b11111111111
            val intY = (int shr 11) and 0b1111111
            val intZ = (int shr 18) and 0b11111111111
            val yawBits = (int shr 29)

            return SpawnLocation(
                (intX-1024) + 0.5,
                intY.toDouble(),
                (intZ-1024) + 0.5,
                if (yawBits == 0) 0.0 else if (yawBits == 1) 90.0 else if (yawBits == 2) -90.0 else 180.0
            )
        }
    }
}
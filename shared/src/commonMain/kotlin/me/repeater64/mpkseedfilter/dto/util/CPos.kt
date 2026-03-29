package me.repeater64.mpkseedfilter.dto.util

data class CPos(
    val x: Int,
    val z: Int
) {
    fun toShort(): Int {
        val shiftedX = this.x + 128
        val shiftedZ = this.z + 128
        if (shiftedX !in 0..255 || shiftedZ !in 0..255) {
            throw RuntimeException("Can't serialize CPos, too far from origin ($this)!")
        }
        return shiftedX or (shiftedZ shl 8)
    }

    companion object {
        fun fromShort(short: Short): CPos {
            val shortInt = short.toUShort().toInt()
            return CPos((shortInt and 0b11111111) - 128, (shortInt shr 8) - 128)
        }
    }
}
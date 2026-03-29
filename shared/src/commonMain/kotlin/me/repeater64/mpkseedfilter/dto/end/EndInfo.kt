package me.repeater64.mpkseedfilter.dto.end

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.repeater64.mpkseedfilter.dto.SavedSeedInfoSerializer.toInt

@Serializable(with = EndInfoSerializer::class)
data class EndInfo(
    val pillarInfo: SeedPillarInfo,
    val spawnInfo: SeedSpawnInfo,
) {
    fun toShort() : Short {
        val pillarByte = this.pillarInfo.frontDragon.toInt() or (this.pillarInfo.pillar.index shl 1)

        val oLevel = this.spawnInfo.oLevel
        val oLevelShifted = if (oLevel < 52) 0 else oLevel.coerceAtMost(60) - 51
        val spawnByte = (this.spawnInfo.type.index) or (oLevelShifted shl 3)

        return (pillarByte or (spawnByte shl 8)).toShort()
    }


    companion object {
        fun fromShort(short: Short) : EndInfo {
            val int = short.toUShort().toInt()
            val pillarByte = int and 0b11111111
            val spawnByte = int shr 8

            val frontDragon = (pillarByte and 1) != 0
            val pillarIndex = pillarByte shr 1
            val pillar = Pillar.fromIndex(pillarIndex)

            val spawnIndex = spawnByte and 0b111
            val spawnType = SpawnType.fromIndex(spawnIndex)
            val spawnShiftedOLevel = spawnByte shr 3
            val oLevel = if (spawnShiftedOLevel == 0) -1 else spawnShiftedOLevel + 51

            return EndInfo(SeedPillarInfo(frontDragon, pillar), SeedSpawnInfo(spawnType, oLevel))
        }
    }


}

object EndInfoSerializer : KSerializer<EndInfo> {
    override val descriptor = Short.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: EndInfo
    ) {
        encoder.encodeShort(value.toShort())
    }

    override fun deserialize(decoder: Decoder): EndInfo {
        return EndInfo.fromShort(decoder.decodeShort())
    }
}

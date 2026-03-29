package me.repeater64.mpkseedfilter.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.repeater64.mpkseedfilter.dto.bastion.BastionIndexedByInfo
import me.repeater64.mpkseedfilter.dto.bastion.SavedBastionInfo
import me.repeater64.mpkseedfilter.dto.end.EndInfo
import me.repeater64.mpkseedfilter.dto.fortress.SavedFortressInfo
import me.repeater64.mpkseedfilter.dto.util.CPos
import okio.Buffer
import okio.ByteString.Companion.decodeBase64
import kotlin.math.roundToInt

@Serializable(with = SavedSeedInfoSerializer::class)
data class SavedSeedInfo(
    val seed: Long,
    val goodSpawnToBastion: Boolean,
    val bastionInfo: SavedBastionInfo?,
    val goodBastionToFort: Boolean,
    val fortInfo: SavedFortressInfo?,
    val blindToStrongholdOpenness: Pair<Double, Double>,
    val endInfo: EndInfo,
) {

}

// We encode the seed info into a base64 string
object SavedSeedInfoSerializer : KSerializer<SavedSeedInfo> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: SavedSeedInfo) {
        val buffer = Buffer()

        buffer.writeLong(value.seed)

        // Merge some booleans - put goodSpawnToBastion, goodBastionToFort, bastionInfo != null, fortInfo != null, bastionInfo?.hasObbyChest, bastionInfo?.wetManhunt all into the same byte to waste less space
        val packedBools = value.goodSpawnToBastion.toInt() or
                (value.goodBastionToFort.toInt() shl 1) or
                ((value.bastionInfo != null).toInt() shl 2) or
                ((value.fortInfo != null).toInt() shl 3) or
                ((value.bastionInfo?.hasObbyChest ?: false).toInt() shl 4) or
                ((value.bastionInfo?.wetManhunt ?: false).toInt() shl 5)
        buffer.writeByte(packedBools)

        if (value.bastionInfo != null) {
            buffer.writeShort(value.bastionInfo.pos.toShort())
            buffer.writeByte(value.bastionInfo.indexedByInfo.toByte())
        }

        if (value.fortInfo != null) {
            buffer.writeShort(value.fortInfo.pos.toShort())
            buffer.writeInt(value.fortInfo.spawner1SpawnPoint.toInt())
            buffer.writeInt(value.fortInfo.spawner2SpawnPoint.toInt())
            buffer.writeInt(value.fortInfo.lavaRoomSpawnPoint.toInt())
            buffer.writeInt(value.fortInfo.badPartRandomSpawnPoint.toInt())
            buffer.writeInt(value.fortInfo.goodPartRandomSpawnPoint.toInt())

            // Round openness to only 8bit precision (we know it's between 0 and 1 incl)
            val byteOpenness = (value.fortInfo.openness*255).roundToInt().coerceIn(0..255)
            buffer.writeByte(byteOpenness)
        }

        // Round blind opennesses to 8 bit precision
        val byteBlind1Openness = (value.blindToStrongholdOpenness.first*255).roundToInt().coerceIn(0..255)
        val byteBlind2Openness = (value.blindToStrongholdOpenness.second*255).roundToInt().coerceIn(0..255)
        buffer.writeByte(byteBlind1Openness)
        buffer.writeByte(byteBlind2Openness)

        buffer.writeShort(value.endInfo.toShort().toInt())

        val base64String = buffer.readByteString().base64()
        encoder.encodeString(base64String)
    }

    override fun deserialize(decoder: Decoder): SavedSeedInfo {
        val base64String = decoder.decodeString()

        val decodedByteString = base64String.decodeBase64() ?: throw IllegalArgumentException("Invalid Base64 payload")

        val buffer = Buffer().write(decodedByteString)

        val seed = buffer.readLong()
        val packedBools = buffer.readByte().toInt()

        // Unpack these booleans
        val goodSpawnToBastion = (packedBools and (1)) != 0
        val goodBastionToFort = (packedBools and (1 shl 1)) != 0
        val hasBastionInfo = (packedBools and (1 shl 2)) != 0
        val hasFortInfo = (packedBools and (1 shl 3)) != 0
        val bastionHasObbyChest = (packedBools and (1 shl 4)) != 0
        val bastionWetManhunt = (packedBools and (1 shl 5)) != 0

        val bastionInfo = if (hasBastionInfo) {
            val bastionPos = CPos.fromShort(buffer.readShort())
            val bastionIndexedByInfo = BastionIndexedByInfo.fromByte(buffer.readByte())
            SavedBastionInfo(bastionPos, bastionIndexedByInfo, bastionHasObbyChest, bastionWetManhunt)
        } else null

        val fortInfo = if (hasFortInfo) {
            val fortPos = CPos.fromShort(buffer.readShort())
            val spawner1SpawnPoint = SpawnLocation.fromInt(buffer.readInt())
            val spawner2SpawnPoint = SpawnLocation.fromInt(buffer.readInt())
            val lavaRoomSpawnPoint = SpawnLocation.fromInt(buffer.readInt())
            val badPartRandomSpawnPoint = SpawnLocation.fromInt(buffer.readInt())
            val goodPartRandomSpawnPoint = SpawnLocation.fromInt(buffer.readInt())

            val byteOpenness = buffer.readByte().toUByte()
            val openness = byteOpenness.toDouble() / 255

            SavedFortressInfo(fortPos, spawner1SpawnPoint, spawner2SpawnPoint, lavaRoomSpawnPoint, badPartRandomSpawnPoint, goodPartRandomSpawnPoint, openness)
        } else null

        val byteBlind1Openness = buffer.readByte().toUByte()
        val byteBlind2Openness = buffer.readByte().toUByte()
        val blind1Openness = byteBlind1Openness.toDouble() / 255
        val blind2Openness = byteBlind2Openness.toDouble() / 255

        val endInfo = EndInfo.fromShort(buffer.readShort())

        return SavedSeedInfo(
            seed,
            goodSpawnToBastion,
            bastionInfo,
            goodBastionToFort,
            fortInfo,
            Pair(blind1Openness, blind2Openness),
            endInfo
        )
    }

    fun Boolean.toInt(): Int {
        return if (this) 1 else 0
    }
}
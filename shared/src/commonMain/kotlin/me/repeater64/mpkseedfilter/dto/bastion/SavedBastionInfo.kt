package me.repeater64.mpkseedfilter.dto.bastion

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.BastionRamparts
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.BridgeRampart
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.BridgeRamparts
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.HousingRampart
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.HousingRamparts
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.StablesGap
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.StablesRampart
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.StablesRamparts
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.TreasureRamparts
import me.repeater64.mpkseedfilter.dto.util.CPos
import me.repeater64.mpkseedfilter.dto.SavedSeedInfoSerializer.toInt

@Serializable(with = BastionIndexedByInfoSerializer::class)
data class BastionIndexedByInfo(
    val type: BastionType,
    val ramparts: BastionRamparts
) {
    // Bastion type and ramparts serialized to a byte, in order from LSB to MSB
    // First bit - 1 for stables, 0 otherwise
    // If stables - next bit is 1 for left side good gap, 0 bad gap. Then next bit is 1 for right side good gap, 0 bad gap
    //              Then the remaining 5 bits encode the ramparts. A single is assigned the number 0, a double 1 and a triple 2. Rampart encoding = left*9 + middle*3 + right
    // If not stables, next two bits give bastion type: 00 = housing, 01 = treasure, 10 = bridge
    // If treasure, rest of bits are zero
    // If housing, next two bits are: 00 for ruins, 01 for single chest, 10 for triple chest
    // If bridge, next bit is left side is triple, then right side is triple

    fun toByte(): Int {
        val byte = when (this.type) {
            BastionType.TREASURE -> 0b00000010
            BastionType.HOUSING -> when ((this.ramparts as HousingRamparts).leftRampart) {
                HousingRampart.RUINS -> 0b00000000
                HousingRampart.SINGLE_CHEST -> 0b00001000
                HousingRampart.TRIPLE_CHEST -> 0b00010000
            }

            BastionType.BRIDGE -> 0b00000100 or (((this.ramparts as BridgeRamparts).leftSide == BridgeRampart.TRIPLE_CHEST).toInt() shl 3) or ((this.ramparts.rightSide == BridgeRampart.TRIPLE_CHEST).toInt() shl 4)
            BastionType.STABLES -> {
                val theByte =
                    0b00000001 or (((this.ramparts as StablesRamparts).leftGap == StablesGap.GOOD_GAP).toInt() shl 1) or ((this.ramparts.rightGap == StablesGap.GOOD_GAP).toInt() shl 2)
                val rampartEncoding =
                    this.ramparts.leftRampart.getNum() * 9 + this.ramparts.middleRampart.getNum() * 3 + this.ramparts.rightRampart.getNum()
                theByte or (rampartEncoding shl 3)
            }
        }
        return byte
    }

    companion object {
        fun fromByte(byte: Byte): BastionIndexedByInfo {
            val byteAsInt = byte.toUByte().toInt()
            if (byteAsInt == 0b00000010) return BastionIndexedByInfo(BastionType.TREASURE, TreasureRamparts)
            if (byteAsInt == 0b00000000) return BastionIndexedByInfo(BastionType.HOUSING, HousingRamparts(HousingRampart.RUINS))
            if (byteAsInt == 0b00001000) return BastionIndexedByInfo(
                BastionType.HOUSING,
                HousingRamparts(HousingRampart.SINGLE_CHEST)
            )
            if (byteAsInt == 0b00010000) return BastionIndexedByInfo(
                BastionType.HOUSING,
                HousingRamparts(HousingRampart.TRIPLE_CHEST)
            )
            if (byteAsInt and 0b00000001 != 1 && byteAsInt and 0b00000110 == 0b100) {
                // Bridge
                val leftTriple = (byteAsInt and 0b00001000) == 0b00001000
                val rightTriple = (byteAsInt and 0b00010000) == 0b00010000
                return BastionIndexedByInfo(
                    BastionType.BRIDGE,
                    BridgeRamparts(
                        if (leftTriple) BridgeRampart.TRIPLE_CHEST else BridgeRampart.SINGLE_CHEST,
                        if (rightTriple) BridgeRampart.TRIPLE_CHEST else BridgeRampart.SINGLE_CHEST
                    )
                )
            } else {
                // Stables
                val leftGoodGap = (byteAsInt and 0b00000010) == 0b00000010
                val rightGoodGap = (byteAsInt and 0b00000100) == 0b00000100
                val rampartEncoding = byteAsInt shr 3
                val rightRampartNum = rampartEncoding % 3
                val middleRampartNum = (rampartEncoding % 9) / 3
                val leftRampartNum = rampartEncoding / 9
                return BastionIndexedByInfo(
                    BastionType.STABLES, StablesRamparts(
                        fromNum(leftRampartNum),
                        fromNum(middleRampartNum),
                        fromNum(rightRampartNum),
                        if (leftGoodGap) StablesGap.GOOD_GAP else StablesGap.BAD_GAP,
                        if (rightGoodGap) StablesGap.GOOD_GAP else StablesGap.BAD_GAP
                    )
                )
            }
        }

        private fun StablesRampart.getNum(): Int {
            return when(this) {
                StablesRampart.SINGLE_CHEST -> 0
                StablesRampart.DOUBLE_CHEST -> 1
                StablesRampart.TRIPLE_CHEST -> 2
            }
        }

        private fun fromNum(num: Int): StablesRampart {
            return when(num) {
                0 -> StablesRampart.SINGLE_CHEST
                1 -> StablesRampart.DOUBLE_CHEST
                else -> StablesRampart.TRIPLE_CHEST
            }
        }
    }
}

object BastionIndexedByInfoSerializer : KSerializer<BastionIndexedByInfo> {
    override val descriptor = Byte.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: BastionIndexedByInfo
    ) {
        encoder.encodeByte(value.toByte().toByte())
    }

    override fun deserialize(decoder: Decoder): BastionIndexedByInfo {
        return BastionIndexedByInfo.fromByte(decoder.decodeByte())
    }
}

data class SavedBastionInfo(
    val pos: CPos,
    val indexedByInfo: BastionIndexedByInfo,
    val hasObbyChest: Boolean,
    val wetManhunt: Boolean,
)

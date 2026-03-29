package me.repeater64.mpkseedfilter.filtering.fortress

import com.seedfinding.mccore.util.block.BlockBox
import com.seedfinding.mccore.util.block.BlockDirection
import kludwisz.fortressgen.StaticFortressGenerator

data class FortPiece(
    val boundingBox: BlockBox,
    val orientation: BlockDirection,
    val type: PieceType
) {
    constructor(piece: StaticFortressGenerator.Piece) : this(piece.boundingBox, piece.orientation, getPieceType(piece))

    companion object {
        fun getPieceType(piece: StaticFortressGenerator.Piece): PieceType {
            return when (piece) {
                is StaticFortressGenerator.StartPiece -> PieceType.NETHER_FORTRESS_START
                is StaticFortressGenerator.BridgeCrossing -> PieceType.NETHER_FORTRESS_BRIDGE_CROSSING
                is StaticFortressGenerator.BridgeStraight -> PieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT
                is StaticFortressGenerator.BridgeEndFiller -> PieceType.NETHER_FORTRESS_BRIDGE_END_FILLER
                is StaticFortressGenerator.CastleCorridorStairsPiece -> PieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS
                is StaticFortressGenerator.CastleCorridorTBalconyPiece -> PieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY
                is StaticFortressGenerator.CastleEntrance -> PieceType.NETHER_FORTRESS_CASTLE_ENTRANCE
                is StaticFortressGenerator.CastleSmallCorridorCrossingPiece -> PieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING
                is StaticFortressGenerator.CastleSmallCorridorLeftTurnPiece -> PieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN
                is StaticFortressGenerator.CastleSmallCorridorPiece -> PieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR
                is StaticFortressGenerator.CastleSmallCorridorRightTurnPiece  -> PieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN
                is StaticFortressGenerator.CastleStalkRoom -> PieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM
                is StaticFortressGenerator.MonsterThrone -> PieceType.NETHER_FORTRESS_MONSTER_THRONE
                is StaticFortressGenerator.RoomCrossing -> PieceType.NETHER_FORTRESS_ROOM_CROSSING
                is StaticFortressGenerator.StairsRoom -> PieceType.NETHER_FORTRESS_STAIRS_ROOM
                else -> PieceType.NETHER_FORTRESS_START
            }
        }
    }
}

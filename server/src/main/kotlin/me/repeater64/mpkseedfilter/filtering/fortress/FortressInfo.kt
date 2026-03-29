package me.repeater64.mpkseedfilter.filtering.fortress

import com.seedfinding.mccore.util.block.BlockDirection
import com.seedfinding.mccore.util.pos.BPos
import com.seedfinding.mccore.util.pos.CPos
import me.repeater64.mpkseedfilter.dto.SpawnLocation
import me.repeater64.mpkseedfilter.dto.fortress.SavedFortressInfo
import me.repeater64.mpkseedfilter.filtering.toDTO

data class FortressInfo(
    val quadrantX: Int,
    val quadrantZ: Int,
    val pos: CPos,
    val spawner1Piece: FortPiece,
    val spawner2Piece: FortPiece,
    val lavaRoomPiece: FortPiece,
    val badPartPossibleSpawnPieces: List<FortPiece>,
    val goodPartPossibleSpawnPieces: List<FortPiece>,
    val openness: Double // 0 means good part of the fort is fully buried, 1 means fully open. Not guaranteed to be a perfect linear scale or to be generally perfect
) {

    val spawner1SpawnPoint by lazy { atSpawnerSpawnPoint(spawner1Piece) }
    val spawner2SpawnPoint by lazy { atSpawnerSpawnPoint(spawner2Piece) }

    val lavaRoomSpawnPoint: SpawnLocation
        get() {
            var pos = BPos(lavaRoomPiece.boundingBox.center)
            pos = pos.add(BPos(lavaRoomPiece.orientation.vector.invert()).shl(1))
            return SpawnLocation(
                pos.x + 0.5,
                pos.y - 2.0,
                pos.z + 0.5,
                lavaRoomYawFromOrientation(lavaRoomPiece.orientation)
            )
        }

    fun toSavedFortressInfo(): SavedFortressInfo {
        return SavedFortressInfo(
            this.pos.toDTO(),
            this.spawner1SpawnPoint,
            this.spawner2SpawnPoint,
            this.lavaRoomSpawnPoint,
            spawnPointForRandomPiece(this.badPartPossibleSpawnPieces.random()),
            spawnPointForRandomPiece(this.goodPartPossibleSpawnPieces.random()),
            this.openness
        )
    }

    companion object {
        private fun spawnPointForRandomPiece(piece: FortPiece) : SpawnLocation {
            val center = BPos(piece.boundingBox.center)
            return when (piece.type) {
                PieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT,
                PieceType.NETHER_FORTRESS_BRIDGE_CROSSING -> SpawnLocation(
                    center.x + 0.5,
                    center.y + 0.0,
                    center.z + 0.5,
                    0.0
                )

                else -> SpawnLocation(center.x + 0.5, center.y - 1.0, center.z + 0.5, 0.0)
            }
        }
        private fun atSpawnerSpawnPoint(spawnerPiece: FortPiece) : SpawnLocation {
            return when (spawnerPiece.orientation) {
                BlockDirection.NORTH -> SpawnLocation(
                    spawnerPiece.boundingBox.minX + 3.5,
                    spawnerPiece.boundingBox.minY + 2.0,
                    spawnerPiece.boundingBox.minZ + 12.5,
                    180.0
                )
                BlockDirection.EAST -> SpawnLocation(
                    spawnerPiece.boundingBox.minX - 3.5,
                    spawnerPiece.boundingBox.minY + 2.0,
                    spawnerPiece.boundingBox.minZ + 3.5,
                    -90.0
                )
                BlockDirection.SOUTH -> SpawnLocation(
                    spawnerPiece.boundingBox.minX + 3.5,
                    spawnerPiece.boundingBox.minY + 2.0,
                    spawnerPiece.boundingBox.minZ - 3.5,
                    0.0
                )
                else -> SpawnLocation(
                    spawnerPiece.boundingBox.minX + 12.5,
                    spawnerPiece.boundingBox.minY + 2.0,
                    spawnerPiece.boundingBox.minZ + 3.5,
                    -90.0
                )
            }
        }

        private fun lavaRoomYawFromOrientation(orientation: BlockDirection): Double {
            return when (orientation) {
                BlockDirection.NORTH -> 0.0
                BlockDirection.EAST -> 90.0
                BlockDirection.SOUTH -> 180.0
                else -> -90.0
            }
        }
    }
}
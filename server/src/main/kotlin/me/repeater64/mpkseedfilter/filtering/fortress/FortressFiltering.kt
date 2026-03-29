package me.repeater64.mpkseedfilter.filtering.fortress

import com.seedfinding.mcbiome.source.NetherBiomeSource
import com.seedfinding.mccore.rand.ChunkRand
import com.seedfinding.mccore.util.math.DistanceMetric
import com.seedfinding.mccore.util.pos.BPos
import com.seedfinding.mccore.util.pos.CPos
import com.seedfinding.mcfeature.structure.Fortress
import com.seedfinding.mcterrain.terrain.NetherTerrainGenerator
import kludwisz.fortressgen.FortressGenerator
import kludwisz.fortressgen.StaticFortressGenerator
import kludwisz.fortressgen.StaticFortressGenerator.Piece
import me.repeater64.mpkseedfilter.filtering.VERSION

object FortressFiltering {
    val fortress = Fortress(VERSION)

    fun genFortress(worldSeed: Long, chunkPos: CPos): List<Piece> {
        val fortressGenerator = FortressGenerator()
        val structureSeed = worldSeed and 0x0000FFFFFFFFFFFFL
        fortressGenerator.generate(structureSeed, chunkPos.x, chunkPos.z, false)

        return fortressGenerator.pieces
    }

    fun getFortressSpawnerInfo(gennedFortPlacements: List<Piece>): List<Piece> {
        val result = mutableListOf<Piece>()

        var spawners = 0
        for (piece in gennedFortPlacements) {
            if (spawners >= 2) break

            if (piece is StaticFortressGenerator.MonsterThrone) {
                spawners++
                result.add(piece)
            }
        }

        return result
    }

    fun approxSpawnerBPosHorizontal(spawnerPieceInfo: FortPiece): BPos {
        return BPos((spawnerPieceInfo.boundingBox.maxX + spawnerPieceInfo.boundingBox.minX) / 2, 0, (spawnerPieceInfo.boundingBox.maxZ + spawnerPieceInfo.boundingBox.minZ) / 2)
    }

    fun getFortBadPartSpawnLocations(gennedFortPlacements: List<Piece>) : List<FortPiece> {
        return gennedFortPlacements.filter { it is StaticFortressGenerator.CastleSmallCorridorPiece || it is StaticFortressGenerator.CastleSmallCorridorCrossingPiece }.map { FortPiece(it) }
    }

    fun getFortOpennessAndGoodPartSpawnLocations(seed: Long, gennedFortPlacements: List<Piece>): Pair<Double, List<FortPiece>> {
        val biomeSource = NetherBiomeSource(VERSION, seed)
        val terrainGenerator = NetherTerrainGenerator(biomeSource)

        val consideredPieces = gennedFortPlacements.filter { it is StaticFortressGenerator.BridgeStraight || it is StaticFortressGenerator.BridgeCrossing }
        var total = 0
        var open = 0
        for (straight in consideredPieces) {
            val centerPos = BPos(straight.boundingBox.center)
            val blockAt = terrainGenerator.getBlockAt(centerPos)
            if (blockAt.isPresent) {
                total++
                if (blockAt.get().name == "air") open++
            }
        }

        return Pair(open.toDouble() / total.toDouble(), consideredPieces.map { FortPiece(it) })
    }

    // Returns fortress info if a good fortress (position wise) is found.
    // Requirements for good fortress position:
    //  - In one of the 4 quadrants around the origin
    //  - Within 16*16 blocks of a position where a good bastion could generate
    fun getGoodFortress(seed: Long): FortressInfo? {
        val quadrants = listOf(Pair(0, 0), Pair(0, -1), Pair(-1, 0), Pair(-1, -1)).shuffled() // Check quadrants in random order so we aren't unnaturally biased towards bastions in a certain quadrant

        for ((quadrantX, quadrantZ) in quadrants) {
            val chunkPos = fortressInQuadrant(seed, quadrantX, quadrantZ) ?: continue
            val blockPos = chunkPos.toBlockPos(0)

            // Some hardcoded logic to work out if this fortress is within 16 chunks (256 blocks) of a position where a bastion could generate.
            // These conditions were figured out by playing with this Desmos graph: https://www.desmos.com/calculator/g72jb7giof

            val fortGood = if (quadrantX == 0 && quadrantZ == 0) {
                if (BPos(214, 0, -64).distanceTo(blockPos, DistanceMetric.EUCLIDEAN_SQ) < 256*256) true
                else if (BPos(-64, 0, 214).distanceTo(blockPos, DistanceMetric.EUCLIDEAN_SQ) < 256*256) true
                else false
            } else if (quadrantX == -1 && quadrantZ == 0) {
                if (BPos(0, 0, 224).distanceTo(blockPos, DistanceMetric.EUCLIDEAN_SQ) < 256*256) true
                else if (BPos(-214, 0, -64).distanceTo(blockPos, DistanceMetric.EUCLIDEAN_SQ) < 256*256) true
                else false
            } else if (quadrantX == 0 && quadrantZ == -1) {
                if (BPos(224, 0, 0).distanceTo(blockPos, DistanceMetric.EUCLIDEAN_SQ) < 256*256) true
                else if (BPos(-64, 0, -214).distanceTo(blockPos, DistanceMetric.EUCLIDEAN_SQ) < 256*256) true
                else false
            } else {
                if (BPos(-224, 0, 0).distanceTo(blockPos, DistanceMetric.EUCLIDEAN_SQ) < 256*256) true
                else if (BPos(0, 0, -224).distanceTo(blockPos, DistanceMetric.EUCLIDEAN_SQ) < 256*256) true
                else false
            }

            if (fortGood) {
                val gennedFortPlacements = genFortress(seed, chunkPos)

                val spawnerLocations = getFortressSpawnerInfo(gennedFortPlacements)
                if (spawnerLocations.size != 2) return null

                val (openness, goodPartPossibleSpawnLocations) = getFortOpennessAndGoodPartSpawnLocations(seed, gennedFortPlacements)
                if (goodPartPossibleSpawnLocations.isEmpty()) return null

                val badPartPossibleSpawnLocations = getFortBadPartSpawnLocations(gennedFortPlacements)
                if (badPartPossibleSpawnLocations.isEmpty()) return null

                return FortressInfo(
                    quadrantX, quadrantZ, chunkPos,
                    FortPiece(spawnerLocations[0]), FortPiece(spawnerLocations[1]),
                    FortPiece(gennedFortPlacements.firstOrNull { it is StaticFortressGenerator.CastleEntrance } ?: return null),
                    badPartPossibleSpawnLocations, goodPartPossibleSpawnLocations,
                    openness
                )
            }
        }

        return null
    }

    fun fortressInQuadrant(seed: Long, quadrantX: Int, quadrantZ: Int): CPos? {
        val structureSeed = seed and 0x0000FFFFFFFFFFFFL
        val rand = ChunkRand()
        return fortress.getInRegion(structureSeed, quadrantX, quadrantZ, rand)
    }
}
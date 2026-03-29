package me.repeater64.mpkseedfilter.filtering

import Xinyuiii.enumType.BastionType
import com.seedfinding.mcbiome.source.NetherBiomeSource
import com.seedfinding.mcbiome.source.OverworldBiomeSource
import com.seedfinding.mccore.rand.ChunkRand
import com.seedfinding.mccore.util.math.DistanceMetric
import com.seedfinding.mccore.util.pos.BPos
import com.seedfinding.mccore.util.pos.CPos
import com.seedfinding.mcfeature.structure.Stronghold
import com.seedfinding.mcterrain.terrain.NetherTerrainGenerator
import me.repeater64.mpkseedfilter.filtering.bastion.BastionFiltering
import me.repeater64.mpkseedfilter.filtering.bastion.BastionInfo
import me.repeater64.mpkseedfilter.filtering.fortress.FortressFiltering
import me.repeater64.mpkseedfilter.filtering.fortress.FortressInfo
import kotlin.math.roundToInt
import kotlin.math.sqrt

object GeneralNetherFiltering {
    val stronghold = Stronghold(VERSION)

    // Checks if this seed is suitable to play the nether entry -> bastion split on, with the specified bastion position. Assumes the bastion is already a "good" bastion
    // Requirements:
    //  - Any other bastions must be at least 10 chunks further away from 0,0 than the specified bastion
    //  - Successful open terrain check between 0,0 and bastion
    fun hasGoodSpawnToBastion(seed: Long, bastionInfo: BastionInfo): Boolean {
        val bastionBlockPos = bastionInfo.pos.toBlockPos()
        val intendedBastionDist = bastionBlockPos.distanceTo(BPos(0, 0, 0), DistanceMetric.EUCLIDEAN)

        for (quadrantX in -1..0) {
            for (quadrantZ in -1..0) {
                if (quadrantX == bastionInfo.quadrantX && quadrantZ == bastionInfo.quadrantZ) continue
                BastionFiltering.bastionInQuadrant(seed, quadrantX, quadrantZ)?.let {
                    val dist = it.toBlockPos().distanceTo(BPos(0, 0, 0), DistanceMetric.EUCLIDEAN)
                    if (dist < intendedBastionDist+10*16) {
                        return false
                    }
                }
            }
        }

        return isOpenEnoughAlongPath(seed, 0, 0, bastionBlockPos.x, bastionBlockPos.z, false, 0.7)
    }

    // Checks if this seed is suitable to play the bastion -> fort split on, with the specified fortress and bastion positions
    // Requirements:
    //  - Fort is within 16*16 blocks of the bastion
    //  - No treasure bastions anywhere close to the specified bastion that the player could possibly get led to by piedar
    //  - No fortresses other than the specified one that the player could get led to by piedar from the bastion
    //  - Successful open terrain check between bastion and fort (lava lakes are fine)
    fun hasGoodBastionToFort(seed: Long, bastionInfo: BastionInfo, fortInfo: FortressInfo): Boolean {
        val bastionBlockPos = bastionInfo.pos.toBlockPos()
        val fortBlockPos = fortInfo.pos.toBlockPos()

        // Bastion -> fort distance check
        if (bastionBlockPos.distanceTo(fortBlockPos, DistanceMetric.EUCLIDEAN_SQ) > 256*256) return false

        // Check for treasures and spawners of other fortresses, make sure none could be closer (all distances calculated with Chebyshev distance as that's what determines piedar)
        val spawner1Pos = FortressFiltering.approxSpawnerBPosHorizontal(fortInfo.spawner1Piece)
        val spawner2Pos = FortressFiltering.approxSpawnerBPosHorizontal(fortInfo.spawner2Piece)
        val spawner1Dist = spawner1Pos.distanceTo(bastionBlockPos, DistanceMetric.CHEBYSHEV)
        val spawner2Dist = spawner2Pos.distanceTo(bastionBlockPos, DistanceMetric.CHEBYSHEV)
        val closestSpawnerDist = minOf(spawner1Dist, spawner2Dist)

        for (quadrantX in -2..1) {
            for (quadrantZ in -2..1) {
                if ((quadrantX == fortInfo.quadrantX && quadrantZ == fortInfo.quadrantZ) || (quadrantX == bastionInfo.quadrantX && quadrantZ == bastionInfo.quadrantZ)) continue

                val otherBastionPos = BastionFiltering.bastionInQuadrant(seed, quadrantX, quadrantZ)
                if (otherBastionPos?.let { BastionFiltering.getBastionType(seed, it) } == BastionType.TREASURE) {
                    val distToTreasure = otherBastionPos.toBlockPos().distanceTo(bastionBlockPos, DistanceMetric.CHEBYSHEV)
                    if (distToTreasure < closestSpawnerDist + 100) { // 100 block margin to account for spawner not being at exact origin of treasure, player not piedaring from exact origin of their bastion, to be safe
                        return false
                    }
                }

                val otherFortressPos = FortressFiltering.fortressInQuadrant(seed, quadrantX, quadrantZ)
                if (otherFortressPos != null) {
                    val distToOtherFort = otherFortressPos.toBlockPos().distanceTo(bastionBlockPos, DistanceMetric.CHEBYSHEV)
                    if (distToOtherFort < closestSpawnerDist + 128 + 50) { // 128 is max extent of fortress, 50 is safety margin to account for player not piedaring from exact origin of their bastion
                        return false
                    }
                }
            }
        }


        return isOpenEnoughAlongPath(seed, bastionBlockPos.x, bastionBlockPos.z, fortBlockPos.x, fortBlockPos.z, true, 0.85)
    }

    fun getBlindToStrongholdOpenness(seed: Long, fortInfo: FortressInfo): Pair<Double, Double> { // Openness from the two spawners
        val spawner1Pos = fortInfo.spawner1SpawnPoint.getBlockPos()
        val spawner2Pos = fortInfo.spawner2SpawnPoint.getBlockPos()

        val strongholdLocations = getFirstTwoRingsStrongholdLocations(seed)

        val shSpawner1NetherPos = getClosestStronghold(spawner1Pos, strongholdLocations).shr(3, 0, 3)
        val shSpawner2NetherPos = getClosestStronghold(spawner2Pos, strongholdLocations).shr(3, 0, 3)

        return Pair(
            getOpennessAlongPath(seed, spawner1Pos.x, spawner1Pos.z, shSpawner1NetherPos.x, shSpawner1NetherPos.z, true, null),
            getOpennessAlongPath(seed, spawner2Pos.x, spawner2Pos.z, shSpawner2NetherPos.x, shSpawner2NetherPos.z, true, null)
        )
    }

    fun getClosestStronghold(spawnerPos: BPos, strongholdChunks: List<CPos>): BPos { // Returns overworld BPos
        val spawnerOverworldPos = spawnerPos.shl(3, 0, 3)

        var closestDistSq = Double.MAX_VALUE
        var closestStronghold: BPos? = null
        for (chunk in strongholdChunks) {
            val strongholdBpos = chunk.toBlockPos(spawnerPos.y)
            val dist = strongholdBpos.distanceTo(spawnerOverworldPos, DistanceMetric.EUCLIDEAN_SQ)
            if (dist < closestDistSq) {
                closestDistSq = dist
                closestStronghold = strongholdBpos
            }
        }
        return closestStronghold!!
    }

    fun getFirstTwoRingsStrongholdLocations(seed: Long) : List<CPos> {
        val biomeSource = OverworldBiomeSource(VERSION, seed)

        val rand = ChunkRand()
        return stronghold.getStarts(biomeSource, 9, rand).toList()
    }

    fun isOpenEnoughAlongPath(seed: Long, fromX: Int, fromZ: Int, toX: Int, toZ: Int, lavaFine: Boolean, minOpenness: Double): Boolean {
        return getOpennessAlongPath(seed, fromX, fromZ, toX, toZ, lavaFine, minOpenness) >= minOpenness
    }

    fun getOpennessAlongPath(seed: Long, fromX: Int, fromZ: Int, toX: Int, toZ: Int, lavaFine: Boolean, minOpenness: Double?): Double {
        val biomeSource = NetherBiomeSource(VERSION, seed)
        val terrainGenerator = NetherTerrainGenerator(biomeSource)

        val dist = sqrt(((fromX - toX) * (fromX - toX) + (fromZ - toZ) * (fromZ - toZ)).toDouble())
        if (dist == 0.0) return 1.0

        val unitVecX = (toX-fromX) / dist
        val unitVecZ = (toZ-fromZ) / dist

        val numSamples = ((dist / 15).coerceAtMost(50.0)).roundToInt().coerceAtLeast(1)
        val distIncrement = dist/numSamples

        var currentX = fromX.toDouble()
        var currentZ = fromZ.toDouble()
        var numOpens = 0
        repeat(numSamples) { repeatIndex ->
            currentX += unitVecX*distIncrement
            currentZ += unitVecZ*distIncrement

            if (isOpenAt(currentX.roundToInt(), currentZ.roundToInt(), terrainGenerator, lavaFine)) {
                numOpens++
                if (minOpenness != null) {
                    // If we'd meet the openness requirement even if the rest of the checks failed, the check is succeeded
                    if (numOpens.toDouble() / numSamples >= minOpenness) return 1.0 // Can pretend it's fully open
                }
            } else {
                // If we've failed enough checks such that it's no longer possible to meet the min openness, the check is failed
                val iterationsLeft = numSamples-repeatIndex-1
                if (minOpenness != null) {
                    if ((numOpens + iterationsLeft).toDouble() / numSamples < minOpenness) return 0.0 // Can pretend it's fully closed
                }
            }
        }

        return numOpens.toDouble() / numSamples
    }

    fun isOpenAt(x: Int, z: Int, terrainGenerator: NetherTerrainGenerator, lavaFine: Boolean) : Boolean {
        val column = terrainGenerator.getColumnAt(x, z)
        var consecutiveAir = 0
        var lavaBelowAir = false
        for (y in 32..105) {
            if (column[y].name == "air" || column[y].name == "cave_air") {
                if (lavaFine || !lavaBelowAir) {
                    consecutiveAir++
                    if (consecutiveAir == 6) {
                        return true
                    }
                }
            } else {
                consecutiveAir = 0
                if (column[y].name == "lava") {
                    lavaBelowAir = true
                } else {
                    lavaBelowAir = false
                }
            }
        }
        return false
    }
}
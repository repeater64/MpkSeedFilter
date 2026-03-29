package me.repeater64.mpkseedfilter.filtering.end

import me.repeater64.mpkseedfilter.dto.end.SeedSpawnInfo
import me.repeater64.mpkseedfilter.dto.end.SpawnType

object SpawnSeedFilter {
    fun getSpawnInfo(seed: Long): SeedSpawnInfo {
        val chunkGenerator = NoiseBasedChunkGenerator(seed)

        val mainChunk = chunkGenerator.doEndstoneGeneration(6, 0)

        // Check if back block is buried
        var buried = false
        var oLevel = -1
        var weirdIfNotSevereOverhang = false
        for (y in 64 downTo 52) {
            if (mainChunk[6][y][0]) {
                if (!buried) {
                    buried = true
                    oLevel = y
                }
            } else if (buried) {
                // Was buried but then some air underneath
                weirdIfNotSevereOverhang = true
                break
            }
        }

        if (buried) {
            // Check if fully enclosed.
            val backOpen = !mainChunk[7][50][0]
            if (!mainChunk[1][50][0] || !mainChunk[4][50][3] || backOpen) {
                // Front side or left side is open (not checking right side to avoid needing to gen an extra chunk)

                // Check for severe overhang
                if (backOpen && !mainChunk[8][50][0] && !mainChunk[9][50][0] && !mainChunk[10][50][0]) {
                    var opensUp = true
                    for (y in 64 downTo 51) {
                        if (mainChunk[10][y][0]) {
                            opensUp = false
                            break
                        }
                    }
                    if (opensUp) {
                        return SeedSpawnInfo(SpawnType.SEVERE_OVERHANG)
                    }
                }

                return SeedSpawnInfo(SpawnType.WEIRD)
            }

            if (weirdIfNotSevereOverhang) return SeedSpawnInfo(SpawnType.WEIRD)

            // At this point we are buried and enclosed on at least 3 sides. First check for o52
            if (oLevel == 52) {
                // At this point, it's decently likely that we are right side exposed rather than true buried 52, so worth loading that extra chunk to check
                val rightChunk = chunkGenerator.doEndstoneGeneration(6, -1)
                if (!rightChunk[4][50][13]) {
                    // We are right side exposed
                    return SeedSpawnInfo(SpawnType.WEIRD)
                }
            }

            // At this point we are buried and fully enclosed. Check for flat or not.
            return if (mainChunk[5][oLevel][0] && !mainChunk[5][oLevel+1][0] && mainChunk[7][oLevel][0] && !mainChunk[7][oLevel+1][0]) {
                SeedSpawnInfo(SpawnType.BURIED_FLAT, oLevel)
            } else {
                SeedSpawnInfo(SpawnType.BURIED_NOT_FLAT, oLevel)
            }
        }

        // Not buried at this point.

        // Check for overhang
        if (mainChunk[1][50][0]) {
            return SeedSpawnInfo(SpawnType.OVERHANG)
        }

        // Check for void
        val inFrontChunk = chunkGenerator.doEndstoneGeneration(5, 0)
        for (y in 70 downTo 0) {
            if (inFrontChunk[12][y][0]) {
                return if (y > 51) {
                    SeedSpawnInfo(SpawnType.POTENTIALLY_MILD_OVERHANG)
                } else {
                    SeedSpawnInfo(SpawnType.EASY)
                }
            }
        }

        return SeedSpawnInfo(SpawnType.VOID)
    }
}
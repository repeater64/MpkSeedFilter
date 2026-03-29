package me.repeater64.mpkseedfilter.filtering.end

import me.repeater64.mpkseedfilter.dto.end.Pillar
import me.repeater64.mpkseedfilter.dto.end.SeedPillarInfo
import java.util.*

object PillarSeedFilter {
    fun getPillarInfo(seed: Long): SeedPillarInfo {
        val pillarSeed = Random(seed).nextLong() and 65535L

        val pillarIndices = (0..9).toMutableList()
        @Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
        Collections.shuffle(pillarIndices, Random(pillarSeed))
        val frontStraightPillar = Pillar.entries[pillarIndices[0]]
        val backStraightPillar = Pillar.entries[pillarIndices[5]]
        val frontDragon = frontStraightPillar.topHeight > backStraightPillar.topHeight
        return SeedPillarInfo(frontDragon, if (frontDragon) Pillar.entries[pillarIndices[9]] else Pillar.entries[pillarIndices[4]])
    }
}
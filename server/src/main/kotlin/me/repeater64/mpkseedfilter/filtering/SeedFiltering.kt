package me.repeater64.mpkseedfilter.filtering

import me.repeater64.mpkseedfilter.dto.SavedSeedInfo
import me.repeater64.mpkseedfilter.dto.end.EndInfo
import me.repeater64.mpkseedfilter.filtering.bastion.BastionFiltering
import me.repeater64.mpkseedfilter.filtering.database.LoadedSeedDatabase
import me.repeater64.mpkseedfilter.filtering.end.PillarSeedFilter
import me.repeater64.mpkseedfilter.filtering.end.SpawnSeedFilter
import me.repeater64.mpkseedfilter.filtering.fortress.FortressFiltering

object SeedFiltering {
    fun filterSeed(seed: Long) {
        val bastionInfo = BastionFiltering.getGoodBastion(seed) ?: return
        val fortInfo = FortressFiltering.getGoodFortress(seed) ?: return

        val goodSpawnToBastion = GeneralNetherFiltering.hasGoodSpawnToBastion(seed, bastionInfo)
        val goodBastionToFort = GeneralNetherFiltering.hasGoodBastionToFort(seed, bastionInfo, fortInfo)
        val toStrongholdOpennesses = GeneralNetherFiltering.getBlindToStrongholdOpenness(seed, fortInfo)
        val seedInfo = SavedSeedInfo(
            seed,
            goodSpawnToBastion,
            bastionInfo.toSavedBastionInfo(),
            goodBastionToFort,
            fortInfo.toSavedFortressInfo(),
            toStrongholdOpennesses,
            EndInfo(PillarSeedFilter.getPillarInfo(seed), SpawnSeedFilter.getSpawnInfo(seed))
        )

        LoadedSeedDatabase.db.addSeedInfo(seedInfo)

        LoadedSeedDatabase.db.addBastionSeed(seedInfo)
        LoadedSeedDatabase.db.addFortOnwardsSeed(seedInfo)
        LoadedSeedDatabase.db.addEndgameSeed(seedInfo)

        if (goodSpawnToBastion) {
            LoadedSeedDatabase.db.addEntryToBastionSeed(seedInfo)
            if (goodBastionToFort) {
                LoadedSeedDatabase.db.addEntryOnwardsSeed(seedInfo)
            }
        } else {
            if (goodBastionToFort) {
                LoadedSeedDatabase.db.addBastionOnwardsSeed(seedInfo)
            }
        }
    }
}
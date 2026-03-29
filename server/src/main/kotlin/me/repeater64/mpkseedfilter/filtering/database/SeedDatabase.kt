package me.repeater64.mpkseedfilter.filtering.database

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import me.repeater64.mpkseedfilter.dto.SavedSeedInfo
import me.repeater64.mpkseedfilter.dto.bastion.BastionIndexedByInfo
import me.repeater64.mpkseedfilter.dto.end.EndInfo
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import kotlin.collections.set


// The types of practice supported:
//  - Nether entry -> Bastion                       (entryToBastionSeeds)
//  - Nether entry -> Fort                          (entryOnwardsSeeds)
//  - Nether entry -> Finish                        (entryOnwardsSeeds)
//  - Bastion                                       (bastionSeeds)
//  - Bastion -> Fort                               (bastionOnwardsSeeds)
//  - Bastion -> Finish                             (bastionOnwardsSeeds)
//  - Fort                                          (fortOnwardsSeeds)
//  - Fort -> Finish                                (fortOnwardsSeeds)
//  - Endgame (stronghold enter or end enter)       (endgameSeeds)
@Serializable
data class SeedDatabase(
    val entryToBastionSeeds: HashMap<BastionIndexedByInfo, SeedList> = hashMapOf(),
    val entryOnwardsSeeds: HashMap<BastionIndexedByInfo, FortOpennessSortedSeedList> = hashMapOf(),
    val bastionSeeds: HashMap<BastionIndexedByInfo, SeedList> = hashMapOf(),
    val bastionOnwardsSeeds: HashMap<BastionIndexedByInfo, FortOpennessSortedSeedList> = hashMapOf(),
    val fortOnwardsSeeds: FortOpennessSortedSeedList = FortOpennessSortedSeedList(),
    val endgameSeeds: HashMap<EndInfo, SeedList> = hashMapOf(),

    val seedInfo: HashMap<Long, SavedSeedInfo> = hashMapOf(),
) {
    fun getSeedsEntryToBastion(userID: String, bastionInfo: BastionIndexedByInfo): List<Long> {
        val accessCounter = LoadedNumAccessesDatabase.db.getAndIncrementAccessCounterEntryToBastion(userID, bastionInfo)
        return entryToBastionSeeds.getOrDefault(bastionInfo, SeedList()).getSeeds(accessCounter)
    }

    fun getSeedsEntryOnwards(userID: String, bastionInfo: BastionIndexedByInfo, minOpennessIncl: Double, maxOpennessExcl: Double): List<Long> {
        val accessCounter = LoadedNumAccessesDatabase.db.getAndIncrementAccessCounterEntryOnwards(userID, bastionInfo)
        return entryOnwardsSeeds.getOrDefault(bastionInfo, FortOpennessSortedSeedList()).getSeeds(accessCounter, minOpennessIncl, maxOpennessExcl)
    }

    fun getSeedsBastion(userID: String, bastionInfo: BastionIndexedByInfo): List<Long> {
        val accessCounter = LoadedNumAccessesDatabase.db.getAndIncrementAccessCounterBastion(userID, bastionInfo)
        return bastionSeeds.getOrDefault(bastionInfo, SeedList()).getSeeds(accessCounter)
    }

    fun getSeedsBastionOnwards(userID: String, bastionInfo: BastionIndexedByInfo, minOpennessIncl: Double, maxOpennessExcl: Double): List<Long> {
        val accessCounter = LoadedNumAccessesDatabase.db.getAndIncrementAccessCounterBastionOnwards(userID, bastionInfo)
        return bastionOnwardsSeeds.getOrDefault(bastionInfo, FortOpennessSortedSeedList()).getSeeds(accessCounter, minOpennessIncl, maxOpennessExcl)
    }

    fun getSeedsFortOnwards(userID: String, minOpennessIncl: Double, maxOpennessExcl: Double): List<Long> {
        val accessCounter = LoadedNumAccessesDatabase.db.getAndIncrementAccessCounterFortOnwards(userID)
        return fortOnwardsSeeds.getSeeds(accessCounter, minOpennessIncl, maxOpennessExcl)
    }

    fun getSeedsEndgame(userID: String, endInfo: EndInfo): List<Long> {
        val accessCounter = LoadedNumAccessesDatabase.db.getAndIncrementAccessCounterEndgame(userID, endInfo)
        return endgameSeeds.getOrDefault(endInfo, SeedList()).getSeeds(accessCounter)
    }



    fun addSeedInfo(seedInfo: SavedSeedInfo) {
        this.seedInfo[seedInfo.seed] = seedInfo
    }

    fun addEntryToBastionSeed(seedInfo: SavedSeedInfo) {
        entryToBastionSeeds.getOrPut(seedInfo.bastionInfo!!.indexedByInfo) { SeedList(mutableListOf()) }.list.add(seedInfo.seed)
    }

    fun addEntryOnwardsSeed(seedInfo: SavedSeedInfo) {
        entryOnwardsSeeds.getOrPut(seedInfo.bastionInfo!!.indexedByInfo) { FortOpennessSortedSeedList() }.add(seedInfo)
    }

    fun addBastionSeed(seedInfo: SavedSeedInfo) {
        bastionSeeds.getOrPut(seedInfo.bastionInfo!!.indexedByInfo) { SeedList(mutableListOf()) }.list.add(seedInfo.seed)
    }

    fun addBastionOnwardsSeed(seedInfo: SavedSeedInfo) {
        bastionOnwardsSeeds.getOrPut(seedInfo.bastionInfo!!.indexedByInfo) { FortOpennessSortedSeedList() }.add(seedInfo)
    }

    fun addFortOnwardsSeed(seedInfo: SavedSeedInfo) {
        fortOnwardsSeeds.add(seedInfo)
    }

    fun addEndgameSeed(seedInfo: SavedSeedInfo) {
        endgameSeeds.getOrPut(seedInfo.endInfo) { SeedList(mutableListOf()) }.list.add(seedInfo.seed)
    }
}

val JSON = Json { allowStructuredMapKeys = true }
object LoadedSeedDatabase {
    lateinit var db: SeedDatabase

    private val filePath = "seeds_database.json".toPath()

    @OptIn(ExperimentalSerializationApi::class)
    fun saveToDisk() {
        FileSystem.SYSTEM.sink(filePath).buffer().use { sink ->
            JSON.encodeToBufferedSink(db, sink)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadFromDisk() {
        db = FileSystem.SYSTEM.source(filePath).buffer().use { source ->
            Json.decodeFromBufferedSource(source)
        }
    }
}
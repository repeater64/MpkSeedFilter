package me.repeater64.mpkseedfilter.filtering.database

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import me.repeater64.mpkseedfilter.dto.bastion.BastionIndexedByInfo
import me.repeater64.mpkseedfilter.dto.end.EndInfo
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import kotlin.random.Random

const val SEED_CHUNK_SIZE = 100

@Serializable
data class NumAccessesDatabase(
    // In each case, the key is the userID. The Int is the "num accesses", except for a new user ID it doesn't start from zero but some random value.

    private val entryToBastionNumAccesses: HashMap<String, HashMap<BastionIndexedByInfo, Int>> = hashMapOf(),
    private val entryOnwardsNumAccesses: HashMap<String, HashMap<BastionIndexedByInfo, Int>> = hashMapOf(),
    private val bastionNumAccesses: HashMap<String, HashMap<BastionIndexedByInfo, Int>> = hashMapOf(),
    private val bastionOnwardsNumAccesses: HashMap<String, HashMap<BastionIndexedByInfo, Int>> = hashMapOf(),
    private val fortOnwardsNumAccesses: HashMap<String, Int> = hashMapOf(),
    private val endgameNumAccesses: HashMap<String, HashMap<EndInfo, Int>> = hashMapOf(),
) {

    fun getAndIncrementAccessCounterEntryToBastion(userID: String, bastionInfo: BastionIndexedByInfo) : Int {
        return getAndIncrementAccessCounterWithBastionInfo(entryToBastionNumAccesses, userID, bastionInfo)
    }

    fun getAndIncrementAccessCounterEntryOnwards(userID: String, bastionInfo: BastionIndexedByInfo) : Int {
        return getAndIncrementAccessCounterWithBastionInfo(entryOnwardsNumAccesses, userID, bastionInfo)
    }

    fun getAndIncrementAccessCounterBastion(userID: String, bastionInfo: BastionIndexedByInfo) : Int {
        return getAndIncrementAccessCounterWithBastionInfo(bastionNumAccesses, userID, bastionInfo)
    }

    fun getAndIncrementAccessCounterBastionOnwards(userID: String, bastionInfo: BastionIndexedByInfo) : Int {
        return getAndIncrementAccessCounterWithBastionInfo(bastionOnwardsNumAccesses, userID, bastionInfo)
    }

    fun getAndIncrementAccessCounterFortOnwards(userID: String) : Int {
        val accessCounter = fortOnwardsNumAccesses.getOrPut(userID) { Random.nextInt(1000) }
        fortOnwardsNumAccesses.merge(userID, 1) { a, b -> a + b } // Increment it for next time
        return accessCounter
    }

    fun getAndIncrementAccessCounterEndgame(userID: String, endInfo: EndInfo) : Int {
        val innerMap = endgameNumAccesses.getOrPut(userID) { hashMapOf() }
        val accessCounter = innerMap.getOrPut(endInfo) { Random.nextInt(1000) }
        innerMap.merge(endInfo, 1) { a, b -> a + b } // Increment it for next time
        return accessCounter
    }

    private fun getAndIncrementAccessCounterWithBastionInfo(map: HashMap<String, HashMap<BastionIndexedByInfo, Int>>, userID: String, bastionInfo: BastionIndexedByInfo) : Int {
        val innerMap = map.getOrPut(userID) { hashMapOf() }
        val accessCounter = innerMap.getOrPut(bastionInfo) { Random.nextInt(1000) }
        innerMap.merge(bastionInfo, 1) { a, b -> a + b } // Increment it for next time
        return accessCounter
    }
}

object LoadedNumAccessesDatabase {
    lateinit var db: NumAccessesDatabase

    private val filePath = "num_accesses_database.json".toPath()

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
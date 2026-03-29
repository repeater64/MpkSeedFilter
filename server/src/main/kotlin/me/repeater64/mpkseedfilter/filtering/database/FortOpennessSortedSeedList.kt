package me.repeater64.mpkseedfilter.filtering.database

import kotlinx.serialization.Serializable
import me.repeater64.mpkseedfilter.dto.SavedSeedInfo
import kotlin.math.floor

@Serializable
data class FortOpennessSortedSeedList(
    private val buckets: Array<ArrayList<Long>> = Array(NUM_BUCKETS) { ArrayList() }
) {
    companion object {
        const val NUM_BUCKETS = 20
    }

    // First bucket is seeds with fort openness [0, 0.05), then [0.05, 0.1), ..., last bucket is seeds with fort openness [0.95, 1]

    fun add(seed: SavedSeedInfo) {
        if (seed.fortInfo == null) throw IllegalArgumentException("Can't add a seed without fort info to a FortOpennessSortedSeedList")

        val openness = seed.fortInfo!!.openness
        val bucketIndex = floor(openness * NUM_BUCKETS).toInt().coerceAtMost(NUM_BUCKETS-1) // Coerce needed to handle openness of 1

        buckets[bucketIndex].add(seed.seed)
    }

    // Only accurate if min and max match the bucket size. It is intended that only multiples of the bucket size are used for these parameters
    fun getSeeds(accessCounter: Int, minOpennessIncl: Double, maxOpennessExcl: Double): List<Long> {
        val minBucketIndex = floor(minOpennessIncl * NUM_BUCKETS).toInt().coerceAtMost(NUM_BUCKETS-1) // Coerce needed to handle minOpenness of 1
        val maxBucketIndex = floor(maxOpennessExcl * NUM_BUCKETS).toInt().coerceAtMost(NUM_BUCKETS) - 1

        if (maxBucketIndex < minBucketIndex) return emptyList()

        var totalNumSeeds = 0
        for (bucketIndex in minBucketIndex..maxBucketIndex) {
            totalNumSeeds+=buckets[bucketIndex].size
        }

        if (totalNumSeeds == 0) return emptyList()

        if (SEED_CHUNK_SIZE >= totalNumSeeds) {
            // We don't have enough seeds in matching buckets, so just return all the ones we have in matching buckets
            val toReturn = mutableListOf<Long>()
            for (bucketIndex in minBucketIndex..maxBucketIndex) {
                toReturn.addAll(buckets[bucketIndex])
            }
            return toReturn
        }

        val startIndex = (accessCounter* SEED_CHUNK_SIZE) % totalNumSeeds


        val toReturn = collectSeedsNoWrapping(minBucketIndex, maxBucketIndex, startIndex, SEED_CHUNK_SIZE)

        return if (toReturn.size == SEED_CHUNK_SIZE) {
            // We found all the needed seeds without wrapping
            toReturn
        } else {
            toReturn.addAll(collectSeedsNoWrapping(minBucketIndex, maxBucketIndex, 0, SEED_CHUNK_SIZE -toReturn.size))
            toReturn
        }
    }

    private fun collectSeedsNoWrapping(minBucketIndex: Int, maxBucketIndex: Int, startIndex: Int, numSeeds: Int) : MutableList<Long> {
        var bucketStartIndex = 0
        var foundStart = false
        val toReturn = mutableListOf<Long>()
        for (bucketIndex in minBucketIndex..maxBucketIndex) {
            val bucket = buckets[bucketIndex]
            val bucketSize = bucket.size

            if (!foundStart) {
                if (startIndex in bucketStartIndex until (bucketStartIndex+bucketSize)) {
                    foundStart = true
                    if (numSeeds <= bucketSize-(startIndex-bucketStartIndex)) {
                        // All the seeds we need are in this bucket
                        return bucket.subList(startIndex-bucketStartIndex, startIndex-bucketStartIndex + numSeeds)
                    } else {
                        // Will need all seeds in this bucket from startIndex on, plus more seeds from future buckets
                        toReturn.addAll(bucket.subList(startIndex-bucketStartIndex, bucketSize))
                    }
                }
            } else {
                val numSeedsStillNeeded = numSeeds-toReturn.size
                if (numSeedsStillNeeded <= bucketSize) {
                    // All the remaining seeds we need are in this bucket
                    toReturn.addAll(bucket.subList(0, numSeedsStillNeeded))
                    return toReturn
                } else {
                    // We'll need all the seeds in this bucket, plus more from future buckets
                    toReturn.addAll(bucket)
                }
            }

            bucketStartIndex += bucketSize
        }

        return toReturn // If we return here it means we weren't able to find numSeeds seeds without wrapping
    }

    override fun toString(): String {
        return buckets.contentToString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FortOpennessSortedSeedList

        return buckets.contentEquals(other.buckets)
    }

    override fun hashCode(): Int {
        return buckets.contentHashCode()
    }
}
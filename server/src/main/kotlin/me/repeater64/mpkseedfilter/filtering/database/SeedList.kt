package me.repeater64.mpkseedfilter.filtering.database

import kotlinx.serialization.Serializable

@Serializable
data class SeedList(
    val list: MutableList<Long> = mutableListOf()
) {

    fun getSeeds(accessCounter: Int): List<Long> {
        if (list.size <= SEED_CHUNK_SIZE) {
            return list
        }

        val startIndex = (accessCounter* SEED_CHUNK_SIZE) % list.size

        return if (startIndex + SEED_CHUNK_SIZE <= list.size) {
            // No wrapping around to the start, can return a simple sublist
            list.subList(startIndex, startIndex + SEED_CHUNK_SIZE)
        } else {
            // Wrap around to some of the start of the list
            list.subList(startIndex, list.size) + list.subList(0, SEED_CHUNK_SIZE - (list.size - startIndex))
        }
    }
}
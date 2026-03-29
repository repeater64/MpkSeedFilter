package me.repeater64.mpkseedfilter.dto.fortress

import me.repeater64.mpkseedfilter.dto.util.CPos
import me.repeater64.mpkseedfilter.dto.SpawnLocation

data class SavedFortressInfo(
    val pos: CPos,
    val spawner1SpawnPoint: SpawnLocation,
    val spawner2SpawnPoint: SpawnLocation,
    val lavaRoomSpawnPoint: SpawnLocation,
    val badPartRandomSpawnPoint: SpawnLocation,
    val goodPartRandomSpawnPoint: SpawnLocation,
    val openness: Double
)
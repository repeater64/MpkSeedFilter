package me.repeater64.mpkseedfilter.dto.end

data class SeedPillarInfo(
    val frontDragon: Boolean,
    val pillar: Pillar
) {
    val displayName = "${if (frontDragon) "Front" else "Back"} ${pillar.displayName}"
}

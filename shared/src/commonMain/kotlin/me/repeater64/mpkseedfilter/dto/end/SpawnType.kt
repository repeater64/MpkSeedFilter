package me.repeater64.mpkseedfilter.dto.end

enum class SpawnType(val index: Int) {
    BURIED_FLAT(0),
    BURIED_NOT_FLAT(1),
    VOID(2),
    EASY(3),
    POTENTIALLY_MILD_OVERHANG(4),
    OVERHANG(5),
    SEVERE_OVERHANG(6),
    WEIRD(7);

    companion object {
        fun fromIndex(index: Int): SpawnType {
            return entries.first { it.index == index }
        }
    }
}
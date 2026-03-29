package me.repeater64.mpkseedfilter.dto.end

enum class Pillar(public val index: Int, public val displayName: String, public val topHeight: Int) {
    SMALL_BOY(0, "Small Boy", 76),
    SMALL_CAGE(1, "Small Cage", 83),
    TALL_CAGE(2, "Tall Cage", 86),
    M_85(3, "M85", 85),
    M_88(4, "M88", 88),
    M_91(5, "M91", 91),
    T_94(6, "T94", 94),
    T_97(7, "T97", 97),
    T_100(8, "T100", 100),
    TALL_BOY(9, "Tall Boy", 103);

    companion object {
        fun fromIndex(index: Int): Pillar {
            return entries.first { it.index == index }
        }
    }
}
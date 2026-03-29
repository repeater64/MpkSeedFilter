package me.repeater64.mpkseedfilter.filtering.bastion

import Xinyuiii.enumType.BastionType
import Xinyuiii.properties.BastionGenerator
import com.seedfinding.mcbiome.source.NetherBiomeSource
import com.seedfinding.mccore.rand.ChunkRand
import com.seedfinding.mccore.util.block.BlockRotation
import com.seedfinding.mccore.util.math.DistanceMetric
import com.seedfinding.mccore.util.pos.BPos
import com.seedfinding.mccore.util.pos.CPos
import com.seedfinding.mcfeature.structure.BastionRemnant
import com.seedfinding.mcterrain.terrain.NetherTerrainGenerator
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.BridgeRampart
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.BridgeRamparts
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.HousingRampart
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.HousingRamparts
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.StablesGap
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.StablesRampart
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.StablesRamparts
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.TreasureRamparts
import me.repeater64.mpkseedfilter.filtering.VERSION
import kotlin.jvm.optionals.getOrNull

object BastionFiltering {
    val bastionRemnant = BastionRemnant(VERSION)

    val rampartsForObby = mapOf(
        BastionType.TREASURE to setOf("treasure/ramparts/mid_wall_main", "treasure/ramparts/mid_wall_side"), // Main treasure ramparts
        BastionType.STABLES to setOf("hoglin_stable/ramparts/ramparts_1", "hoglin_stable/ramparts/ramparts_2", "hoglin_stable/ramparts/ramparts_3"), // Main stables ramparts
        BastionType.HOUSING to setOf("units/ramparts/ramparts_0", "units/ramparts/ramparts_1", "units/walls/wall_base", "units/center_pieces/center_0", "units/center_pieces/center_1", "units/center_pieces/center_2"), // Triples + single chest, bottom double chest, manhunt chest
        BastionType.BRIDGE to setOf("bridge/ramparts/rampart_1", "bridge/ramparts/rampart_0") // Triple and single chest ramparts
    )

    // Returns bastion info if a good bastion (position wise) is found.
    // Requirements for good bastion position:
    //  - In one of the 4 quadrants around the origin
    //  - Less than 14*16 blocks away from the origin
    fun getGoodBastion(seed: Long): BastionInfo? {
        val quadrants = listOf(Pair(0, 0), Pair(0, -1), Pair(-1, 0), Pair(-1, -1)).shuffled() // Check quadrants in random order so we aren't unnaturally biased towards bastions in a certain quadrant

        for ((quadrantX, quadrantZ) in quadrants) {
            val chunkPos = bastionInQuadrant(seed, quadrantX, quadrantZ) ?: continue

            val distSq = chunkPos.toBlockPos(0).distanceTo(BPos(0, 0, 0), DistanceMetric.EUCLIDEAN_SQ)
            if (distSq <= (14*16)*(14*16)) {
                // This is a good bastion

                val generator = BastionGenerator(VERSION)
                generator.generate(seed, chunkPos)

                val type = generator.type
                val hasObbyChest = generator.hasFiveObsidianChest_1_16_1(rampartsForObby[type])
                val ramparts = when (type) {
                    BastionType.TREASURE -> TreasureRamparts
                    BastionType.HOUSING -> getHousingRamparts(generator)
                    BastionType.BRIDGE -> getBridgeRamparts(generator)
                    BastionType.STABLES -> getStablesRamparts(generator)
                }

                val wetManhunt = if (type == BastionType.HOUSING) {
                    val biomeSource = NetherBiomeSource(VERSION, seed)
                    val terrainGenerator = NetherTerrainGenerator(biomeSource)

                    var offset1 = BPos(-4, -1, 7)
                    var offset2 = BPos(-9, -1, 7)

                    val centerPiece = generator.pieces.first { it.name.startsWith("units/center_pieces/center_") }
                    when (centerPiece.rotation) {
                        BlockRotation.NONE -> {}
                        BlockRotation.CLOCKWISE_180 -> {
                            offset1 = BPos(-offset1.x, offset1.y, -offset1.z)
                            offset2 = BPos(-offset2.x, offset2.y, -offset2.z)
                        }
                        BlockRotation.CLOCKWISE_90 -> {
                            offset1 = BPos(-offset1.z, offset1.y, offset1.x)
                            offset2 = BPos(-offset2.z, offset2.y, offset2.x)
                        }
                        BlockRotation.COUNTERCLOCKWISE_90 -> {
                            offset1 = BPos(offset1.z, offset1.y, -offset1.x)
                            offset2 = BPos(offset2.z, offset2.y, -offset2.x)
                        }
                    }

                    val checkPos1 = centerPiece.pos.add(offset1)
                    val checkPos2 = centerPiece.pos.add(offset2)

                    if (terrainGenerator.getBlockAt(checkPos1).getOrNull()?.name == "lava") {
                        true
                    } else {
                        terrainGenerator.getBlockAt(checkPos2).getOrNull()?.name == "lava"
                    }
                } else false

                return BastionInfo(quadrantX, quadrantZ, chunkPos, type, ramparts, hasObbyChest, wetManhunt)
            }
        }

        return null
    }

    fun bastionInQuadrant(seed: Long, quadrantX: Int, quadrantZ: Int): CPos? {
        val structureSeed = seed and 0x0000FFFFFFFFFFFFL
        val biomeSource = NetherBiomeSource(VERSION, seed)
        val rand = ChunkRand()
        val pos = bastionRemnant.getInRegion(structureSeed, quadrantX, quadrantZ, rand) ?: return null
        val bastionAt = bastionRemnant.at(pos.x, pos.z)

        if (!bastionAt.testBiome(biomeSource)) return null
        return pos
    }

    fun getBastionType(seed: Long, pos: CPos): BastionType {
        val rand = ChunkRand()
        rand.setCarverSeed(seed, pos.x, pos.z, VERSION)
        return BastionType.getRandom(rand)
    }

    fun getHousingRamparts(bastion: BastionGenerator): HousingRamparts {
        if (bastion.pieceNameCounts.contains("units/ramparts/ramparts_1")) {
            return HousingRamparts(HousingRampart.SINGLE_CHEST)
        } else if (bastion.pieceNameCounts.contains("units/ramparts/ramparts_2")) {
            return HousingRamparts(HousingRampart.RUINS)
        }
        return HousingRamparts(HousingRampart.TRIPLE_CHEST)
    }

    fun getBridgeRamparts(bastion: BastionGenerator): BridgeRamparts {
        if (!bastion.pieceNameCounts.contains("bridge/ramparts/rampart_1")) {
            // Double single
            return BridgeRamparts(BridgeRampart.SINGLE_CHEST, BridgeRampart.SINGLE_CHEST)
        } else if (!bastion.pieceNameCounts.contains("bridge/ramparts/rampart_0")) {
            // Double triple
            return BridgeRamparts(BridgeRampart.TRIPLE_CHEST, BridgeRampart.TRIPLE_CHEST)
        }

        var rampart1: BridgeRampart? = null
        var rampart1Pos: BPos? = null
        var rampart2: BridgeRampart? = null
        var rampart2Pos: BPos? = null
        lateinit var rotation: BlockRotation

        for (piece in bastion.pieces) {
            if (piece.name == "bridge/ramparts/rampart_1") {
                // This is a triple
                if (rampart1 == null) {
                    rampart1 = BridgeRampart.TRIPLE_CHEST
                    rampart1Pos = piece.pos
                } else {
                    rampart2 = BridgeRampart.TRIPLE_CHEST
                    rampart2Pos = piece.pos
                }
                rotation = piece.rotation
            } else if (piece.name == "bridge/ramparts/rampart_0") {
                // This is a single
                if (rampart1 == null) {
                    rampart1 = BridgeRampart.SINGLE_CHEST
                    rampart1Pos = piece.pos
                } else {
                    rampart2 = BridgeRampart.SINGLE_CHEST
                    rampart2Pos = piece.pos
                }
                rotation = piece.rotation
            }
        }

        // Need to determine which rampart is left and which is right.
        return when (rotation) {
            BlockRotation.NONE -> {
                // Right rampart is whichever has greater Z
                if (rampart1Pos!!.z > rampart2Pos!!.z) {
                    BridgeRamparts(rampart2!!, rampart1!!)
                } else {
                    BridgeRamparts(rampart1!!, rampart2!!)
                }
            }
            BlockRotation.CLOCKWISE_180 -> {
                // Left rampart is whichever has greater Z
                if (rampart1Pos!!.z < rampart2Pos!!.z) {
                    BridgeRamparts(rampart2!!, rampart1!!)
                } else {
                    BridgeRamparts(rampart1!!, rampart2!!)
                }
            }
            BlockRotation.CLOCKWISE_90 -> {
                // Left rampart is whichever has greater X
                if (rampart1Pos!!.x < rampart2Pos!!.x) {
                    BridgeRamparts(rampart2!!, rampart1!!)
                } else {
                    BridgeRamparts(rampart1!!, rampart2!!)
                }
            }
            BlockRotation.COUNTERCLOCKWISE_90 -> {
                // Right rampart is whichever has greater X
                if (rampart1Pos!!.x > rampart2Pos!!.x) {
                    BridgeRamparts(rampart2!!, rampart1!!)
                } else {
                    BridgeRamparts(rampart1!!, rampart2!!)
                }
            }
        }
    }

    fun getStablesRamparts(bastion: BastionGenerator): StablesRamparts {
        lateinit var rotation: BlockRotation
        lateinit var middleRampart: StablesRampart

        var sideRampart1: StablesRampart? = null
        var sideRampart1Pos: BPos? = null
        var sideRampart2: StablesRampart? = null
        var sideRampart2Pos: BPos? = null

        var gap1: StablesGap? = null
        var gap1Pos: BPos? = null
        var gap2: StablesGap? = null
        var gap2Pos: BPos? = null

        for (piece in bastion.pieces) {
            if (piece.name.startsWith("hoglin_stable/ramparts/ramparts_")) {
                rotation = piece.rotation

                val rampart = when (piece.name) {
                    "hoglin_stable/ramparts/ramparts_1" -> StablesRampart.TRIPLE_CHEST
                    "hoglin_stable/ramparts/ramparts_2" -> StablesRampart.DOUBLE_CHEST
                    else -> StablesRampart.SINGLE_CHEST
                }

                if (piece.depth == 2) {
                    middleRampart = rampart
                } else {
                    if (sideRampart1 == null) {
                        sideRampart1 = rampart
                        sideRampart1Pos = piece.pos
                    } else {
                        sideRampart2 = rampart
                        sideRampart2Pos = piece.pos
                    }
                }
            } else if (piece.name.startsWith("hoglin_stable/walls/side_wall_")) {
                // It's a gap
                val gap = if (piece.name == "hoglin_stable/walls/side_wall_1") StablesGap.GOOD_GAP else StablesGap.BAD_GAP

                if (gap1 == null) {
                    gap1 = gap
                    gap1Pos = piece.pos
                } else {
                    gap2 = gap
                    gap2Pos = piece.pos
                }
            }
        }

        val (leftRampart, rightRampart) = when (rotation) {
            BlockRotation.NONE -> {
                // Left rampart is whichever has greater Z
                if (sideRampart1Pos!!.z < sideRampart2Pos!!.z) {
                    Pair(sideRampart2!!, sideRampart1!!)
                } else {
                    Pair(sideRampart1!!, sideRampart2!!)
                }
            }
            BlockRotation.CLOCKWISE_180 -> {
                // Right rampart is whichever has greater Z
                if (sideRampart1Pos!!.z > sideRampart2Pos!!.z) {
                    Pair(sideRampart2!!, sideRampart1!!)
                } else {
                    Pair(sideRampart1!!, sideRampart2!!)
                }
            }
            BlockRotation.CLOCKWISE_90 -> {
                // Right rampart is whichever has greater X
                if (sideRampart1Pos!!.x > sideRampart2Pos!!.x) {
                    Pair(sideRampart2!!, sideRampart1!!)
                } else {
                    Pair(sideRampart1!!, sideRampart2!!)
                }
            }
            BlockRotation.COUNTERCLOCKWISE_90 -> {
                // Left rampart is whichever has greater X
                if (sideRampart1Pos!!.x < sideRampart2Pos!!.x) {
                    Pair(sideRampart2!!, sideRampart1!!)
                } else {
                    Pair(sideRampart1!!, sideRampart2!!)
                }
            }
        }

        val (leftGap, rightGap) = when (rotation) {
            BlockRotation.NONE -> {
                // Right gap is whichever has greater X
                if (gap1Pos!!.x > gap2Pos!!.x) {
                    Pair(gap2!!, gap1!!)
                } else {
                    Pair(gap1!!, gap2!!)
                }
            }
            BlockRotation.CLOCKWISE_180 -> {
                // Left gap is whichever has greater X
                if (gap1Pos!!.x < gap2Pos!!.x) {
                    Pair(gap2!!, gap1!!)
                } else {
                    Pair(gap1!!, gap2!!)
                }
            }
            BlockRotation.CLOCKWISE_90 -> {
                // Left gap is whichever has greater Z
                if (gap1Pos!!.z < gap2Pos!!.z) {
                    Pair(gap2!!, gap1!!)
                } else {
                    Pair(gap1!!, gap2!!)
                }
            }
            BlockRotation.COUNTERCLOCKWISE_90 -> {
                // Right gap is whichever has greater Z
                if (gap1Pos!!.z > gap2Pos!!.z) {
                    Pair(gap2!!, gap1!!)
                } else {
                    Pair(gap1!!, gap2!!)
                }
            }
        }

        return StablesRamparts(leftRampart, middleRampart, rightRampart, leftGap, rightGap)
    }
}
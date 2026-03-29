package me.repeater64.mpkseedfilter.filtering.end;

import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class NoiseBasedChunkGenerator {

    public static <T> T utilMake(T var0, Consumer<T> var1) {
        var1.accept(var0);
        return var0;
    }
    private final int chunkHeight;
    private final int chunkWidth;
    private final int chunkCountX;
    private final int chunkCountY;
    private final int chunkCountZ;
    private final PerlinNoise minLimitPerlinNoise;
    private final PerlinNoise maxLimitPerlinNoise;
    private final PerlinNoise mainPerlinNoise;

    private final SimplexNoise islandNoise;

    public NoiseBasedChunkGenerator(long seed) {
        int height = 128;
        this.chunkHeight = 1 * 4;
        this.chunkWidth = 2 * 4;
        this.chunkCountX = 16 / this.chunkWidth;
        this.chunkCountY = height / this.chunkHeight;
        this.chunkCountZ = 16 / this.chunkWidth;
        WorldgenRandom random = new WorldgenRandom(seed);
        this.minLimitPerlinNoise = new PerlinNoise(random, IntStream.rangeClosed(-15, 0));
        this.maxLimitPerlinNoise = new PerlinNoise(random, IntStream.rangeClosed(-15, 0));
        this.mainPerlinNoise = new PerlinNoise(random, IntStream.rangeClosed(-7, 0));
        random.consumeCount(2620);

        WorldgenRandom var8 = new WorldgenRandom(seed);
        var8.consumeCount(17292);
        this.islandNoise = new SimplexNoise(var8);

    }

    private double sampleAndClampNoise(int var1, int var2, int var3, double var4, double var6, double var8, double var10) {
        double var12 = 0.0D;
        double var14 = 0.0D;
        double var16 = 0.0D;
        boolean var18 = true;
        double var19 = 1.0D;

        for(int var21 = 0; var21 < 16; ++var21) {
            double var22 = PerlinNoise.wrap((double)var1 * var4 * var19);
            double var24 = PerlinNoise.wrap((double)var2 * var6 * var19);
            double var26 = PerlinNoise.wrap((double)var3 * var4 * var19);
            double var28 = var6 * var19;
            ImprovedNoise var30 = this.minLimitPerlinNoise.getOctaveNoise(var21);
            if (var30 != null) {
                var12 += var30.noise(var22, var24, var26, var28, (double)var2 * var28) / var19;
            }

            ImprovedNoise var31 = this.maxLimitPerlinNoise.getOctaveNoise(var21);
            if (var31 != null) {
                var14 += var31.noise(var22, var24, var26, var28, (double)var2 * var28) / var19;
            }

            if (var21 < 8) {
                ImprovedNoise var32 = this.mainPerlinNoise.getOctaveNoise(var21);
                if (var32 != null) {
                    var16 += var32.noise(PerlinNoise.wrap((double)var1 * var8 * var19), PerlinNoise.wrap((double)var2 * var10 * var19), PerlinNoise.wrap((double)var3 * var8 * var19), var10 * var19, (double)var2 * var10 * var19) / var19;
                }
            }

            var19 /= 2.0D;
        }

        return Mth.clampedLerp(var12 / 512.0D, var14 / 512.0D, (var16 / 10.0D + 1.0D) / 2.0D);
    }

    private float theEndBiomeSourceGetHeightValue(SimplexNoise var0, int var1, int var2) {
        int var3 = var1 / 2;
        int var4 = var2 / 2;
        int var5 = var1 % 2;
        int var6 = var2 % 2;
        float var7 = 100.0F - Mth.sqrt((float)(var1 * var1 + var2 * var2)) * 8.0F;
        var7 = Mth.clamp(var7, -100.0F, 80.0F);

        for(int var8 = -12; var8 <= 12; ++var8) {
            for(int var9 = -12; var9 <= 12; ++var9) {
                long var10 = var3 + var8;
                long var12 = var4 + var9;
                if (var10 * var10 + var12 * var12 > 4096L && var0.getValue((double)var10, (double)var12) < -0.8999999761581421D) {
                    float var14 = (Mth.abs((float)var10) * 3439.0F + Mth.abs((float)var12) * 147.0F) % 13.0F + 9.0F;
                    float var15 = (float)(var5 - var8 * 2);
                    float var16 = (float)(var6 - var9 * 2);
                    float var17 = 100.0F - Mth.sqrt(var15 * var15 + var16 * var16) * var14;
                    var17 = Mth.clamp(var17, -100.0F, 80.0F);
                    var7 = Math.max(var7, var17);
                }
            }
        }

        return var7;
    }

    private void fillNoiseColumn(double[] var1, int var2, int var3) {
        double var4;
        double var6;
        double var52;
        double var53;


        var4 = theEndBiomeSourceGetHeightValue(this.islandNoise, var2, var3) - 8.0F;
        if (var4 > 0.0D) {
            var6 = 0.25D;
        } else {
            var6 = 1.0D;
        }

        double var46 = 684.412D * 2; // xzScale
        double var47 = 684.412D * 1; // yScale
        double var48 = var46 / 80; // xzFactor
        double var50 = var47 / 160; // yFactor
        var52 = -3000.0; // topSlideSettings().target();
        var53 = 64.0; // topSlideSettings().size();
        double var54 = -46.0; // topSlideSettings().offset()
        double var55 = -30.0; // bottomSlideSettings().target()
        double var25 = 7.0; // bottomSlideSettings().size()
        double var27 = 1.0; // bottomSlideSettings().offset()
        double var29 = 0.0D;
        double var31 = 0.0; // densityFactor
        double var33 = 0.0; // densityOffset

        for(int var35 = 0; var35 <= this.chunkCountY; ++var35) {
            double var36 = this.sampleAndClampNoise(var2, var35, var3, var46, var47, var48, var50);
            double var38 = 1.0D - (double)var35 * 2.0D / (double)this.chunkCountY + var29;
            double var40 = var38 * var31 + var33;
            double var42 = (var40 + var4) * var6;
            if (var42 > 0.0D) {
                var36 += var42 * 4.0D;
            } else {
                var36 += var42;
            }

            double var44;
            var44 = ((double) (this.chunkCountY - var35) - var54) / var53;
            var36 = Mth.clampedLerp(var52, var36, var44);

            var44 = ((double) var35 - var27) / var25;
            var36 = Mth.clampedLerp(var55, var36, var44);

            var1[var35] = var36;
        }

    }

    private boolean generateBaseState(double var1, int var3) { // Return true for endstone, false for air
        if (var1 > 0.0D) {
            return true;
        } else if (var3 < this.getSeaLevel()) {
            return false;
        } else {
            return false;
        }
    }

    public boolean[][][] doEndstoneGeneration(int chunkX, int chunkZ) { // Returns 16x256x16 array of true for endstone, false for air
        boolean[][][] finalArray = new boolean[16][256][16];

        int var9 = chunkX << 4;
        int var10 = chunkZ << 4;

        double[][][] var75 = new double[2][this.chunkCountZ + 1][this.chunkCountY + 1];

        for(int var76 = 0; var76 < this.chunkCountZ + 1; ++var76) {
            var75[0][var76] = new double[this.chunkCountY + 1];
            this.fillNoiseColumn(var75[0][var76], chunkX * this.chunkCountX, chunkZ * this.chunkCountZ + var76);
            var75[1][var76] = new double[this.chunkCountY + 1];
        }


        for(int var18 = 0; var18 < this.chunkCountX; ++var18) {
            int var19;
            for(var19 = 0; var19 < this.chunkCountZ + 1; ++var19) {
                this.fillNoiseColumn(var75[1][var19], chunkX * this.chunkCountX + var18 + 1, chunkZ * this.chunkCountZ + var19);
            }

            for(var19 = 0; var19 < this.chunkCountZ; ++var19) {
                int chunkSectionBottomBlockY = 15 << 4;

                for(int var21 = this.chunkCountY - 1; var21 >= 0; --var21) {
                    double var22 = var75[0][var19][var21];
                    double var24 = var75[0][var19 + 1][var21];
                    double var26 = var75[1][var19][var21];
                    double var28 = var75[1][var19 + 1][var21];
                    double var30 = var75[0][var19][var21 + 1];
                    double var32 = var75[0][var19 + 1][var21 + 1];
                    double var34 = var75[1][var19][var21 + 1];
                    double var36 = var75[1][var19 + 1][var21 + 1];

                    for(int var38 = this.chunkHeight - 1; var38 >= 0; --var38) {
                        int var39 = var21 * this.chunkHeight + var38;
                        int var40 = var39 & 15;
                        int var41 = var39 >> 4;
                        if (chunkSectionBottomBlockY >> 4 != var41) {
                            chunkSectionBottomBlockY = var41 << 4;
                        }

                        double var42 = (double)var38 / (double)this.chunkHeight;
                        double var44 = Mth.lerp(var42, var22, var30);
                        double var46 = Mth.lerp(var42, var26, var34);
                        double var48 = Mth.lerp(var42, var24, var32);
                        double var50 = Mth.lerp(var42, var28, var36);

                        for(int var52 = 0; var52 < this.chunkWidth; ++var52) {
                            int var53 = var9 + var18 * this.chunkWidth + var52;
                            int var54 = var53 & 15;
                            double var55 = (double)var52 / (double)this.chunkWidth;
                            double var57 = Mth.lerp(var55, var44, var46);
                            double var59 = Mth.lerp(var55, var48, var50);

                            for(int var61 = 0; var61 < this.chunkWidth; ++var61) {
                                int var62 = var10 + var19 * this.chunkWidth + var61;
                                int var63 = var62 & 15;
                                double var64 = (double)var61 / (double)this.chunkWidth;
                                double var66 = Mth.lerp(var64, var57, var59);
                                double var68 = Mth.clamp(var66 / 200.0D, -1.0D, 1.0D);

                                boolean baseBlockState = this.generateBaseState(var68, var39); // True for endstone, false for air
                                if (baseBlockState) {
                                    // ENDSTONE AT var54, var40, var63
                                    finalArray[var54][chunkSectionBottomBlockY + var40][var63] = true;
                                }
                            }
                        }
                    }
                }

            }

            double[][] var78 = var75[0];
            var75[0] = var75[1];
            var75[1] = var78;
        }

        return finalArray;
    }
    public int getSeaLevel() {
        return 0;
    }
}

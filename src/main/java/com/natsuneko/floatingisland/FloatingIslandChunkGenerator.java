package com.natsuneko.floatingisland;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.SwampHutStructure;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FloatingIslandChunkGenerator extends AbstractChunkGenerator<OverworldGenSettings> {
    private final IWorld _world;
    private final Random _rand;
    private final double[] _heightMap;
    private final float[] _biomeWeights;
    private final WorldType _terrainType;
    private final OverworldGenSettings _settings;
    private final PhantomSpawner _phantomSpawner = new PhantomSpawner();
    private NoiseGeneratorOctaves _minLimitPerlinNoise;
    private NoiseGeneratorOctaves _maxLimitPerlinNoise;
    private NoiseGeneratorOctaves _mainPerlinNoise;
    private NoiseGeneratorPerlin _surfacePerlinNoise;
    private NoiseGeneratorOctaves _scaleNoise;
    private NoiseGeneratorOctaves _depthNoise;
    private double[] _mainNoiseRegion;
    private double[] _minLimitRegion;
    private double[] _maxLimitRegion;
    private double[] _depthRegion;

    public FloatingIslandChunkGenerator(IWorld world, BiomeProvider provider, OverworldGenSettings settings) {
        super(world, provider);

        this._world = world;
        this._terrainType = world.getWorldInfo().getTerrainType();
        this._rand = new Random(seed);
        this._minLimitPerlinNoise = new NoiseGeneratorOctaves(this._rand, 16);
        this._maxLimitPerlinNoise = new NoiseGeneratorOctaves(this._rand, 16);
        this._mainPerlinNoise = new NoiseGeneratorOctaves(this._rand, 8);
        this._surfacePerlinNoise = new NoiseGeneratorPerlin(this._rand, 4);
        this._scaleNoise = new NoiseGeneratorOctaves(this._rand, 10);
        this._depthNoise = new NoiseGeneratorOctaves(this._rand, 16);
        this._heightMap = new double[825];
        this._biomeWeights = new float[25];
        this._settings = settings;

        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                float f = 10.0F / MathHelper.sqrt((float) (i * i + j * j) + 0.2F);
                this._biomeWeights[i + 2 + (j + 2) * 5] = f;
            }
        }

        InitNoiseGensEvent.ContextOverworld ctx = new InitNoiseGensEvent.ContextOverworld(
                this._minLimitPerlinNoise,
                this._maxLimitPerlinNoise,
                this._mainPerlinNoise,
                this._surfacePerlinNoise,
                this._scaleNoise,
                this._depthNoise
        );

        this._minLimitPerlinNoise = ctx.getLPerlin1();
        this._maxLimitPerlinNoise = ctx.getLPerlin2();
        this._mainPerlinNoise = ctx.getPerlin();
        this._surfacePerlinNoise = ctx.getHeight();
        this._scaleNoise = ctx.getScale();
        this._depthNoise = ctx.getDepth();
    }

    public int getSqrt(int par1, int par2, int par3, int par4) {
        return (int) Math.sqrt((par3 - par1) * (par3 - par1) + (par4 - par2) * (par4 - par2));
    }

    private void setBlocksInChunk(int x, int z, IChunk primer) {
        Biome[] biomes = this.biomeProvider.getBiomes(primer.getPos().x * 4 - 2, primer.getPos().z * 4 - 2, 10, 10);
        double[] unknown_p2024072801 = new double[825];
        this.generateHeightmap(biomes, primer.getPos().x * 4, 0, primer.getPos().z * 4, unknown_p2024072801);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 4; i++) {
            int j = i * 5;
            int k = (i + 1) * 5;

            for (int l = 0; l < 4; l++) {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;

                for (int i2 = 0; i2 < 32; i2++) {
                    double d1 = this._heightMap[i1 + i2];
                    double d2 = this._heightMap[j1 + i2];
                    double d3 = this._heightMap[k1 + i2];
                    double d4 = this._heightMap[l1 + i2];
                    double d5 = (this._heightMap[i1 + i2 + 1] - d1) * 0.125d;
                    double d6 = (this._heightMap[j1 + i2 + 1] - d2) * 0.125d;
                    double d7 = (this._heightMap[k1 + i2 + 1] - d3) * 0.125d;
                    double d8 = (this._heightMap[l1 + i2 + 1] - d4) * 0.125d;

                    for (int j2 = 0; j2 < 8; j2++) {
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25d;
                        double d13 = (d4 - d2) * 0.25d;

                        for (int k2 = 0; k2 < 4; k2++) {
                            // TODO: MAYBE CUSTOMIZED CODE FOUND : a(int paramInt1, int paramInt2, ahz[] paramArrayOfAhz)#142
                            double d16 = (d11 - d10) * 0.25d;
                            double lvt_45_1_ = d10 - d16;

                            for (int l2 = 0; l2 < 4; l2++) {
                                int xInChunk = i * 4 + k2;
                                int yInChunk = i2 * 8 + j2;
                                int zInChunk = l * 4 + l2;

                                pos.setPos(xInChunk, yInChunk, zInChunk);
                                if ((lvt_45_1_ += d16) > 0.0D) {
                                    primer.setBlockState(pos, Blocks.STONE.getDefaultState(), false);
                                } else if (i2 * 8 + j2 < this._settings.getSeaLevel()) {
                                    primer.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    public void replaceIslandBlocks(int chunkX, int chunkZ, IChunk primer, Biome[] biomes) {

        int floorHeight = 34 + _rand.nextInt(256 - 34 - 2);
        int height = floorHeight + _rand.nextInt(Math.max(256 - floorHeight, 16));
        int roundFactor = _rand.nextInt(10);
        int[] offset = new int[height];

        for (int i = 0; i < offset.length; i++) {
            offset[i] = roundFactor + _rand.nextInt(2) - this._rand.nextInt(4);

        }

        boolean erase = _rand.nextInt(100) <= 5;


        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                for (int y = 0; y < 256; y++) {
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
                    Block block = primer.getBlockState(pos).getBlock();

                    if (y <= 32) {
                        primer.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
                        continue;
                    }

                    if (y < floorHeight || height < y || erase) {
                        primer.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
                        continue;
                    }

                    if (floorHeight <= y && y <= floorHeight + height) {
                        int i = Math.min(y - floorHeight, height);
                        int factor = offset[i];
                        boolean c = getSqrt(x, z, 8, 8) >= factor;
                        if (c) {
                            primer.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
                            continue;
                        }
                    }

                    // replace ocean biome
                    if (block == Blocks.WATER) {
                        primer.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
                        continue;
                    }
                }
            }
        }
    }

    @Override
    public void makeBase(IChunk chunkIn) {
        ChunkPos chunkPos = chunkIn.getPos();
        int x = chunkPos.x;
        int z = chunkPos.z;
        SharedSeedRandom random = new SharedSeedRandom();
        random.setBaseChunkSeed(x, z);

        Biome[] biomes = this.biomeProvider.getBiomeBlock(x * 16, z * 16, 16, 16);
        chunkIn.setBiomes(biomes);

        this.setBlocksInChunk(x, z, chunkIn);
        chunkIn.createHeightMap(Heightmap.Type.WORLD_SURFACE_WG, Heightmap.Type.OCEAN_FLOOR_WG);

        // this._biomesForGeneration = this.biomeProvider.getBiomes(x * 16, z * 16, 16, 16);
        this.replaceIslandBlocks(x, z, chunkIn, biomes);
        this.buildSurface(chunkIn, biomes, random, this.world.getSeaLevel());
        chunkIn.createHeightMap(Heightmap.Type.WORLD_SURFACE_WG, Heightmap.Type.OCEAN_FLOOR_WG);
        chunkIn.setStatus(ChunkStatus.BASE);
    }

    @Override
    public void spawnMobs(WorldGenRegion region) {
        int i = region.getMainChunkX();
        int j = region.getMainChunkZ();
        Biome biome = region.getChunk(i, j).getBiomes()[0];
        SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
        sharedseedrandom.setDecorationSeed(region.getSeed(), i << 4, j << 4);
        WorldEntitySpawner.performWorldGenSpawning(region, biome, i, j, sharedseedrandom);
    }

    private void generateHeightmap(Biome[] biomes, int x, int y, int z, double[] unknown_202108_5_) {
        this._depthRegion = this._depthNoise.func_202646_a(x, z, 5, 5, this._settings.getDepthNoiseScaleX(), this._settings.getDepthNoiseScaleZ(), this._settings.getDepthNoiseScaleExponent());
        float coordinateScale = this._settings.getCoordinateScale();
        float heightScale = this._settings.getHeightScale();
        this._mainNoiseRegion = this._mainPerlinNoise.func_202647_a(x, y, z, 5, 33, 5, (coordinateScale / this._settings.getMainNoiseScaleX()), heightScale / this._settings.getMainNoiseScaleY(), (coordinateScale / this._settings.getMainNoiseScaleZ()));
        this._minLimitRegion = this._minLimitPerlinNoise.func_202647_a(x, y, z, 5, 33, 5, coordinateScale, heightScale, coordinateScale);
        this._maxLimitRegion = this._maxLimitPerlinNoise.func_202647_a(x, y, z, 5, 33, 5, coordinateScale, heightScale, coordinateScale);
        int i = 0;
        int j = 0;

        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                float f2 = 0.0F;
                float f3 = 0.0F;
                float f4 = 0.0F;
                int i1 = 2;
                Biome biome =biomes[k + 2 + (l + 2) * 10];

                for (int j1 = -2; j1 <= 2; ++j1) {
                    for (int k1 = -2; k1 <= 2; ++k1) {
                        Biome biome1 = biomes[k + j1 + 2 + (l + k1 + 2) * 10];
                        float f5 = this._settings.func_202203_v() + biome1.getDepth() * this._settings.func_202202_w();
                        float f6 = this._settings.func_202204_x() + biome1.getScale() * this._settings.func_202205_y();

                        if (this._terrainType == WorldType.AMPLIFIED && f5 > 0.0F) {
                            f5 = 1.0F + f5 * 2.0F;
                            f6 = 1.0F + f6 * 4.0F;
                        }

                        float f7 = this._biomeWeights[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0F);

                        if (biome1.getDepth() > biome.getDepth()) {
                            f7 /= 2.0F;
                        }

                        f2 += f6 * f7;
                        f3 += f5 * f7;
                        f4 += f7;
                    }
                }

                f2 = f2 / f4;
                f3 = f3 / f4;
                f2 = f2 * 0.9F + 0.1F;
                f3 = (f3 * 4.0F - 1.0F) / 8.0F;
                double d7 = this._depthRegion[j] / 8000.0D;

                if (d7 < 0.0D) {
                    d7 = -d7 * 0.3D;
                }

                d7 = d7 * 3.0D - 2.0D;

                if (d7 < 0.0D) {
                    d7 = d7 / 2.0D;

                    if (d7 < -1.0D) {
                        d7 = -1.0D;
                    }

                    d7 = d7 / 1.4D;
                    d7 = d7 / 2.0D;
                } else {
                    if (d7 > 1.0D) {
                        d7 = 1.0D;
                    }

                    d7 = d7 / 8.0D;
                }

                ++j;
                double d8 = f3;
                double d9 = f2;
                d8 = d8 + d7 * 0.2D;
                d8 = d8 * (double) this._settings.func_202201_z() / 8.0D;
                double d0 = (double) this._settings.func_202201_z() + d8 * 4.0D;

                for (int l1 = 0; l1 < 33; ++l1) {
                    double d1 = ((double) l1 - d0) * this._settings.func_202206_A() * 128.0D / 256.0D / d9;

                    if (d1 < 0.0D) {
                        d1 *= 4.0D;
                    }

                    double d2 = this._minLimitRegion[i] / (double) this._settings.getLowerLimitScale();
                    double d3 = this._maxLimitRegion[i] / (double) this._settings.getUpperLimitScale();
                    double d4 = (this._mainNoiseRegion[i] / 10.0D + 1.0D) / 2.0D;
                    double d5 = MathHelper.clampedLerp(d2, d3, d4) - d1;

                    if (l1 > 29) {
                        double d6 = (float) (l1 - 29) / 3.0F;
                        d5 = d5 * (1.0D - d6) + -10.0D * d6;
                    }

                    this._heightMap[i] = d5;
                    ++i;
                }
            }
        }
    }

    /*
    @Override
    public void populate(int x, int z) {
        BlockFalling.fallInstantly = true;

        int i = x * 16;
        int j = z * 16;
        BlockPos blockPos = new BlockPos(i, 0, j);
        Biome biome = this._world.getBiome(blockPos.add(16, 0, 16));

        this._rand.setSeed(this._world.getSeed());

        long k = this._rand.nextLong() / 2L * 2L + 1L;
        long l = this._rand.nextLong() / 2L * 2L + 1L;

        this._rand.setSeed((long) x * k + (long) z * l ^ this._world.getSeed());

        boolean hasVillageGenerated = false;
        ChunkPos chunkPos = new ChunkPos(x, z);
        ForgeEventFactory.onChunkPopulate(true, this, this._world, this._rand, x, z, hasVillageGenerated);

        if (this._settings.useMineShafts) {
            this._mineshaftGenerator.generateStructure(this._world, this._rand, chunkPos);
        }
        if (this._settings.useVillages) {
            hasVillageGenerated = this._villageGenerator.generateStructure(this._world, this._rand, chunkPos);
        }
        if (this._settings.useStrongholds) {
            this._strongholdGenerator.generateStructure(this._world, this._rand, chunkPos);
        }
        if (this._settings.useTemples) {
            this._scatteredFeatureGenerator.generateStructure(this._world, this._rand, chunkPos);
        }
        if (this._settings.useMonuments) {
            this._oceanMonumentGenerator.generateStructure(this._world, this._rand, chunkPos);
        }
        if (this._settings.useMansions) {
            //
        }

        if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS) {
            if (this._settings.useWaterLakes && !hasVillageGenerated && this._rand.nextInt(this._settings.waterLakeChance) == 0) {
                if (TerrainGen.populate(this, this._world, this._rand, x, z, hasVillageGenerated, PopulateChunkEvent.Populate.EventType.LAKE)) {
                    int xInChunk = this._rand.nextInt(16) + 8;
                    int yInChunk = this._rand.nextInt(256);
                    int zInChunk = this._rand.nextInt(16) + 8;

                    (new WorldGenLakes(Blocks.WATER)).generate(this._world, this._rand, blockPos.add(xInChunk, yInChunk, zInChunk));
                }
            }
        }

        if (this._settings.useLavaLakes && !hasVillageGenerated && this._rand.nextInt(this._settings.lavaLakeChance / 10) == 0) {
            if (TerrainGen.populate(this, this._world, this._rand, x, z, hasVillageGenerated, PopulateChunkEvent.Populate.EventType.LAKE)) {
                int xInChunk = this._rand.nextInt(16) + 8;
                int yInChunk = this._rand.nextInt(this._rand.nextInt(248) + 8);
                int zInChunk = this._rand.nextInt(16) + 8;

                if (yInChunk < this._world.getSeaLevel() || this._rand.nextInt(this._settings.lavaLakeChance / 8) == 0) {
                    (new WorldGenLakes(Blocks.LAVA)).generate(this._world, this._rand, blockPos.add(xInChunk, yInChunk, zInChunk));
                }
            }
        }

        if (this._settings.useDungeons) {
            if (TerrainGen.populate(this, this._world, this._rand, x, z, hasVillageGenerated, PopulateChunkEvent.Populate.EventType.DUNGEON)) {
                for (int o = 0; o < this._settings.dungeonChance; o++) {
                    int xInChunk = this._rand.nextInt(16) + 8;
                    int yInChunk = this._rand.nextInt(256);
                    int zInChunk = this._rand.nextInt(16) + 8;

                    (new WorldGenDungeons()).generate(this._world, this._rand, blockPos.add(xInChunk, yInChunk, zInChunk));

                }
            }
        }

        biome.decorate(this._world, this._rand, new BlockPos(i, 0, j));

        if (TerrainGen.populate(this, this._world, this._rand, x, z, hasVillageGenerated, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
            WorldEntitySpawner.performWorldGenSpawning(this._world, biome, i + 8, j + 8, 16, 16, this._rand);
        }

        blockPos = blockPos.add(8, 0, 8);

        if (TerrainGen.populate(this, this._world, this._rand, x, z, hasVillageGenerated, PopulateChunkEvent.Populate.EventType.ICE)) {
            for (int k2 = 0; k2 < 16; k2++) {
                for (int j3 = 0; j3 < 16; j3++) {
                    BlockPos a = this._world.getPrecipitationHeight(blockPos.add(k2, 0, j3));
                    BlockPos b = a.down();

                    if (this._world.canBlockFreezeWater(b)) {
                        this._world.setBlockState(b, Blocks.ICE.getDefaultState(), 2);
                    }

                    if (this._world.canSnowAt(b, true)) {
                        this._world.setBlockState(b, Blocks.SNOW_LAYER.getDefaultState(), 2);
                    }
                }
            }
        }

        ForgeEventFactory.onChunkPopulate(false, this, this._world, this._rand, x, z, hasVillageGenerated);
        BlockFalling.fallInstantly = false;
    }
    */


    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        Biome biome = this.world.getBiome(pos);
        if (creatureType == EnumCreatureType.MONSTER && ((SwampHutStructure) Feature.SWAMP_HUT).func_202383_b(this.world, pos)) {
            return Feature.SWAMP_HUT.getSpawnList();
        } else {
            return creatureType == EnumCreatureType.MONSTER && Feature.OCEAN_MONUMENT.isPositionInStructure(this.world, pos) ? Feature.OCEAN_MONUMENT.getSpawnList() : biome.getSpawns(creatureType);
        }
    }

    public OverworldGenSettings getSettings() {
        return this._settings;
    }

    public int spawnMobs(World worldIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs) {
        return this._phantomSpawner.spawnMobs(worldIn, spawnHostileMobs, spawnPeacefulMobs);
    }

    public int getGroundHeight() {
        return this._world.getSeaLevel() + 1;
    }

    public double[] generateNoiseRegion(int x, int z) {
        double d0 = 0.03125D;
        return this._surfacePerlinNoise.generateRegion(x << 4, z << 4, 16, 16, 0.0625D, 0.0625D, 1.0D);

    }
}

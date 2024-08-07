package com.natsuneko.floatingisland;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class FloatingIslandChunkGenerator implements IChunkGenerator {
    private final World _world;
    private final Random _rand;
    private final double[] _heightMap;
    private final float[] _biomeWeights;
    private final WorldType _terrainType;

    private NoiseGeneratorOctaves _minLimitPerlinNoise;
    private NoiseGeneratorOctaves _maxLimitPerlinNoise;
    private NoiseGeneratorOctaves _mainPerlinNoise;
    private NoiseGeneratorPerlin _surfacePerlinNoise;
    private NoiseGeneratorOctaves _scaleNoise;
    private NoiseGeneratorOctaves _depthNoise;
    private NoiseGeneratorOctaves _forestNoise;
    private MapGenStronghold _strongholdGenerator = new MapGenStronghold();
    private MapGenVillage _villageGenerator = new MapGenVillage();
    private MapGenMineshaft _mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature _scatteredFeatureGenerator = new MapGenScatteredFeature();
    private MapGenRavine _ravineGenerator = new MapGenRavine();
    private StructureOceanMonument _oceanMonumentGenerator = new StructureOceanMonument();
    private ChunkGeneratorSettings _settings;
    private Biome[] _biomesForGeneration;
    private double[] _mainNoiseRegion;
    private double[] _minLimitRegion;
    private double[] _maxLimitRegion;
    private double[] _depthRegion;
    private double[] _depthBuffer = new double[256];

    public FloatingIslandChunkGenerator(World world, long seed, String generatorOptions) {
        this._world = world;
        this._terrainType = world.getWorldInfo().getTerrainType();
        this._rand = new Random(seed);
        this._minLimitPerlinNoise = new NoiseGeneratorOctaves(this._rand, 16);
        this._maxLimitPerlinNoise = new NoiseGeneratorOctaves(this._rand, 16);
        this._mainPerlinNoise = new NoiseGeneratorOctaves(this._rand, 8);
        this._surfacePerlinNoise = new NoiseGeneratorPerlin(this._rand, 4);
        this._scaleNoise = new NoiseGeneratorOctaves(this._rand, 10);
        this._depthNoise = new NoiseGeneratorOctaves(this._rand, 16);
        this._forestNoise = new NoiseGeneratorOctaves(this._rand, 8);
        this._heightMap = new double[825];
        this._biomeWeights = new float[25];
        this._strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(this._strongholdGenerator, InitMapGenEvent.EventType.STRONGHOLD);
        this._villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(this._villageGenerator, InitMapGenEvent.EventType.VILLAGE);
        this._mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(this._mineshaftGenerator, InitMapGenEvent.EventType.MINESHAFT);
        this._scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(this._scatteredFeatureGenerator, InitMapGenEvent.EventType.SCATTERED_FEATURE);
        this._ravineGenerator = (MapGenRavine) TerrainGen.getModdedMapGen(this._ravineGenerator, InitMapGenEvent.EventType.RAVINE);
        this._oceanMonumentGenerator = (StructureOceanMonument) TerrainGen.getModdedMapGen(this._oceanMonumentGenerator, InitMapGenEvent.EventType.OCEAN_MONUMENT);

        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                float f = 10.0F / MathHelper.sqrt((float) (i * i + j * j) + 0.2F);
                this._biomeWeights[i + 2 + (j + 2) * 5] = f;
            }
        }

        if (generatorOptions != null) {
            this._settings = ChunkGeneratorSettings.Factory.jsonToFactory(generatorOptions).build();
            world.setSeaLevel(this._settings.seaLevel);
        }

        InitNoiseGensEvent.ContextOverworld ctx = new InitNoiseGensEvent.ContextOverworld(
                this._minLimitPerlinNoise,
                this._maxLimitPerlinNoise,
                this._mainPerlinNoise,
                this._surfacePerlinNoise,
                this._scaleNoise,
                this._depthNoise,
                this._forestNoise
        );

        this._minLimitPerlinNoise = ctx.getLPerlin1();
        this._maxLimitPerlinNoise = ctx.getLPerlin2();
        this._mainPerlinNoise = ctx.getPerlin();
        this._surfacePerlinNoise = ctx.getHeight();
        this._scaleNoise = ctx.getScale();
        this._depthNoise = ctx.getDepth();
        this._forestNoise = ctx.getForest();
    }

    public int getSqrt(int par1, int par2, int par3, int par4) {
        return (int) Math.sqrt((double) ((par3 - par1) * (par3 - par1) + (par4 - par2) * (par4 - par2)));
    }

    private void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
        this._biomesForGeneration = this._world.getBiomeProvider().getBiomesForGeneration(this._biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        this.generateHeightmap(x * 4, 0, z * 4);

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

                                if ((lvt_45_1_ += d16) > 0.0D) {
                                    primer.setBlockState(xInChunk, yInChunk, zInChunk, Blocks.STONE.getDefaultState());
                                } else if (i2 * 8 + j2 < this._settings.seaLevel) {
                                    primer.setBlockState(xInChunk, yInChunk, zInChunk, Blocks.WATER.getDefaultState());
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

    public void replaceIslandBlocks(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
        boolean isSpawnArea = _world.isSpawnChunk(chunkX, chunkZ);
        int floorHeight = 34 + _rand.nextInt(256 - 34 - 2);
        int height = floorHeight + _rand.nextInt(Math.max(256 - floorHeight, 16));
        int roundFactor = _rand.nextInt(10);
        int[] offset = new int[height];

        for (int i = 0; i < offset.length; i++) {
            offset[i] = roundFactor + _rand.nextInt(2) - this._rand.nextInt(4);

            if (isSpawnArea) {
                offset[i] += 2;
            }
        }

        boolean erase = _rand.nextInt(100) <= 5;

        if (isSpawnArea) {
            erase = false;
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                for (int y = 0; y < 256; y++) {
                    Block block = primer.getBlockState(x, y, z).getBlock();

                    if (y <= 32) {
                        primer.setBlockState(x, y, z, Blocks.WATER.getDefaultState());
                        continue;
                    }

                    if (y < floorHeight || height < y || erase) {
                        primer.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
                        continue;
                    }

                    if (floorHeight <= y && y <= floorHeight + height) {
                        int i = Math.min(y - floorHeight, height);
                        int factor = offset[i];
                        boolean c = getSqrt(x, z, 8, 8) >= factor;
                        if (c) {
                            primer.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
                            continue;
                        }
                    }

                    // replace ocean biome
                    if (block == Blocks.WATER) {
                        primer.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
                        continue;
                    }
                }
            }
        }
    }

    private void replaceBiomeBlocks(int x, int z, ChunkPrimer primer, Biome[] biomes) {
        if (!net.minecraftforge.event.ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, this._world)) return;
        this._depthBuffer = this._surfacePerlinNoise.getRegion(this._depthBuffer, (double) (x * 16), (double) (z * 16), 16, 16, 0.0625D, 0.0625D, 1.0D);

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                Biome biome = biomes[j + i * 16];
                biome.genTerrainBlocks(this._world, this._rand, primer, x * 16 + i, z * 16 + j, this._depthBuffer[j + i * 16]);
            }
        }
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        this._rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);

        ChunkPrimer primer = new ChunkPrimer();

        this.setBlocksInChunk(x, z, primer);
        this._biomesForGeneration = this._world.getBiomeProvider().getBiomes(this._biomesForGeneration, x * 16, z * 16, 16, 16);
        this.replaceIslandBlocks(x, z, primer, this._biomesForGeneration);
        this.replaceBiomeBlocks(x, z, primer, this._biomesForGeneration);

        if (this._settings.useCaves) {
            //
        }

        if (this._settings.useRavines) {
            this._ravineGenerator.generate(this._world, x, z, primer);
        }

        if (this._settings.useMineShafts) {
            this._mineshaftGenerator.generate(this._world, x, z, primer);
        }

        if (this._settings.useVillages) {
            this._villageGenerator.generate(this._world, x, z, primer);
        }

        if (this._settings.useStrongholds) {
            this._strongholdGenerator.generate(this._world, x, z, primer);
        }

        if (this._settings.useMonuments) {
            this._oceanMonumentGenerator.generate(this._world, x, z, primer);
        }

        if (this._settings.useMansions) {
            //
        }

        Chunk chunk = new Chunk(this._world, primer, x, z);
        byte[] biomes = chunk.getBiomeArray();

        for (int i = 0; i < biomes.length; i++) {
            biomes[i] = (byte) Biome.getIdForBiome(this._biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    private void generateHeightmap(int p_185978_1_, int p_185978_2_, int p_185978_3_) {
        this._depthRegion = this._depthNoise.generateNoiseOctaves(this._depthRegion, p_185978_1_, p_185978_3_, 5, 5, (double) this._settings.depthNoiseScaleX, (double) this._settings.depthNoiseScaleZ, (double) this._settings.depthNoiseScaleExponent);
        float f = this._settings.coordinateScale;
        float f1 = this._settings.heightScale;
        this._mainNoiseRegion = this._mainPerlinNoise.generateNoiseOctaves(this._mainNoiseRegion, p_185978_1_, p_185978_2_, p_185978_3_, 5, 33, 5, (double) (f / this._settings.mainNoiseScaleX), (double) (f1 / this._settings.mainNoiseScaleY), (double) (f / this._settings.mainNoiseScaleZ));
        this._minLimitRegion = this._minLimitPerlinNoise.generateNoiseOctaves(this._minLimitRegion, p_185978_1_, p_185978_2_, p_185978_3_, 5, 33, 5, (double) f, (double) f1, (double) f);
        this._maxLimitRegion = this._maxLimitPerlinNoise.generateNoiseOctaves(this._maxLimitRegion, p_185978_1_, p_185978_2_, p_185978_3_, 5, 33, 5, (double) f, (double) f1, (double) f);
        int i = 0;
        int j = 0;

        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                float f2 = 0.0F;
                float f3 = 0.0F;
                float f4 = 0.0F;
                int i1 = 2;
                Biome biome = this._biomesForGeneration[k + 2 + (l + 2) * 10];

                for (int j1 = -2; j1 <= 2; ++j1) {
                    for (int k1 = -2; k1 <= 2; ++k1) {
                        Biome biome1 = this._biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
                        float f5 = this._settings.biomeDepthOffSet + biome1.getBaseHeight() * this._settings.biomeDepthWeight;
                        float f6 = this._settings.biomeScaleOffset + biome1.getHeightVariation() * this._settings.biomeScaleWeight;

                        if (this._terrainType == WorldType.AMPLIFIED && f5 > 0.0F) {
                            f5 = 1.0F + f5 * 2.0F;
                            f6 = 1.0F + f6 * 4.0F;
                        }

                        float f7 = this._biomeWeights[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0F);

                        if (biome1.getBaseHeight() > biome.getBaseHeight()) {
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
                double d8 = (double) f3;
                double d9 = (double) f2;
                d8 = d8 + d7 * 0.2D;
                d8 = d8 * (double) this._settings.baseSize / 8.0D;
                double d0 = (double) this._settings.baseSize + d8 * 4.0D;

                for (int l1 = 0; l1 < 33; ++l1) {
                    double d1 = ((double) l1 - d0) * (double) this._settings.stretchY * 128.0D / 256.0D / d9;

                    if (d1 < 0.0D) {
                        d1 *= 4.0D;
                    }

                    double d2 = this._minLimitRegion[i] / (double) this._settings.lowerLimitScale;
                    double d3 = this._maxLimitRegion[i] / (double) this._settings.upperLimitScale;
                    double d4 = (this._mainNoiseRegion[i] / 10.0D + 1.0D) / 2.0D;
                    double d5 = MathHelper.clampedLerp(d2, d3, d4) - d1;

                    if (l1 > 29) {
                        double d6 = (double) ((float) (l1 - 29) / 3.0F);
                        d5 = d5 * (1.0D - d6) + -10.0D * d6;
                    }

                    this._heightMap[i] = d5;
                    ++i;
                }
            }
        }
    }

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

        // Adjust Difficulty - RedStone and Diamond
        if (TerrainGen.populate(this, this._world, this._rand, x, z, hasVillageGenerated, PopulateChunkEvent.Populate.EventType.CUSTOM)) {
            FloatingIslandBiomeDecorator decorator = new FloatingIslandBiomeDecorator();
            decorator.generateAdditionalOres(this._world, this._rand, biome, new BlockPos(i, 0, j));
        }

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

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        boolean hasStructuredGenerated = false;

        if (chunkIn.getInhabitedTime() < 3600L) {
            hasStructuredGenerated = this._oceanMonumentGenerator.generateStructure(this._world, this._rand, new ChunkPos(x, z));
        }

        return hasStructuredGenerated;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        Biome biome = this._world.getBiome(pos);

        if (creatureType == EnumCreatureType.MONSTER) {
            if (this._scatteredFeatureGenerator.isSwampHut(pos)) {
                return this._scatteredFeatureGenerator.getMonsters();
            }

            if (this._oceanMonumentGenerator.isPositionInStructure(this._world, pos)) {
                return this._oceanMonumentGenerator.getMonsters();
            }
        }

        return biome.getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
        if (structureName.equals("Stronghold")) {
            return this._strongholdGenerator.getNearestStructurePos(worldIn, position, p_180513_4_);
        }

        if (structureName.equals("Mansion")) {
            return null;
        }

        if (structureName.equals("Monument")) {
            return this._oceanMonumentGenerator.getNearestStructurePos(worldIn, position, p_180513_4_);
        }

        if (structureName.equals("Village")) {
            return this._villageGenerator.getNearestStructurePos(worldIn, position, p_180513_4_);
        }

        if (structureName.equals("Mineshaft")) {
            return this._mineshaftGenerator.getNearestStructurePos(worldIn, position, p_180513_4_);
        }

        if (structureName.equals("Temple")) {
            return this._scatteredFeatureGenerator.getNearestStructurePos(worldIn, position, p_180513_4_);
        }

        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        if (this._settings.useMineShafts) {
            this._mineshaftGenerator.generate(this._world, x, z, null);
        }

        if (this._settings.useVillages) {
            this._villageGenerator.generate(this._world, x, z, null);
        }

        if (this._settings.useStrongholds) {
            this._strongholdGenerator.generate(this._world, x, z, null);
        }

        if (this._settings.useTemples) {
            this._scatteredFeatureGenerator.generate(this._world, x, z, null);
        }

        if (this._settings.useMonuments) {
            this._oceanMonumentGenerator.generate(this._world, x, z, null);
        }

        /*
        if (this._settings.useMansions)
        {
            this.woodlandMansionGenerator.generate(this._world, x, z, (ChunkPrimer)null);
        }
        */
    }

    @Override
    public boolean isInsideStructure(World world, String s, BlockPos blockPos) {
        return false;
    }

    static class FloatingIslandBiomeDecorator extends BiomeDecorator {
        public void generateAdditionalOres(World worldIn, Random random, Biome biome, BlockPos pos) {
            if (this.decorating) {
                throw new RuntimeException("Already decorating");
            } else {
                this.decorating = true;
                this.chunkPos = pos;

                this.chunkProviderSettings = ChunkGeneratorSettings.Factory.jsonToFactory(worldIn.getWorldInfo().getGeneratorOptions()).build();
                this.redstoneGen = new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState(), this.chunkProviderSettings.redstoneSize);
                this.diamondGen = new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState(), this.chunkProviderSettings.diamondSize);

                if (net.minecraftforge.event.terraingen.TerrainGen.generateOre(worldIn, random, redstoneGen, chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.REDSTONE))
                    this.genStandardOre1(worldIn, random, this.chunkProviderSettings.redstoneCount, this.redstoneGen, 0, 42);
                if (net.minecraftforge.event.terraingen.TerrainGen.generateOre(worldIn, random, diamondGen, chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.DIAMOND))
                    this.genStandardOre1(worldIn, random, this.chunkProviderSettings.diamondCount, this.diamondGen, 0, 42);

                this.decorating = false;
            }
        }
    }
}

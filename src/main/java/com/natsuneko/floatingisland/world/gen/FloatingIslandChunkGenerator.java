package com.natsuneko.floatingisland.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;

public final class FloatingIslandChunkGenerator extends NoiseChunkGenerator {
    public static final MapCodec<FloatingIslandChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                    ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter(NoiseChunkGenerator::getSettings)
            ).apply(instance, instance.stable(FloatingIslandChunkGenerator::new))
    );

    public FloatingIslandChunkGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings ) {
        super(biomeSource, settings);

    }

    public int getSqrt(int par1, int par2, int par3, int par4) {
        return (int) Math.sqrt((par3 - par1) * (par3 - par1) + (par4 - par2) * (par4 - par2));
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public void buildSurface(Chunk chunk, HeightContext heightContext, NoiseConfig noiseConfig, StructureAccessor structureAccessor, BiomeAccess biomeAccess, Registry<Biome> biomeRegistry, Blender blender) {
        this.buildIslands(noiseConfig, biomeAccess, structureAccessor, chunk);
        super.buildSurface(chunk, heightContext, noiseConfig, structureAccessor, biomeAccess, biomeRegistry, blender);
    }

    // NOTE: Floating Island DOES NOT have any carves. But Floating Island are generated as CAVES!!!!!!
    // @Override
    public void buildIslands(NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk) {
        ChunkRandom random = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
        int floorHeight = this.getSeaLevel() + random.nextInt(this.getWorldHeight() - 36 - this.getSeaLevel());
        int height = floorHeight + random.nextInt(Math.min(Math.max(this.getWorldHeight() - floorHeight, 16), 32));
        int roundFactor = random.nextInt(10);
        int[] offset = new int[height];

        for (int i = 0; i < height; i++) {
            offset[i] = roundFactor + random.nextInt(2) - random.nextInt(4);
        }

        boolean isAllErasing = random.nextInt(100) <= 5;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // not support minus
                for (int y = 0; y < this.getWorldHeight(); y++) {
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    BlockState current = state;

                    // force replacement
                    if (y <= this.getSeaLevel()) {
                        state = Blocks.WATER.getDefaultState();
                        chunk.setBlockState(pos, state, true);
                        continue;
                    }

                    if (y < floorHeight || height < y || isAllErasing) {
                        state = Blocks.AIR.getDefaultState();
                        chunk.setBlockState(pos, state, true);
                        continue;
                    }

                    if (floorHeight <= y && y <= floorHeight + height) {
                        int i = Math.min(y - floorHeight, height);
                        int factor = offset[i];
                        boolean c = getSqrt(x, z, 8, 8) >= factor;
                        if (c) {
                            state = Blocks.AIR.getDefaultState();
                            chunk.setBlockState(pos, state, true);
                            continue;
                        } else {
                            if (current == Blocks.AIR.getDefaultState()) {
                                state = Blocks.STONE.getDefaultState();
                                chunk.setBlockState(pos, state, true);
                                continue;
                            }
                        }
                    }

                    if (state == Blocks.WATER.getDefaultState()) {
                        chunk.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
                        continue;
                    }
                }
            }
        }
    }
}

package com.natsuneko.floatingisland;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;

public class FloatingIslandWorldType extends WorldType {
    public FloatingIslandWorldType() {
        super("floating-island");
    }

    public IChunkGenerator<?> createChunkGenerator(World worldIn) {
        OverworldGenSettings worldGenSettings = FloatingIsland.FLOATING_ISLAND.createSettings();
        OverworldBiomeProviderSettings biomeProviderSettings = BiomeProviderType.VANILLA_LAYERED.createSettings().setWorldInfo(worldIn.getWorldInfo()).setGeneratorSettings(worldGenSettings);
        BiomeProvider biomeProvider = BiomeProviderType.VANILLA_LAYERED.create(biomeProviderSettings);
        return FloatingIsland.FLOATING_ISLAND.create(worldIn, biomeProvider, worldGenSettings);
    }
}

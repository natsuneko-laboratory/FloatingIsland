package com.natsuneko.floatingisland.mixin;

import com.natsuneko.floatingisland.FloatingIsland;
import com.natsuneko.floatingisland.world.gen.FloatingIslandChunkGenerator;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.gen.WorldPresets$Registrar")
public abstract class WorldPresetMixin {
    @Shadow
    protected abstract RegistryEntry<WorldPreset> register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);

    @Shadow
    protected abstract DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator);

    @Shadow
    @Final
    private Registry<StructureSet> structureSetRegistry;

    @Shadow
    @Final
    private Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseParametersRegistry;

    @Shadow
    @Final
    private Registry<Biome> biomeRegistry;

    @Shadow
    @Final
    private Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry;

    @Unique
    private static final RegistryKey<WorldPreset> FLOATING_ISLAND_KEY = RegistryKey.of(Registry.WORLD_PRESET_KEY, new Identifier(FloatingIsland.MOD_ID, "floating_island"));

    @Inject(method = "initAndGetDefault", at = @At("RETURN"))
    private void addPreset(CallbackInfoReturnable<RegistryEntry<WorldPreset>> ret) {
        MultiNoiseBiomeSource multiNoiseBiomeSource = MultiNoiseBiomeSource.Preset.OVERWORLD.getBiomeSource(this.biomeRegistry);
        RegistryEntry<ChunkGeneratorSettings> registryEntry = this.chunkGeneratorSettingsRegistry.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD);

        this.register(FLOATING_ISLAND_KEY, this.createOverworldOptions(new FloatingIslandChunkGenerator(
                this.structureSetRegistry,
                this.noiseParametersRegistry,
                multiNoiseBiomeSource,
                registryEntry
        )));
    }
}

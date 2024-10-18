package com.natsuneko.floatingisland.mixin;

import com.natsuneko.floatingisland.FloatingIsland;
import com.natsuneko.floatingisland.world.gen.FloatingIslandChunkGenerator;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.gen.WorldPresets$Registrar")
public abstract class WorldPresetRegistrarMixin {
    @Shadow
    protected abstract void register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);

    @Shadow
    protected abstract DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator);

    @Shadow
    @Final
    private RegistryEntryLookup<StructureSet> structureSetLookup;

    @Shadow
    @Final
    private RegistryEntryLookup<MultiNoiseBiomeSourceParameterList> multiNoisePresetLookup;

    @Shadow
    @Final
    private RegistryEntryLookup<Biome> biomeLookup;

    @Shadow
    @Final
    private RegistryEntryLookup<ChunkGeneratorSettings> chunkGeneratorSettingsLookup;

    @Final
    private static final RegistryKey<WorldPreset> FLOATING_ISLAND_KEY = RegistryKey.of(RegistryKeys.WORLD_PRESET, new Identifier(FloatingIsland.MOD_ID, "floating_island"));


    @Inject(method = "bootstrap()V", at = @At("RETURN"))
    private void bootstrap(CallbackInfo ci) {
        MultiNoiseBiomeSource multiNoiseBiomeSource = MultiNoiseBiomeSource.create(this.multiNoisePresetLookup.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD));
        RegistryEntry<ChunkGeneratorSettings> registryEntry = this.chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettings.OVERWORLD);

        this.register(FLOATING_ISLAND_KEY, this.createOverworldOptions(new FloatingIslandChunkGenerator(
                multiNoiseBiomeSource,
                registryEntry
        )));
    }
}

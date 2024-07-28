package com.natsuneko.floatingisland;

import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("floating-island-revived")
public class FloatingIsland
{
    public static final ChunkGeneratorType<OverworldGenSettings, FloatingIslandChunkGenerator> FLOATING_ISLAND = ChunkGeneratorType.func_212676_a("floating-island-revived", FloatingIslandChunkGenerator::new, OverworldGenSettings::new, true);

    public FloatingIsland()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogManager.getLogger();

    public void setup(final FMLCommonSetupEvent event)
    {
        new FloatingIslandWorldType();
    }
}

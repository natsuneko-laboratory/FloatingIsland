package com.natsuneko.floatingisland;

<<<<<<< Updated upstream
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
=======
import com.natsuneko.floatingisland.world.gen.FloatingIslandChunkGenerator;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FloatingIsland implements ModInitializer {
	public static final String MOD_ID = "floating-island";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier(MOD_ID, "floating_island"), FloatingIslandChunkGenerator.CODEC);
	}
}
>>>>>>> Stashed changes

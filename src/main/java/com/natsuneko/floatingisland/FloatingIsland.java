package com.natsuneko.floatingisland;

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
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier(MOD_ID, "floating_island"), FloatingIslandChunkGenerator.CODEC);
	}
}

package com.natsuneko.floatingisland;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(modid ="com.natsuneko.floating-island", name="Floating Island", version = "2024.07.15")
public class FloatingIsland
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogManager.getLogger();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        new FloatingIslandWorldType();
    }
}

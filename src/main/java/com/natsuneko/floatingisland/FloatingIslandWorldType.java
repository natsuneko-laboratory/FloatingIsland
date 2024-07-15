package com.natsuneko.floatingisland;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkGenerator;

public class FloatingIslandWorldType extends WorldType {
    public FloatingIslandWorldType() {
        super("floating-island");
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new FloatingIslandChunkGenerator(world, world.getSeed(), generatorOptions);
    }
}

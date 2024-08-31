package com.natsuneko.floatingisland.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkNoiseSampler.class)
public interface ChunkNoiseSamplerMixin {

    @Invoker("sampleBlockState")
    BlockState invokeSampleBlockState();

    @Invoker("getHorizontalBlockSize")
    int invokeGetHorizontalBlockSize();

    @Invoker("getVerticalBlockSize")
    int invokeGetVerticalBlockSize();
}

/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.world.gen.structure;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.MapGenStructure;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.Random;

/**
 * This mixin is making MapGenStructure be a populator as well as a
 * generationpopulator as the structures are called both from the generation
 * phase and the population phase of chunk creation.
 */
@Mixin(MapGenStructure.class)
public abstract class MixinMapGenStructure implements Populator {

    @Shadow
    public abstract boolean generateStructure(World worldIn, Random p_175794_2_, ChunkPos p_175794_3_);
    
    @Override
    public PopulatorType getType() {
        return InternalPopulatorTypes.STRUCTURE;
    }

    @Override
    public void populate(org.spongepowered.api.world.World worldIn, Extent extent, Random random) {
        Vector3i min = extent.getBlockMin();
        World world = (World) worldIn;
        generateStructure(world, random, new ChunkPos((min.getX() - 8) / 16, (min.getZ() - 8) / 16));
    }

    @Inject(method = "generateStructure", at = @At("HEAD"), cancellable = true)
    public void onGenerateStructure(World worldIn, Random randomIn, ChunkPos chunkCoord, CallbackInfoReturnable<Boolean> cir) {
        Chunk chunk = worldIn.getChunkProvider().getLoadedChunk(chunkCoord.chunkXPos, chunkCoord.chunkZPos);
        if (chunk == null) {
            cir.setReturnValue(false);
        }
    }

}

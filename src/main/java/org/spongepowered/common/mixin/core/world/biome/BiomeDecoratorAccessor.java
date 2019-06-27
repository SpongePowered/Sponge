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
package org.spongepowered.common.mixin.core.world.biome;

import net.minecraft.world.biome.BiomeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeDecorator.class)
public interface BiomeDecoratorAccessor {

    @Accessor("sandPatchesPerChunk") int accessor$getSandPerChunk();

    @Accessor("sandPatchesPerChunk") void accessor$setSandPerChunk(int patches);

    @Accessor("clayPerChunk") int accessor$getClayPerChunk();

    @Accessor("clayPerChunk") void accessor$setClayPerChunk(int patches);

    @Accessor("gravelPatchesPerChunk") int accessor$getGravelPerChunk();

    @Accessor("gravelPatchesPerChunk") void accessor$setGravelPerChunk(int patches);

    @Accessor("treesPerChunk") int accessor$getTreesPerChunk();

    @Accessor("treesPerChunk") void accessor$setTreesPerChunk(int patches);

    @Accessor("cactiPerChunk") int accessor$getCactiPerChunk();

    @Accessor("cactiPerChunk") void accessor$setCactiPerChunk(int patches);

    @Accessor("reedsPerChunk") int accessor$getReedsPerChunk();

    @Accessor("reedsPerChunk") void accessor$setReedsPerChunk(int patches);

    @Accessor("bigMushroomsPerChunk") int accessor$getBigMushroomsPerChunk();

    @Accessor("bigMushroomsPerChunk") void accessor$setBigMushroomsPerChunk(int patches);

    @Accessor("flowersPerChunk") int accessor$getFlowersPerChunk();

    @Accessor("flowersPerChunk") void accessor$setFlowersPerChunk(int patches);

    @Accessor("grassPerChunk") int accessor$getGrassPerChunk();

    @Accessor("grassPerChunk") void accessor$setGrassPerChunk(int patches);

    @Accessor("deadBushPerChunk") int accessor$getDeadBushPerChunk();

    @Accessor("deadBushPerChunk") void accessor$setDeadBushPerChunk(int patches);

    @Accessor("waterlilyPerChunk") int accessor$getWaterLilyPerChunk();

    @Accessor("waterlilyPerChunk") void accessor$setWaterLilyPerChunk(int patches);

    @Accessor("mushroomsPerChunk") int accessor$getMushroomsPerChunk();

    @Accessor("mushroomsPerChunk") void accessor$setMushroomsPerChunk(int patches);


}

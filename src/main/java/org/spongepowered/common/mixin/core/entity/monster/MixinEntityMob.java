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
package org.spongepowered.common.mixin.core.entity.monster;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.mixin.core.entity.MixinEntityCreature;

@Mixin(EntityMob.class)
public abstract class MixinEntityMob extends MixinEntityCreature implements Monster {

    /**
     * @author aikar - February 20th, 2017 - Optimizes light level check.
     * @author blood - February 20th, 2017 - Avoids checking unloaded chunks and chunks with pending light updates.
     * @author Aaron1011 - February 7th, 2019 - Updated for 1.13
     *
     * @reason Avoids checking unloaded chunks and chunks with pending light updates.
     *
     * @return Whether current position has a valid light level for spawning
     */
    @Overwrite
    protected boolean isValidLightLevel()
    {
        final BlockPos blockpos = new BlockPos(this.posX, this.shadow$getBoundingBox().minY, this.posZ);
        final Chunk chunk = ((IMixinChunkProviderServer) this.world.getChunkProvider()).getLoadedChunkWithoutMarkingActive(blockpos.getX() >> 4, blockpos.getZ() >> 4);
        if (chunk == null || !((IMixinChunk) chunk).isActive()) {
            return false;
        }

        if (this.world.getLightFor(EnumLightType.SKY, blockpos) > this.rand.nextInt(32))
        {
            return false;
        }
        else
        {
            int i = this.world.isThundering() ? this.world.getNeighborAwareLightSubtracted(blockpos, 10) : this.world.getLight(blockpos);
            return i <= this.rand.nextInt(8);
        }
    }
}

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
package org.spongepowered.common.mixin.optimization.entity.monster;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;

@Mixin(value = EntityMob.class, priority = 1001)
public abstract class MixinEntityMob_Async_Lighting {

    @Redirect(method = "isValidLightLevel", at = @At(value = "INVOKE", target = "Lorg/spongepowered/common/interfaces/world/gen/IMixinChunkProviderServer;getLoadedChunkWithoutMarkingActive(II)Lnet/minecraft/world/chunk/Chunk;", remap = false))
    public Chunk onIsValidLightLevelLoadChunk(IMixinChunkProviderServer chunkProvider, int chunkX, int chunkZ) {
        final Chunk chunk = chunkProvider.getLoadedChunkWithoutMarkingActive(chunkX, chunkZ);
        if (chunk != null) {
            final IMixinChunk spongeChunk = (IMixinChunk) chunk;
            if (spongeChunk.getPendingLightUpdates().get() > 0
                    || (chunkProvider.getWorld().getTotalWorldTime() - spongeChunk.getLightUpdateTime() < 20)) {
                return null;
            }
        }
        return chunk;
    }
}

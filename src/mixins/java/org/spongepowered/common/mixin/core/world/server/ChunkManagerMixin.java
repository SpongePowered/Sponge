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
package org.spongepowered.common.mixin.core.world.server;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.storage.ChunkSerializer;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;

@Mixin(ChunkManager.class)
public abstract class ChunkManagerMixin {

    // @formatter:off
    @Shadow @Final private ServerWorld level;
    // @formatter:on

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/PointOfInterestManager;flush(Lnet/minecraft/util/math/ChunkPos;)V"))
    private void impl$useSerializationBehaviorForPOI(PointOfInterestManager pointOfInterestManager, ChunkPos p_219112_1_) {
        final ServerWorldInfoBridge infoBridge = (ServerWorldInfoBridge) this.level.getLevelData();
        final SerializationBehavior serializationBehavior = infoBridge.bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
        if (serializationBehavior == SerializationBehavior.AUTOMATIC || serializationBehavior == SerializationBehavior.MANUAL) {
            pointOfInterestManager.flush(p_219112_1_);
        }
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/storage/ChunkSerializer;write(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/world/chunk/IChunk;)Lnet/minecraft/nbt/CompoundNBT;"))
    private CompoundNBT impl$useSerializationBehaviorForChunkSave(ServerWorld worldIn, IChunk chunkIn) {
        final ServerWorldInfoBridge infoBridge = (ServerWorldInfoBridge) this.level.getLevelData();
        final SerializationBehavior serializationBehavior = infoBridge.bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
        if (serializationBehavior == SerializationBehavior.AUTOMATIC || serializationBehavior == SerializationBehavior.MANUAL) {
            return ChunkSerializer.write(worldIn, chunkIn);
        }

        return null;
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ChunkManager;write(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/nbt/CompoundNBT;)V"))
    private void impl$doNotWriteIfWeHaveNoData(ChunkManager chunkManager, ChunkPos pos, CompoundNBT compound) {
        if (compound == null) {
            return;
        }

        chunkManager.write(pos, compound);
    }
}

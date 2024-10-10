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
package org.spongepowered.common.mixin.core.world.level.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.data.DataUtil;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {

    @Inject(method = "write", at = @At(value = "RETURN"))
    private static void impl$writeSpongeChunkData(final ServerLevel level, final ChunkAccess chunk, final CallbackInfoReturnable<CompoundTag> cir) {
        if (!(chunk instanceof LevelChunk)) {
            return;
        }

        final CompoundTag compound = cir.getReturnValue();
        if (DataUtil.syncDataToTag(chunk)) {
            compound.merge(((DataCompoundHolder) chunk).data$getCompound());
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void impl$readSpongeChunkData(final ServerLevel level, final PoiManager poi, final RegionStorageInfo $$2, final ChunkPos pos, final CompoundTag compound, final CallbackInfoReturnable<ProtoChunk> cir) {
        if (!(cir.getReturnValue() instanceof ImposterProtoChunk imposter)) {
            return;
        }

        ((DataCompoundHolder) imposter.getWrapped()).data$setCompound(compound);
        DataUtil.syncTagToData(imposter.getWrapped());
        ((DataCompoundHolder) imposter.getWrapped()).data$setCompound(null);
    }
}

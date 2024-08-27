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

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.level.chunk.storage.SerializableChunkDataBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.data.DataUtil;

@Mixin(SerializableChunkData.class)
public abstract class SerializableChunkDataMixin implements SerializableChunkDataBridge {

    private CompoundTag impl$tag;
    private SerializationBehavior impl$serializationBehavior;


    @Inject(method = "copyOf", at = @At(value = "RETURN"))
    private static void impl$copyOfSpongeData(final ServerLevel level, final ChunkAccess chunkAccess, final CallbackInfoReturnable<SerializableChunkData> cir) {
        final var bridge = (SerializableChunkDataBridge) (Object) cir.getReturnValue();
        if (level.getLevelData() instanceof PrimaryLevelDataBridge levelBridge) {
            final var behavior = levelBridge.bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
            bridge.bridge$setSerializationBehavior(behavior);
        }
        if (chunkAccess instanceof LevelChunk levelChunk) {

            bridge.bridge$setTrackerData(levelChunk);
            bridge.bridge$setDataHolderData(levelChunk);
        }
    }

    @Override
    public void bridge$setSerializationBehavior(final SerializationBehavior serializationBehavior) {
        this.impl$serializationBehavior = serializationBehavior;
    }

    @Override
    public void bridge$setDataHolderData(final LevelChunk levelChunk) {
        this.impl$tag = null;
        if (DataUtil.syncDataToTag(levelChunk)) {
            this.impl$tag = ((DataCompoundHolder) levelChunk).data$getCompound();
        }
    }

    @Inject(method = "write", at = @At(value = "HEAD"), cancellable = true)
    private void tracker$beforeWrite(final CallbackInfoReturnable<CompoundTag> cir) {
        switch (this.impl$serializationBehavior) {
            case AUTOMATIC, MANUAL -> {} // write normally
            default -> cir.setReturnValue(null);
        }

    }

    @Inject(method = "write", at = @At(value = "RETURN"))
    private void tracker$writeSpongeData(final CallbackInfoReturnable<CompoundTag> cir) {
        final CompoundTag level = cir.getReturnValue();
        this.bridge$writeTrackerData(level);
        this.bridge$writeDataHolderData(level);
    }

    @Override
    public void bridge$writeDataHolderData(final CompoundTag level) {
        if (this.impl$tag != null) {
            level.merge(this.impl$tag);
        }
    }

    @Inject(method = "parse", at = @At(value = "RETURN"))
    private static void tracker$parseSpongeData(final LevelHeightAccessor $$0, final RegistryAccess $$1, final CompoundTag tag, final CallbackInfoReturnable<SerializableChunkData> cir) {
        final var bridge = (SerializableChunkDataBridge) (Object) cir.getReturnValue();
        bridge.bridge$parseTrackerData(tag);
        bridge.bridge$parseDataHolderData(tag);
    }

    @Override
    public void bridge$parseDataHolderData(final CompoundTag tag) {
        this.impl$tag = tag;
    }

    @Redirect(method = "read",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setLightCorrect(Z)V"))
    private void impl$readSpongeLevelData(final ChunkAccess chunkAccess, final boolean lightCorrect) {
        chunkAccess.setLightCorrect(lightCorrect);

        if (!(chunkAccess instanceof LevelChunk levelChunk)) {
            return;
        }

        this.bridge$readTrackerDataFrom(levelChunk);
        this.bridge$readDataHolderDataFrom(levelChunk);
    }

    @Override
    public void bridge$readDataHolderDataFrom(final LevelChunk levelChunk) {
        ((DataCompoundHolder) levelChunk).data$setCompound(this.impl$tag);
        DataUtil.syncTagToData(levelChunk);
        ((DataCompoundHolder) levelChunk).data$setCompound(null);
    }


}

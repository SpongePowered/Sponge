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
package org.spongepowered.common.mixin.core.world.level.chunk.storage;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.world.level.chunk.storage.RegionFileAccessor;
import org.spongepowered.common.bridge.world.level.chunk.storage.RegionFileBridge;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(RegionFile.class)
public abstract class RegionFileMixin implements RegionFileBridge {

    // @formatter:off
    @Shadow @Final private Path path;

    @Shadow protected abstract int shadow$getOffset(final ChunkPos $$0);
    @Shadow protected abstract Path shadow$getExternalChunkPath(final ChunkPos $$0);
    // @formatter:on

    private ByteBuffer impl$allData;

    /**
     * This caches the entire region file instead of just reading parts of it like {@link RegionFile#doesChunkExist}.
     */
    @Override
    public boolean bridge$doesChunkExist(final ChunkPos chunkPos) {
        try {

            final var offset = this.shadow$getOffset(chunkPos);
            if (offset == 0) {
                return false;
            }

            final var sectorNumber = RegionFileAccessor.invoker$getSectorNumber(offset);
            final var numSectors = RegionFileAccessor.invoker$getNumSectors(offset);

            if (this.impl$allData == null) {
                this.impl$allData = ByteBuffer.wrap(Files.readAllBytes(this.path));
            }

            final var chunkStart = sectorNumber * 4096;
            this.impl$allData.position(chunkStart);
            var size = this.impl$allData.getInt();
            var version = this.impl$allData.get();


            if (RegionFileAccessor.invoker$isExternalStreamChunk(version)) {
                if (!RegionFileVersion.isValidVersion(RegionFileAccessor.invoker$getExternalChunkVersion(version))) {
                    return false;
                }
                if (!Files.isRegularFile(this.shadow$getExternalChunkPath(chunkPos))) {
                    return false;
                }
            } else {
                if (!RegionFileVersion.isValidVersion(version)) {
                    return false;
                }
                if (size == 0) {
                    return false;
                }

                var $$7 = size - 1;
                if ($$7 < 0 || $$7 > 4096 * numSectors) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void impl$onClose(final CallbackInfo ci) {
        this.impl$allData = null;
    }


}

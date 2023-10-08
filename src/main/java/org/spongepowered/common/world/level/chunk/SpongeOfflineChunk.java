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
package org.spongepowered.common.world.level.chunk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.chunk.OfflineChunk;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.math.vector.Vector3i;

import java.io.DataInputStream;
import java.io.IOException;

public final class SpongeOfflineChunk implements OfflineChunk {

    private final CompoundTag nbt;
    private final Vector3i chunkPos;

    public SpongeOfflineChunk(final CompoundTag nbt, final int cx, final int cz) {
        this.nbt = nbt;
        this.chunkPos = new Vector3i(cx, 0, cz);
    }

    @Nullable
    public static OfflineChunk of(final RegionFile regionFile, final ChunkPos pos) {
        CompoundTag chunkNbt;
        try (DataInputStream $$2 = regionFile.getChunkDataInputStream(pos)) {
            if ($$2 == null) {
                return null;
            }

            chunkNbt = NbtIo.read($$2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new SpongeOfflineChunk(chunkNbt, pos.x, pos.z);
    }

    @Override
    public org.spongepowered.math.vector.Vector3i chunkPosition() {
        return this.chunkPos;
    }

    @Override
    public int contentVersion() {
        return 2; // TODO get from actual data?, assuming compression type is 2
    }

    @Override
    public DataContainer toContainer() {
        return NBTTranslator.INSTANCE.translate(this.nbt);
    }

}

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
package org.spongepowered.common.mixin.core.world.chunk.storage;

import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.DataInputStream;
import java.io.File;

@Mixin(RegionFileCache.class)
public abstract class RegionFileCacheMixin {

    @Shadow public static RegionFile getRegionFileIfExists(File worldDir, int chunkX, int chunkZ) {return null;}

    /**
     * @author JBYoshi - January 2, 2020 (1.12.2)
     * @reason Support for ChunkSerializationBehaviors that don't save chunks:
     * uses getRegionFileIfExists instead of createOrLoadRegionFile to avoid
     * creating new files.
     */
    @Overwrite
    public static DataInputStream getChunkInputStream(File worldDir, int chunkX, int chunkZ) {
        // Sponge start
        // Use getRegionFileIfExists instead of createOrLoadRegionFile
        RegionFile regionfile = getRegionFileIfExists(worldDir, chunkX, chunkZ);
        if (regionfile == null) {
            // Returning null here is acceptable: getChunkDataInputStream()
            // already returns null if the file does not exist.
            return null;
        }
        // Sponge end
        return regionfile.getChunkDataInputStream(chunkX & 31, chunkZ & 31);
    }
}

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
package org.spongepowered.server.mixin.chunkio;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.io.IOException;

@Mixin(AnvilChunkLoader.class)
public abstract class AnvilChunkLoaderMixin_ChunkIO {

    /**
     * @author Minecrell - May 28th, 2016
     * @reason Replaced to throw an exception because everything should go
     *     through the async chunk loader
     */
    @Nullable
    @Overwrite
    @Final
    public Chunk loadChunk(World worldIn, int x, int z) throws IOException {
        throw new UnsupportedOperationException("Attempting to load a chunk synchronously");
    }

    @Inject(method = "readChunkFromNBT",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;getTagList(Ljava/lang/String;I)Lnet/minecraft/nbt/NBTTagList;",
                    ordinal = 1),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void chunkIO$asyncReadChunkFromNBT(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir, int x, int z, Chunk chunk) {
        cir.setReturnValue(chunk);
    }

    @Surrogate
    private void chunkIO$asyncReadChunkFromNBT(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir,
            int x, int z, Chunk chunk, NBTTagList nbttaglist, int k, ExtendedBlockStorage[] aextendedblockstorage, boolean flag) {
        cir.setReturnValue(chunk);
    }

}

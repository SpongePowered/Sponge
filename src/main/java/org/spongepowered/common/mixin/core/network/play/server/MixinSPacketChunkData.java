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
package org.spongepowered.common.mixin.core.network.play.server;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.interfaces.IMixinBlockStateContainer;

@Mixin(SPacketChunkData.class)
public abstract class MixinSPacketChunkData {

    @Shadow private int chunkX;
    @Shadow private int chunkZ;
    @Shadow private boolean fullChunk;
    @Shadow private byte[] buffer;

    @Shadow protected abstract int calculateChunkSize(Chunk chunkIn, boolean p_189556_2_, int p_189556_3_);

    private int calculatedSize;

    @Redirect(
        method = "<init>(Lnet/minecraft/world/chunk/Chunk;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/play/server/SPacketChunkData;calculateChunkSize(Lnet/minecraft/world/chunk/Chunk;ZI)I"
        )
    )
    private int spongeImpl$getCalculatedSizeForArray(SPacketChunkData sPacketChunkData, Chunk chunkIn, boolean p_189556_2_, int p_189556_3_) {
        this.calculatedSize = this.calculateChunkSize(chunkIn, p_189556_2_, p_189556_3_);
        return this.calculatedSize;
    }

    @Redirect(
        method = "<init>(Lnet/minecraft/world/chunk/Chunk;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/play/server/SPacketChunkData;extractChunkData(Lnet/minecraft/network/PacketBuffer;Lnet/minecraft/world/chunk/Chunk;ZI)I"
        )
    )
    private int spongeImpl$surroundExtractingChunkDataWithExceptionPrinter(SPacketChunkData this$0, PacketBuffer buf, Chunk chunkIn, boolean writeSkylight, int changedSectionFilter) {
        try {
            return this$0.extractChunkData(buf, chunkIn, writeSkylight, changedSectionFilter);
        } catch (Exception e) {
            spongeImpl$printVerbosity(chunkIn, writeSkylight, changedSectionFilter, e);
            throw new RuntimeException(String.format("Exception creating chunk packet for chunk at '%s %s'!", this.chunkX, this.chunkZ), e);
        }
    }

    private void spongeImpl$printVerbosity(Chunk chunkIn, boolean writeSkylight, int changedSectionFilter, Exception e) {
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to create a ChunkPacket").centre().hr()
            .addWrapped(70, "Sponge has been attempting to resolve an issue where a chunk "
                            + "is being \"serialized\" to a packet, but somehow the chunk data is mismatched "
                            + "according to the chunk's data actual size. This message has been tailored to "
                            + "specifically provide as much information as possible about the issue.")
            .add()
            .add("%s : %s, %s", "Chunk Information", this.chunkX, this.chunkZ)
            .add("%s : %s", "World", chunkIn.getWorld())
            .add("%s : %s", "Writing Skylight", writeSkylight)
            .add("%s : %s", "Full Chunk", this.fullChunk)
            .add("%s : %s", "ByteArraySize", this.buffer.length)
            .add("%s : %s", "CalculatedSize", this.calculatedSize)
            .add("%s : %s", "ChangedSection Filter", changedSectionFilter)
            .add("%s : %s", "Recalculated Size", this.calculateChunkSize(chunkIn, writeSkylight, changedSectionFilter))
            .add();
        printer.add("Printing ExtendedStorage data")
            .add();
        ExtendedBlockStorage[] aextendedblockstorage = chunkIn.getBlockStorageArray();
        int j = 0;

        for (int k = aextendedblockstorage.length; j < k; ++j)
        {
            ExtendedBlockStorage extendedblockstorage = aextendedblockstorage[j];

            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && (changedSectionFilter & 1 << j) != 0)
            {
                final BlockStateContainer data = extendedblockstorage.getData();
                printer.add(" - %s : %s", j, "ExtendedArrayIndex");
                final IMixinBlockStateContainer mixinData = (IMixinBlockStateContainer) data;
                printer.add("  - %s : %s", "ContainerBits", mixinData.getBits())
                    .add("  - %s : %s", "Palette Size", mixinData.getPalette().getSerializedSize())
                    .add("  - %s : %s", "BackingArray", mixinData.getStorage().getBackingLongArray())
                    .add("  - %s : %s", "BlockLight", extendedblockstorage.getBlockLight().getData());


                if (writeSkylight)
                {
                    printer.add("  - %s : %s", "SkyLight", extendedblockstorage.getSkyLight().getData());
                }
            }
        }

        if (this.fullChunk)
        {
            printer.add(" - %s : %s", "BiomeArray", chunkIn.getBiomeArray());
        }

        printer
            .add("Exception")
            .add(e)
            .trace();
    }

}

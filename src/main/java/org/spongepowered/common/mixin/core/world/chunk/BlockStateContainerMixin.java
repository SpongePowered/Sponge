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
package org.spongepowered.common.mixin.core.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;
import net.minecraft.world.chunk.NibbleArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.chunk.BlockStateContainerBridge;

import javax.annotation.Nullable;

@Mixin(BlockStateContainer.class)
public abstract class BlockStateContainerMixin implements BlockStateContainerBridge {

    @Shadow private int bits;
    @Shadow protected IBlockStatePalette palette;
    @Shadow protected BitArray storage;

    @Shadow protected abstract void set(int index, BlockState state);

    @Override
    public int bridge$getBits() {
        return this.bits;
    }

    @Override
    public IBlockStatePalette bridge$getPalette() {
        return this.palette;
    }

    @Override
    public BitArray bridge$getStorage() {
        return this.storage;
    }

    /**
     * @author barteks2x
     *
     * Attempts to fix invalid block metadata instead of completely throwing away the block.
     * When block state lookup returns null - gets block by ID and attempts to use the default state.
     */
    @Redirect(
            method = "setDataFromNBT",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockStateContainer;set(ILnet/minecraft/block/state/IBlockState;)V")
    )
    private void setFixedBlockState(BlockStateContainer this_, int i, BlockState state, byte[] id, NibbleArray meta, @Nullable NibbleArray add) {
        BlockState newState;

        if (state != null) {
            newState = state;
        } else {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            int idAdd = add == null ? 0 : add.get(x, y, z);
            int blockId = idAdd << 8 | (id[i] & 255);
            Block block = Block.field_149771_c.func_148754_a(blockId);
            if (block != null) {
                newState = block.getDefaultState();
            } else {
                newState = null;
            }
        }
        this.set(i, newState);
    }

    /**
     * Serializing a BlockStateContainer to a PacketBuffer is done in two parts:
     * calculating the size of the allocation needed in the PacketBuffer, and actually
     * writing it.
     *
     * When the BlockStateContainer is actually written to the PacketBuffer,
     * its 'storage.BitArray.getBackingLongArray' is written as a VarInt-length-prefixed
     * array. However, when calculating the size of the allocation needed, the size of
     * 'storage.size()' encoded as a VarInt is used, not the size of 'getBackingLongArray'
     * encoded as a VarInt. If the size of getBackingLongArray is ever large enough to require
     * an extra byte in its VarInt encoding, the allocated buffer will be too small, resuling in a crash.
     *
     * To fix this issue, we calculate the length of getBackingLongArray encoded as a VarInt,
     * when we're calculating the necessary allocation size.
     * @param bits
     * @return
     */
    @Redirect(method = "getSerializedSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/BitArray;size()I"))
    private int onGetStorageSize$FixVanillaBug(BitArray bits) {
        return bits.getBackingLongArray().length;
    }

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IBlockStatePalette;write(Lnet/minecraft/network/PacketBuffer;)V"))
    private void onPaletteWrite(IBlockStatePalette palette, PacketBuffer buffer) {
        final int serializedSize = palette.getSerializedSize();
        final int index = buffer.writerIndex();
        try {
            palette.write(buffer);
        } catch (Exception e) {
            throw new RuntimeException("Attempted to serialize a block palette of size: " + serializedSize);
        }
        final int newIndex = buffer.writerIndex();
        if (index + serializedSize != newIndex) {
            throw new IllegalStateException("Expected to have written " + serializedSize +  " for a block palette, but instead wrote " +  (newIndex - index));
        }
    }

    /**
     * @author gabizou - September 6th, 2018
     * @reason Instead of allowing the crash to occur usually when the packet buffer is attempting
     * to write a long array entry that exceeds the maximum capacity, then we should do a better job
     * at detecting it by performing checks on expected sizes versus actual new sizes. If a long value
     * ended up causing the whole thing to go kaput, this will find out.
     *
     * @param buffer The buffer
     * @param backingArray The backing array
     * @return The buffer, if successful.
     */
    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;writeLongArray([J)Lnet/minecraft/network/PacketBuffer;"))
    private PacketBuffer onSpongeWriteLongArrayPacketBuffer(PacketBuffer buffer, long[] backingArray) {
        final int expectedSize = PacketBuffer.getVarIntSize(backingArray.length);
        final int lengthIndex = buffer.writerIndex();

        try {
            // This is what is written first, the long array length as a var int.
            buffer.writeVarInt(backingArray.length);
        } catch (Exception e) { // If there was an exception, at least we'll be making sure it's logged where...
            throw new RuntimeException("Attempted to serialize the backing long array size but couldn't! Expected writer index("
                                       + lengthIndex + ") with expected size("+ expectedSize + ") and current writer index(" + buffer.writerIndex() +")");
        }
        final int currentIndex = buffer.writerIndex();
        if (lengthIndex + expectedSize != currentIndex) {
            throw new IllegalStateException("Attempted to serialize a long array size incorrectly! Expected index to start " + lengthIndex + " with var int size of " + expectedSize + " but got " + (currentIndex - lengthIndex));
        }

        // Now onto the target of the issue, writing the backing array to the buffer...
        final int arrayIndex = buffer.writerIndex();
        final int expectedArraySize = backingArray.length * 8; // this is what the array should be stored as regardless

        for (int i = 0; i < backingArray.length; i++) {
            final int bufferIndex = buffer.writerIndex();
            final long value = backingArray[i];
            if (bufferIndex + 8 > buffer.writableBytes()) {
                switch (buffer.ensureWritable(8, true)) {
                    case 0: // Capacity matches and has enough bytes.
                        break;
                    case 1: // Can't fit enough bytes, capacity is unchanged
                        // We can't fit any more data period.
                        new PrettyPrinter(60).add("Unable to resize Buffer for SPackeetChunkData").centre().hr()
                            .addWrapped(60, "Sponge is attempting to recover from a potentially fatal issue"
                                            + " with sending chunk packets. Because the cause of the issue is very difficult"
                                            + " to find, Sponge is attempting to recover the buffer before it crashes. Since"
                                            + " the buffer appears to be maxed out, no more data can be written to the buffer"
                                            + " and therefor it must be returned.")
                            .add()
                            .add("Please refer to the SpongeForge issue if this warning has been printed on yoru server, or the side effects of this warning.")
                            .trace();
                        throw new ArrayIndexOutOfBoundsException("Unable to resize packet buffer to fit more data. Current ");
                    case 2: // Buffer has enough and capacity increased to a new maximum
                        SpongeImpl.getLogger().warn("Sponge is attempting to prevent a crash for sending chunk data to clients. Managed to increase the buffer size. Refer to SpongeForge Issue #2405");
                        break;
                        // cool, nothing happens
                    case 3: // Buffer does not have enough space, but capacity increased.
                        // Cool, nothing happens
                        new PrettyPrinter(60).add("Unable to resize Buffer for SPackeetChunkData").centre().hr()
                            .addWrapped(60, "Sponge is attempting to recover from a potentially fatal issue"
                                            + " with sending chunk packets. Because the cause of the issue is very difficult"
                                            + " to find, Sponge is attempting to recover the buffer before it crashes. Since"
                                            + " the buffer appears to be maxed out, no more data can be written to the buffer"
                                            + " and therefor it must be returned.")
                            .add()
                            .add("Please refer to the SpongeForge issue if this warning has been printed on yoru server, or the side effects of this warning.")
                            .trace();
                        SpongeImpl.getLogger().error("Unable to increase the size of the buffer to fit enough data.");
                        // We can't fit any more data.
                        return buffer;
                }
            }
            try {
                buffer.writeLong(value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize chunk data to buffer. Attempted to write entry long[" + i + "]: " + value + " at writer index: " + bufferIndex + " with size " + 8, e);
            }
            final int newIndex = buffer.writerIndex();
            if (newIndex - bufferIndex != 8) {
                throw new IllegalStateException("Attempted to write a long to a packet buffer at index: " + bufferIndex + " with expected end of size of 8, however found " + (newIndex - bufferIndex) + " instead.");
            }
        }
        final int postIndex = buffer.writerIndex();
        final int difference = (arrayIndex + expectedArraySize) - postIndex;
        if (difference != 0) {
            throw new IllegalStateException("Potentially leaking or pruning data... Started index at " + arrayIndex + " with expected end being " + arrayIndex + expectedArraySize + " but got " + postIndex + " with a difference of " + difference);
        }
        return buffer;
    }

}

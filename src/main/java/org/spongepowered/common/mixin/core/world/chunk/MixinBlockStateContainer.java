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

import net.minecraft.util.BitArray;
import net.minecraft.world.chunk.BlockStateContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockStateContainer.class)
public abstract class MixinBlockStateContainer {

    /**
     * @author barteks2x
     *
     * Attempts to fix invalid block metadata instead of completely throwing away the block.
     * When block state lookup returns null - gets block by ID and attempts to use the default state.
     */
    // TODO 1.13: Check if this is still necessary
    /*@Redirect(
            method = "setDataFromNBT",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockStateContainer;set(ILnet/minecraft/block/state/IBlockState;)V")
    )
    private void setFixedBlockState(BlockStateContainer this_, int i, IBlockState state, byte[] id, NibbleArray meta, @Nullable NibbleArray add) {
        IBlockState newState;

        if (state != null) {
            newState = state;
        } else {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            int idAdd = add == null ? 0 : add.get(x, y, z);
            int blockId = idAdd << 8 | (id[i] & 255);
            Block block = IRegistry.BLOCK.get(blockId);
            newState = block.getDefaultState();
            if (block != null) {
                newState = block.getDefaultState();
            } else {
                newState = null;
            }
        }
        this.set(i, newState);
    }*/

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
    public int onGetStorageSize$FixVanillaBug(BitArray bits) {
        return bits.getBackingLongArray().length;
    }

}

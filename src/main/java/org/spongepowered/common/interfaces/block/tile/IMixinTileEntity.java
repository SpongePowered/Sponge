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
package org.spongepowered.common.interfaces.block.tile;

import co.aikar.timings.Timing;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.common.data.util.NbtDataUtil;

public interface IMixinTileEntity {

    /**
     * Gets a {@link NBTTagCompound} that can be used to store custom data for
     * this tile entity. It will be written, and read from disc, so it persists
     * over world saves.
     *
     * @return A compound tag for custom data
     */
    NBTTagCompound getTileData();

    /**
     * Gets the included {@link NBTTagCompound} for a tile entity. With Vanilla, this is
     * created by vanilla. With Forge, this is included.
     *
     * @return Gets the tag compound containing various tile data
     */
    default NBTTagCompound getSpongeData() {
        NBTTagCompound data = this.getTileData();
        if (!data.hasKey(NbtDataUtil.SPONGE_DATA, NbtDataUtil.TAG_COMPOUND)) {
            data.setTag(NbtDataUtil.SPONGE_DATA, new NBTTagCompound());
        }
        return data.getCompoundTag(NbtDataUtil.SPONGE_DATA);
    }

    /**
     * Read extra data (SpongeData) from the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    void readFromNbt(NBTTagCompound compound);

    /**
     * Write extra data (SpongeData) to the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    void writeToNbt(NBTTagCompound compound);

    /**
     * Due to the nature of how {@link TileEntity} instances translate their
     * data with {@link TileEntity#toContainer()}, all {@link DataManipulator}s
     * are serialized under a {@link DataQuery} specified query from
     * {@link NbtDataUtil}. This is to help separate the real
     * required data such as position, tile type, etc. from data api manipulators.
     *
     * @param dataView The data view to set all data api related data
     */
    void sendDataToContainer(DataView dataView);

    void markDirty();

    boolean isVanilla();

    // Timings
    Timing getTimingsHandler();
}

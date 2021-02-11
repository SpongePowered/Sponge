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
package org.spongepowered.common.bridge.data;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.util.Constants;

public interface DataCompoundHolder {

    CompoundTag data$getCompound();

    void data$setCompound(CompoundTag nbt);

    default boolean data$hasForgeData() {
        if (this.data$getCompound() == null) {
            return false;
        }
        return this.data$getCompound().contains(Constants.Forge.FORGE_DATA);
    }

    default CompoundTag data$getForgeData() {
        if (this.data$getCompound() == null) {
            this.data$setCompound(new CompoundTag());
        }
        CompoundTag forgeCompound = this.data$getCompound().getCompound(Constants.Forge.FORGE_DATA);
        if (forgeCompound.isEmpty()) {
            this.data$getCompound().put(Constants.Forge.FORGE_DATA, forgeCompound);
        }
        return forgeCompound;
    }

    default boolean data$hasSpongeData() {
        return this.data$hasForgeData() && this.data$getForgeData().contains(Constants.Sponge.Data.V2.SPONGE_DATA, Constants.NBT.TAG_COMPOUND);
    }

    default CompoundTag data$getSpongeData() {
        final CompoundTag spongeCompound = this.data$getForgeData();
        final CompoundTag dataCompound = spongeCompound.getCompound(Constants.Sponge.Data.V2.SPONGE_DATA);
        if (dataCompound.isEmpty()) {
            spongeCompound.put(Constants.Sponge.Data.V2.SPONGE_DATA, dataCompound);
        }
        return dataCompound;
    }

    /**
     * Gets the {@link NBTDataType} which this
     * nbt data holder contains data for.
     *
     * @return The nbt data type
     */
    NBTDataType data$getNBTDataType();
}

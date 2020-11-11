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

import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.util.Constants;

public interface DataCompoundHolder {

    CompoundNBT data$getCompound();
    void data$setCompound(CompoundNBT nbt);

    default boolean data$hasForgeData() {
        if (this.data$getCompound() == null) {
            return false;
        }
        return this.data$getCompound().contains(Constants.Forge.FORGE_DATA);
    }

    default CompoundNBT data$getForgeData() {
        if (this.data$getCompound() == null) {
            this.data$setCompound(new CompoundNBT());
        }
        CompoundNBT forgeCompound = this.data$getCompound().getCompound(Constants.Forge.FORGE_DATA);
        if (forgeCompound.isEmpty()) {
            this.data$getCompound().put(Constants.Forge.FORGE_DATA, forgeCompound);
        }
        return forgeCompound;
    }

    default boolean data$hasSpongeData() {
        return this.data$hasForgeData() && this.data$getForgeData().contains(Constants.Sponge.SPONGE_DATA, Constants.NBT.TAG_COMPOUND);
    }

    default CompoundNBT data$getSpongeData() {
        final CompoundNBT spongeCompound = this.data$getForgeData();
        final CompoundNBT dataCompound = spongeCompound.getCompound(Constants.Sponge.SPONGE_DATA);
        if (dataCompound.isEmpty()) {
            spongeCompound.put(Constants.Sponge.SPONGE_DATA, dataCompound);
        }
        return dataCompound;
    }

    default void data$cleanEmptySpongeData() {
        final CompoundNBT spongeData = this.data$getSpongeData();
        if (spongeData.isEmpty()) {
            final CompoundNBT spongeCompound = this.data$getForgeData();
            spongeCompound.remove(Constants.Sponge.SPONGE_DATA);
            if (spongeCompound.isEmpty()) {
                final CompoundNBT nbt = this.data$getCompound();
                nbt.remove(Constants.Forge.FORGE_DATA);
                if (nbt.isEmpty()) {
                    this.data$setCompound(null);
                }
            }
        }
    }

    /**
     * Gets the {@link NBTDataType} which this
     * nbt data holder contains data for.
     *
     * @return The nbt data type
     */
    NBTDataType data$getNbtDataType();
}

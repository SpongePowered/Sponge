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
package org.spongepowered.common.data.holder;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.data.provider.nbt.NBTDataType;

/**
 * Simple mutable data holder wrapper around a nbt compound. {@link SpongeDataHolderBridge} is mixed in.
 * Used for preparing data for Immutable data holders like {@link org.spongepowered.api.entity.EntitySnapshot}
 */
public final class SimpleNBTDataHolder implements DataCompoundHolder, SpongeMutableDataHolder {

    private CompoundTag compound;
    private final NBTDataType dataType;

    public SimpleNBTDataHolder(final CompoundTag compound, final NBTDataType dataType) {
        this.compound = compound;
        this.dataType = dataType;
    }

    @Override
    public CompoundTag data$getCompound() {
        return this.compound;
    }

    @Override
    public void data$setCompound(final CompoundTag compound) {
        this.compound = compound;
    }

    @Override
    public NBTDataType data$getNBTDataType() {
        return this.dataType;
    }
}

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
package org.spongepowered.common.data.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.util.Constants;

public final class NbtHelper {

    public static @Nullable Boolean getNullableBoolean(CompoundNBT compound, String key) {
        final INBT tag = compound.get(key);
        if (tag instanceof NumberNBT) {
            return ((NumberNBT) tag).getByte() != 0;
        }
        return null;
    }

    public static @Nullable CompoundNBT getNullableCompound(CompoundNBT compound, String key) {
        if (!compound.contains(key, Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        return compound.getCompound(key);
    }

    public static CompoundNBT getOrCreateCompound(CompoundNBT compound, String key) {
        if (!compound.contains(key, Constants.NBT.TAG_COMPOUND)) {
            final CompoundNBT child = new CompoundNBT();
            compound.put(key, child);
            return child;
        }
        return compound.getCompound(key);
    }

    private NbtHelper() {
    }
}

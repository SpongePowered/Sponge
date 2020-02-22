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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import java.util.function.Supplier;

public class ItemStackHideFlagsValueProvider extends ItemStackDataProvider<Boolean> {

    private final int flag;

    ItemStackHideFlagsValueProvider(Supplier<? extends Key<? extends Value<Boolean>>> key, int flag) {
        super(key);
        this.flag = flag;
    }

    @Override
    protected Optional<Boolean> getFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT tag = dataHolder.getTag();
        if (tag != null && tag.contains(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
            int flag = tag.getInt(Constants.Item.ITEM_HIDE_FLAGS);
            if ((flag & this.flag) != 0) {
                return OptBool.TRUE;
            }
        }
        return OptBool.FALSE;
    }

    @Override
    protected boolean set(ItemStack dataHolder, Boolean value) {
        final CompoundNBT tag = dataHolder.getOrCreateTag();
        if (tag.contains(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
            final int flag = tag.getInt(Constants.Item.ITEM_HIDE_FLAGS);
            if (value) {
                tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, flag | this.flag);
            } else {
                final int flags = flag & ~this.flag;
                if (flags == 0) {
                    tag.remove(Constants.Item.ITEM_HIDE_FLAGS);
                } else {
                    tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, flags);
                }
            }
        } else if (value) {
            tag.putInt(Constants.Item.ITEM_HIDE_FLAGS, this.flag);
        }
        return true;
    }
}

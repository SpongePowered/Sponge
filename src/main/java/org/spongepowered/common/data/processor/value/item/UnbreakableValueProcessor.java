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
package org.spongepowered.common.data.processor.value.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class UnbreakableValueProcessor extends AbstractSpongeValueProcessor<ItemStack, Boolean> {

    public UnbreakableValueProcessor() {
        super(ItemStack.class, Keys.UNBREAKABLE);
    }

    @Override
    public boolean supports(ItemStack container) {
        return container.getItem().isDamageable();
    }

    @Override
    public Value.Mutable<Boolean> constructMutableValue(Boolean defaultValue) {
        return SpongeValueFactory.getInstance().createValue(Keys.UNBREAKABLE, defaultValue);
    }

    @Override
    public boolean set(ItemStack container, Boolean value) {
        if (!container.hasTag()) {
            container.setTag(new NBTTagCompound());
        }
        container.getTag().setBoolean(NbtDataUtil.ITEM_UNBREAKABLE, value);
        return true;
    }

    @Override
    public Optional<Boolean> getVal(ItemStack container) {
        if (container.hasTag() && container.getTag().hasKey(NbtDataUtil.ITEM_UNBREAKABLE)) {
            return Optional.of(container.getTag().getBoolean(NbtDataUtil.ITEM_UNBREAKABLE));
        }
        return Optional.of(false);
    }

    @Override
    public Value.Immutable<Boolean> constructImmutableValue(Boolean value) {
        return constructMutableValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}

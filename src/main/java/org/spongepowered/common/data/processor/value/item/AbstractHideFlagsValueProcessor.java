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
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public abstract class AbstractHideFlagsValueProcessor extends AbstractSpongeValueProcessor<ItemStack, Boolean, Value<Boolean>> {

    private final int flag;

    protected AbstractHideFlagsValueProcessor(Key<Value<Boolean>> key, int flag) {
        super(ItemStack.class, key);
        this.flag = flag;
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return SpongeValueFactory.getInstance().createValue(this.key, actualValue, false);
    }

    @Override
    protected boolean set(ItemStack container, Boolean value) {
        if (!container.func_77942_o()) {
            container.func_77982_d(new CompoundNBT());
        }
        if (container.func_77978_p().func_150297_b(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
            int flag = container.func_77978_p().func_74762_e(Constants.Item.ITEM_HIDE_FLAGS);
            if (value) {
                container.func_77978_p()
                        .func_74768_a(Constants.Item.ITEM_HIDE_FLAGS, flag | this.flag);
            } else {
                container.func_77978_p()
                        .func_74768_a(Constants.Item.ITEM_HIDE_FLAGS,
                                flag & ~this.flag);
            }
        } else {
            if (value) {
                container.func_77978_p().func_74768_a(Constants.Item.ITEM_HIDE_FLAGS, this.flag);
            }
        }
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(ItemStack container) {
        if (container.func_77942_o() && container.func_77978_p().func_150297_b(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
            int flag = container.func_77978_p().func_74762_e(Constants.Item.ITEM_HIDE_FLAGS);
            if ((flag & this.flag) != 0) {
                return OptBool.TRUE;
            }
        }
        return OptBool.FALSE;
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}

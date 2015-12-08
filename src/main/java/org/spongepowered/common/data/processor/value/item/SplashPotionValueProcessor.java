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

import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class SplashPotionValueProcessor extends AbstractSpongeValueProcessor<ItemStack, Boolean, Value<Boolean>> {

    public SplashPotionValueProcessor() {
        super(ItemStack.class, Keys.IS_SPLASH_POTION);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (super.supports(container)) {
            ItemStack stack = (ItemStack) container;
            
            if (this.supports(stack)) {
                ImmutableValue<Boolean> previous = constructImmutableValue((stack.getItemDamage() & 16384) != 0);
                ImmutableValue<Boolean> next = constructImmutableValue(false);
                
                stack.setItemDamage((stack.getItemDamage() ^ 16384) | 8192);
                return DataTransactionResult.successReplaceResult(previous, next);
            }
        }
        
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return new SpongeValue<>(Keys.IS_SPLASH_POTION, actualValue);
    }

    @Override
    protected boolean set(ItemStack container, Boolean value) {
        if (this.supports(container)) {
            if (value) {
                container.setItemDamage((container.getItemDamage() ^ 8192) | 16384);
            } else if (!value) {
                container.setItemDamage((container.getItemDamage() ^ 16384) | 8192);
            }
            return true;
        }
        return false;
    }

    @Override
    protected Optional<Boolean> getVal(ItemStack container) {
        if (this.supports(container)) {
            return Optional.of(ItemPotion.isSplash(container.getMetadata()));
        }
        
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(Keys.IS_SPLASH_POTION, false, value);
    }

    @Override
    protected boolean supports(ItemStack container) {
        return container.getItem().equals(Items.potionitem);
    }
}

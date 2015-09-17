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
package org.spongepowered.common.data.processor.value;

import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import com.google.common.base.Optional;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;

public class WetValueProcessor extends AbstractSpongeValueProcessor<Boolean, Value<Boolean>> {
    protected WetValueProcessor() {
        super(Keys.IS_WET);
    }

    @Override
    public Optional<Boolean> getValueFromContainer(ValueContainer<?> container) {
        if (container.supports(Keys.IS_WET)) {
            if ((container instanceof ItemStack) && ((ItemStack) container).getItem().equals(ItemTypes.SPONGE)) {
                return Optional.of(((ItemStack) container).getTagCompound().getBoolean("wet"));
        	} else if (container instanceof EntityWolf) {
                return Optional.of(((EntityWolf) container).isWet() || ((EntityWolf) container).isWolfWet());
            }
            return Optional.of(container.get(Keys.IS_WET).get());
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityWolf || (container instanceof ItemStack && ((ItemStack) container).getItem().equals(ItemTypes.SPONGE));
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Boolean value) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<Boolean> constructValue(Boolean defaultValue) {
        return new SpongeValue<Boolean>(Keys.IS_WET, false, defaultValue);
    }

}

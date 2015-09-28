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
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import com.google.common.base.Optional;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;

public class WetValueProcessor extends AbstractSpongeValueProcessor<Boolean, Value<Boolean>> {

    public WetValueProcessor() {
        super(Keys.IS_WET);
    }

    @Override
    public Optional<Boolean> getValueFromContainer(ValueContainer<?> container) {
        if (this.supports(container)) {
            if (container instanceof ItemStack) {
                ItemStack stack = (ItemStack) container;
                
                if (stack.getItem().equals(ItemTypes.SPONGE)) {
                    return Optional.of(stack.getItemDamage() == 1);
                }
            } else if (container instanceof EntityWolf) {
                EntityWolf wolf = (EntityWolf) container;
                return Optional.of(wolf.isWet());
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityWolf || (container instanceof ItemStack && ((ItemStack) container).getItem().equals(ItemTypes.SPONGE));
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Boolean value) {
        ImmutableValue<Boolean> proposed = constructValue(value).asImmutable();
        ImmutableValue<Boolean> previous;
        
        if (this.supports(container)) {
            previous = getApiValueFromContainer(container).get().asImmutable();
            
            if (container instanceof ItemStack) {
                ItemStack stack = (ItemStack) container;
                
                if (stack.getItem().equals(ItemTypes.SPONGE)) {
                    stack.setItemDamage(value ? 1 : 0);
                    
                    return DataTransactionBuilder.successReplaceResult(proposed, previous);
                }
            }
        }
        
        return DataTransactionBuilder.failResult(proposed);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
             if (container instanceof ItemStack) {
                ItemStack stack = (ItemStack) container;
                
                if (stack.getItem().equals(ItemTypes.SPONGE)) {
                    stack.setItemDamage(0);
                    return DataTransactionBuilder.successNoData();
                }
    	    }
    	}
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<Boolean> constructValue(Boolean defaultValue) {
        return new SpongeValue<Boolean>(Keys.IS_WET, false, defaultValue);
    }

}

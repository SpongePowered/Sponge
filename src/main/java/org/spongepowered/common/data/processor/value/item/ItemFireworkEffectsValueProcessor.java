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

import com.google.common.collect.ImmutableList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.List;
import java.util.Optional;

public class ItemFireworkEffectsValueProcessor extends AbstractSpongeValueProcessor<ItemStack, List<FireworkEffect>, ListValue<FireworkEffect>> {

    public ItemFireworkEffectsValueProcessor() {
        super(ItemStack.class, Keys.FIREWORK_EFFECTS);

    }

    @Override
    protected boolean supports(ItemStack container) {
        return container.getItem() == Items.firework_charge || container.getItem() == Items.fireworks;
    }

    @Override
    protected ListValue<FireworkEffect> constructValue(List<FireworkEffect> value) {
        return new SpongeListValue<>(Keys.FIREWORK_EFFECTS, value);
    }

    @Override
    protected boolean set(ItemStack item, List<FireworkEffect> value) {
        return FireworkUtils.setFireworkEffects(item, value);
    }

    @Override
    protected Optional<List<FireworkEffect>> getVal(ItemStack item) {
        return FireworkUtils.getFireworkEffects(item);
    }

    @Override
    protected ImmutableValue<List<FireworkEffect>> constructImmutableValue(List<FireworkEffect> value) {
        return new ImmutableSpongeListValue<>(Keys.FIREWORK_EFFECTS, ImmutableList.copyOf(value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if(FireworkUtils.removeFireworkEffects(container)) {
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }
}

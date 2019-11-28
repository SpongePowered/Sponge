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
package org.spongepowered.common.data.processor.data.item;

import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableCookedFishData;
import org.spongepowered.api.data.manipulator.mutable.item.CookedFishData;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.CookedFishes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeCookedFishData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.type.SpongeCookedFish;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class CookedFishDataProcessor extends AbstractItemSingleDataProcessor<CookedFish, Value<CookedFish>, CookedFishData, ImmutableCookedFishData> {

    public CookedFishDataProcessor() {
        super(stack -> stack.getItem().equals(Items.field_179566_aV), Keys.COOKED_FISH);
    }

    @Override
    protected CookedFishData createManipulator() {
        return new SpongeCookedFishData();
    }

    @Override
    protected boolean set(ItemStack itemStack, CookedFish value) {
        itemStack.func_77964_b(((SpongeCookedFish) value).fish.func_150976_a());
        return true;
    }

    @Override
    protected Optional<CookedFish> getVal(ItemStack itemStack) {
        final ItemFishFood.FishType fishType = ItemFishFood.FishType.func_150974_a(itemStack.func_77960_j());
        return SpongeImpl.getRegistry().getType(CookedFish.class, fishType.name());
    }

    @Override
    protected Value<CookedFish> constructValue(CookedFish actualValue) {
        return new SpongeValue<>(Keys.COOKED_FISH, CookedFishes.COD, actualValue);
    }

    @Override
    protected ImmutableValue<CookedFish> constructImmutableValue(CookedFish value) {
        return ImmutableSpongeValue.cachedOf(Keys.COOKED_FISH, CookedFishes.COD, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}

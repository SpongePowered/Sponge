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

import com.google.common.collect.Iterables;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableCookedFishData;
import org.spongepowered.api.data.manipulator.mutable.item.CookedFishData;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.CookedFishes;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeCookedFishData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.type.SpongeCookedFish;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class CookedFishDataProcessor extends AbstractItemSingleDataProcessor<CookedFish, Value<CookedFish>, CookedFishData, ImmutableCookedFishData> {

    public CookedFishDataProcessor() {
        super(stack -> stack.getItem().equals(Items.cooked_fish), Keys.COOKED_FISH);
    }

    @Override
    protected CookedFishData createManipulator() {
        return new SpongeCookedFishData();
    }

    @Override
    protected boolean set(ItemStack itemStack, CookedFish value) {
        itemStack.setItemDamage(((SpongeCookedFish) value).fish.getMetadata());
        return true;
    }

    @Override
    protected Optional<CookedFish> getVal(ItemStack itemStack) {
        return Optional.of(Iterables.get(Sponge.getRegistry().getAllOf(CookedFish.class), itemStack.getMetadata()));
    }

    @Override
    protected ImmutableValue<CookedFish> constructImmutableValue(CookedFish value) {
        return ImmutableSpongeValue.cachedOf(Keys.COOKED_FISH, CookedFishes.COD, value);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}

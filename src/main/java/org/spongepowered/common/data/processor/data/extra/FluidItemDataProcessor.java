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
package org.spongepowered.common.data.processor.data.extra;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.extra.fluid.FluidStackSnapshot;
import org.spongepowered.api.extra.fluid.FluidTypes;
import org.spongepowered.api.extra.fluid.data.manipulator.immutable.ImmutableFluidItemData;
import org.spongepowered.api.extra.fluid.data.manipulator.mutable.FluidItemData;
import org.spongepowered.common.data.manipulator.mutable.extra.SpongeFluidItemData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.extra.fluid.SpongeFluidStackBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshot;

import java.util.Optional;

public class FluidItemDataProcessor extends AbstractItemSingleDataProcessor<FluidStackSnapshot, Value<FluidStackSnapshot>, FluidItemData,
        ImmutableFluidItemData> {

    private static final FluidStackSnapshot WATER = new SpongeFluidStackBuilder().fluid(FluidTypes.WATER).volume(1000).build().createSnapshot();
    private static final FluidStackSnapshot LAVA = new SpongeFluidStackBuilder().fluid(FluidTypes.LAVA).volume(1000).build().createSnapshot();

    public FluidItemDataProcessor() {
        super((item) -> item.func_77973_b() == Items.field_151133_ar || item.func_77973_b() == Items.field_151131_as || item.func_77973_b() == Items.field_151129_at, Keys.FLUID_ITEM_STACK);
    }

    @Override
    protected boolean set(ItemStack dataHolder, FluidStackSnapshot value) {
        // TODO - the API needs to be refactored, as it's no longer possible to change the type of an ItemStack
        return false;
        /*
        FluidType fluidType = value.getFluid();
        if (fluidType == FluidTypes.WATER) {
            dataHolder.setItem(Items.WATER_BUCKET);
            return true;
        } else if (fluidType == FluidTypes.LAVA) {
            dataHolder.setItem(Items.LAVA_BUCKET);
            return true;
        } else {
            return false;
        }*/
    }

    @Override
    protected Optional<FluidStackSnapshot> getVal(ItemStack dataHolder) {
        if (dataHolder.func_77973_b() == Items.field_151131_as) {
            return Optional.of(WATER);
        } else if (dataHolder.func_77973_b() == Items.field_151129_at) {
            return Optional.of(LAVA);
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<FluidStackSnapshot> constructImmutableValue(FluidStackSnapshot value) {
        return new ImmutableSpongeValue<>(Keys.FLUID_ITEM_STACK, SpongeFluidStackSnapshot.DEFAULT, value);
    }

    @Override
    protected Value<FluidStackSnapshot> constructValue(FluidStackSnapshot actualValue) {
        return new SpongeValue<>(Keys.FLUID_ITEM_STACK, SpongeFluidStackSnapshot.DEFAULT, actualValue);
    }

    @Override
    protected FluidItemData createManipulator() {
        return new SpongeFluidItemData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        /*
        if (container instanceof ItemStack) {
            final ItemStack itemStack = (ItemStack) container;
            if (itemStack.getType() == Items.WATER_BUCKET) {
                itemStack.setItem(Items.BUCKET);
                return DataTransactionResult.successRemove(constructImmutableValue(WATER));
            } else if (itemStack.getType() == Items.LAVA_BUCKET) {
                itemStack.setItem(Items.BUCKET);
                return DataTransactionResult.successRemove(constructImmutableValue(LAVA));
            }
        }*/

        // TODO - the API needs to be refactored, as it's no longer possible to change the type of an ItemStack
        return DataTransactionResult.failNoData();
    }
}

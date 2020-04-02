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

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.extra.fluid.SpongeFluidStackBuilder;
import org.spongepowered.common.extra.fluid.SpongeFluidStackSnapshotBuilder;
import org.spongepowered.common.accessor.item.BucketItemAccessor;

import java.util.Optional;

// TODO - setter not possible - the API needs to be refactored, as it's no longer possible to change the type of an ItemStack
public class ItemStackFluidProvider extends ItemStackDataProvider<FluidStackSnapshot> {

    private static final FluidStackSnapshot WATER = new SpongeFluidStackBuilder().fluid(FluidTypes.WATER).volume(1000).build().createSnapshot();
    private static final FluidStackSnapshot LAVA = new SpongeFluidStackBuilder().fluid(FluidTypes.LAVA).volume(1000).build().createSnapshot();

    public ItemStackFluidProvider() {
        super(Keys.FLUID_ITEM_STACK);
    }

    @Override
    protected Optional<FluidStackSnapshot> getFrom(ItemStack dataHolder) {
        if (dataHolder.getItem() instanceof BucketItem) {
            Fluid fluid = ((BucketItemAccessor) dataHolder.getItem()).accessor$getContainedBlock();
            if (fluid == Fluids.EMPTY) {
                return Optional.empty();
            }
            return Optional.of(new SpongeFluidStackSnapshotBuilder().fluid((FluidType) fluid).volume(1000).build());
        }
        return Optional.empty();
    }

    @Override
    protected boolean supports(Item item) {
        return item instanceof BucketItem;
    }
}

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
package org.spongepowered.common.data.processor.data.tileentity;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBrewingStandData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBrewingStandData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.mixin.core.tileentity.TileEntityBrewingStandAccessor;

import java.util.Optional;
import net.minecraft.tileentity.BrewingStandTileEntity;

public class BrewingStandDataProcessor extends
        AbstractTileEntitySingleDataProcessor<BrewingStandTileEntity, Integer, MutableBoundedValue<Integer>, BrewingStandData, ImmutableBrewingStandData> {

    public BrewingStandDataProcessor() {
        super(BrewingStandTileEntity.class, Keys.REMAINING_BREW_TIME);
    }

    @Override
    protected boolean set(final BrewingStandTileEntity entity, final Integer value) {
        if (!((TileEntityBrewingStandAccessor) entity).accessor$canBrew()) {
            return false;
        }

        entity.func_174885_b(0, value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(final BrewingStandTileEntity entity) {
        return Optional.of(((TileEntityBrewingStandAccessor) entity).accessor$canBrew() ? entity.func_174887_a_(0) : 0);
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(final Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.REMAINING_BREW_TIME)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(400)
                .actualValue(actualValue)
                .build();
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected BrewingStandData createManipulator() {
        return new SpongeBrewingStandData();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData(); // cannot be removed
    }

}

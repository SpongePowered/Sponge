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

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class SkullValueProcessor extends AbstractSpongeValueProcessor<SkullType, Value<SkullType>> {

    public SkullValueProcessor() {
        super(Keys.SKULL_TYPE);
    }

    @Override
    public Value<SkullType> constructValue(SkullType defaultValue) {
        return new SpongeValue<SkullType>(Keys.SKULL_TYPE, SkullTypes.SKELETON, defaultValue);
    }

    @Override
    public Optional<SkullType> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof TileEntitySkull) {
            return Optional.of(SkullUtils.getSkullType((TileEntitySkull) container));
        } else if (SkullUtils.isValidItemStack(container)) {
            return Optional.of(SkullUtils.getSkullType((ItemStack) container));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return SkullUtils.supportsObject(container);
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, SkullType value) {
        @SuppressWarnings("unchecked")
        ImmutableValue<SkullType> proposedValue = this.constructValue(value).asImmutable();
        ImmutableValue<SkullType> oldValue;

        DataTransactionBuilder builder = DataTransactionBuilder.builder();

        int skullType = ((SpongeSkullType) value).getByteId();

        if (container instanceof TileEntitySkull) {
            oldValue = getApiValueFromContainer(container).get().asImmutable();
            SkullUtils.setSkullType((TileEntitySkull) container, skullType);
        } else if (SkullUtils.isValidItemStack(container)) {
            oldValue = getApiValueFromContainer(container).get().asImmutable();
            ((ItemStack) container).setItemDamage(skullType);
        } else {
            return DataTransactionBuilder.failResult(proposedValue);
        }

        return builder.success(proposedValue).replace(oldValue).result(DataTransactionResult.Type.SUCCESS).build();

    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}

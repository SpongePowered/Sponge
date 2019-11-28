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

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSkullData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeSkullData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class ItemSkullDataProcessor
        extends AbstractItemSingleDataProcessor<SkullType, Value<SkullType>, SkullData, ImmutableSkullData> {

    public ItemSkullDataProcessor() {
        super(SkullUtils::supportsObject, Keys.SKULL_TYPE);
    }

    @Override
    protected Optional<SkullType> getVal(ItemStack itemStack) {
        return Optional.of(SkullUtils.getSkullType(itemStack.getMetadata()));
    }

    @Override
    public Optional<SkullData> fill(DataContainer container, SkullData skullData) {
        return Optional.of(skullData.set(Keys.SKULL_TYPE, SpongeImpl.getGame().getRegistry()
                .getType(SkullType.class, DataUtil.getData(container, Keys.SKULL_TYPE, String.class)).get()));
    }

    @Override
    protected boolean set(ItemStack itemStack, SkullType type) {
        itemStack.setItemDamage(((SpongeSkullType) type).getByteId());
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<SkullType> constructValue(SkullType defaultValue) {
        return new SpongeValue<>(Keys.SKULL_TYPE, SkullTypes.SKELETON, defaultValue);
    }

    @Override
    protected ImmutableValue<SkullType> constructImmutableValue(SkullType value) {
        return ImmutableSpongeValue.cachedOf(Keys.SKULL_TYPE, SkullTypes.SKELETON, value);
    }

    @Override
    protected SkullData createManipulator() {
        return new SpongeSkullData();
    }

}

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
package org.spongepowered.common.data.manipulator.immutable.item;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableDurabilityData;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeDurabilityData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeDurabilityData extends AbstractImmutableData<ImmutableDurabilityData, DurabilityData> implements ImmutableDurabilityData {

    private final Integer durability;
    private final Boolean unbreakable;

    private final ImmutableBoundedValue<Integer> durabilityValue;
    private final ImmutableValue<Boolean> unbreakableValue;

    public ImmutableSpongeDurabilityData(int defaultDurability, int durability, boolean unbreakable) {
        super(ImmutableDurabilityData.class);
        checkArgument(durability >= 0);
        this.durability = durability;
        this.unbreakable = unbreakable;
        this.durabilityValue = SpongeValueFactory.boundedBuilder(Keys.ITEM_DURABILITY)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(defaultDurability)
                .actualValue(durability)
                .build().asImmutable();
        this.unbreakableValue = ImmutableSpongeValue.cachedOf(Keys.UNBREAKABLE, false, unbreakable);
        this.registerGetters();
    }

    public ImmutableSpongeDurabilityData() {
        this(60, 60, false);
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.ITEM_DURABILITY, () -> this.durability);
        registerKeyValue(Keys.ITEM_DURABILITY, this::durability);

        registerFieldGetter(Keys.UNBREAKABLE, () -> this.unbreakable);
        registerKeyValue(Keys.UNBREAKABLE, this::unbreakable);
    }

    @Override
    public ImmutableBoundedValue<Integer> durability() {
        return this.durabilityValue;
    }

    @Override
    public ImmutableValue<Boolean> unbreakable() {
        return this.unbreakableValue;
    }

    @Override
    public DurabilityData asMutable() {
        return new SpongeDurabilityData(this.durabilityValue.getDefault(), this.durability, this.unbreakable);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.ITEM_DURABILITY, this.durability)
                .set(Keys.UNBREAKABLE, this.unbreakable);
    }
}

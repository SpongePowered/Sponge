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
package org.spongepowered.common.data.manipulator.mutable.item;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableDurabilityData;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeDurabilityData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeDurabilityData extends AbstractData<DurabilityData, ImmutableDurabilityData> implements DurabilityData {

    private int durability;
    private final int defaultDurability;
    private boolean unbreakable;

    public SpongeDurabilityData() {
        this(60, 60, false);
    }

    public SpongeDurabilityData(int defaultDurability, int durability, boolean unbreakable) {
        super(DurabilityData.class);
        checkArgument(durability >= 0);
        this.durability = durability;
        this.defaultDurability = defaultDurability;
        this.unbreakable = unbreakable;
        this.registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.ITEM_DURABILITY, () -> this.durability);
        registerFieldSetter(Keys.ITEM_DURABILITY, this::setDurability);
        registerKeyValue(Keys.ITEM_DURABILITY, this::durability);

        registerFieldGetter(Keys.UNBREAKABLE, () -> this.unbreakable);
        registerFieldSetter(Keys.UNBREAKABLE, value -> this.unbreakable = value);
        registerKeyValue(Keys.UNBREAKABLE, this::unbreakable);
    }

    public void setDurability(int durability) {
        checkArgument(durability >= 0, "Durability cannot be less than zero!");
        this.durability = durability;
    }

    @Override
    public MutableBoundedValue<Integer> durability() {
        return SpongeValueFactory.boundedBuilder(Keys.ITEM_DURABILITY)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(this.defaultDurability)
                .actualValue(this.durability)
                .build();
    }

    @Override
    public Value<Boolean> unbreakable() {
        return SpongeValueFactory.getInstance().createValue(Keys.UNBREAKABLE, this.unbreakable, false);
    }

    @Override
    public DurabilityData copy() {
        return new SpongeDurabilityData(this.defaultDurability, this.durability, this.unbreakable);
    }

    @Override
    public ImmutableDurabilityData asImmutable() {
        return new ImmutableSpongeDurabilityData(this.defaultDurability, this.durability, this.unbreakable);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.ITEM_DURABILITY, this.durability)
                .set(Keys.UNBREAKABLE, this.unbreakable);
    }
}

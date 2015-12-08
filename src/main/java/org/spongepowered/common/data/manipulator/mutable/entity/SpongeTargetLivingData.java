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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetLivingData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetLivingData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTargetLivingData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.mutable.common.SpongeEntityValue;

@ImplementationRequiredForTest
public class SpongeTargetLivingData extends AbstractSingleData<Living, TargetLivingData, ImmutableTargetLivingData> implements TargetLivingData {

    private final Value<Living> value;

    public SpongeTargetLivingData() {
        super(TargetLivingData.class, null, Keys.TARGET);
        this.value = new SpongeEntityValue<>(Keys.TARGET, null);
    }

    public SpongeTargetLivingData(Living target) {
        super(TargetLivingData.class, target, Keys.TARGET);
        this.value = new SpongeEntityValue<>(Keys.TARGET, target);
    }

    @Override
    public Value<Living> getValueGetter() {
        return this.value;
    }

    @Override
    public TargetLivingData copy() {
        return new SpongeTargetLivingData(getValue());
    }

    @Override
    public ImmutableTargetLivingData asImmutable() {
        return new ImmutableSpongeTargetLivingData(getValue());
    }

    @Override
    public int compareTo(TargetLivingData o) {
        return 0;
    }

    @Override
    public Value<Living> target() {
        return this.value;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.TARGET.getQuery(), getValue().getUniqueId());
    }
}

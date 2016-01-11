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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpirableData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpirableData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableIntData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpirableData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class ImmutableSpongeExpirableData extends AbstractImmutableIntData<ImmutableExpirableData, ExpirableData>
    implements ImmutableExpirableData{

    public ImmutableSpongeExpirableData(int value, int upperBound) {
        super(ImmutableExpirableData.class, value, Keys.EXPIRATION_TICKS, SpongeExpirableData.class, 0, upperBound, 0);
    }

    @Override
    public ImmutableBoundedValue<Integer> expireTicks() {
        return SpongeValueFactory.boundedBuilder(Keys.EXPIRATION_TICKS)
            .minimum(0)
            .maximum(this.upperBound)
            .defaultValue(this.defaultValue)
            .actualValue(this.value)
            .build()
            .asImmutable();
    }

    @Override
    public ExpirableData asMutable() {
        return new SpongeExpirableData(this.value, this.upperBound);
    }
}

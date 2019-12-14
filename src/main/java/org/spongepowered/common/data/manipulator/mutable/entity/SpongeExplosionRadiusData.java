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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExplosionRadiusData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExplosionRadiusData;
import org.spongepowered.api.data.value.OptionalValue.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExplosionRadiusData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;

public class SpongeExplosionRadiusData extends AbstractSingleData<Optional<Integer>, ExplosionRadiusData, ImmutableExplosionRadiusData>
        implements ExplosionRadiusData {

    public SpongeExplosionRadiusData(Optional<Integer> explosionRadius) {
        super(ExplosionRadiusData.class, explosionRadius, Keys.EXPLOSION_RADIUS);
    }

    public SpongeExplosionRadiusData() {
        this(Optional.empty());
    }

    @Override
    protected Mutable<Integer> getValueGetter() {
        return this.explosionRadius();
    }

    @Override
    public ExplosionRadiusData copy() {
        return new SpongeExplosionRadiusData(this.getValue());
    }

    @Override
    public ImmutableExplosionRadiusData asImmutable() {
        return new ImmutableSpongeExplosionRadiusData(this.getValue());
    }

    @Override
    public Mutable<Integer> explosionRadius() {
        return new SpongeOptionalValue<>(Keys.EXPLOSION_RADIUS, this.getValue());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int compare(ValueContainer dis, ValueContainer dat) {
        Optional<Integer> value = ((Mutable<Integer>) dis.get(Keys.EXPLOSION_RADIUS).get()).get();
        Optional<Integer> other = ((Mutable<Integer>) dat.get(Keys.EXPLOSION_RADIUS).get()).get();
        if (value.isPresent()) {
            if (other.isPresent()) {
                return value.get() - other.get();
            }
            return 1;
        } else if (other.isPresent()) {
            return -1;
        }
        return 0;
    }

}

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
import org.spongepowered.api.data.manipulator.immutable.ImmutableExplosionRadiusData;
import org.spongepowered.api.data.manipulator.mutable.ExplosionRadiusData;
import org.spongepowered.api.data.value.OptionalValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExplosionRadiusData;
import org.spongepowered.common.data.value.SpongeImmutableOptionalValue;

import java.util.Optional;

public class ImmutableSpongeExplosionRadiusData
        extends AbstractImmutableSingleData<Optional<Integer>, ImmutableExplosionRadiusData, ExplosionRadiusData>
        implements ImmutableExplosionRadiusData {

    private final OptionalValue.Immutable<Integer> value;

    public ImmutableSpongeExplosionRadiusData(Optional<Integer> explosionRadius) {
        super(ImmutableExplosionRadiusData.class, explosionRadius, Keys.EXPLOSION_RADIUS);
        this.value = new SpongeImmutableOptionalValue<>(Keys.EXPLOSION_RADIUS, explosionRadius);
    }

    @Override
    protected OptionalValue.Immutable<Integer> getValueGetter() {
        return explosionRadius();
    }

    @Override
    public ExplosionRadiusData asMutable() {
        return new SpongeExplosionRadiusData(getValue());
    }

    @Override
    public OptionalValue.Immutable<Integer> explosionRadius() {
        return this.value;
    }

}

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
package org.spongepowered.common.data.provider.entity.living;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.provider.GenericMutableBoundedDataProvider;

import java.util.Optional;

public class LivingEntityRemainingAirProvider extends GenericMutableBoundedDataProvider<LivingEntity, Integer> {

    public LivingEntityRemainingAirProvider() {
        super(Keys.REMAINING_AIR.get());
    }

    @Override
    protected BoundedValue<Integer> constructValue(LivingEntity dataHolder, Integer element) {
        return BoundedValue.immutableOf(this.getKey(), element, 0, dataHolder.getMaxAir());
    }

    @Override
    protected Optional<Integer> getFrom(LivingEntity dataHolder) {
        // Air can go down to -20 for the damage counter, so prevent this
        return Optional.of(Math.max(0, dataHolder.getAir()));
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Integer value) {
        final int air = MathHelper.clamp(value, 0, dataHolder.getMaxAir());
        if (air == 0 && dataHolder.getAir() < 0) {
            return true;
        }
        dataHolder.setAir(air);
        return true;
    }
}

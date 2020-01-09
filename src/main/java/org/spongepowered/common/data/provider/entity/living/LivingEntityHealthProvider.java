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
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.bridge.entity.LivingEntityBridge;
import org.spongepowered.common.data.provider.GenericMutableBoundedDataProvider;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;

import java.util.Optional;

public class LivingEntityHealthProvider extends GenericMutableBoundedDataProvider<LivingEntity, Double> {

    public LivingEntityHealthProvider() {
        super(Keys.HEALTH.get());
    }

    @Override
    protected BoundedValue<Double> constructValue(LivingEntity dataHolder, Double element) {
        return BoundedValue.immutableOf(this.getKey(), element, 0.0, (double) dataHolder.getMaxHealth());
    }

    @Override
    protected Optional<Double> getFrom(LivingEntity dataHolder) {
        return Optional.of((double) dataHolder.getHealth());
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Double value) {
        final double maxHealth = value;
        // Check bounds
        if (value > maxHealth || value < 0) {
            return false;
        }

        dataHolder.setHealth(value.floatValue());
        if (value == 0) {
            dataHolder.attackEntityFrom(DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE, 1000F);
        } else {
            ((LivingEntityBridge) dataHolder).bridge$resetDeathEventsPosted();
        }
        return true;
    }
}

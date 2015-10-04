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
package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeIgniteableData;

import java.util.Optional;

public class IgniteableDataBuilder implements DataManipulatorBuilder<IgniteableData, ImmutableIgniteableData> {

    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof Entity;
    }

    @Override
    public IgniteableData create() {
        return new SpongeIgniteableData();
    }

    @Override
    public Optional<IgniteableData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            final int fireTicks = ((Entity) dataHolder).fire;
            final int fireDelay = ((Entity) dataHolder).fireResistance;
            if (fireTicks > 0) {
                return Optional.<IgniteableData>of(new SpongeIgniteableData(fireTicks, fireDelay));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<IgniteableData> build(DataView container) throws InvalidDataException {
        int delay = getData(container, Keys.FIRE_DAMAGE_DELAY);
        int ticks = getData(container, Keys.FIRE_TICKS);
        IgniteableData igniteableData = new SpongeIgniteableData(ticks, delay);
        return Optional.of(igniteableData);
    }
}

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

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.data.processor.data.entity.HorseDataProcessor;

import java.util.Optional;

public class HorseDataBuilder implements DataManipulatorBuilder<HorseData, ImmutableHorseData> {

    private HorseDataProcessor processor;

    public HorseDataBuilder(HorseDataProcessor processor) {
        this.processor = processor;
    }

    @Override
    public HorseData create() {
        return new SpongeHorseData();
    }

    @Override
    public Optional<HorseData> createFrom(DataHolder dataHolder) {
        return this.processor.createFrom(dataHolder);
    }

    @Override
    public Optional<HorseData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.HORSE_COLOR.getQuery()) && container.contains(Keys.HORSE_STYLE.getQuery()) && container.contains(Keys.HORSE_VARIANT.getQuery())) {
            return Optional.<HorseData>of(new SpongeHorseData(HorseUtils.getHorseColor(container), HorseUtils.getHorseStyle(container),
                    HorseUtils.getHorseVariant(container)));
        }
        return Optional.empty();
    }
}

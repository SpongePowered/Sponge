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
import net.minecraft.entity.passive.EntitySheep;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableShearedData;
import org.spongepowered.api.data.manipulator.mutable.entity.ShearedData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeShearedData;

import java.util.Optional;

public class ShearedDataBuilder implements DataManipulatorBuilder<ShearedData, ImmutableShearedData> {

    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntitySheep;
    }

    @Override
    public ShearedData create() {
        return new SpongeShearedData();
    }

    @Override
    public Optional<ShearedData> createFrom(DataHolder dataHolder) {
        final boolean isSheared;
        if(supports(dataHolder)) {
            isSheared = ((EntitySheep) dataHolder).getSheared();
            return Optional.of(new SpongeShearedData(isSheared));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ShearedData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.IS_SHEARED.getQuery())) {
            ShearedData shearedData = create();
            shearedData.set(Keys.IS_SHEARED, getData(container, Keys.IS_SHEARED));
            return Optional.of(shearedData);
        }
        return Optional.empty();
    }
}

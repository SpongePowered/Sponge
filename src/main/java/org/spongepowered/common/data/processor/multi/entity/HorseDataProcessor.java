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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.entity.SpongeHorseStyle;
import org.spongepowered.common.registry.type.entity.HorseColorRegistryModule;
import org.spongepowered.common.registry.type.entity.HorseStyleRegistryModule;

import java.util.Map;
import java.util.Optional;

public class HorseDataProcessor extends AbstractEntityDataProcessor<EntityHorse, HorseData, ImmutableHorseData> {

    public HorseDataProcessor() {
        super(EntityHorse.class);
    }

    @Override
    protected HorseData createManipulator() {
        return new SpongeHorseData();
    }

    @Override
    protected boolean doesDataExist(EntityHorse entity) {
        return true;
    }

    @Override
    protected boolean set(EntityHorse entity, Map<Key<?>, Object> keyValues) {
        SpongeHorseColor horseColor = (SpongeHorseColor) keyValues.get(Keys.HORSE_COLOR);
        SpongeHorseStyle horseStyle = (SpongeHorseStyle) keyValues.get(Keys.HORSE_STYLE);

        int variant = horseColor.getBitMask() | horseStyle.getBitMask();

        entity.func_110235_q(variant);

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityHorse entity) {
        return ImmutableMap.<Key<?>, Object>of(
                Keys.HORSE_COLOR, HorseColorRegistryModule.getHorseColor(entity),
                Keys.HORSE_STYLE, HorseStyleRegistryModule.getHorseStyle(entity)
        );
    }

    @Override
    public Optional<HorseData> fill(DataContainer container, HorseData horseData) {

        horseData.set(Keys.HORSE_COLOR, HorseColorRegistryModule.getHorseColor(container));
        horseData.set(Keys.HORSE_STYLE, HorseStyleRegistryModule.getHorseStyle(container));

        return Optional.of(horseData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}

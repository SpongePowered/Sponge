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

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgeableData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgeableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgeableData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.AgeableEntity;

public class AgeableDataProcessor extends AbstractEntityDataProcessor<AgeableEntity, AgeableData, ImmutableAgeableData> {

    public AgeableDataProcessor() {
        super(AgeableEntity.class);
    }

    @Override
    protected AgeableData createManipulator() {
        return new SpongeAgeableData(Constants.Entity.Ageable.ADULT, true);
    }

    @Override
    protected boolean doesDataExist(AgeableEntity entity) {
        return true;
    }

    @Override
    protected boolean set(AgeableEntity entity, Map<Key<?>, Object> keyValues) {
        Integer age = (Integer) keyValues.get(Keys.AGE);
        boolean adult = (Boolean) keyValues.get(Keys.IS_ADULT);

        if (age != null) {
            entity.setGrowingAge(age);
            return true;
        }

        if (adult) {
            entity.setGrowingAge(Constants.Entity.Ageable.ADULT);
        } else {
            entity.setGrowingAge(Constants.Entity.Ageable.CHILD);
        }
        return true;

    }

    @Override
    protected Map<Key<?>, ?> getValues(AgeableEntity entity) {
        return ImmutableMap.<Key<?>, Object>of(Keys.AGE, entity.getIdleTime(), Keys.IS_ADULT, !entity.isChild());
    }

    @Override
    public Optional<AgeableData> fill(DataContainer container, AgeableData ageableData) {
        if (!container.contains(Keys.AGE.getQuery()) || !container.contains(Keys.IS_ADULT.getQuery())) {
            return Optional.empty();
        }
        ageableData.set(Keys.AGE, getData(container, Keys.AGE));
        ageableData.set(Keys.IS_ADULT, getData(container, Keys.IS_ADULT));
        return Optional.of(ageableData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}

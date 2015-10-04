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
package org.spongepowered.common.data.processor.data.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableScreamingData;
import org.spongepowered.api.data.manipulator.mutable.entity.ScreamingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeScreamingData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.util.DataUtil;

import java.util.Map;
import java.util.Optional;

public class ScreamingDataProcessor extends AbstractEntityDataProcessor<EntityEnderman, ScreamingData, ImmutableScreamingData> {

    public ScreamingDataProcessor() {
        super(EntityEnderman.class);
    }

    @Override
    protected ScreamingData createManipulator() {
        return new SpongeScreamingData(false);
    }

    @Override
    protected boolean doesDataExist(EntityEnderman entity) {
        return true;
    }

    @Override
    protected boolean set(EntityEnderman entity, Map<Key<?>, Object> keyValues) {
        entity.setScreaming((Boolean) keyValues.get(Keys.IS_SCREAMING));
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityEnderman entity) {
        return ImmutableMap.<Key<?>, Object>of(Keys.IS_SCREAMING, entity.isScreaming());
    }

    @Override
    public Optional<ScreamingData> fill(DataContainer container, ScreamingData screamingData) {
        screamingData.set(Keys.IS_SCREAMING, DataUtil.getData(container, Keys.IS_SCREAMING));
        return Optional.of(screamingData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
    }
}

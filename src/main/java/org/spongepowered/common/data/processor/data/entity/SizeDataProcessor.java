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

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSizeData;
import org.spongepowered.api.data.manipulator.mutable.entity.SizeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSizeData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Map;
import java.util.Optional;

public class SizeDataProcessor extends AbstractEntityDataProcessor<Entity, SizeData, ImmutableSizeData> {

    public SizeDataProcessor() {
        super(Entity.class);
    }

    @Override
    public Optional<SizeData> fill(DataContainer container, SizeData sizeData) {
        sizeData.set(Keys.BASE_SIZE, getData(container, Keys.BASE_SIZE));
        sizeData.set(Keys.HEIGHT, getData(container, Keys.HEIGHT));
        sizeData.set(Keys.SCALE, getData(container, Keys.SCALE));
        return Optional.of(sizeData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected SizeData createManipulator() {
        return new SpongeSizeData();
    }

    @Override
    protected boolean doesDataExist(Entity entity) {
        return true;
    }

    @Override
    protected boolean set(Entity entity, Map<Key<?>, Object> keyValues) {
        float width = (Float) keyValues.get(Keys.BASE_SIZE);
        float height = (Float) keyValues.get(Keys.HEIGHT);
        float scale = (Float) keyValues.get(Keys.SCALE);
        IMixinEntity mixinEntity = (IMixinEntity) entity;
        mixinEntity.setSpongeSize(width, height, scale);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(Entity entity) {
        final float base = entity.width;
        final float height = entity.height;
        return ImmutableMap.<Key<?>, Object>of(Keys.BASE_SIZE, base, Keys.HEIGHT, height, Keys.SCALE, 1f);
    }

}

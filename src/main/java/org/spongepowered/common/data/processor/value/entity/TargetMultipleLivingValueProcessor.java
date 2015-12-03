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
package org.spongepowered.common.data.processor.value.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TargetMultipleLivingValueProcessor extends AbstractSpongeValueProcessor<EntityWither, List<Living>, ImmutableListValue<Living>>  {

    public TargetMultipleLivingValueProcessor() {
        super(EntityWither.class, Keys.TARGETS);
    }

    private static int MAX_TARGET_INDEX = 3;

    @Override
    protected ImmutableListValue<Living> constructValue(List<Living> defaultValue) {
        return new ImmutableSpongeListValue<>(Keys.TARGETS, ImmutableList.copyOf(defaultValue));
    }

    @Override
    protected boolean set(EntityWither container, List<Living> value) {
        boolean hasSet = false;
        for (int i = 0; i < MAX_TARGET_INDEX; i++) {
            container.updateWatchedTargetId(i, value.size() > i ? ((EntityLivingBase) value.get(i)).getEntityId() : 0);
            hasSet = true;
        }
        return hasSet;
    }

    @Override
    protected Optional<List<Living>> getVal(EntityWither container) {
        List<Living> values = new ArrayList<>();
        for (int i = 0; i < MAX_TARGET_INDEX; i++) {
            int id = container.getWatchedTargetId(i);
            if (id > 0) {
                values.add((Living) container.getEntityWorld().getEntityByID(id));
            }
        }
        return Optional.of(values);
    }

    @Override
    protected ImmutableValue<List<Living>> constructImmutableValue(List<Living> value) {
        return constructValue(value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}

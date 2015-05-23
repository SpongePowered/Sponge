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
package org.spongepowered.common.data.processor.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityAgeable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.AgeableData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.entity.SpongeAgeableData;

public class SpongeAgeableDataProcessor implements SpongeDataProcessor<AgeableData> {

    @Override
    public Optional<AgeableData> getFrom(DataHolder dataHolder) {
        if (!(checkNotNull(dataHolder) instanceof EntityAgeable)) {
            return Optional.absent();
        }
        final int growth = ((EntityAgeable) dataHolder).getGrowingAge();
        return Optional.of(create().setValue(growth));
    }

    @Override
    public Optional<AgeableData> fillData(DataHolder dataHolder, AgeableData manipulator, DataPriority priority) {
        if (!(checkNotNull(dataHolder) instanceof EntityAgeable)) {
            return Optional.of(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
            {
                final int growth = ((EntityAgeable) dataHolder).getGrowingAge();
                return Optional.of(manipulator.setAge(growth));
            }
            default:
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, AgeableData manipulator, DataPriority priority) {
        if (!(checkNotNull(dataHolder) instanceof EntityAgeable)) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                return successNoData();
            default:
                final AgeableData previous = getFrom(dataHolder).get();
                final DataTransactionBuilder builder = builder().replace(previous);
                ((EntityAgeable) dataHolder).setGrowingAge(manipulator.getValue());
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<AgeableData> build(DataView container) throws InvalidDataException {
        checkDataExists(container, SpongeAgeableData.AGE);
        final int age = container.getInt(SpongeAgeableData.AGE).get();
        return Optional.of(create().setValue(age));
    }

    @Override
    public AgeableData create() {
        return new SpongeAgeableData();
    }

    @Override
    public Optional<AgeableData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityAgeable)) {
            return Optional.absent();
        }
        final int age = ((EntityAgeable) dataHolder).getGrowingAge();
        return Optional.of(create().setValue(age));
    }
}

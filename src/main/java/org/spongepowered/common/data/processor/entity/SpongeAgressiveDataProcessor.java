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
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.AggressiveData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.entity.SpongeAggressiveData;
import org.spongepowered.common.interfaces.entity.IMixinAggressive;

public class SpongeAgressiveDataProcessor implements SpongeDataProcessor<AggressiveData> {

    @Override
    public Optional<AggressiveData> getFrom(DataHolder dataHolder) {
        if (checkNotNull(dataHolder) instanceof IMixinAggressive) {
            final boolean isAngry = ((IMixinAggressive) dataHolder).isAngry();
            return isAngry ? Optional.of(create()) : Optional.<AggressiveData>absent();
        }
        return Optional.absent();
    }

    @Override
    public Optional<AggressiveData> fillData(DataHolder dataHolder, AggressiveData manipulator, DataPriority priority) {
        checkNotNull(manipulator);
        if (dataHolder instanceof IMixinAggressive) {
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                case PRE_MERGE:
                    final boolean isAngry = ((IMixinAggressive) dataHolder).isAngry();
                    return isAngry ? Optional.of(manipulator) : Optional.<AggressiveData>absent();
                default:
                    return Optional.of(manipulator);
            }
        } else {
            return Optional.absent();
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, AggressiveData manipulator, DataPriority priority) {
        checkNotNull(manipulator);
        if (checkNotNull(dataHolder) instanceof IMixinAggressive) {
            switch (checkNotNull(priority)) {
                case DATA_MANIPULATOR:
                case POST_MERGE:
                    ((IMixinAggressive) dataHolder).setAngry(true);
                    return successNoData(); // todo
                default:
                    return successNoData(); // todo
            }
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof IMixinAggressive)) {
            return false;
        }
        ((IMixinAggressive) dataHolder).setAngry(false);
        return true;
    }

    @Override
    public Optional<AggressiveData> build(DataView container) throws InvalidDataException {
        checkDataExists(container, SpongeAggressiveData.AGGRESSIVE);
        final boolean aiEnabled = container.getBoolean(SpongeAggressiveData.AGGRESSIVE).get();
        if (aiEnabled) {
            return Optional.of(create());
        }
        return Optional.absent();
    }

    @Override
    public AggressiveData create() {
        return new SpongeAggressiveData();
    }

    @Override
    public Optional<AggressiveData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof IMixinAggressive)) {
            return Optional.absent();
        }
        // we create it regardless whether the entity is angry.
        return Optional.of(create());
    }
}

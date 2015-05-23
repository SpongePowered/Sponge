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

import com.google.common.base.Optional;
import net.minecraft.entity.monster.EntityCreeper;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.ChargedData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.entity.SpongeChargedData;
import org.spongepowered.common.data.util.DataUtil;

public class SpongeChargedDataProcessor implements SpongeDataProcessor<ChargedData> {

    @Override
    public Optional<ChargedData> getFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityCreeper)) {
            return Optional.absent();
        }
        return ((EntityCreeper) dataHolder).getPowered() ? Optional.of(create()) : Optional.<ChargedData>absent();
    }

    @Override
    public Optional<ChargedData> fillData(DataHolder dataHolder, ChargedData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof EntityCreeper)) {
            return Optional.absent();
        }
        return Optional.of(checkNotNull(manipulator));
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, ChargedData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof EntityCreeper)) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                ((EntityCreeper) dataHolder).getDataWatcher().updateObject(17, 1);
                return successNoData();
            default:
                return successNoData();
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityCreeper)) {
            return false;
        }
        ((EntityCreeper) dataHolder).getDataWatcher().updateObject(17, 0);
        return true;
    }

    @Override
    public Optional<ChargedData> build(DataView container) throws InvalidDataException {
        final boolean charged = DataUtil.getData(container, SpongeChargedData.CHARGED, Boolean.TYPE);
        return charged ? Optional.of(create()) : Optional.<ChargedData>absent();
    }

    @Override
    public ChargedData create() {
        return new SpongeChargedData();
    }

    @Override
    public Optional<ChargedData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityCreeper)) {
            return Optional.absent();
        }
        return ((EntityCreeper) dataHolder).getPowered() ? Optional.of(create()) : Optional.<ChargedData>absent();
    }
}

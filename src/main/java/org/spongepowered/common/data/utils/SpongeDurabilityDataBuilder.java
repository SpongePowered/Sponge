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
package org.spongepowered.common.data.utils;

import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.items.DurabilityData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.manipulators.items.SpongeDurabilityData;

public class SpongeDurabilityDataBuilder implements SpongeDataUtil<DurabilityData> {

    @Override
    public Optional<DurabilityData> build(DataView container) throws InvalidDataException {
        return null;
    }

    @Override
    public DurabilityData create() {
        return new SpongeDurabilityData();
    }

    @Override
    public Optional<DurabilityData> createFrom(DataHolder dataHolder) {
        final SpongeDurabilityData durabilityData = new SpongeDurabilityData();

        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder, DurabilityData manipulator) {
        return fail(manipulator);
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, DurabilityData manipulator, DataPriority priority) {
        return fail(manipulator);
    }
}

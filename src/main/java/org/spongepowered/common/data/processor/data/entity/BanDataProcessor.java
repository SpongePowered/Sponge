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

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBanData;
import org.spongepowered.api.data.manipulator.mutable.entity.BanData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBanData;

import java.util.Set;

public class BanDataProcessor implements DataProcessor<BanData, ImmutableBanData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof User;
    }

    @Override
    public Optional<BanData> from(DataHolder dataHolder) {
        if (dataHolder instanceof User) {
            final SpongeBanData healthData = new SpongeBanData(
                    (Set<Ban.User>) Sponge.getGame().getServiceManager().provide(BanService.class).get()
                            .getBansFor((User) dataHolder));
            return Optional.<BanData>of(healthData).or(Optional.<SpongeBanData>absent());
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<BanData> fill(DataHolder dataHolder, BanData manipulator) {
        return Optional.absent();
    }

    @Override
    public Optional<BanData> fill(DataHolder dataHolder, BanData manipulator, MergeFunction overlap) {
        return Optional.absent();
    }

    @Override
    public Optional<BanData> fill(DataContainer container, BanData banData) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, BanData manipulator) {
        return null;
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, BanData manipulator, MergeFunction function) {
        return null;
    }

    @Override
    public Optional<ImmutableBanData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableBanData immutable) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return null;
    }

    @Override
    public BanData create() {
        return new SpongeBanData();
    }

    @Override
    public ImmutableBanData createImmutable() {
        return create().asImmutable();
    }

    @Override
    public Optional<BanData> createFrom(DataHolder dataHolder) {
        return from(dataHolder);
    }

    @Override
    public Optional<BanData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }
}

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

import static com.google.common.base.Preconditions.checkNotNull;

import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.entity.passive.EntityTameable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTameableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public class TameableDataProcessor extends AbstractSpongeDataProcessor<TameableData, ImmutableTameableData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityTameable;
    }

    @Override
    public Optional<TameableData> from(DataHolder dataHolder) {
        if (dataHolder instanceof EntityTameable) {
            final Optional<UUID> uuidOptional = getTamer(((EntityTameable) dataHolder));
            return Optional.<TameableData>of(new SpongeTameableData(uuidOptional.orElse(null)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TameableData> fill(DataHolder dataHolder, TameableData manipulator, MergeFunction overlap) {
        if (dataHolder instanceof EntityTameable) {
            final TameableData
                merged =
                checkNotNull(overlap, "MergeFunction cannot be null!").merge(checkNotNull(manipulator).copy(), from(dataHolder).orElse(null));
            manipulator.set(Keys.TAMED_OWNER, merged.owner().get());
            return Optional.of(manipulator);
        }
        return Optional.empty();
    }

    @Override
    public Optional<TameableData> fill(final DataContainer container, final TameableData tameableData) {
        if (!container.contains(Keys.TAMED_OWNER.getQuery())) {
            return Optional.empty();
        }
        final String uuid = container.getString(Keys.TAMED_OWNER.getQuery()).get();
        if (uuid.equals("none")) {
            return Optional.of(tameableData);
        } else {
            final UUID ownerUUID = UUID.fromString(uuid);
            return Optional.of(tameableData.set(Keys.TAMED_OWNER, Optional.of(ownerUUID)));
        }
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, TameableData manipulator, MergeFunction overlap) {
        if (dataHolder instanceof EntityTameable) {
            final EntityTameable entityTameable = (EntityTameable) dataHolder;
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final String sPrevTamer = entityTameable.getOwnerId();
            final Optional<UUID> prevTamer = TameableDataProcessor.asUUID(sPrevTamer);
            final TameableData
                newdata =
                checkNotNull(overlap, "MergeFunction must not be null").merge(from(dataHolder).orElse(null), checkNotNull(manipulator));
            final ImmutableSpongeOptionalValue<UUID> prevValue = ImmutableSpongeTameableData.createValue(prevTamer);
            final ImmutableValue<Optional<UUID>> newValue = newdata.owner().asImmutable();
            try {
                builder.replace(prevValue);
                entityTameable.setOwnerId(asString(newdata.owner().get()));
                builder.success(newValue)
                    .result(DataTransactionResult.Type.SUCCESS);
                return builder.build();
            } catch (Exception e) {
                entityTameable.setOwnerId(sPrevTamer);
                builder.reject(newValue)
                    .result(DataTransactionResult.Type.ERROR);
                return builder.build();
            }
        }
        return DataTransactionResult.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableTameableData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableTameableData immutable) {
        //TODO: Health returns empty, investigate solutions.
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        //Fail to remove data, at this stage untameable tameables are not supported.
        return DataTransactionResult.builder().result(DataTransactionResult.Type.FAILURE).build();
    }

    @Override
    public Optional<TameableData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityTameable) {
            final Optional<UUID> uuidOptional = getTamer((EntityTameable) dataHolder);
            return Optional.<TameableData>of(new SpongeTameableData(uuidOptional.orElse(null)));
        }
        return Optional.empty();
    }

    public static String asString(final Optional<UUID> uuidOptional) {
        if (uuidOptional.isPresent()) {
            return UUIDTypeAdapter.fromUUID(uuidOptional.get());
        }
        return "";
    }

    public static Optional<UUID> getTamer(@Nullable final EntityTameable tameable) {
        if (tameable == null) {
            return Optional.empty();
        }
        return asUUID(tameable.getOwnerId());
    }

    public static Optional<UUID> asUUID(@Nullable final String sUUID) {
        if (sUUID == null) {
            return Optional.empty();
        }
        @Nullable UUID uuid;
        try {
            uuid = UUIDTypeAdapter.fromString(sUUID);
        } catch (final RuntimeException ignored) {
            uuid = null;
        }
        return Optional.ofNullable(uuid);
    }
}

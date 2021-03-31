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
package org.spongepowered.common.data.provider;

import io.leangen.geantyref.GenericTypeReflector;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

public class CustomDataProvider<V extends Value<E>, E> extends MutableDataProvider<V, E> {

    private final Set<Type> supportedTokens;

    public CustomDataProvider(final Key<V> key, final Set<Type> supportedTokens) {
        super(key);
        this.supportedTokens = supportedTokens;
    }

    @Override
    public Optional<E> get(DataHolder dataHolder) {
        if (this.isSupported(dataHolder)) {
            final SpongeDataHolderBridge customDataHolder = CustomDataProvider.getCustomDataHolder(dataHolder);
            return customDataHolder.bridge$get(this.key());
        }
        return Optional.empty();
    }

    private static SpongeDataHolderBridge getCustomDataHolder(DataHolder dataHolder) {
        final SpongeDataHolderBridge customDataHolder;
        if (dataHolder instanceof ServerLocation) {
            customDataHolder = (SpongeDataHolderBridge) ((ServerLocation) dataHolder).blockEntity().get();
        } else {
            customDataHolder = (SpongeDataHolderBridge) dataHolder;
        }
        return customDataHolder;
    }

    @Override
    public boolean isSupported(DataHolder dataHolder) {
        if (dataHolder instanceof ServerLocation) {
            if (!((ServerLocation) dataHolder).hasBlockEntity()) {
                return false;
            }
            for (final Type type : this.supportedTokens) {
                if (GenericTypeReflector.erase(type).isAssignableFrom(BlockEntity.class)) {
                    return true;
                }
            }
            return false;
        }
        if (!(dataHolder instanceof SpongeDataHolderBridge)) {
            return false;
        }
        for (final Type type : this.supportedTokens) {
            if (GenericTypeReflector.erase(type).isAssignableFrom(dataHolder.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSupported(final Type dataHolder) {
        if (!SpongeDataHolderBridge.class.isAssignableFrom(GenericTypeReflector.erase(dataHolder))) {
            return true;
        }
        for (final Type token : this.supportedTokens) {
            if (GenericTypeReflector.isSuperType(token, dataHolder)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataTransactionResult offer(DataHolder.Mutable dataHolder, E element) {
        if (this.isSupported(dataHolder)) {
            return CustomDataProvider.getCustomDataHolder(dataHolder).bridge$offer(this.key(),  element);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(DataHolder.Mutable dataHolder) {
        if (this.isSupported(dataHolder)) {
            return CustomDataProvider.getCustomDataHolder(dataHolder).bridge$remove(this.key());
        }
        return DataTransactionResult.failNoData();
    }
}

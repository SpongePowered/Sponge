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

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;

import java.util.Optional;
import java.util.Set;

public class CustomDataProvider<V extends Value<E>, E> extends MutableDataProvider<V, E> {

    private final Set<TypeToken<? extends DataHolder>> supportedTokens;

    public CustomDataProvider(Key<V> key, Set<TypeToken<? extends DataHolder>> supportedTokens) {
        super(key);
        this.supportedTokens = supportedTokens;
    }

    @Override
    public Optional<E> get(DataHolder dataHolder) {
        if (this.isSupported(dataHolder)) {
            final CustomDataHolderBridge customDataHolder = CustomDataProvider.getCustomDataHolder(dataHolder);
            return customDataHolder.bridge$getCustom(this.getKey());
        }
        return Optional.empty();
    }

    private static CustomDataHolderBridge getCustomDataHolder(DataHolder dataHolder) {
        final CustomDataHolderBridge customDataHolder;
        if (dataHolder instanceof ServerLocation) {
            customDataHolder = (CustomDataHolderBridge) ((ServerLocation) dataHolder).getBlockEntity().get();
        } else {
            customDataHolder = (CustomDataHolderBridge) dataHolder;
        }
        return customDataHolder;
    }

    @Override
    public boolean isSupported(DataHolder dataHolder) {
        if (dataHolder instanceof ServerLocation) {
            if (!((ServerLocation) dataHolder).hasBlockEntity()) {
                return false;
            }
            for (TypeToken<? extends DataHolder> token : this.supportedTokens) {
                if (token.getRawType().isAssignableFrom(BlockEntity.class)) {
                    return true;
                }
            }
            return false;
        }
        if (!(dataHolder instanceof CustomDataHolderBridge)) {
            return false;
        }
        for (TypeToken<? extends DataHolder> token : this.supportedTokens) {
            if (token.getRawType().isAssignableFrom(dataHolder.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSupported(TypeToken<? extends DataHolder> dataHolder) {
        if (!CustomDataHolderBridge.class.isAssignableFrom(dataHolder.getRawType())) {
            return true;
        }
        for (TypeToken<? extends DataHolder> token : this.supportedTokens) {
            if (token.isSupertypeOf(dataHolder)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataTransactionResult offer(DataHolder.Mutable dataHolder, E element) {
        if (this.isSupported(dataHolder)) {
            return CustomDataProvider.getCustomDataHolder(dataHolder).bridge$offerCustom(this.getKey(),  element);
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    public DataTransactionResult remove(DataHolder.Mutable dataHolder) {
        if (this.isSupported(dataHolder)) {
            return CustomDataProvider.getCustomDataHolder(dataHolder).bridge$removeCustom(this.getKey());
        }
        return DataTransactionResult.failNoData();
    }
}

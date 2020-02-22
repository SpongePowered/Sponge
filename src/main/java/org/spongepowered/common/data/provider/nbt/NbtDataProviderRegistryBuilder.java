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
package org.spongepowered.common.data.provider.nbt;

import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class NbtDataProviderRegistryBuilder extends DataProviderRegistryBuilder {

    public NbtDataProviderRegistryBuilder(DataProviderRegistry registry) {
        super(registry);
    }

    protected <E> void register(NbtDataType dataType, Supplier<? extends Key<? extends Value<E>>> key,
            Function<CompoundNBT, E> getter) {
        this.register(dataType, key.get(), getter);
    }

    protected <E> void register(NbtDataType dataType, Key<? extends Value<E>> key,
            Function<CompoundNBT, E> getter) {
        final Function<CompoundNBT, Optional<E>> optionalGetter = toOptionalGetter(key, getter);
        this.register(new NbtDataProvider<E>(key, dataType) {

            @Override
            protected Optional<E> getFrom(CompoundNBT dataHolder) {
                return optionalGetter.apply(dataHolder);
            }
        });
    }

    protected <E> void register(NbtDataType dataType, Supplier<? extends Key<? extends Value<E>>> key,
            Function<CompoundNBT, E> getter, BiConsumer<CompoundNBT, E> setter) {
        this.register(dataType, key.get(), getter, setter);
    }

    protected <E> void register(NbtDataType dataType, Key<? extends Value<E>> key,
            Function<CompoundNBT, E> getter, BiConsumer<CompoundNBT, E> setter) {
        final Function<CompoundNBT, Optional<E>> optionalGetter = toOptionalGetter(key, getter);
        this.register(new NbtDataProvider<E>(key, dataType) {

            @Override
            protected Optional<E> getFrom(CompoundNBT dataHolder) {
                return optionalGetter.apply(dataHolder);
            }

            @Override
            protected boolean set(CompoundNBT dataHolder, E value) {
                setter.accept(dataHolder, value);
                return true;
            }
        });
    }
}

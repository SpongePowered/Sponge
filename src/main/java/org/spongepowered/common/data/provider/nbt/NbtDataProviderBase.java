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
import org.spongepowered.common.data.holder.nbt.NbtCompoundDataHolder;
import org.spongepowered.common.data.provider.GenericMutableDataProviderBase;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class NbtDataProviderBase<V extends Value<E>, E> extends GenericMutableDataProviderBase<NbtCompoundDataHolder, V, E> {

    private final Predicate<NbtDataType> dataTypePredicate;

    public NbtDataProviderBase(Supplier<? extends Key<V>> key, NbtDataType dataType) {
        this(key, type -> type == dataType);
    }

    public NbtDataProviderBase(Key<V> key, NbtDataType dataType) {
        this(key, type -> type == dataType);
    }

    public NbtDataProviderBase(Supplier<? extends Key<V>> key, Predicate<NbtDataType> dataTypePredicate) {
        this(key.get(), dataTypePredicate);
    }

    public NbtDataProviderBase(Key<V> key, Predicate<NbtDataType> dataTypePredicate) {
        super(key, NbtCompoundDataHolder.class);
        this.dataTypePredicate = dataTypePredicate;
    }

    /**
     * Gets whether the given {@link NbtDataType} is supported.
     *
     * @param type The nbt data type
     * @return Whether it's supported
     */
    public final boolean supports(NbtDataType type) {
        return this.dataTypePredicate.test(type);
    }

    @Override
    protected final boolean supports(NbtCompoundDataHolder dataHolder) {
        return this.supports(dataHolder.getNbtDataType()) && this.supports(dataHolder.getNbtCompound());
    }

    protected boolean supports(CompoundNBT compound) {
        return true;
    }

    @Override
    protected final Optional<E> getFrom(NbtCompoundDataHolder dataHolder) {
        return this.getFrom(dataHolder.getNbtCompound());
    }

    protected abstract Optional<E> getFrom(CompoundNBT compound);

    @Override
    protected final boolean set(NbtCompoundDataHolder dataHolder, E value) {
        return this.set(dataHolder.getNbtCompound(), value);
    }

    protected boolean set(CompoundNBT compound, E value) {
        return false;
    }

    @Override
    protected final boolean delete(NbtCompoundDataHolder dataHolder) {
        return this.delete(dataHolder.getNbtCompound());
    }

    protected boolean delete(CompoundNBT compound) {
        return false;
    }
}

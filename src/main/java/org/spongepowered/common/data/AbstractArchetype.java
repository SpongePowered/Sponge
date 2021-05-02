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
package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.Archetype;
import org.spongepowered.api.world.LocatableSnapshot;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.data.nbt.validation.DelegateDataValidator;
import org.spongepowered.common.data.nbt.validation.RawDataValidator;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.data.provider.DataProviderLookup;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public abstract class AbstractArchetype<T, S extends LocatableSnapshot<S>, E> implements Archetype<S, E>,
        SpongeMutableDataHolder {

    protected final T type;
    protected CompoundTag data;

    protected AbstractArchetype(final T type, final CompoundTag data) {
        this.type = type;
        this.data = data;
    }

    public abstract DataProviderLookup getLookup();

    protected abstract ValidationType getValidationType();

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractArchetype)) {
            return false;
        }
        final AbstractArchetype<?, ?, ?> that = (AbstractArchetype<?, ?, ?>) o;
        return this.type.equals(that.type) &&
                this.data.equals(that.data);
    }

    @Override
    public void setRawData(final DataView container) throws InvalidDataException {
        checkNotNull(container, "Raw data cannot be null!");
        final CompoundTag copy = NBTTranslator.INSTANCE.translate(container);
        final boolean valid = this.getValidator().validate(copy);
        if (valid) {
            this.data = copy;
        } else {
            throw new InvalidDataException("Invalid data for " + this.getValidationType());
        }
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return this.getValidator().validate(container);
    }

    private DelegateDataValidator getValidator() {
        return new DelegateDataValidator(this.getValidators(), this.getValidationType());
    }

    protected abstract ImmutableList<RawDataValidator> getValidators();

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.data);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", this.type).add("data", this.data).toString();
    }

    public CompoundTag getCompound() {
        return this.data;
    }
}

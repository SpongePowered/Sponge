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
package org.spongepowered.common.data.holder;

import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.Archetype;
import org.spongepowered.api.world.LocatableSnapshot;
import org.spongepowered.common.data.holder.nbt.NbtCompoundDataHolder;
import org.spongepowered.common.data.provider.DataProviderLookup;
import org.spongepowered.common.data.provider.DataProviderRegistry;

import java.util.Collection;

public abstract class SpongeArchetype<S extends LocatableSnapshot<S>, T> implements SpongeMutableDataHolder, NbtCompoundDataHolder, Archetype<S, T> {

    private final CompoundNBT compound;
    private final DataProviderLookup lookup = DataProviderRegistry.get().getProviderLookup(this.getClass());

    protected SpongeArchetype(CompoundNBT compound) {
        this.compound = compound;
    }

    @Override
    public CompoundNBT getNbtCompound() {
        return this.compound;
    }

    @Override
    public <V extends Value<E>, E> DataProvider<V, E> getProviderFor(Key<V> key) {
        return this.lookup.getProvider(key);
    }

    @Override
    public Collection<DataProvider<?, ?>> getAllProviders() {
        return this.lookup.getAllProviders();
    }
}

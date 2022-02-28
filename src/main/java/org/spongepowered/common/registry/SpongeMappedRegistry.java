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
package org.spongepowered.common.registry;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;

/**
 * Based on {@link net.minecraft.core.MappedRegistry} but supports non-unique inverse mappings
 */
public final class SpongeMappedRegistry<T> extends WritableRegistry<T> {

    private final ObjectList<T> byId = new ObjectArrayList<>(256);
    private final Object2IntMap<T> toId = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), ($$0x) -> $$0x.defaultReturnValue(-1));
    private final Map<ResourceLocation, T> storage = new HashMap<>();
    private final IdentityHashMap<T, ResourceLocation> inverseStorage = Maps.newIdentityHashMap();
    private final Map<ResourceKey<T>, T> keyStorage = new HashMap<>();
    private final IdentityHashMap<T, ResourceKey<T>> inverseKeyStorage = Maps.newIdentityHashMap();
    private final Map<T, Lifecycle> lifecycles = Maps.newIdentityHashMap();
    private @MonotonicNonNull List<T> randomCache;
    private Lifecycle elementsLifecycle;
    private int nextId;

    public SpongeMappedRegistry(final ResourceKey<? extends Registry<T>> $$0, final Lifecycle $$1) {
        super($$0, $$1);
        this.elementsLifecycle = $$1;
    }

    public <V extends T> V registerMapping(final int $$0, final ResourceKey<T> $$1, final V $$2, final Lifecycle $$3) {
        Validate.notNull($$1);
        Validate.notNull((T)$$2);
        this.byId.size(Math.max(this.byId.size(), $$0 + 1));
        this.byId.set($$0, $$2);
        this.toId.put((T)$$2, $$0);

        this.storage.put($$1.location(), $$2);
        this.inverseStorage.put($$2, $$1.location());
        this.keyStorage.put($$1, (T)$$2);
        this.inverseKeyStorage.put($$2, $$1);
        this.lifecycles.put((T)$$2, $$3);
        this.elementsLifecycle = this.elementsLifecycle.add($$3);
        if (this.nextId <= $$0) {
            this.nextId = $$0 + 1;
        }

        return $$2;
    }


    public <V extends T> V register(final ResourceKey<T> $$0, final V $$1, final Lifecycle $$2) {
        return this.registerMapping(this.nextId, $$0, $$1, $$2);
    }

    public <V extends T> V registerOrOverride(
        final OptionalInt $$0, final ResourceKey<T> $$1, final V $$2, final Lifecycle $$3) {
        Validate.notNull($$1);
        Validate.notNull((T)$$2);
        final T $$4 = this.keyStorage.get($$1);
        final int $$5;
        if ($$4 == null) {
            $$5 = $$0.isPresent() ? $$0.getAsInt() : this.nextId;
        } else {
            $$5 = this.toId.getInt($$4);
            if ($$0.isPresent() && $$0.getAsInt() != $$5) {
                throw new IllegalStateException("ID mismatch");
            }

            this.toId.removeInt($$4);
            this.lifecycles.remove($$4);
        }

        return this.registerMapping($$5, $$1, $$2, $$3);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Nullable
    public ResourceLocation getKey(final T $$0) {
        return this.inverseStorage.get($$0);
    }

    public Optional<ResourceKey<T>> getResourceKey(final T $$0) {
        return Optional.ofNullable(this.inverseKeyStorage.get($$0));
    }

    public int getId(@Nullable final T $$0) {
        return this.toId.getInt($$0);
    }

    @Nullable
    public T get(@Nullable final ResourceKey<T> $$0) {
        return this.keyStorage.get($$0);
    }

    @Nullable
    public T byId(final int $$0) {
        return (T)($$0 >= 0 && $$0 < this.byId.size() ? this.byId.get($$0) : null);
    }

    @Override
    public int size() {
        return 0;
    }

    public Lifecycle lifecycle(final T $$0) {
        return this.lifecycles.get($$0);
    }

    public Lifecycle elementsLifecycle() {
        return this.elementsLifecycle;
    }

    public Iterator<T> iterator() {
        return Iterators.filter(this.byId.iterator(), Objects::nonNull);
    }

    @Nullable
    public T get(@Nullable final ResourceLocation $$0) {
        return this.storage.get($$0);
    }

    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.storage.keySet());
    }

    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return Collections.unmodifiableMap(this.keyStorage).entrySet();
    }

    @Nullable
    @Override
    public T getRandom(final Random random) {
        if (this.randomCache == null) {
            final Collection<T> $$1 = this.storage.values();
            if ($$1.isEmpty()) {
                return (T)null;
            }

            this.randomCache = $$1.stream().toList();
        }

        return this.randomCache.get(random.nextInt(this.randomCache.size()));
    }

    public boolean containsKey(final ResourceLocation $$0) {
        return this.storage.containsKey($$0);
    }

    @Override
    public boolean containsKey(final ResourceKey<T> var1) {
        return this.storage.containsKey(var1.location());
    }

}

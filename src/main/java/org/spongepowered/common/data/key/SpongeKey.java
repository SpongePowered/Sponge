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
package org.spongepowered.common.data.key;

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.EmptyDataProvider;
import org.spongepowered.common.data.value.ValueConstructor;
import org.spongepowered.common.data.value.ValueConstructorFactory;
import org.spongepowered.plugin.PluginContainer;

import java.util.Comparator;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public final class SpongeKey<V extends Value<E>, E> extends SpongeCatalogType implements Key<V> {

    private final TypeToken<V> valueToken;
    private final TypeToken<E> elementToken;
    private final Comparator<? super E> elementComparator;
    private final BiPredicate<? super E, ? super E> elementIncludesTester;
    private final ValueConstructor<V, E> valueConstructor;
    private final Supplier<E> defaultValueSupplier;
    private final EmptyDataProvider<V, E> emptyDataProvider;

    public SpongeKey(final ResourceKey key, final TypeToken<V> valueToken, final TypeToken<E> elementToken,
            final Comparator<? super E> elementComparator,
            final BiPredicate<? super E, ? super E> elementIncludesTester, final Supplier<E> defaultValueSupplier) {
        super(key);
        this.valueToken = valueToken;
        this.elementToken = elementToken;
        this.elementComparator = elementComparator;
        this.elementIncludesTester = elementIncludesTester;
        this.defaultValueSupplier = defaultValueSupplier;
        this.emptyDataProvider = new EmptyDataProvider<>(this);
        this.valueConstructor = ValueConstructorFactory.getConstructor(this);
    }

    @Override
    public TypeToken<V> getValueToken() {
        return this.valueToken;
    }

    @Override
    public TypeToken<E> getElementToken() {
        return this.elementToken;
    }

    @Override
    public Comparator<? super E> getElementComparator() {
        return this.elementComparator;
    }

    @Override
    public BiPredicate<? super E, ? super E> getElementIncludesTester() {
        return this.elementIncludesTester;
    }

    @Override
    public <H extends DataHolder> void registerEvent(final PluginContainer plugin, final Class<H> holderFilter,
            final EventListener<ChangeDataHolderEvent.ValueChange> listener) {
        SpongeDataManager.getInstance().registerKeyListener(new KeyBasedDataListener<>(plugin, holderFilter, this, listener));
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("resourceKey", this.getKey())
                .add("valueToken", this.valueToken);
    }

    public ValueConstructor<V, E> getValueConstructor() {
        return this.valueConstructor;
    }

    public Supplier<E> getDefaultValueSupplier() {
        return this.defaultValueSupplier;
    }

    public EmptyDataProvider<V, E> getEmptyDataProvider() {
        return this.emptyDataProvider;
    }
}

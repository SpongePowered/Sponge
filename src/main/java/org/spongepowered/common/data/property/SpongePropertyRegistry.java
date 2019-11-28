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
package org.spongepowered.common.data.property;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.PropertyRegistry;
import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.common.data.property.SpongePropertyRegistry.TempRegistry;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
@Singleton
public class SpongePropertyRegistry implements PropertyRegistry {

    @Nullable private TempRegistry tempRegistry = new TempRegistry();
    private final Map<Class<? extends Property<?, ?>>, PropertyStoreDelegate<?>> delegateMap = Maps.newConcurrentMap();

    static final class TempRegistry {
        final Map<Class<? extends Property<?, ?>>, List<PropertyStore<?>>> propertyStoreMap = Maps.newConcurrentMap();
        final Map<Class<? extends Property<?, ?>>, PropertyStoreDelegate<?>> delegateMap = Maps.newConcurrentMap();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void completeRegistration() {
        checkState(this.tempRegistry != null, "PropertyRegistry already finalized");
        final TempRegistry temp = this.tempRegistry;
        this.tempRegistry = null;
        if (temp != null) {
            for (final Map.Entry<Class<? extends Property<?, ?>>, List<PropertyStore<?>>> entry : temp.propertyStoreMap.entrySet()) {
                final ImmutableList.Builder<PropertyStore<?>> propertyStoreBuilder = ImmutableList.builder();
                entry.getValue().sort(Constants.Functional.PROPERTY_STORE_COMPARATOR);
                propertyStoreBuilder.addAll(entry.getValue());
                final PropertyStoreDelegate<?> delegate = new PropertyStoreDelegate(propertyStoreBuilder.build());
                this.delegateMap.put(entry.getKey(), delegate);
            }
            temp.delegateMap.clear();
            temp.propertyStoreMap.clear();
        }
    }

    @Override
    public <T extends Property<?, ?>> void register(final Class<T> propertyClass, final PropertyStore<T> propertyStore) {
        checkState(this.tempRegistry != null, "Registrations are no longer allowed!");
        checkArgument(propertyClass != null, "The property class can not be null!");
        if (!this.tempRegistry.propertyStoreMap.containsKey(propertyClass)) {
            this.tempRegistry.propertyStoreMap.put(propertyClass, Collections.synchronizedList(Lists.<PropertyStore<?>>newArrayList()));
        }
        this.tempRegistry.delegateMap.remove(propertyClass);
        final List<PropertyStore<?>> propertyStores = this.tempRegistry.propertyStoreMap.get(propertyClass);
        propertyStores.add(checkNotNull(propertyStore));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Property<?, ?>> Optional<PropertyStore<T>> getStore(final Class<T> propertyClass) {
        checkArgument(propertyClass != null, "The property class can not be null!");
        if (!this.delegateMap.containsKey(propertyClass)) {
            return Optional.empty();
        }
        return Optional.of((PropertyStore<T>) this.delegateMap.get(propertyClass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Property<?, ?>> getPropertiesFor(final PropertyHolder holder) {
        final ImmutableList.Builder<Property<?, ?>> builder = ImmutableList.builder();
        if (this.tempRegistry != null) { // Still doing registrations
            final Set<Class<? extends Property<?, ?>>> used = new HashSet<>();
            for (final Map.Entry<Class<? extends Property<?, ?>>, PropertyStoreDelegate<?>> entry : this.tempRegistry.delegateMap.entrySet()) {
                used.add(entry.getKey());
                entry.getValue().getFor(holder).ifPresent(builder::add);
            }
            for (final Map.Entry<Class<? extends Property<?, ?>>, List<PropertyStore<?>>> entry : this.tempRegistry.propertyStoreMap.entrySet()) {
                if (!used.contains(entry.getKey())) {
                    used.add(entry.getKey());
                    final ImmutableList.Builder<PropertyStore<?>> propertyStoreBuilder = ImmutableList.builder();
                    entry.getValue().sort(Constants.Functional.PROPERTY_STORE_COMPARATOR);
                    propertyStoreBuilder.addAll(entry.getValue());
                    final PropertyStoreDelegate<?> delegate = new PropertyStoreDelegate(propertyStoreBuilder.build());
                    this.tempRegistry.delegateMap.put(entry.getKey(), delegate);
                    delegate.getFor(holder).ifPresent(builder::add);
                }
            }
            return builder.build();
        }
        for (final Map.Entry<Class<? extends Property<?, ?>>, PropertyStoreDelegate<?>> entry : this.delegateMap.entrySet()) {
            entry.getValue().getFor(holder).ifPresent(builder::add);
        }
        return builder.build();
    }
}

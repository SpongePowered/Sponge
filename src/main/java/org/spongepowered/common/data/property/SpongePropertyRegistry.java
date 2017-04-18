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
import org.spongepowered.common.data.util.ComparatorUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class SpongePropertyRegistry implements PropertyRegistry {

    private final Map<Class<? extends Property<?, ?>>, List<PropertyStore<?>>> propertyStoreMap = Maps.newConcurrentMap();
    private final Map<Class<? extends Property<?, ?>>, PropertyStoreDelegate<?>> delegateMap = Maps.newConcurrentMap();
    private boolean allowRegistrations = true;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void completeRegistration() {
        this.allowRegistrations = false;
        for (Map.Entry<Class<? extends Property<?, ?>>, List<PropertyStore<?>>> entry : this.propertyStoreMap.entrySet()) {
            ImmutableList.Builder<PropertyStore<?>> propertyStoreBuilder = ImmutableList.builder();
            Collections.sort(entry.getValue(), ComparatorUtil.PROPERTY_STORE_COMPARATOR);
            propertyStoreBuilder.addAll(entry.getValue());
            final PropertyStoreDelegate<?> delegate = new PropertyStoreDelegate(propertyStoreBuilder.build());
            this.delegateMap.put(entry.getKey(), delegate);
        }
        this.propertyStoreMap.clear();
    }

    @Override
    public <T extends Property<?, ?>> void register(Class<T> propertyClass, PropertyStore<T> propertyStore) {
        checkState(this.allowRegistrations, "Registrations are no longer allowed!");
        checkArgument(propertyClass != null, "The property class can not be null!");
        if (!this.propertyStoreMap.containsKey(propertyClass)) {
            this.propertyStoreMap.put(propertyClass, Collections.synchronizedList(Lists.<PropertyStore<?>>newArrayList()));
        }
        final List<PropertyStore<?>> propertyStores = this.propertyStoreMap.get(propertyClass);
        propertyStores.add(checkNotNull(propertyStore));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Property<?, ?>> Optional<PropertyStore<T>> getStore(Class<T> propertyClass) {
        checkArgument(propertyClass != null, "The property class can not be null!");
        if (!this.delegateMap.containsKey(propertyClass)) {
            return Optional.empty();
        } else {
            return Optional.of((PropertyStore<T>) this.delegateMap.get(propertyClass));
        }
    }

    public Collection<Property<?, ?>> getPropertiesFor(PropertyHolder holder) {
        final ImmutableList.Builder<Property<?, ?>> builder = ImmutableList.builder();
        for (Map.Entry<Class<? extends Property<?, ?>>, PropertyStoreDelegate<?>> entry : this.delegateMap.entrySet()) {
            final Optional<? extends Property<?, ?>> optional = entry.getValue().getFor(holder);
            if (optional.isPresent()) {
                builder.add(optional.get());
            }
        }
        return builder.build();
    }
}

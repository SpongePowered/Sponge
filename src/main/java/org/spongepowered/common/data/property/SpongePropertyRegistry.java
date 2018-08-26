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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.property.Properties;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.PropertyRegistry;
import org.spongepowered.api.data.property.store.DoublePropertyStore;
import org.spongepowered.api.data.property.store.IntPropertyStore;
import org.spongepowered.api.data.property.store.PropertyStore;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class SpongePropertyRegistry extends AbstractCatalogRegistryModule<Property<?>> implements PropertyRegistry,
        SpongeAdditionalCatalogRegistryModule<Property<?>> {

    static final class Holder {
        final static SpongePropertyRegistry INSTANCE = new SpongePropertyRegistry();
    }

    public static SpongePropertyRegistry getInstance() {
        return Holder.INSTANCE;
    }

    private final Multimap<Property<?>, PropertyStore<?>> propertyStoreMap = HashMultimap.create();
    private final Map<Property<?>, PropertyStoreDelegate<?>> delegateMap = new ConcurrentHashMap<>();

    private boolean allowRegistrations = true;

    private SpongePropertyRegistry() {
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(Property extraCatalog) {
        checkNotNull(extraCatalog, "CatalogType cannot be null");
        checkArgument(!extraCatalog.getKey().getValue().isEmpty(), "Id cannot be empty");
        checkArgument(!this.map.containsKey(extraCatalog.getKey()), "Duplicate id:" + extraCatalog.getKey());
        this.map.put(extraCatalog.getKey(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        final TypeVariable<?> typeVariable = Property.class.getTypeParameters()[0];
        for (Class<?> catalog : Arrays.asList(Properties.class, InventoryProperties.class)) {
            for (Field field : catalog.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                final CatalogKey key = CatalogKey.sponge(field.getName().toLowerCase());
                final Optional<Property<?>> optProperty = get(key);

                final Property<?> property;
                if (optProperty.isPresent()) {
                    property = optProperty.get();
                } else {
                    final TypeToken<?> typeToken = TypeToken.of(field.getGenericType());
                    final TypeToken<?> valueType = typeToken.resolveType(typeVariable);
                    property = new SpongePropertyBuilder<CatalogType>()
                            .valueType(valueType)
                            .key(key)
                            .build();
                    registerAdditionalCatalog(property);
                }

                try {
                    field.set(null, property);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void completeRegistration() {
        this.allowRegistrations = false;
        for (Map.Entry<Property<?>, Collection<PropertyStore<?>>> entry : this.propertyStoreMap.asMap().entrySet()) {
            this.delegateMap.put(entry.getKey(), constructDelegate(entry.getKey(), (Collection) entry.getValue()));
        }
        this.propertyStoreMap.clear();
    }

    private <V> PropertyStoreDelegate<V> constructDelegate(Property<V> property) {
        return constructDelegate(property, (Collection) this.propertyStoreMap.get(property));
    }

    private <V> PropertyStoreDelegate<V> constructDelegate(Property<V> property, Collection<PropertyStore<V>> propertyStores) {
        final List<PropertyStore<V>> stores = new ArrayList<>(propertyStores);
        stores.sort(ComparatorUtil.PROPERTY_STORE_COMPARATOR);
        final ImmutableList immutableStores = ImmutableList.copyOf(stores);
        final Class<?> valueType = property.getValueType().getRawType();
        if (valueType == Integer.class) {
            return (PropertyStoreDelegate<V>) new IntPropertyStoreDelegate(immutableStores);
        } else if (valueType == Double.class) {
            return (PropertyStoreDelegate<V>) new DoublePropertyStoreDelegate(immutableStores);
        }
        return new PropertyStoreDelegate<>(immutableStores);
    }

    @Override
    public <V> void register(Property<V> property, PropertyStore<V> store) {
        checkNotNull(property, "property");
        checkNotNull(store, "store");
        checkState(this.allowRegistrations, "Registrations are no longer allowed!");
        this.propertyStoreMap.put(property, store);
    }

    @Override
    public <V> PropertyStore<V> getStore(Property<V> property) {
        checkNotNull(property, "property");
        if (this.allowRegistrations) {
            return constructDelegate(property);
        }
        return (PropertyStore<V>) this.delegateMap.computeIfAbsent(property, this::constructDelegate);
    }

    @Override
    public IntPropertyStore getIntStore(Property<Integer> property) {
        return (IntPropertyStore) getStore(property);
    }

    @Override
    public DoublePropertyStore getDoubleStore(Property<Double> property) {
        return (DoublePropertyStore) getStore(property);
    }

    public Map<Property<?>, ?> getPropertiesFor(PropertyHolder holder) {
        final ImmutableMap.Builder<Property<?>, Object> builder = ImmutableMap.builder();
        for (Map.Entry<Property<?>, PropertyStoreDelegate<?>> entry : this.delegateMap.entrySet()) {
            entry.getValue().getFor(holder).ifPresent(value -> builder.put(entry.getKey(), value));
        }
        return builder.build();
    }
}

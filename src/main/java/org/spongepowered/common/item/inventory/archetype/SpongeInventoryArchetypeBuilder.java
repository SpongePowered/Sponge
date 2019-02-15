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
package org.spongepowered.common.item.inventory.archetype;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.registry.type.item.InventoryArchetypeRegistryModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpongeInventoryArchetypeBuilder implements InventoryArchetype.Builder {

    private List<InventoryArchetype> types = new ArrayList<>();
    private Map<Property<?>, Object> properties = new HashMap<>();
    private Set<Class<? extends InteractContainerEvent>> events = new HashSet<>();
    private CompositeInventoryArchetype.ContainerProvider containerProvider;
    private CatalogKey key;
    private String name;
    private String id;

    @Override
    public <V> SpongeInventoryArchetypeBuilder property(Property<V> property, V value) {
        checkNotNull(property, "property");
        checkNotNull(value, "value");
        this.properties.put(property, value);
        return this;
    }

    @Override
    public SpongeInventoryArchetypeBuilder with(InventoryArchetype archetype) {
        checkNotNull(archetype, "archetype");
        this.types.add(archetype);
        return this;
    }

    @Override
    public SpongeInventoryArchetypeBuilder with(InventoryArchetype... archetypes) {
        checkNotNull(archetypes, "archetypes");
        Arrays.stream(archetypes).forEach(this::with);
        return this;
    }

    @Override
    public SpongeInventoryArchetypeBuilder from(InventoryArchetype value) {
        if (value instanceof CompositeInventoryArchetype) {
            this.types.addAll(value.getChildArchetypes());
            this.properties.putAll(value.getProperties());
        }
        return this;
    }

    public SpongeInventoryArchetypeBuilder container(CompositeInventoryArchetype.ContainerProvider containerProvider) {
        this.containerProvider = containerProvider;
        return this;
    }

    @Override
    public SpongeInventoryArchetypeBuilder reset() {
        this.types.clear();
        this.properties.clear();
        this.events.clear();
        return this;
    }

    @Override
    public InventoryArchetype.Builder key(CatalogKey key) {
        this.key = key;
        this.name = key.getValue();
        return this;
    }

    @Override
    public InventoryArchetype.Builder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public InventoryArchetype.Builder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public InventoryArchetype.Builder name(Translation translation) {
        // TODO - implement
        this.name = translation.getId();
        return this;
    }

    @Override
    public InventoryArchetype build() {
        // TODO register archetype
        // TODO events
        final CompositeInventoryArchetype
            archetype =
            new CompositeInventoryArchetype(id, name, this.types, this.properties, this.containerProvider);
        InventoryArchetypeRegistryModule.getInstance()
            .registerAdditionalCatalog(archetype);
        return archetype;
    }
}

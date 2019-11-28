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

import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpongeInventoryArchetypeBuilder implements InventoryArchetype.Builder {

    private List<InventoryArchetype> types = new ArrayList<>();
    private Map<String, InventoryProperty<String, ?>> properties = new HashMap<>();
    private Set<Class<? extends InteractContainerEvent>> events = new HashSet<>();
    private CompositeInventoryArchetype.ContainerProvider containerProvider;

    @Override
    public SpongeInventoryArchetypeBuilder property(InventoryProperty<String, ?> property) {
        this.properties.put(property.getKey(), property);
        return this;
    }

    @Override
    public SpongeInventoryArchetypeBuilder with(InventoryArchetype archetype) {
        this.types.add(archetype);
        return this;
    }

    @Override
    public SpongeInventoryArchetypeBuilder with(InventoryArchetype... archetypes) {
        Collections.addAll(this.types, archetypes);
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
    public InventoryArchetype build(String id, String name) {
        // TODO register archetype
        // TODO events
        return new CompositeInventoryArchetype(id, name, this.types, this.properties, this.containerProvider);
    }
}

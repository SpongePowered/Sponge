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
package org.spongepowered.common.item.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.common.item.inventory.custom.CustomInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SpongeInventoryBuilder implements Inventory.Builder {

    private final static Map<Class<?>, InventoryArchetype> inventoryTypes = new HashMap<>();

    public static void registerInventory(Class<? extends IInventory> inventory, InventoryArchetype archetype) {
        inventoryTypes.put(inventory, archetype);
    }

    public static void registerContainer(Class<? extends Container> container, InventoryArchetype archetype) {
        inventoryTypes.put(container, archetype);
    }

    private InventoryArchetype archetype;
    private Map<String, InventoryProperty<?, ?>> properties = new HashMap<>();
    private Map<Class<? extends InteractContainerEvent>, List<Consumer<? extends InteractContainerEvent>>> listeners = new HashMap<>();
    private Carrier carrier;

    public SpongeInventoryBuilder() {
        this.archetype = InventoryArchetypes.CHEST;
    }

    @Override
    public Inventory.Builder of(InventoryArchetype archetype) {
        this.archetype = archetype;
        return this;
    }

    @Override
    public Inventory.Builder property(String name, InventoryProperty<?, ?> property) {
        this.properties.put(name, property);
        return this;
    }

    @Override
    public Inventory.Builder property(InventoryProperty<?, ?> property) {
        Object key = AbstractInventoryProperty.getDefaultKey(property.getClass());
        this.property(key.toString(), property);
        return this;
    }

    @Override
    public Inventory.Builder withCarrier(Carrier carrier) {
        this.carrier = carrier;
        return this;
    }

    @Override
    public Inventory build(Object plugin) {
        return (Inventory) new CustomInventory(this.archetype, this.properties, this.carrier, this.listeners,
                Sponge.getPluginManager().fromInstance(plugin).orElseThrow(() -> new IllegalArgumentException(plugin + " is not a plugin")));
    }

    @Override
    public Inventory.Builder from(Inventory value) {
        if (value instanceof CustomInventory) {
            this.archetype = value.getArchetype();
            this.properties.putAll(((CustomInventory) value).getProperties());
            return this;
        }

        InventoryArchetype archetype = inventoryTypes.get(value.getClass());
        if (archetype == null) throw new UnsupportedOperationException("Currently not supported for all inventories");
        // TODO how to get Archetype from inventory?
        this.archetype = archetype;
        this.properties = new HashMap<>();
        return this;
    }

    @Override
    public Inventory.Builder forCarrier(Carrier carrier) {
        return forCarrier(carrier.getClass());
    }

    @Override
    public Inventory.Builder forCarrier(Class<? extends Carrier> carrier) {
        throw new UnsupportedOperationException();
//        this.archetype = null; // TODO get Archetype for Carrier
//        return null;
    }

    @Override
    public Inventory.Builder reset() {
        this.archetype = InventoryArchetypes.CHEST;
        this.properties = new HashMap<>();
        this.listeners.clear();
        return this;
    }

    @Override
    public <E extends InteractContainerEvent> Inventory.Builder listener(Class<E> type, Consumer<E> listener) {
        List<Consumer<? extends InteractContainerEvent>> list = this.listeners.get(type);
        if (list == null) {
            list = new ArrayList<>();
            this.listeners.put(type, list);
        }
        list.add(listener);
        return this;
    }
}

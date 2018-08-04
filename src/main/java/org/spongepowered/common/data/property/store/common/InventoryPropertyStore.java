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
package org.spongepowered.common.data.property.store.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Streams;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperty;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.property.AbstractInventoryProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoryPropertyStore<T extends InventoryProperty<?, ?>> implements PropertyStore<T> {

    private Class<T> property;

    public InventoryPropertyStore(Class<T> property) {
        this.property = property;
    }

    @Override
    public Optional<T> getFor(PropertyHolder propertyHolder) {
        if (propertyHolder instanceof Inventory) {
            Object key = AbstractInventoryProperty.getDefaultKey(this.property);
            return ((Inventory) propertyHolder).getProperty(this.property, key);
        }
        return Optional.empty();
    }

    @Override
    public Optional<T> getFor(Location<World> location) {
        return location.getTileEntity()
                .flatMap(te -> (te instanceof Carrier) ? Optional.of(((Carrier) te).getInventory()) : Optional.empty())
                .flatMap(this::getFor);
    }

    @Override
    public Optional<T> getFor(Location<World> location, Direction direction) {
        // TODO directional access
        return this.getFor(location);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    public static Collection<InventoryProperty<?, ?>> getProperties(InventoryAdapter adapter,
            Inventory child, Class<? extends InventoryProperty<?, ?>> property) {
        return getProperties(adapter.getFabric(), adapter.getRootLens(), child, property);
    }

    public static Collection<InventoryProperty<?, ?>> getProperties(Fabric inv, Lens lens,
            Inventory child, Class<? extends InventoryProperty<?, ?>> property) {

        if (child instanceof InventoryAdapter) {
            checkNotNull(property, "property");
            int index = lens.getChildren().indexOf(((InventoryAdapter) child).getRootLens());
            if (index > -1) {
                return lens.getProperties(index).stream().filter(prop -> property.isAssignableFrom(prop.getClass()))
                        .collect(Collectors.toCollection(ArrayList::new));
            }
        }

        return Collections.emptyList();
    }

    public static <T extends InventoryProperty<?, ?>> Collection<T> getRootProperties(InventoryAdapter adapter, Class<T> property) {
        adapter = inventoryRoot(adapter);
        if (adapter instanceof CustomInventory) {
            return ((CustomInventory) adapter).getProperties().values().stream().filter(p -> property.isAssignableFrom(p.getClass()))
                    .map(property::cast).collect(Collectors.toList());
        }
        return Streams.stream(findRootProperty(adapter, property)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T extends InventoryProperty<?, ?>> Optional<T> getRootProperty(InventoryAdapter adapter, Class<T> property, Object key) {
        adapter = inventoryRoot(adapter);
        if (adapter instanceof CustomInventory) {
            InventoryProperty<?, ?> forKey = ((CustomInventory) adapter).getProperties().get(key);
            if (forKey != null && property.isAssignableFrom(forKey.getClass())) {
                return Optional.of((T) forKey);
            }
        }
        return findRootProperty(adapter, property);
    }

    @SuppressWarnings("unchecked")
    private static <T extends InventoryProperty<?, ?>> Optional<T> findRootProperty(InventoryAdapter adapter, Class<T> property) {
        if (property == InventoryTitle.class) {
            Text text = Text.of(adapter.getFabric().getDisplayName());
            return (Optional<T>) Optional.of(InventoryTitle.of(text));
        }
        // TODO more properties of top level inventory
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static InventoryAdapter inventoryRoot(InventoryAdapter adapter) {
        // Get Root Inventory
        adapter = ((InventoryAdapter) adapter.root());
        if (adapter instanceof Container) {
            // If Root is a Container get the viewed inventory
            Object first = adapter.getFabric().allInventories().iterator().next();
            if (first instanceof CustomInventory) {
                // if viewed inventory is a custom inventory get it instead
                adapter = ((InventoryAdapter) first);
            }
        }
        return adapter;
    }
}

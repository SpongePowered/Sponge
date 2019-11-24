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

import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.provider.PropertyProvider;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;

import java.util.Optional;

@SuppressWarnings("unchecked")
public class InventoryPropertyProvider<V> implements PropertyProvider<V> {

    private final Property<V> property;

    public InventoryPropertyProvider(Property<V> property) {
        this.property = property;
    }

    @Override
    public Optional<V> getFor(PropertyHolder propertyHolder) {
        // Inventories handle their properties differently,
        // so we can retrieve them from the method
        if (propertyHolder instanceof Location) {
            final BlockEntity te = ((Location) propertyHolder).getBlockEntity().orElse(null);
            return te instanceof Carrier ? ((Carrier) te).getInventory().getProperty(this.property) : Optional.empty();
        } else if (propertyHolder instanceof Carrier) {
            return ((Carrier) propertyHolder).getInventory().getProperty(this.property);
        } else if (propertyHolder instanceof Inventory) {
            return propertyHolder.getProperty(this.property);
        }
        return Optional.empty();
    }

    public static <V> Optional<V> getProperty(InventoryAdapter adapter, Inventory child, Property<V> property) {
        return getProperty(adapter.bridge$getFabric(), adapter.bridge$getRootLens(), child, property);
    }

    public static <V> Optional<V> getProperty(Fabric fabric, Lens lens, Inventory child, Property<V> property) {
        if (child instanceof InventoryAdapter) {
            checkNotNull(property, "property");
            int index = lens.getChildren().indexOf(((InventoryAdapter) child).bridge$getRootLens());
            if (index > -1) {
                return Optional.ofNullable((V) lens.getProperties(index).get(property));
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <V> Optional<V> getRootProperty(InventoryAdapter adapter, Property<V> property) {
        adapter = inventoryRoot(adapter);
        if (adapter instanceof CustomInventory) {
            final Object value = ((CustomInventory) adapter).getProperties().get(property);
            if (value != null) {
                return Optional.of((V) value);
            }
        }
        return findRootProperty(adapter, property);
    }

    @SuppressWarnings("unchecked")
    private static <V> Optional<V> findRootProperty(InventoryAdapter adapter, Property<V> property) {
        // TODO more properties of top level inventory
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static InventoryAdapter inventoryRoot(InventoryAdapter adapter) {
        // Get Root Inventory
        adapter = (InventoryAdapter) ((Inventory) adapter).root();
        if (adapter instanceof Container) {
            // If Root is a Container get the viewed inventory
            Object first = adapter.bridge$getFabric().fabric$get(0);
            if (first instanceof CustomInventory) {
                // if viewed inventory is a custom inventory get it instead
                adapter = ((InventoryAdapter) first);
            }
        }
        return adapter;
    }

}

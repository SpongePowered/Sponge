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

import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.provider.PropertyProvider;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryProperties;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;
import java.util.UUID;

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

    public static <V> Optional<V> getProperty(Inventory inventory, Inventory child, Property<V> property) {
        InventoryAdapter adapter = ((InventoryBridge) inventory).bridge$getAdapter();
        return getProperty(adapter.inventoryAdapter$getFabric(), adapter.inventoryAdapter$getRootLens(), child, property);
    }

    public static <V> Optional<V> getProperty(Fabric fabric, Lens lens, Inventory child, Property<V> property) {
        // TODO properties that do not come from lenses
        checkNotNull(property, "property");
        InventoryAdapter childAdapter = ((InventoryBridge) child).bridge$getAdapter();
        V propertyValue = (V) lens.getProperties(childAdapter.inventoryAdapter$getRootLens()).get(property);
        return Optional.ofNullable(propertyValue);
    }

    @SuppressWarnings("unchecked")
    public static <V> Optional<V> getRootProperty(Inventory inventory, Property<V> property) {
        Inventory root = inventory.root();

        if (property == InventoryProperties.PLUGIN) {
            return (Optional<V>) getPlugin(root);
        }
        if (property == InventoryProperties.MAX_STACK_SIZE) {
            return (Optional<V>) getMaxStackSize((InventoryBridge) root);
        }
        if (property == InventoryProperties.UNIQUE_ID) {
            return (Optional<V>) getIdentity(root);
        }
        if (property == InventoryProperties.TITLE) {
            return (Optional<V>) getTitleProperty(root);
        }
        return Optional.empty();
    }

    private static Optional<Integer> getMaxStackSize(InventoryBridge inventory) {
        Integer i = inventory.bridge$getAdapter().inventoryAdapter$getFabric().fabric$getMaxStackSize();
        return Optional.of(i);
    }

    private static Optional<PluginContainer> getPlugin(Inventory inventory) {
        return Optional.ofNullable(InventoryUtil.getPluginContainer(inventory));
    }

    private static Optional<UUID> getIdentity(Inventory inventory) {
        if (inventory instanceof CustomInventory) {
            UUID uuid = ((CustomInventory) inventory).getIdentity();
            return Optional.ofNullable(uuid);
        }
        return Optional.empty();
    }

    private static Optional<Text> getTitleProperty(Inventory inventory) {
        if (inventory instanceof INameable) {
            ITextComponent name = ((INameable) inventory).getName();
            if (name == null) {
                return Optional.empty();
            }
            return Optional.of(SpongeTexts.toText(name));
        }

        return Optional.empty();
    }
}

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
package org.spongepowered.common.registry.type.item;

import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class ItemStackComparatorRegistryModule implements RegistryModule {

    @RegisterCatalog(ItemStackComparators.class)
    private final Map<String, Comparator<ItemStack>> comparators = new HashMap<>();

    @DelayedRegistration(value = RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {

        Comparator<ItemStack> type = Comparator.comparing(i -> i.getType().getId());
        this.comparators.put("type", type);
        Comparator<ItemStack> size = Comparator.comparing(ItemStack::getQuantity);
        this.comparators.put("size", size);
        Comparator<ItemStack> typeSize = type.thenComparing(size);
        this.comparators.put("type_size", typeSize);
        this.comparators.put("default", typeSize);
        Properties properties = new Properties();
        this.comparators.put("properties", properties);
        ItemDataComparator itemData = new ItemDataComparator();
        this.comparators.put("item_data", itemData);
        this.comparators.put("item_data_ignore_damage", new ItemDataComparator(DurabilityData.class));
        this.comparators.put("ignore_size", type.thenComparing(properties).thenComparing(itemData).thenComparingInt(i -> ItemStackUtil.toNative(i).getDamage()));
        this.comparators.put("all", type.thenComparing(size).thenComparing(properties).thenComparing(itemData).thenComparingInt(i -> ItemStackUtil.toNative(i).getDamage()));
    }

    private static final class Properties implements Comparator<ItemStack> {

        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            Set<Property<?, ?>> properties = new HashSet<>(o2.getApplicableProperties());
            for (Property<?, ?> property : o1.getApplicableProperties()) {
                if (properties.contains(property)) {
                    properties.remove(property);
                } else {
                    return -1;
                }
            }
            return properties.size();
        }
    }

    private static final class ItemDataComparator implements Comparator<ItemStack> {

        private final Class<? extends DataManipulator<?, ?>>[] ignored;

        ItemDataComparator(Class<? extends DataManipulator<?, ?>>... ignored) {
            this.ignored = ignored;
        }

        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            Set<DataManipulator<?, ?>> manipulators = new LinkedHashSet<>(o2.getContainers());
            for (final DataManipulator<?, ?> manipulator : o1.getContainers()) {
                if (manipulators.contains(manipulator)) {
                    manipulators.remove(manipulator);
                } else if (!this.isIgnored(manipulators, manipulator)) {
                    return -1;
                }
            }
            return manipulators.size();
        }

        private boolean isIgnored(Set<DataManipulator<?, ?>> list, DataManipulator<?, ?> toCheck) {
            for (Class<? extends DataManipulator<?, ?>> ignore : this.ignored) {
                if (ignore.isAssignableFrom(toCheck.getClass())) {
                    list.removeIf(manip -> ignore.isAssignableFrom(manip.getClass()));
                    return true;
                }
            }
            return false;
        }
    }
}
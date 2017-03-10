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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeEquipmentType;
import org.spongepowered.common.data.type.SpongeEquipmentTypeWorn;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class EquipmentTypeRegistryModule implements CatalogRegistryModule<EquipmentType> {

    @RegisterCatalog(EquipmentTypes.class)
    private final Map<String, EquipmentType> equipmentTypeMap = new HashMap<>();

    @Override
    public Optional<EquipmentType> getById(String id) {
        return Optional.ofNullable(this.equipmentTypeMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<EquipmentType> getAll() {
        return ImmutableList.copyOf(this.equipmentTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        this.equipmentTypeMap.put("any", new SpongeEquipmentType("any"));
        this.equipmentTypeMap.put("equipped", new SpongeEquipmentType("equipped"));
        // TODO - evaluate whether the id's are used for any other purpose than the catalog id type to look up
        final SpongeEquipmentTypeWorn head = new SpongeEquipmentTypeWorn("head", 0);
        this.equipmentTypeMap.put("headwear", head);
        this.equipmentTypeMap.put("head", head);
        this.equipmentTypeMap.put("chestplate", new SpongeEquipmentTypeWorn("chestplate", 1));
        this.equipmentTypeMap.put("leggings", new SpongeEquipmentTypeWorn("leggings", 2));
        this.equipmentTypeMap.put("boots", new SpongeEquipmentTypeWorn("boots", 3));
        this.equipmentTypeMap.put("worn", new SpongeEquipmentTypeWorn("worn", 0));
    }
}

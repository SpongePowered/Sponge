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
import org.spongepowered.common.data.type.SpongeHeldEquipmentType;
import org.spongepowered.common.data.type.SpongeWornEquipmentType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.inventory.EquipmentSlotType;

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
        this.registerType("any");
        this.registerType("equipped");

        final SpongeWornEquipmentType head = this.registerWornType("head", EquipmentSlotType.HEAD);
        this.equipmentTypeMap.put("headwear", head);
        this.registerWornType("chestplate", EquipmentSlotType.CHEST);
        this.registerWornType("leggings", EquipmentSlotType.LEGS);
        this.registerWornType("boots", EquipmentSlotType.FEET);
        this.registerWornType("worn", EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET);

        this.registerHeldType("main_hand", EquipmentSlotType.MAINHAND);
        this.registerHeldType("off_hand", EquipmentSlotType.OFFHAND);
        this.registerHeldType("held", EquipmentSlotType.MAINHAND, EquipmentSlotType.OFFHAND);
    }

    private void registerType(String id) {
        this.equipmentTypeMap.put(id, new SpongeEquipmentType(id));
    }

    private SpongeWornEquipmentType registerWornType(String id, EquipmentSlotType... types) {
        SpongeWornEquipmentType newType = new SpongeWornEquipmentType(id, types);
        this.equipmentTypeMap.put(id, newType);
        return newType;
    }

    private void registerHeldType(String id, EquipmentSlotType... types) {
        this.equipmentTypeMap.put(id, new SpongeHeldEquipmentType(id, types));
    }
}

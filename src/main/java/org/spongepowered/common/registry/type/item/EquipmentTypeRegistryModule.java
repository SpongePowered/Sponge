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

import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.equipment.HeldEquipmentType;
import org.spongepowered.api.item.inventory.equipment.WornEquipmentType;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeEquipmentType;
import org.spongepowered.common.data.type.SpongeHeldEquipmentType;
import org.spongepowered.common.data.type.SpongeWornEquipmentType;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.function.Predicate;

@RegisterCatalog(EquipmentTypes.class)
public class EquipmentTypeRegistryModule extends AbstractCatalogRegistryModule<EquipmentType> implements CatalogRegistryModule<EquipmentType> {

    @Override
    public void registerDefaults() {
        this.registerType("any", other -> true);
        this.registerType("equipped", other -> other instanceof WornEquipmentType || other instanceof HeldEquipmentType);

        final SpongeWornEquipmentType head = this.registerWornType("head", EntityEquipmentSlot.HEAD);
        this.map.put(CatalogKey.minecraft("headwear"), head);
        this.registerWornType("chestplate", EntityEquipmentSlot.CHEST);
        this.registerWornType("leggings", EntityEquipmentSlot.LEGS);
        this.registerWornType("boots", EntityEquipmentSlot.FEET);
        this.registerWornType("worn", EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET);

        this.registerHeldType("main_hand", EntityEquipmentSlot.MAINHAND);
        this.registerHeldType("off_hand", EntityEquipmentSlot.OFFHAND);
        this.registerHeldType("held", EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND);
    }

    private void registerType(String id, Predicate<EquipmentType> includesTester) {
        this.map.put(CatalogKey.minecraft(id), new SpongeEquipmentType(id, includesTester));
    }

    private SpongeWornEquipmentType registerWornType(String id, EntityEquipmentSlot... types) {
        SpongeWornEquipmentType newType = new SpongeWornEquipmentType(id, types);
        this.map.put(CatalogKey.minecraft(id), newType);
        return newType;
    }

    private void registerHeldType(String id, EntityEquipmentSlot... types) {
        this.map.put(CatalogKey.minecraft(id), new SpongeHeldEquipmentType(id, types));
    }
}

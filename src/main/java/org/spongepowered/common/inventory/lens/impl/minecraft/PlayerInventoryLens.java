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
package org.spongepowered.common.inventory.lens.impl.minecraft;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.impl.AbstractLens;
import org.spongepowered.common.inventory.lens.impl.comp.ArmorInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.EquipmentInventoryLens;
import org.spongepowered.common.inventory.lens.impl.comp.PrimaryPlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.HeldHandSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.inventory.property.KeyValuePair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PlayerInventoryLens extends AbstractLens {

    private static final int ARMOR = 4;
    private static final int OFFHAND = 1;

    private PrimaryPlayerInventoryLens primary;
    private EquipmentInventoryLens equipment;
    private ArmorInventoryLens armor;
    private SlotLens offhand;
    private final boolean isContainer;

    public PlayerInventoryLens(int size, Class<? extends Inventory> adapter, SlotLensProvider slots) {
        super(0, size, adapter);
        this.isContainer = false;
        this.init(slots);
    }

    /**
     * Constructor for ContainerPlayer Inventory
     *
     * @param base The base index
     * @param size The size
     * @param slots The slots
     */
    public PlayerInventoryLens(int base, int size, SlotLensProvider slots) {
        super(base, size, PlayerInventory.class);
        this.isContainer = true;
        this.init(slots);
    }

    protected void init(SlotLensProvider slots) {
        // Adding slots
        for (int ord = 0, slot = this.base; ord < this.size; ord++, slot++) {
            this.addChild(slots.getSlotLens(slot), KeyValuePair.slotIndex(ord));
        }

        int base = this.base;
        Map<EquipmentType, SlotLens> equipmentLenses = new LinkedHashMap<>();
        if (this.isContainer) {
            this.armor = new ArmorInventoryLens(base, slots, true);
            equipmentLenses.put(EquipmentTypes.HEAD.get(), slots.getSlotLens(base + 0));
            equipmentLenses.put(EquipmentTypes.CHEST.get(), slots.getSlotLens(base + 1));
            equipmentLenses.put(EquipmentTypes.LEGS.get(), slots.getSlotLens(base + 2));
            equipmentLenses.put(EquipmentTypes.FEET.get(), slots.getSlotLens(base + 3));
            base += PlayerInventoryLens.ARMOR; // 4
            this.primary = new PrimaryPlayerInventoryLens(base, slots, true);
            base += this.primary.slotCount();
            this.offhand = slots.getSlotLens(base);

            base += PlayerInventoryLens.OFFHAND;
            equipmentLenses.put(EquipmentTypes.OFF_HAND.get(), this.offhand);

            this.addSpanningChild(this.armor);
            this.addSpanningChild(this.primary);
            this.addSpanningChild(this.offhand);

        } else {
            this.primary = new PrimaryPlayerInventoryLens(base, slots, false);
            base += this.primary.slotCount();
            this.armor = new ArmorInventoryLens(base, slots, false);

            equipmentLenses.put(EquipmentTypes.FEET.get(), slots.getSlotLens(base + 0));
            equipmentLenses.put(EquipmentTypes.LEGS.get(), slots.getSlotLens(base + 1));
            equipmentLenses.put(EquipmentTypes.CHEST.get(), slots.getSlotLens(base + 2));
            equipmentLenses.put(EquipmentTypes.HEAD.get(), slots.getSlotLens(base + 3));

            base += PlayerInventoryLens.ARMOR;
            this.offhand = slots.getSlotLens(base);

            base += PlayerInventoryLens.OFFHAND;
            equipmentLenses.put(EquipmentTypes.OFF_HAND.get(), this.offhand);

            this.addSpanningChild(this.primary);
            this.addSpanningChild(this.armor);
            this.addSpanningChild(this.offhand);
        }

        equipmentLenses.put(EquipmentTypes.MAIN_HAND.get(), new HeldHandSlotLens(this.primary.getHotbar()));
        this.equipment = new EquipmentInventoryLens(equipmentLenses);

        for (Map.Entry<EquipmentType, SlotLens> entry : equipmentLenses.entrySet()) {
            this.addChild(entry.getValue(), KeyValuePair.of(Keys.EQUIPMENT_TYPE, entry.getKey()));
        }
        this.addChild(this.equipment);
        this.addMissingSpanningSlots(base, slots);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Inventory getAdapter(Fabric fabric, Inventory parent) {
        if (this.isContainer && fabric instanceof AbstractContainerMenu) {
            // If Lens is for Container extract the PlayerInventory
            AbstractContainerMenu container = (AbstractContainerMenu) fabric;
            Optional carrier = ((CarriedInventory) container).carrier();
            if (carrier.isPresent() && carrier.get() instanceof Player) {
                return ((Player) carrier.get()).inventory();
            }
        }
        return (Inventory) fabric.fabric$get(this.base).bridge$getAdapter();
    }

    public PrimaryPlayerInventoryLens getPrimaryInventoryLens() {
        return this.primary;
    }

    public EquipmentInventoryLens getEquipmentLens() {
        return this.equipment;
    }

    public SlotLens getOffhandLens() {
        return this.offhand;
    }

    public ArmorInventoryLens getArmorLens() {
        return this.armor;
    }
}

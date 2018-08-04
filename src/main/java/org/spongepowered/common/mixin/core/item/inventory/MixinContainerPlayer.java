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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.inventory.ContainerPlayer;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.inventory.IMixinContainerPlayer;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.CraftingOutputAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.LensProvider;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotLensCollection;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.container.ContainerPlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.CraftingOutputSlotLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.slots.EquipmentSlotLensImpl;

@Mixin(ContainerPlayer.class)
public abstract class MixinContainerPlayer extends MixinContainer implements IMixinContainerPlayer, LensProvider {

    @Override
    public Lens rootLens(Fabric fabric, InventoryAdapter adapter) {
        return new ContainerPlayerInventoryLens(adapter, inventory$getSlotProvider());
    }

    private int offHandSlot = -1;

    @Override
    public int getOffHandSlot() {
        return this.offHandSlot;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SlotProvider slotProvider(Fabric fabric, InventoryAdapter adapter) {
        SlotLensCollection.Builder builder = new SlotLensCollection.Builder()
                .add(1, CraftingOutputAdapter.class, (i) -> new CraftingOutputSlotLensImpl(i, (t) -> false, (t) -> false))
                .add(4)
                // TODO predicates for ItemStack/ItemType?
                // order for equipment is reversed in containers
                .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.HEADWEAR))
                .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.CHESTPLATE))
                .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.LEGGINGS))
                .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.BOOTS))
                .add(36)
                .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.OFF_HAND));

        if (this.offHandSlot == -1) {
            this.offHandSlot = builder.size() - 1;
        }

        builder.add(this.inventorySlots.size() - 46); // Add additional slots (e.g. from mods)
        return builder.build();
    }
}

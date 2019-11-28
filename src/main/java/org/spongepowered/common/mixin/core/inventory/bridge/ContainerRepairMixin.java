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
package org.spongepowered.common.mixin.core.inventory.bridge;

import net.minecraft.inventory.container.RepairContainer;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.inventory.LensProviderBridge;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.InputSlotAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.OutputSlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.item.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensCollection;
import org.spongepowered.common.inventory.lens.impl.comp.PrimaryPlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.container.ContainerLens;
import org.spongepowered.common.inventory.lens.impl.slot.InputSlotLens;
import org.spongepowered.common.inventory.lens.impl.slot.OutputSlotLens;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.InputSlotAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.OutputSlotAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.slots.SlotLensProvider;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.inventory.lens.impl.slots.SlotLensCollection;
import org.spongepowered.common.inventory.lens.impl.comp.PrimaryPlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensCollection;
import org.spongepowered.common.inventory.lens.impl.minecraft.container.ContainerLens;
import org.spongepowered.common.inventory.lens.impl.slots.InputSlotLens;
import org.spongepowered.common.inventory.lens.impl.slots.OutputSlotLens;

import java.util.ArrayList;
import java.util.List;

@Mixin(RepairContainer.class)
public abstract class ContainerRepairMixin implements LensProviderBridge {

    @Override
    public Lens bridge$rootLens(final Fabric fabric, final InventoryAdapter adapter) {
        final List<Lens> lenses = new ArrayList<>();
        lenses.add(new DefaultIndexedLens(0, 3, adapter.bridge$getSlotProvider()));
        lenses.add(new PrimaryPlayerInventoryLens(3, adapter.bridge$getSlotProvider(), true));
        return new ContainerLens(adapter.bridge$getFabric().fabric$getSize(), (Class<? extends Inventory>) adapter.getClass(), adapter.bridge$getSlotProvider(), lenses);
    }

    @Override
    public SlotLensProvider bridge$slotProvider(final Fabric fabric, final InventoryAdapter adapter) {
        final SlotLensCollection.Builder builder = new SlotLensCollection.Builder()
                .add(2, InputSlotAdapter.class, i -> new InputSlotLens(i, s -> true, t -> true))
                .add(1, OutputSlotAdapter.class, i -> new OutputSlotLens(i, s -> false, t -> false))
                .add(36);
        return builder.build();
    }

}

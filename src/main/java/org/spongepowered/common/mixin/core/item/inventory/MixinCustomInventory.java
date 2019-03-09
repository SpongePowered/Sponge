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

import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;

import java.util.Optional;

@SuppressWarnings("rawtypes")
@Mixin(CustomInventory.class)
public abstract class MixinCustomInventory implements MinecraftInventoryAdapter, Inventory, CarriedInventory<Carrier> {

    @Shadow(remap = false) private Carrier carrier;
    @Shadow(remap = false) private PluginContainer plugin;
    @Shadow(remap = false) private SlotProvider slots;
    @Shadow(remap = false) private Lens lens;

    @Override
    public Lens getRootLens() {
        return this.lens;
    }

    @Override
    public Optional<Carrier> getCarrier() {
        return Optional.ofNullable(this.carrier);
    }

    @Override
    public PluginContainer getPlugin() {
        return this.plugin;
    }

    @Override
    public SlotProvider getSlotProvider() {
        return this.slots;
    }
}

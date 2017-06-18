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
package org.spongepowered.common.mixin.core.entity.player;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.entity.UserInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.entity.player.SpongeUserInventory;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.MainPlayerInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.item.inventory.observer.InventoryEventArgs;

import java.util.List;
import java.util.Optional;

@Mixin(SpongeUserInventory.class)
public abstract class MixinSpongeUserInventory implements MinecraftInventoryAdapter, UserInventory<User> {

    @Shadow @Final public NonNullList<ItemStack> mainInventory;
    @Shadow @Final public NonNullList<ItemStack> armorInventory;
    @Shadow @Final public NonNullList<ItemStack> offHandInventory;
    @Shadow @Final private List<NonNullList<ItemStack>> allInventories;

    @Shadow public abstract int getInventoryStackLimit();

    @Shadow public abstract int getSizeInventory();

    protected SlotCollection slots;
    protected Fabric<IInventory> inventory;
    protected PlayerInventoryLens lens;

    private User carrier;
    private MainPlayerInventoryAdapter main;
    private EquipmentInventoryAdapter equipment;
    private SlotAdapter offhand;

    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructed(SpongeUser playerIn, CallbackInfo ci) {
        // We only care about Server inventories
        this.inventory = new DefaultInventoryFabric((IInventory) this);

        this.slots = new SlotCollection.Builder()
                .add(this.mainInventory.size())
                .add(this.offHandInventory.size())
                .add(this.armorInventory.size(), EquipmentSlotAdapter.class)
                .add(this.getSizeInventory() - this.mainInventory.size() - this.offHandInventory.size() - this.armorInventory.size())
                .build();

        this.carrier = ((User) playerIn);
        this.lens = new PlayerInventoryLens(this, this.slots);
    }

    @Override
    public Lens<IInventory, ItemStack> getRootLens() {
        return this.lens;
    }

    @Override
    public Fabric<IInventory> getInventory() {
        return this.inventory;
    }

    @Override
    public Inventory getChild(Lens<IInventory, ItemStack> lens) {
        return null;
    }

    @Override
    public Optional<User> getCarrier() {
        return Optional.ofNullable(this.carrier);
    }

    @Override
    public MainPlayerInventory getMain() {
        if (this.main == null) {
            this.main = (MainPlayerInventoryAdapter) this.lens.getMainLens().getAdapter(this.inventory, this);
        }
        return this.main;
    }

    @Override
    public EquipmentInventory getEquipment() {
        if (this.equipment == null) {
            this.equipment = (EquipmentInventoryAdapter) this.lens.getEquipmentLens().getAdapter(this.inventory, this);
        }
        return this.equipment;
    }

    @Override
    public Slot getOffhand() {
        if (this.offhand == null) {
            this.offhand = (SlotAdapter) this.lens.getOffhandLens().getAdapter(this.inventory, this);
        }
        return this.offhand;
    }

    @Override
    public void notify(Object source, InventoryEventArgs eventArgs) {
    }

    @Override
    public SlotProvider<IInventory, ItemStack> getSlotProvider() {
        return this.slots;
    }

}

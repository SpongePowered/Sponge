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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;
import org.spongepowered.common.item.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.GridInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.HotbarAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.MinecraftLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.item.inventory.observer.InventoryEventArgs;

import java.util.List;
import java.util.Optional;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer implements IMixinInventoryPlayer, PlayerInventory {

    @Shadow public int currentItem;
    @Shadow public EntityPlayer player;
    @Shadow @Final public NonNullList<ItemStack> mainInventory;
    @Shadow @Final public NonNullList<ItemStack> armorInventory;
    @Shadow @Final public NonNullList<ItemStack> offHandInventory;
    @Shadow @Final private List<NonNullList<ItemStack>> allInventories;

    @Shadow public abstract int getInventoryStackLimit();

    @Shadow public abstract int getSizeInventory();

    protected SlotCollection slots;
    protected Fabric<IInventory> inventory;
    protected MinecraftLens lens;

    private Player carrier;
    private HotbarAdapter hotbar;
    private GridInventoryAdapter main;
    private EquipmentInventoryAdapter equipment;
    private SlotAdapter offhand;

    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructed(EntityPlayer playerIn, CallbackInfo ci) {

        // Set Carrier if we got a real Player
        if (playerIn instanceof EntityPlayerMP) {
            this.carrier = (Player) playerIn;

            this.inventory = new DefaultInventoryFabric((IInventory) this);
            Class clazz = this.getClass();
            if (clazz == InventoryPlayer.class) { // Build Player Lens
                // We only care about Server inventories
                this.slots = new SlotCollection.Builder()
                        .add(this.mainInventory.size())
                        .add(this.offHandInventory.size())
                        .add(this.armorInventory.size(), EquipmentSlotAdapter.class)
                        // for mods providing bigger inventories
                        .add(this.getSizeInventory() - this.mainInventory.size() - this.offHandInventory.size() - this.armorInventory.size())
                        .build();
                this.lens = new PlayerInventoryLens(this, this.slots);
            } else if (this.getSizeInventory() != 0) { // Fallback OrderedLens when not 0 sized inventory
                this.slots = new SlotCollection.Builder().add(this.getSizeInventory()).build();
                this.lens = new OrderedInventoryLensImpl(0, this.getSizeInventory(), 1, slots);
            }
        }
    }

    @Override
    public Lens<IInventory, net.minecraft.item.ItemStack> getRootLens() {
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
    public Optional<Player> getCarrier() {
        return Optional.ofNullable(this.carrier);
    }

    @Override
    public Hotbar getHotbar() {
        if (this.hotbar == null && this.lens instanceof PlayerInventoryLens) {
            this.hotbar = (HotbarAdapter) ((PlayerInventoryLens) this.lens).getHotbarLens().getAdapter(this.inventory, this);
        }
        return this.hotbar;
    }

    @Override
    public GridInventory getMain() {
        if (this.main == null && this.lens instanceof PlayerInventoryLens) {
            this.main = (GridInventoryAdapter) ((PlayerInventoryLens) this.lens).getMainLens().getAdapter(this.inventory, this);
        }
        return this.main;
    }

    @Override
    public EquipmentInventory getEquipment() {
        if (this.equipment == null) {
            this.equipment = (EquipmentInventoryAdapter) ((PlayerInventoryLens) this.lens).getEquipmentLens().getAdapter(this.inventory, this);
        }
        return this.equipment;
    }

    @Override
    public Slot getOffhand() {
        if (this.offhand == null && this.lens instanceof PlayerInventoryLens) {
            this.offhand = (SlotAdapter) ((PlayerInventoryLens) this.lens).getOffhandLens().getAdapter(this.inventory, this);
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

    @Override
    public void setSelectedItem(int itemIndex, boolean notify) {
        itemIndex = itemIndex % 9;
        if (notify && this.player instanceof EntityPlayerMP) {
            SPacketHeldItemChange packet = new SPacketHeldItemChange(itemIndex);
            ((EntityPlayerMP)this.player).connection.sendPacket(packet);
        }
        this.currentItem = itemIndex;
    }

    /**
     * @author blood - October 7th, 2015
     * @reason Prevents inventory from being cleared until after events.
     */
    @Overwrite
    public void dropAllItems() { // dropAllItems
        for (NonNullList<ItemStack> aitemstack : this.allInventories)
        {
            for (int i = 0; i < aitemstack.size(); ++i)
            {
                if (!aitemstack.get(i).isEmpty())
                {
                    this.player.dropItem(aitemstack.get(i), true, false);
                    //aitemstack[i] = null; // Sponge - we handle this after calling the death event
                }
            }
        }
    }

    @Override
    public int getFirstAvailableSlot(ItemStack itemstack) {
        for (int i = 0; i < this.mainInventory.size(); ++i) {
            int stackSize = itemstack.getCount();

            if (this.mainInventory.get(i).getCount() == 0) {
                // empty slot
                return i;
            }

            if (this.mainInventory.get(i).getItem() == itemstack.getItem() && this.mainInventory.get(i).isStackable() && this.mainInventory.get(i).getCount() < this.mainInventory
                    .get(i).getMaxStackSize() && this.mainInventory.get(i).getCount() < this.getInventoryStackLimit() && (!this.mainInventory.get(i).getHasSubtypes() || this.mainInventory
                                                                                                                                                                                    .get(i).getItemDamage() == itemstack.getItemDamage()) && ItemStack.areItemStackTagsEqual(this.mainInventory
                    .get(i), itemstack)) {
                stackSize -= (this.mainInventory.get(i).getMaxStackSize() < this.getInventoryStackLimit() ? this.mainInventory.get(i).getMaxStackSize() : this.getInventoryStackLimit()) - this.mainInventory
                        .get(i).getCount();
            }

            if (stackSize <= 0) {
                // available space in slot
                return i;
            }
        }

        return -1;
    }
}

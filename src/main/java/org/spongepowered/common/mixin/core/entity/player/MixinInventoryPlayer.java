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
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;
import org.spongepowered.common.item.inventory.adapter.impl.comp.EquipmentInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.HotbarAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.comp.MainPlayerInventoryAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.EquipmentSlotAdapter;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.IInventoryFabric;
import org.spongepowered.common.item.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.slots.EquipmentSlotLensImpl;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer implements IMixinInventoryPlayer, PlayerInventory {

    @Shadow public int currentItem;
    @Shadow public EntityPlayer player;
    @Shadow @Final public NonNullList<ItemStack> mainInventory;
    @Shadow @Final public NonNullList<ItemStack> armorInventory;
    @Shadow @Final public NonNullList<ItemStack> offHandInventory;
    @Shadow @Final public List<NonNullList<ItemStack>> allInventories;

    @Shadow public abstract int getInventoryStackLimit();

    @Shadow public abstract int getSizeInventory();

    @Shadow public abstract ItemStack getStackInSlot(int index);

    @Shadow protected abstract int addResource(int p_191973_1_, ItemStack p_191973_2_);

    @Shadow public static int getHotbarSize() {
        throw new AbstractMethodError("Shadow");
    }

    private List<SlotTransaction> capturedTransactions = new ArrayList<>();
    private boolean doCapture = false;

    protected SlotCollection slots;
    protected Fabric inventory;
    protected Lens lens;

    private Player carrier;
    private HotbarAdapter hotbar;
    private MainPlayerInventoryAdapter main;
    @Nullable private EquipmentInventoryAdapter equipment;
    private SlotAdapter offhand;

    private int offhandIndex;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructed(EntityPlayer playerIn, CallbackInfo ci) {
        // Find offhand slot
        for (NonNullList<ItemStack> inventory : this.allInventories) {
            if (inventory == this.offHandInventory) {
                break;
            }
            this.offhandIndex += inventory.size();
        }

        // Set Carrier if we got a real Player
        if (playerIn instanceof EntityPlayerMP) {
            this.carrier = (Player) playerIn;

            this.inventory = new IInventoryFabric((IInventory) this);
            Class clazz = this.getClass();
            if (clazz == InventoryPlayer.class) { // Build Player Lens
                // We only care about Server inventories
                this.slots = new SlotCollection.Builder()
                        .add(this.mainInventory.size())
                        .add(this.offHandInventory.size(), EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.OFF_HAND))
                        // TODO predicates for ItemStack/ItemType?
                        .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.BOOTS))
                        .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.LEGGINGS))
                        .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.CHESTPLATE))
                        .add(EquipmentSlotAdapter.class, index -> new EquipmentSlotLensImpl(index, i -> true, t -> true, e -> e == EquipmentTypes.HEADWEAR))
                        // for mods providing bigger inventories
                        .add(this.armorInventory.size() - 4, EquipmentSlotAdapter.class)
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
    public int getHeldItemIndex(EnumHand hand) {
        switch (hand) {
            case MAIN_HAND:
                return this.currentItem;
            case OFF_HAND:
                return this.offhandIndex;
            default:
                throw new AssertionError(hand);
        }
    }

    @Override
    public Lens getRootLens() {
        return this.lens;
    }

    @Override
    public Fabric getFabric() {
        return this.inventory;
    }

    @Override
    public Inventory getChild(Lens lens) {
        return null;
    }

    @Override
    public Optional<Player> getCarrier() {
        return Optional.ofNullable(this.carrier);
    }

    @Override
    public MainPlayerInventory getMain() {
        if (this.main == null && this.lens instanceof PlayerInventoryLens) {
            this.main = (MainPlayerInventoryAdapter) ((PlayerInventoryLens) this.lens).getMainLens().getAdapter(this.inventory, this);
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

    @SuppressWarnings("unchecked")
    @Override
    public SlotProvider getSlotProvider() {
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

    @Override
    public List<SlotTransaction> getCapturedTransactions() {
        return this.capturedTransactions;
    }

    @Override
    public void setCapture(boolean doCapture) {
        this.doCapture = doCapture;
    }

    @Override
    public boolean capturesTransactions() {
        return this.doCapture;
    }

    public Slot getSpongeSlot(int index) {
        if (index < getHotbarSize()) {
            return this.getMain().getHotbar().getSlot(SlotIndex.of(index)).get();
        }
        index -= getHotbarSize();
        return this.getMain().getGrid().getSlot(SlotIndex.of(index)).get();
    }

    @Inject(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    public void onAdd(int index, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.doCapture) {
            // Capture "damaged" items picked up
            Slot slot = getSpongeSlot(index);
            this.capturedTransactions.add(new SlotTransaction(slot, ItemStackSnapshot.NONE, ItemStackUtil.snapshotOf(stack)));
        }
    }

    @Redirect(method = "storePartialItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;addResource(ILnet/minecraft/item/ItemStack;)I"))
    public int onAdd(InventoryPlayer inv, int index, ItemStack stack) {
        if (this.doCapture) {
            // Capture items getting picked up
            Slot slot = index == 40 ? this.getOffhand() : getSpongeSlot(index);
            ItemStackSnapshot original = ItemStackUtil.snapshotOf(this.getStackInSlot(index));
            int result = this.addResource(index, stack);
            ItemStackSnapshot replacement = ItemStackUtil.snapshotOf(this.getStackInSlot(index));
            this.capturedTransactions.add(new SlotTransaction(slot, original, replacement));
            return result;
        }
        return this.addResource(index, stack);

    }
}

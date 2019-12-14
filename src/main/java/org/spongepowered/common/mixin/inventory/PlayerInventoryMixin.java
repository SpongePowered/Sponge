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
package org.spongepowered.common.mixin.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.entity.player.PlayerInventoryBridge;
import org.spongepowered.common.bridge.inventory.LensGeneratorBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.DefaultIndexedLens;
import org.spongepowered.common.inventory.lens.impl.LensRegistrar;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.SlotLensProvider;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.entity.player.PlayerInventory.class)
public abstract class PlayerInventoryMixin implements PlayerInventoryBridge, InventoryAdapter, LensGeneratorBridge, TrackedInventoryBridge {

    @Shadow public int currentItem;
    @Shadow public PlayerEntity player;
    @Shadow @Final public NonNullList<ItemStack> mainInventory;
    @Shadow @Final public NonNullList<ItemStack> armorInventory;
    @Shadow @Final public NonNullList<ItemStack> offHandInventory;
    @Shadow @Final private List<NonNullList<ItemStack>> allInventories;

    @Shadow public abstract int getSizeInventory();
    @Shadow public abstract ItemStack getStackInSlot(int index);
    @Shadow protected abstract int addResource(int p_191973_1_, ItemStack p_191973_2_);
    @Shadow public static int getHotbarSize() {
        throw new AbstractMethodError("Shadow");
    }
    @Shadow private int timesChanged;

    private List<SlotTransaction> impl$capturedTransactions = new ArrayList<>();
    private int impl$lastTimesChanged = this.timesChanged;
    private boolean impl$doCapture = false;
    private int impl$offhandIndex;

    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructed(final PlayerEntity playerIn, final CallbackInfo ci) {
        // Find offhand slot
        for (final NonNullList<ItemStack> inventory : this.allInventories) {
            if (inventory == this.offHandInventory) {
                break;
            }
            this.impl$offhandIndex += inventory.size();
        }
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public SlotLensProvider lensGeneratorBridge$generateSlotLensProvider() {
        return new LensRegistrar.BasicSlotLensProvider(this.getSizeInventory());
    }

    @SuppressWarnings({"Unchecked"})
    @Override
    public Lens lensGeneratorBridge$generateLens(SlotLensProvider slotLensProvider) {
        if ((Class<?>) this.getClass() == net.minecraft.entity.player.PlayerInventory.class) { // Build Player Lens
            return new PlayerInventoryLens(this.getSizeInventory(), this.getClass(), slotLensProvider);
        }
        return new DefaultIndexedLens(0, this.getSizeInventory(), slotLensProvider);
    }

    @Override
    public int bridge$getHeldItemIndex(final Hand hand) {
        switch (hand) {
            case MAIN_HAND:
                return this.currentItem;
            case OFF_HAND:
                return this.impl$offhandIndex;
            default:
                throw new AssertionError(hand);
        }
    }

    @Override
    public void bridge$setSelectedItem(int itemIndex, final boolean notify) {
        itemIndex = itemIndex % 9;
        if (notify && this.player instanceof ServerPlayerEntity) {
            final SHeldItemChangePacket packet = new SHeldItemChangePacket(itemIndex);
            ((ServerPlayerEntity)this.player).connection.sendPacket(packet);
        }
        this.currentItem = itemIndex;
    }

    @Override
    public List<SlotTransaction> bridge$getCapturedSlotTransactions() {
        return this.impl$capturedTransactions;
    }

    @Override
    public void bridge$setCaptureInventory(final boolean doCapture) {
        this.impl$doCapture = doCapture;
    }

    @Override
    public boolean bridge$capturingInventory() {
        return this.impl$doCapture;
    }

    private Slot impl$getSpongeSlotByIndex(int index) {
        if (index < getHotbarSize()) {
            return ((PlayerInventory) this).getPrimary().getHotbar().getSlot(index).get();
        }
        index -= getHotbarSize();
        return ((PlayerInventory) this).getPrimary().getStorage().getSlot(index).get();
    }

    @Inject(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void impl$ifCaptureDoTransactions(final int index, final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
        if (this.impl$doCapture) {
            // Capture "damaged" items picked up
            final Slot slot = this.impl$getSpongeSlotByIndex(index);
            this.impl$capturedTransactions.add(new SlotTransaction(slot, ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(stack)));
        }
    }

    @Redirect(method = "storePartialItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;addResource(ILnet/minecraft/item/ItemStack;)I"))
    private int impl$ifCaptureDoTransactions(final net.minecraft.entity.player.PlayerInventory inv, final int index, final ItemStack stack) {
        if (this.impl$doCapture) {
            // Capture items getting picked up
            final Slot slot = index == 40 ? ((PlayerInventory) this).getOffhand() : this.impl$getSpongeSlotByIndex(index);
            final ItemStackSnapshot original = ItemStackUtil.snapshotOf(this.getStackInSlot(index));
            final int result = this.addResource(index, stack);
            final ItemStackSnapshot replacement = ItemStackUtil.snapshotOf(this.getStackInSlot(index));
            this.impl$capturedTransactions.add(new SlotTransaction(slot, original, replacement));
            return result;
        }
        return this.addResource(index, stack);

    }

    @Override
    public void bridge$cleanupDirty() {
        if (this.timesChanged != this.impl$lastTimesChanged) {
            this.player.openContainer.detectAndSendChanges();
        }
    }

    @Override
    public void bridge$markClean() {
        this.impl$lastTimesChanged = this.timesChanged;
    }
}

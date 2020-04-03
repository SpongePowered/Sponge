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
package org.spongepowered.common.mixin.inventory.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.entity.LivingEntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerInventoryBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.comp.HotbarLens;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.inventory.lens.impl.slot.BasicSlotLens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.EnumMap;
import java.util.Optional;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public abstract ItemStack getItemStackFromSlot(EquipmentSlotType slotIn);
    @Shadow public abstract void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack);

    private EnumMap<EquipmentSlotType, SlotLens> slotLens = new EnumMap<>(EquipmentSlotType.class);

    @Surrogate
    private void onGetItemStackFromSlot(final CallbackInfo ci, final EquipmentSlotType[] slots, final int j, final int k,
            final EquipmentSlotType entityEquipmentSlot, final ItemStack before) {
        this.onGetItemStackFromSlot(ci, 0, slots, j, k, entityEquipmentSlot, before);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "tick", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getItemStackFromSlot(Lnet/minecraft/inventory/EquipmentSlotType;)Lnet/minecraft/item/ItemStack;"))
    private void onGetItemStackFromSlot(final CallbackInfo ci, final int i_unused, final EquipmentSlotType[] slots, final int j, final int k,
            final EquipmentSlotType entityEquipmentSlot, final ItemStack before) {
        if (((Entity)(Object)this).ticksExisted == 1 && (LivingEntity) (Object) this instanceof PlayerEntity) {
            return; // Ignore Equipment on player spawn/respawn
        }
        final ItemStack after = this.getItemStackFromSlot(entityEquipmentSlot);
        final LivingEntity entity = (LivingEntity) (LivingEntityBridge) this;
        if (!ItemStack.areItemStacksEqual(after, before)) {
            final Inventory slotAdapter;
            if (entity instanceof ServerPlayerEntity) {
                final SlotLens slotLens;
                final PlayerInventoryBridge inventory = (PlayerInventoryBridge) ((ServerPlayerEntity) entity).inventory;
                final Lens inventoryLens = ((InventoryAdapter) inventory).inventoryAdapter$getRootLens();
                if (inventoryLens instanceof PlayerInventoryLens) {
                    switch (entityEquipmentSlot) {
                        case OFFHAND:
                            slotLens = ((PlayerInventoryLens) inventoryLens).getOffhandLens();
                            break;
                        case MAINHAND:
                            final HotbarLens hotbarLens = ((PlayerInventoryLens) inventoryLens).getPrimaryInventoryLens().getHotbar();
                            slotLens = hotbarLens.getSlotLens(hotbarLens.getSelectedSlotIndex(((InventoryAdapter) inventory).inventoryAdapter$getFabric()));
                            break;
                        default:
                            slotLens = ((PlayerInventoryLens) inventoryLens).getEquipmentLens().getSlotLens(entityEquipmentSlot.getIndex());
                    }
                } else {
                    slotLens = inventoryLens.getSlotLens(entityEquipmentSlot.getIndex());
                }

                slotAdapter = slotLens.getAdapter(((InventoryAdapter) inventory).inventoryAdapter$getFabric(), (Inventory) inventory);
            } else {
                if (this.slotLens.isEmpty()) {
                    for (final EquipmentSlotType slot : EquipmentSlotType.values()) {
                        this.slotLens.put(slot, new BasicSlotLens(slot.getSlotIndex()));
                    }
                }
                slotAdapter = this.slotLens.get(entityEquipmentSlot).getAdapter((Fabric) this, null);
            }
            final ChangeEntityEquipmentEvent event = InventoryEventFactory.callChangeEntityEquipmentEvent(entity,
                    ItemStackUtil.snapshotOf(before), ItemStackUtil.snapshotOf(after), (SlotAdapter) slotAdapter);
            if (event.isCancelled()) {
                this.setItemStackToSlot(entityEquipmentSlot, before);
                return;
            }
            final Transaction<ItemStackSnapshot> transaction = event.getTransaction();
            if (!transaction.isValid()) {
                this.setItemStackToSlot(entityEquipmentSlot, before);
                return;
            }
            final Optional<ItemStackSnapshot> optional = transaction.getCustom();
            if (optional.isPresent()) {
                final ItemStack custom = ItemStackUtil.fromSnapshotToNative(optional.get());
                this.setItemStackToSlot(entityEquipmentSlot, custom);
            }
        }
    }

}

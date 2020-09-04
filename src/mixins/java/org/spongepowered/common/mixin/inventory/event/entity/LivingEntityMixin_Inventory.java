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
package org.spongepowered.common.mixin.inventory.event.entity;

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
public abstract class LivingEntityMixin_Inventory {

    @Shadow public abstract ItemStack getItemStackFromSlot(EquipmentSlotType slotIn);
    @Shadow public abstract void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack);

    private final EnumMap<EquipmentSlotType, SlotLens> slotLens = new EnumMap<>(EquipmentSlotType.class);

    @Inject(method = "tick", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getItemStackFromSlot(Lnet/minecraft/inventory/EquipmentSlotType;)Lnet/minecraft/item/ItemStack;"))
    private void inventory$throwInventoryEvent(final CallbackInfo ci, final int i, final int j, final EquipmentSlotType[] var3, final int var4, final int var5, final EquipmentSlotType equipmentslottype, final ItemStack itemstack) {
        this.inventory$throwInventoryEvent(ci, var3, var4, var5, equipmentslottype, itemstack);
    }

    @Surrogate
    private void inventory$throwInventoryEvent(final CallbackInfo ci, final EquipmentSlotType[] var3, final int var4, final int var5, final EquipmentSlotType equipmentslottype, final ItemStack itemstack) {
        if (((Entity)(Object)this).ticksExisted == 1 && (LivingEntity) (Object) this instanceof PlayerEntity) {
            return; // Ignore Equipment on player spawn/respawn
        }
        final ItemStack after = this.getItemStackFromSlot(equipmentslottype);
        final LivingEntity entity = (LivingEntity) (Object) this;
        if (!ItemStack.areItemStacksEqual(after, itemstack)) {
            final Inventory slotAdapter;
            if (entity instanceof ServerPlayerEntity) {
                final SlotLens slotLens;
                final PlayerInventoryBridge inventory = (PlayerInventoryBridge) ((ServerPlayerEntity) entity).inventory;
                final Lens inventoryLens = ((InventoryAdapter) inventory).inventoryAdapter$getRootLens();
                final Fabric fabric = ((InventoryAdapter) inventory).inventoryAdapter$getFabric();
                if (inventoryLens instanceof PlayerInventoryLens) {
                    switch (equipmentslottype) {
                        case OFFHAND:
                            slotLens = ((PlayerInventoryLens) inventoryLens).getOffhandLens();
                            break;
                        case MAINHAND:
                            final HotbarLens hotbarLens = ((PlayerInventoryLens) inventoryLens).getPrimaryInventoryLens().getHotbar();
                            slotLens = hotbarLens.getSlotLens(fabric, hotbarLens.getSelectedSlotIndex(fabric));
                            break;
                        default:
                            slotLens = ((PlayerInventoryLens) inventoryLens).getEquipmentLens().getSlotLens(fabric, equipmentslottype.getIndex());
                    }
                } else {
                    slotLens = inventoryLens.getSlotLens(fabric, equipmentslottype.getIndex());
                }

                slotAdapter = slotLens.getAdapter(fabric, (Inventory) inventory);
            } else {
                if (this.slotLens.isEmpty()) {
                    for (final EquipmentSlotType slot : EquipmentSlotType.values()) {
                        this.slotLens.put(slot, new BasicSlotLens(slot.getSlotIndex()));
                    }
                }
                slotAdapter = this.slotLens.get(equipmentslottype).getAdapter((Fabric) this, null);
            }
            final ChangeEntityEquipmentEvent event = InventoryEventFactory.callChangeEntityEquipmentEvent(entity,
                    ItemStackUtil.snapshotOf(itemstack), ItemStackUtil.snapshotOf(after), (SlotAdapter) slotAdapter);
            if (event.isCancelled()) {
                this.setItemStackToSlot(equipmentslottype, itemstack);
                return;
            }
            final Transaction<ItemStackSnapshot> transaction = event.getTransaction();
            if (!transaction.isValid()) {
                this.setItemStackToSlot(equipmentslottype, itemstack);
                return;
            }
            final Optional<ItemStackSnapshot> optional = transaction.getCustom();
            if (optional.isPresent()) {
                final ItemStack custom = ItemStackUtil.fromSnapshotToNative(optional.get());
                this.setItemStackToSlot(equipmentslottype, custom);
            }
        }
    }

}

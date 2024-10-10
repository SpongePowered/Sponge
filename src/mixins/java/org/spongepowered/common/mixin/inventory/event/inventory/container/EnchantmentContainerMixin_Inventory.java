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
package org.spongepowered.common.mixin.inventory.event.inventory.container;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import org.spongepowered.api.event.item.inventory.EnchantItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;

@Mixin(value = EnchantmentMenu.class)
public abstract class EnchantmentContainerMixin_Inventory {

    @Shadow @Final private DataSlot enchantmentSeed;
    @Shadow @Final private Container enchantSlots;

    private ItemStackSnapshot prevItem;
    private ItemStackSnapshot prevLapis;

    // onCraftMatrixChanged lambda
    @Redirect(method = "lambda$slotsChanged$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"), require = 1)
    private int impl$onCalcItemStackEnchantability(
        final RandomSource random, final int option, final int power, final ItemStack itemStack) {
        int levelRequirement = EnchantmentHelper.getEnchantmentCost(random, option, power, itemStack);
        levelRequirement = InventoryEventFactory.callEnchantEventLevelRequirement((EnchantmentMenu)(Object) this, this.enchantmentSeed.get(), option, power, itemStack, levelRequirement);
        return levelRequirement;
    }

    @Inject(method = "getEnchantmentList", cancellable = true, at = @At(value = "RETURN"))
    private void impl$onBuildEnchantmentList(final RegistryAccess ra,
        final ItemStack stack, final int enchantSlot, final int level, final CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        final List<EnchantmentInstance> newList = InventoryEventFactory
                .callEnchantEventEnchantmentList((EnchantmentMenu) (Object) this, this.enchantmentSeed.get(), stack, enchantSlot, level, cir.getReturnValue());

        if (cir.getReturnValue() != newList) {
            cir.setReturnValue(newList);
        }
    }

    // enchantItem lambda
    @Inject(method = "lambda$clickMenuButton$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onEnchantmentPerformed(Lnet/minecraft/world/item/ItemStack;I)V"), require = 1)
    private void impl$beforeEnchantItem(final CallbackInfo ci) {
        this.prevItem = ItemStackUtil.snapshotOf(this.enchantSlots.getItem(0));
        this.prevLapis = ItemStackUtil.snapshotOf(this.enchantSlots.getItem(1));
    }

    // enchantItem lambda
    @Inject(method = "lambda$clickMenuButton$1", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/ResourceLocation;)V"), require = 1)
    private void impl$afterEnchantItem(final ItemStack itemstack, final int id, final Player playerIn, final int i, final ItemStack itemstack1, final Level arg5, final BlockPos arg6, final CallbackInfo ci) {
        final ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(this.enchantSlots.getItem(0));
        final ItemStackSnapshot newLapis = ItemStackUtil.snapshotOf(this.enchantSlots.getItem(1));

        final org.spongepowered.api.item.inventory.Container container = ContainerUtil.fromNative((AbstractContainerMenu) (Object) this);

        final Slot slotItem = ((InventoryAdapter) container).inventoryAdapter$getSlot(0).get();
        final Slot slotLapis = ((InventoryAdapter) container).inventoryAdapter$getSlot(1).get();

        final EnchantItemEvent.Post event =
                InventoryEventFactory.callEnchantEventEnchantPost(playerIn, (EnchantmentMenu) (Object) this,
                        new SlotTransaction(slotItem, this.prevItem, newItem),
                        new SlotTransaction(slotLapis, this.prevLapis, newLapis),
                        id, this.enchantmentSeed.get());

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}

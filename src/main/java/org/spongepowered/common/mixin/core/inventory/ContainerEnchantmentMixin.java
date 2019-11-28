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
package org.spongepowered.common.mixin.core.inventory;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Random;

@Mixin(value = ContainerEnchantment.class)
public abstract class ContainerEnchantmentMixin {

    @Shadow @Final private Random rand;
    @Shadow public int xpSeed;
    @Shadow public IInventory tableInventory;

    private ItemStackSnapshot prevItem;
    private ItemStackSnapshot prevLapis;

    @Redirect(method = "onCraftMatrixChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;calcItemStackEnchantability(Ljava/util/Random;IILnet/minecraft/item/ItemStack;)I"))
    private int impl$onCalcItemStackEnchantability(Random random, int option, int power, ItemStack itemStack) {
        int levelRequirement = EnchantmentHelper.func_77514_a(random, option, power, itemStack);
        levelRequirement = SpongeCommonEventFactory.callEnchantEventLevelRequirement((ContainerEnchantment)(Object) this, this.xpSeed, option, power, itemStack, levelRequirement);
        return levelRequirement;
    }

    @Inject(method = "getEnchantmentList", cancellable = true, at = @At(value = "RETURN"))
    private void impl$onBuildEnchantmentList(ItemStack stack, int enchantSlot, int level, CallbackInfoReturnable<List<EnchantmentData>> cir) {
        List<EnchantmentData> newList = SpongeCommonEventFactory
                .callEnchantEventEnchantmentList((ContainerEnchantment) (Object) this, this.xpSeed, stack, enchantSlot, level, cir.getReturnValue());

        if (cir.getReturnValue() != newList) {
            cir.setReturnValue(newList);
        }
    }

    @Inject(method = "enchantItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;onEnchant(Lnet/minecraft/item/ItemStack;I)V"))
    private void impl$beforeEnchantItem(EntityPlayer playerIn, int option, CallbackInfoReturnable<Boolean> cir) {
        this.prevItem = ItemStackUtil.snapshotOf(this.tableInventory.func_70301_a(0));
        this.prevLapis = ItemStackUtil.snapshotOf(this.tableInventory.func_70301_a(1));
    }

    @Inject(method = "enchantItem", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;addStat(Lnet/minecraft/stats/StatBase;)V"))
    private void impl$afterEnchantItem(EntityPlayer playerIn, int option, CallbackInfoReturnable<Boolean> cir) {
        ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(this.tableInventory.func_70301_a(0));
        ItemStackSnapshot newLapis = ItemStackUtil.snapshotOf(this.tableInventory.func_70301_a(1));

        org.spongepowered.api.item.inventory.Container container = ContainerUtil.fromNative((Container) (Object) this);

        Slot slotItem = ((InventoryAdapter) container).bridge$getSlot(0).get();
        Slot slotLapis = ((InventoryAdapter) container).bridge$getSlot(1).get();

        EnchantItemEvent.Post event =
                SpongeCommonEventFactory.callEnchantEventEnchantPost(playerIn, (ContainerEnchantment) (Object) this,
                        new SlotTransaction(slotItem, this.prevItem, newItem),
                        new SlotTransaction(slotLapis, this.prevLapis, newLapis),
                        option, this.xpSeed);

        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }



}

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
package org.spongepowered.common.mixin.inventory.event.world.inventory;

import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin_Inventory {

    /**
     * Captures the change in the crafting output slot
     *
     * old method name: Container#slotChangedCraftingGrid
     */
    @Inject(method = "slotChangedCraftingGrid", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultContainer;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private static void beforeSlotChangedCraftingGrid(int p_217066_0_, final Level p_217066_1_, final Player p_217066_2_,
            final net.minecraft.world.inventory.CraftingContainer p_217066_3_, final ResultContainer p_217066_4_, final CallbackInfo ci,
            ServerPlayer serverPlayerEntity, ItemStack itemStack) {
        TrackedInventoryBridge trackedContainer = (TrackedInventoryBridge) p_217066_2_.containerMenu;
        TrackedContainerBridge container = (TrackedContainerBridge) p_217066_2_.containerMenu;

        // Capture Inventory is true when caused by a vanilla inventory packet
        // This is to prevent infinite loops when a client mod re-requests the recipe result after we modified/cancelled it
        if (trackedContainer.bridge$capturingInventory()) {
            List<SlotTransaction> craftPreviewTransactions = container.bridge$getPreviewTransactions();
            ItemStackSnapshot orig = ItemStackUtil.snapshotOf(p_217066_4_.getItem(0));
            if (!craftPreviewTransactions.isEmpty()) {
                orig = craftPreviewTransactions.get(0).original();
            }
            craftPreviewTransactions.clear();
            final ItemStackSnapshot repl = ItemStackUtil.snapshotOf(itemStack);
            Slot slot = ((InventoryAdapter) p_217066_2_.containerMenu).inventoryAdapter$getSlot(0).get();
            craftPreviewTransactions.add(new SlotTransaction(slot, orig, repl));
        }
    }

    /**
     * Fires {@link CraftItemEvent.Preview} if active
     *
     * old method name: Container#slotChangedCraftingGrid
     */
    @Inject(method = "slotChangedCraftingGrid", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private static void afterSlotChangedCraftingGrid(int p_217066_0_,
            final Level world, final Player player, final net.minecraft.world.inventory.CraftingContainer craftingInventory, final ResultContainer output, final CallbackInfo ci) {

        TrackedContainerBridge container = (TrackedContainerBridge) player.containerMenu;

        List<SlotTransaction> craftPreviewTransactions = container.bridge$getPreviewTransactions();
        if (container.bridge$firePreview() && !craftPreviewTransactions.isEmpty()) {
            // TODO can we just check the craftingInventory variable?
            for (net.minecraft.world.inventory.Slot slot : player.containerMenu.slots) {

            }

            final Inventory inv = ((Inventory) player.containerMenu).query(QueryTypes.INVENTORY_TYPE.get().of(CraftingInventory.class));
            if (!(inv instanceof CraftingInventory)) {
                SpongeCommon.logger().warn("Detected crafting but Sponge could not get a CraftingInventory for " + player.containerMenu.getClass().getName());
                return;
            }
            final SlotTransaction previewTransaction = craftPreviewTransactions.get(craftPreviewTransactions.size() - 1);

            final net.minecraft.world.item.crafting.CraftingRecipe recipe = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInventory, world).orElse(null);
            InventoryEventFactory.callCraftEventPre(
                    player, ((CraftingInventory) inv), previewTransaction, ((CraftingRecipe) recipe),
                    player.containerMenu, craftPreviewTransactions);
            craftPreviewTransactions.clear();
        }
    }

}

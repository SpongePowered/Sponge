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
package org.spongepowered.common.mixin.inventory.impl.world.entity.player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.entity.player.PlayerInventoryBridge;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(Inventory.class)
public abstract class InventoryMixin_Bridge_Inventory implements PlayerInventoryBridge, InventoryAdapter {

    @Shadow public int selected;
    @Shadow @Final public Player player;
    @Shadow @Final public NonNullList<ItemStack> items;
    @Shadow @Final public NonNullList<ItemStack> armor;
    @Shadow @Final public NonNullList<ItemStack> offhand;
    @Shadow @Final private List<NonNullList<ItemStack>> compartments;

    @Shadow private int timesChanged;

    private int impl$lastTimesChanged = this.timesChanged;

    private int impl$offhandIndex;

    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructed(final Player playerIn, final CallbackInfo ci) {
        // Find offhand slot
        for (final NonNullList<ItemStack> inventory : this.compartments) {
            if (inventory == this.offhand) {
                break;
            }
            this.impl$offhandIndex += inventory.size();
        }
    }

    @Override
    public int bridge$getHeldItemIndex(final InteractionHand hand) {
        switch (hand) {
            case MAIN_HAND:
                return this.selected;
            case OFF_HAND:
                return this.impl$offhandIndex;
            default:
                throw new AssertionError(hand);
        }
    }

    @Override
    public void bridge$setSelectedItem(int itemIndex, final boolean notify) {
        itemIndex = itemIndex % 9;
        if (notify && this.player instanceof ServerPlayer) {
            final ClientboundSetCarriedItemPacket packet = new ClientboundSetCarriedItemPacket(itemIndex);
            ((ServerPlayer)this.player).connection.send(packet);
        }
        this.selected = itemIndex;
    }

    @Override
    public void bridge$cleanupDirty() {
        if (this.timesChanged != this.impl$lastTimesChanged) {
            this.player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public void bridge$markClean() {
        this.impl$lastTimesChanged = this.timesChanged;
    }
}

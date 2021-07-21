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
package org.spongepowered.common.event.tracking.context.transaction;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.item.util.ItemStackUtil;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClickMenuTransaction extends ContainerBasedTransaction {

    private final ServerPlayer player;
    private final int slotNum;
    private final int buttonNum;
    private final ClickType clickType;
    private final @Nullable Slot slot;
    private final ItemStackSnapshot cursor;

    public ClickMenuTransaction(
        final Player player, final AbstractContainerMenu menu, final int slotNum, final int buttonNum,
        final ClickType clickType,
        final @Nullable Slot slot,
        final ItemStackSnapshot cursor
    ) {
        super(((ServerWorld) player.level).key(), menu);
        this.player = (ServerPlayer) player;
        this.slotNum = slotNum;
        this.buttonNum = buttonNum;
        this.clickType = clickType;
        this.slot = slot;
        this.cursor = cursor;
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final ImmutableList<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {
        // Search for crafting preview slot changes
        for (net.minecraft.world.inventory.Slot slot : this.player.containerMenu.slots) {
            if (slot.container instanceof CraftingContainer) {
                final CraftingContainer mcCraftInv = ((CraftingContainer) slot.container);
                final Optional<net.minecraft.world.item.crafting.CraftingRecipe> recipe = this.player.getServer().getRecipeManager().getRecipeFor(
                        RecipeType.CRAFTING, mcCraftInv, this.player.level);
                final CraftingInventory craftInv = ((Inventory) player.containerMenu).query(CraftingInventory.class).get();
                final List<SlotTransaction> previewTransactions = new ArrayList<>(slotTransactions);


//                final CraftItemEvent.Preview previewEvent = InventoryEventFactory.callCraftEventPreview(player, craftInv,
//                        ((CraftingRecipe) recipe.orElse(null)), player.containerMenu, previewTransactions);

                // TODO adjust slotTransaction list
                break;
            }
        }

        // TODO ClickContainerEvent.Double is missing transactions - they are in the side-effect?

        final ItemStackSnapshot resultingCursor = ItemStackUtil.snapshotOf(this.player.inventory.getCarried());
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(this.cursor, resultingCursor);
        final @Nullable ClickContainerEvent event = context.createContainerEvent(cause, this.player, (Container) this.menu,
            cursorTransaction, slotTransactions, entities, this.buttonNum, this.slot);

        return Optional.ofNullable(event);
    }

    @Override
    public void restore(PhaseContext<@NonNull ?> context, ClickContainerEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    public void postProcessEvent(PhaseContext<@NonNull ?> context, ClickContainerEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    boolean isContainerEventAllowed(
        final PhaseContext<@Nullable ?> context
    ) {
        if (!(context instanceof InventoryPacketContext)) {
            return false;
        }
        final int containerId = ((InventoryPacketContext) context).<ServerboundContainerClickPacket>getPacket().getContainerId();
        return containerId != this.player.containerMenu.containerId;
    }

    @Override
    Optional<SlotTransaction> getSlotTransaction() {
        return Optional.empty();
    }

}

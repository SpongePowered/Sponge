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

import com.google.common.collect.ImmutableList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.crafting.CraftingOutput;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PlaceRecipeTransaction extends ContainerBasedTransaction {

    private final ServerPlayer player;
    private final ItemStackSnapshot originalCursor;
    private boolean shift;
    private Recipe<?> recipe;
    private Inventory craftingInventory;

    public PlaceRecipeTransaction(final ServerPlayer player, final boolean shift, final Recipe<?> recipe, Inventory craftingInventory) {
        super(((ServerWorld) player.level).key(), player.containerMenu);
        this.player = player;
        this.originalCursor = ItemStackUtil.snapshotOf(player.inventory.getCarried());
        this.shift = shift;
        this.recipe = recipe;
        this.craftingInventory = craftingInventory;
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final ImmutableList<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {

        // TODO Preview Event handling
        // Get Preview Transactions - if empty create transaction on output slot anyways
        final List<SlotTransaction> previewTransactions = ((TrackedContainerBridge) this.menu).bridge$getPreviewTransactions();
        if (previewTransactions.isEmpty()) {
            final CraftingOutput slot = ((CraftingInventory) this.craftingInventory).result();
            final SlotTransaction st = new SlotTransaction(slot, ItemStackSnapshot.empty(), slot.peek().createSnapshot());
            previewTransactions.add(st);
        }
        // Fire Preview Event
        InventoryEventFactory.callCraftEventPre(player, ((CraftingInventory) this.craftingInventory), previewTransactions.get(0),
                ((CraftingRecipe) recipe), this.menu, previewTransactions);
        previewTransactions.clear();

        // Recipe event
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(this.originalCursor, ItemStackUtil.snapshotOf(player.inventory.getCarried()));
        ClickContainerEvent.Recipe event;
        if (shift) {
            event = SpongeEventFactory.createClickContainerEventRecipeAll(cause, (Container) this.menu,
                    cursorTransaction, ((CraftingRecipe) recipe), Optional.empty(), slotTransactions);
        } else {
            event = SpongeEventFactory.createClickContainerEventRecipeSingle(cause, (Container) this.menu,
                    cursorTransaction, ((CraftingRecipe) recipe), Optional.empty(), slotTransactions);
        }
        return Optional.of(event);
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final ClickContainerEvent event) {
        // TODO cancel/post-processing
        if (event.isCancelled() || !event.cursorTransaction().isValid()) {
            PacketPhaseUtil.handleCustomCursor(player, event.cursorTransaction().original());
        } else {
            PacketPhaseUtil.handleCustomCursor(player, event.cursorTransaction().finalReplacement());
        }
        PacketPhaseUtil.handleSlotRestore(player, player.containerMenu, event.transactions(), event.isCancelled());
    }

    @Override
    boolean isContainerEventAllowed(final PhaseContext<@Nullable ?> context) {
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

    @Override
    List<Entity> getEntitiesSpawned() {
        return Collections.emptyList();
    }

}

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CraftingTransaction extends ContainerBasedTransaction {

    private final ServerPlayer player;
    @Nullable private ItemStack craftedStack;
    private CraftingInventory craftingInventory;
    private CraftingContainer craftSlots;
    private CraftingRecipe lastRecipe;
    private ItemStackSnapshot prevCursor;

    public CraftingTransaction(
            final Player player, @Nullable final ItemStack craftedStack, final CraftingInventory craftingInventory,
            @Nullable final ItemStack prevCursor, CraftingContainer craftSlots, CraftingRecipe lastRecipe) {
        super(((ServerWorld) player.level).key(), player.containerMenu);
        this.player = (ServerPlayer) player;
        this.craftedStack = craftedStack;
        this.craftingInventory = craftingInventory;
        this.craftSlots = craftSlots;
        this.lastRecipe = lastRecipe;
        // Get previous cursor if captured or the current one
        this.prevCursor = ItemStackUtil.snapshotOf(prevCursor == null ? player.inventory.getCarried() : prevCursor);
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final ImmutableList<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {
        SlotTransaction first = null;
        // TODO better way to do this?
        // retain only last slot-transactions on output slot
        for (final Iterator<SlotTransaction> iterator = slotTransactions.iterator(); iterator.hasNext(); ) {
            final SlotTransaction trans = iterator.next();
            Optional<Integer> slotIndex = trans.slot().get(Keys.SLOT_INDEX);
            if (slotIndex.isPresent() && slotIndex.get() == 0) {
                iterator.remove();
                if (first == null) {
                    first = trans;
                }
            }
        }

        final ItemStackSnapshot craftedItem;
        // if we got a transaction on the crafting-slot use this
        if (first != null) {
            slotTransactions.add(first);
            craftedItem = first.original().copy();
        } else {
            craftedItem = ItemStackUtil.snapshotOf(this.craftedStack);
        }

        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(this.prevCursor, ItemStackUtil.snapshotOf(player.inventory.getCarried()));

        final CraftItemEvent.Craft event =
                SpongeEventFactory.createCraftItemEventCraft(PhaseTracker.getCauseStackManager().currentCause(),
                        ContainerUtil.fromNative(this.menu), craftedItem, this.craftingInventory, cursorTransaction,
                        Optional.ofNullable(this.lastRecipe), Optional.of(this.craftingInventory.result()), slotTransactions);

        return Optional.of(event);
    }

    @Override
    public void restore(PhaseContext<@NonNull ?> context, ClickContainerEvent event) {
        this.postProcessEvent(context, event);

    }

    @Override
    public void postProcessEvent(PhaseContext<@NonNull ?> context, ClickContainerEvent event) {
        PacketPhaseUtil.handleSlotRestore(this.player, this.menu, event.transactions(), event.isCancelled());
        PacketPhaseUtil.handleCursorRestore(this.player, event.cursorTransaction());

        try (EffectTransactor ignored = context.getTransactor().logCraftingPreview(this.player, this.craftingInventory, this.craftSlots)) {
            ((TrackedContainerBridge) this.menu).bridge$detectAndSendChanges(true); // capture changes for preview
        }

        ((TrackedContainerBridge) this.menu).bridge$setLastCraft((CraftItemEvent.Craft) event);
        ((TrackedContainerBridge) this.menu).bridge$setFirePreview(true);

        // TODO is logCraftingPreview automatically processed?

        // capture is allowed again


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

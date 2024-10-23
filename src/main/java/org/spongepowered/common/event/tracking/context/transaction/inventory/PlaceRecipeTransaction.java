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
package org.spongepowered.common.event.tracking.context.transaction.inventory;

import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

public class PlaceRecipeTransaction extends ContainerBasedTransaction {

    private final ServerPlayer player;
    private final ItemStackSnapshot originalCursor;
    private boolean shift;
    private RecipeHolder<?> recipe;
    private CraftingInventory craftingInventory;

    public PlaceRecipeTransaction(final ServerPlayer player, final boolean shift, final RecipeHolder<?> recipe, CraftingInventory craftingInventory) {
        super(player.containerMenu);
        this.player = player;
        this.originalCursor = ItemStackUtil.snapshotOf(player.containerMenu.getCarried());
        this.shift = shift;
        this.recipe = recipe;
        this.craftingInventory = craftingInventory;
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final List<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {
        final SlotTransaction preview = this.getPreviewTransaction(this.craftingInventory.result(), slotTransactions);
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(this.originalCursor, ItemStackUtil.snapshotOf(this.player.containerMenu.getCarried()));
        ClickContainerEvent.Recipe event;
        if (this.shift) {
            event = SpongeEventFactory.createClickContainerEventRecipeAll(cause, (Container) this.menu,
                    this.craftingInventory, cursorTransaction, preview,
                    Optional.of(this.recipe).map(RecipeHolder::value).map(CraftingRecipe.class::cast),
                    Optional.of(this.recipe).map(RecipeHolder::id).map(ResourceKey.class::cast),
                    Optional.empty(), slotTransactions);
        } else {
            event = SpongeEventFactory.createClickContainerEventRecipeSingle(cause, (Container) this.menu,
                    this.craftingInventory, cursorTransaction, preview,
                    Optional.of(this.recipe).map(RecipeHolder::value).map(CraftingRecipe.class::cast),
                    Optional.of(this.recipe).map(RecipeHolder::id).map(ResourceKey.class::cast),
                    Optional.empty(), slotTransactions);
        }
        return Optional.of(event);
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final ClickContainerEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    public void postProcessEvent(PhaseContext<@NonNull ?> context, ClickContainerEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    boolean isContainerEventAllowed(final PhaseContext<@Nullable ?> context) {
        if (!(context instanceof InventoryPacketContext)) {
            return false;
        }
        final int containerId = ((InventoryPacketContext) context).<ServerboundPlaceRecipePacket>getPacket().containerId();
        return containerId != this.player.containerMenu.containerId;
    }
}

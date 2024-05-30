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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
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
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

public class CraftingPreviewTransaction extends ContainerBasedTransaction {

    final Player player;
    final CraftingInventory craftingInventory;
    final CraftingContainer craftSlots;

    public CraftingPreviewTransaction(
        final ServerPlayer player, final CraftingInventory craftingInventory, final CraftingContainer craftSlots
    ) {
        super(player.containerMenu);
        this.player = player;
        this.craftingInventory = craftingInventory;
        this.craftSlots = craftSlots;
    }

    @Override
    public Optional<AbsorbingFlowStep> parentAbsorber() {
        return Optional.of((ctx, tx) -> tx.acceptCraftingPreview(ctx, this));
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(final List<SlotTransaction> slotTransactions, final List<Entity> entities,
            final PhaseContext<@NonNull ?> context, final Cause currentCause) {
        if (slotTransactions.isEmpty()) {
            return Optional.empty();
        }
        final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(this.player.containerMenu.getCarried());
        final SlotTransaction previewTransaction = this.getPreviewTransaction(this.craftingInventory.result(), slotTransactions);
        final var recipe = this.player.level().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.craftSlots.asCraftInput(), this.player.level());
        final CraftItemEvent.Preview event = SpongeEventFactory.createCraftItemEventPreview(currentCause,
                ContainerUtil.fromNative(this.menu), this.craftingInventory, new Transaction<>(cursor, cursor), previewTransaction,
                recipe.map(RecipeHolder::value).map(CraftingRecipe.class::cast),
                recipe.map(RecipeHolder::id).map(ResourceKey.class::cast),
                Optional.empty(), slotTransactions);
        return Optional.of(event);
    }

    @Override
    public boolean shouldHaveBeenAbsorbed() {
        return false;
    }
}

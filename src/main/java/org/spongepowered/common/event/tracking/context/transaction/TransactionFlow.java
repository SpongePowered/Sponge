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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.block.PrepareBlockDropsTransaction;
import org.spongepowered.common.event.tracking.context.transaction.inventory.ContainerSlotTransaction;
import org.spongepowered.common.event.tracking.context.transaction.inventory.ShiftCraftingResultTransaction;

interface TransactionFlow {

    default boolean absorbByParent(
        final PhaseContext<@NonNull ?> context, final GameTransaction<@NonNull ?> transaction
    ) {
        return false;
    }

    default boolean absorbSpawnEntity(
        final PhaseContext<@NonNull ?> context, final SpawnEntityTransaction spawn
    ) {
        return false;
    }

    default boolean absorbShiftClickResult(
        final PhaseContext<@NonNull ?> context, final ShiftCraftingResultTransaction transaction
    ) {
        return false;
    }

    default boolean absorbSlotTransaction(final ContainerSlotTransaction slotTransaction) {
        return false;
    }

    default boolean absorbBlockDropsPreparation(
        final PhaseContext<@NonNull ?> context, final PrepareBlockDropsTransaction prepareBlockDropsTransaction
    ) {
        return false;
    }

    default boolean acceptTileRemoval(final @Nullable BlockEntity tileentity) {
        return false;
    }

    default boolean acceptTileAddition(final BlockEntity tileEntity) {
        return false;
    }

    default boolean acceptTileReplacement(final @Nullable BlockEntity existing, final BlockEntity proposed) {
        return false;
    }

    default boolean acceptEntityDrops(final Entity entity) {
        return false;
    }

    default boolean acceptCraftingPreview(final ServerPlayer player, final CraftingInventory craftingInventory, final CraftingContainer craftSlots) {
        return false;
    }

    default boolean acceptCrafting(final Player player, @Nullable final ItemStack craftedStack, final CraftingInventory craftInv, @Nullable final CraftingRecipe lastRecipe) {
        return false;
    }

    default void acceptContainerSet(final Player player) {

    }

    /**
     * Micro-optimization to avoid
     * @return
     */
    default boolean canBeAbsorbed() {
        return false;
    }
}

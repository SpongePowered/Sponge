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
import org.spongepowered.common.event.tracking.context.transaction.inventory.PlayerInventoryTransaction;
import org.spongepowered.common.event.tracking.context.transaction.inventory.ShiftCraftingResultTransaction;

import java.util.Optional;

/**
 * A flow is a transaction being constructed or transformed in flight between
 * a source, such as a block change, to the destination, the {@link TransactionSink}.
 * Often times the sink just blindly accepts any transaction as the final
 * destination in {@link TransactionSink#logTransaction(GameTransaction)}, but
 * on certain cases, the transaction itself can have a specific flow as an
 * intermediary step, specifically by usage of
 * {@link #parentAbsorber()}.
 * <p>This is to plainly say that not all transactions are treated equal, but
 * from the source of creating a transaction, it's a "fire and forget" basis,
 * whereas the transaction itself may have behaviors that involve batching with
 * other transactions that effectively leave the transaction not being recorded
 * by the {@link TransactionSink}.
 */
public interface TransactionFlow {

    /**
     * An abstract function to serve for {@link #parentAbsorber()} to handily
     * call the conventional function onto the transaction without knowing
     * whether the parent function itself is a valid target absorber. This
     * function must not be mutative to the child transaction, but can call
     * mutative functions on the target {@link TransactionFlow transaction}
     * such that a {@code boolean} return value means the child transaction has
     * been successfully absorbed and can be omitted from
     * {@link TransactionSink#logTransaction(GameTransaction)}. This call can
     * and will be called multiple times to exhause a full transaction tree if
     * need be.
     */
    @FunctionalInterface
    interface AbsorbingFlowStep {
        boolean absorb(final PhaseContext<@NonNull ?> context, final TransactionFlow transaction);
    }

    /**
     * Simple function used to verify whether this particular transaction has a
     * behavior of being "merged" or "absorbed" by an already-recorded
     * transaction. This has uses for cases where "fire-and-forget" tend to have
     * unnatural recording behaviors that are non-deterministic based on their
     * root alone, and often times need to be gathered by some parent target.
     *
     * @return The absorbing function, if meant to be absorbed
     */
    default Optional<AbsorbingFlowStep> parentAbsorber() {
        return Optional.empty();
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

    default boolean acceptContainerSet(final Player player) {
        return false;
    }

    default boolean absorbPlayerInventoryChange(
        final PhaseContext<@NonNull ?> context,
        final PlayerInventoryTransaction playerInventoryTransaction
    ) {
        return false;
    }

    default boolean shouldHaveBeenAbsorbed() {
        return false;
    }
}

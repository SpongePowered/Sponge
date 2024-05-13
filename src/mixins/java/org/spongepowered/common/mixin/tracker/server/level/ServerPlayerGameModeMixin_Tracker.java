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
package org.spongepowered.common.mixin.tracker.server.level;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.server.level.ServerPlayerGameModeBridge;
import org.spongepowered.common.bridge.world.inventory.container.ContainerBridge;
import org.spongepowered.common.bridge.world.item.ItemStackBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.context.transaction.inventory.PlayerInventoryTransaction;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

@Mixin(value = ServerPlayerGameMode.class, priority = 998)
public abstract class ServerPlayerGameModeMixin_Tracker {
    @Shadow private GameType gameModeForPlayer;

    @Shadow public abstract boolean isCreative();

    @Inject(method = "useItem", cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;isOnCooldown(Lnet/minecraft/world/item/Item;)Z"))
    public void impl$callInteractItemSecondary(final ServerPlayer player, final Level level, final ItemStack stack, final InteractionHand hand,
        final CallbackInfoReturnable<InteractionResult> cir
    ) {
        final InteractItemEvent.Secondary event = SpongeCommonEventFactory.callInteractItemEventSecondary(player, stack, hand);
        if (event.isCancelled()) {
            player.refreshContainer(player.inventoryMenu);
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    /**
     * @author Morph
     * @author Yeregorix - Updated Apr 13th, 2023 - Add Forge support
     * @reason Fire InteractBlockEvent.Secondary and InteractContainerEvent.Open.
     */
    @Overwrite
    public InteractionResult useItemOn(final ServerPlayer playerIn, final Level worldIn, final ItemStack stackIn, final InteractionHand handIn, final BlockHitResult blockHitResultIn) {
        final BlockPos blockPos = blockHitResultIn.getBlockPos();
        final BlockState blockState = worldIn.getBlockState(blockPos);

        // Sponge start
        final InteractBlockEvent.Secondary event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            event = SpongeCommonEventFactory.createInteractBlockEventSecondary(playerIn, (ServerLevel) worldIn, stackIn, handIn,
                    blockHitResultIn, frame);
            SpongeCommon.post(event);
        }

        final Tristate useItem = event.useItemResult();
        final Tristate useBlock = event.useBlockResult();
        ((ServerPlayerGameModeBridge) this).bridge$setInteractBlockRightClickCancelled(event.isCancelled());
        if (event.isCancelled()) {
            return InteractionResult.FAIL; // On the server, the interaction result only dictates whether to swing the arm or not
        }
        // Sponge end

        if (this.gameModeForPlayer == GameType.SPECTATOR) {
            final MenuProvider menuProvider = blockState.getMenuProvider(worldIn, blockPos);
            if (menuProvider != null) {
                playerIn.openMenu(menuProvider);

                // Sponge start
                final Vector3i pos = VecHelper.toVector3i(blockHitResultIn.getBlockPos());
                final ServerLocation location = ServerLocation.of((ServerWorld) worldIn, pos);
                try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    frame.pushCause(playerIn);
                    frame.addContext(EventContextKeys.BLOCK_HIT, ((ServerWorld)(worldIn)).createSnapshot(pos));
                    ((ContainerBridge) playerIn.containerMenu).bridge$setOpenLocation(location);
                    if (!InventoryEventFactory.callInteractContainerOpenEvent(playerIn)) {
                        return InteractionResult.SUCCESS;
                    }
                }
                // Sponge end

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        } else {
            // Forge start
            final UseOnContext useOnContext = new UseOnContext(playerIn, handIn, blockHitResultIn);
            if (useItem != Tristate.FALSE) {
                InteractionResult interactionResult = ((ItemStackBridge) (Object) stackIn).bridge$onItemUseFirst(useOnContext);
                if (interactionResult != InteractionResult.PASS) {
                    return interactionResult;
                }
            }
            // Forge end

            final boolean hasItemInAnyHand = !playerIn.getMainHandItem().isEmpty() || !playerIn.getOffhandItem().isEmpty();
            final boolean sneakUse = playerIn.isSecondaryUseActive() && hasItemInAnyHand
                    // Forge start
                    && (!((ItemStackBridge) (Object) playerIn.getMainHandItem()).bridge$doesSneakBypassUse(worldIn, blockPos, playerIn)
                        || !((ItemStackBridge) (Object) playerIn.getOffhandItem()).bridge$doesSneakBypassUse(worldIn, blockPos, playerIn));
                    // Forge end

            final ItemStack copiedStack = stackIn.copy();
            if (useBlock != Tristate.FALSE && !sneakUse) { // Sponge check useBlock
                final AbstractContainerMenu lastOpenContainer = playerIn.containerMenu; // Sponge

                final InteractionResult result = blockState.use(worldIn, playerIn, handIn, blockHitResultIn);

                // Sponge start
                if (result.consumesAction() && lastOpenContainer != playerIn.containerMenu) {
                    final Vector3i pos = VecHelper.toVector3i(blockHitResultIn.getBlockPos());
                    final ServerLocation location = ServerLocation.of((ServerWorld) worldIn, pos);
                    try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                        frame.pushCause(playerIn);
                        frame.addContext(EventContextKeys.BLOCK_HIT, ((ServerWorld) (worldIn)).createSnapshot(pos));
                        ((ContainerBridge) playerIn.containerMenu).bridge$setOpenLocation(location);
                        if (!InventoryEventFactory.callInteractContainerOpenEvent(playerIn)) {
                            return InteractionResult.FAIL;
                        }
                    }
                }
                // Sponge end

                if (result.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(playerIn, blockPos, copiedStack);
                    return result;
                }
            }

            if (!stackIn.isEmpty() && !playerIn.getCooldowns().isOnCooldown(stackIn.getItem())) {
                // Sponge start
                if (useItem == Tristate.FALSE) {
                    ((ServerPlayerGameModeBridge) this).bridge$setInteractBlockRightClickCancelled(true);
                    return InteractionResult.PASS;
                }
                // Sponge end

                final InteractionResult result;
                if (this.isCreative()) {
                    final int i = stackIn.getCount();
                    result = stackIn.useOn(useOnContext);
                    stackIn.setCount(i);
                } else {
                    result = stackIn.useOn(useOnContext);

                    // Sponge start - log change in hand
                    final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
                    final TransactionalCaptureSupplier transactor = context.getTransactor();
                    transactor.logPlayerInventoryChange(playerIn, PlayerInventoryTransaction.EventCreator.STANDARD);
                    playerIn.inventoryMenu.broadcastChanges();
                    // Sponge end
                }

                if (result.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(playerIn, blockPos, copiedStack);
                }

                return result;
            } else {
                // Sponge start
                if (useBlock == Tristate.FALSE && !sneakUse) {
                    ((ServerPlayerGameModeBridge) this).bridge$setInteractBlockRightClickCancelled(true);
                }
                // Sponge end

                return InteractionResult.PASS;
            }
        }
    }

}

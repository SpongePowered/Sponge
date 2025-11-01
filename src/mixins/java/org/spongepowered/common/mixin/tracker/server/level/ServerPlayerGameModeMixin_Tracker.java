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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.server.level.ServerPlayerAccessor;
import org.spongepowered.common.bridge.server.level.ServerPlayerGameModeBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.ResultingTransactionBySideEffect;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.context.transaction.effect.InventoryEffect;
import org.spongepowered.common.event.tracking.context.transaction.inventory.PlayerInventoryTransaction;
import org.spongepowered.common.event.tracking.phase.player.PlayerPhase;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin_Tracker {

    @Shadow @Final protected ServerPlayer player;
    @Shadow protected net.minecraft.server.level.ServerLevel level;

    @Shadow public abstract boolean isCreative();

    @Inject(method = "useItem", cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;isOnCooldown(Lnet/minecraft/world/item/Item;)Z"))
    public void impl$callInteractItemSecondary(final ServerPlayer player, final Level level, final ItemStack stack, final InteractionHand hand,
        final CallbackInfoReturnable<InteractionResult> cir
    ) {
        final InteractItemEvent.Secondary event = SpongeCommonEventFactory.callInteractItemEventSecondary(player, stack, hand);
        if (event.isCancelled()) {
            player.inventoryMenu.sendAllDataToRemote();
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @WrapMethod(method = "useItemOn")
    private InteractionResult impl$wrapItemUse(
        final ServerPlayer player, final Level level, final ItemStack stack, final InteractionHand hand, final BlockHitResult blockHit, final Operation<InteractionResult> original,
        @Share("useItem") final LocalRef<Tristate> useItemRef, @Share("useBlock") final LocalRef<Tristate> useBlockRef
    ) {
        final Vector3i blockPos = VecHelper.toVector3i(blockHit.getBlockPos());
        final BlockSnapshot snapshot = ((ServerWorld) level).createSnapshot(blockPos);
        final Vector3d hitVec = VecHelper.toVector3d(blockHit.getLocation());
        final org.spongepowered.api.util.Direction direction = DirectionFacingProvider.INSTANCE.getKey(blockHit.getDirection()).get();
        final InteractBlockEvent.Secondary event = SpongeCommonEventFactory.callInteractBlockEventSecondary(player, stack, hitVec, snapshot, direction, hand);

        ((ServerPlayerGameModeBridge) this).bridge$setInteractBlockRightClickCancelled(event.isCancelled());
        if (event.isCancelled()) {
            this.player.inventoryMenu.sendAllDataToRemote();
            // Eating a cake increases the food level on client-side
            final FoodData foodData = player.getFoodData();
            this.player.connection.send(new ClientboundSetHealthPacket(player.getHealth(), foodData.getFoodLevel(), foodData.getSaturationLevel()));
            return InteractionResult.FAIL;
        }

        useItemRef.set(event.useItemResult());
        useBlockRef.set(event.useBlockResult());

        final PhaseTracker tracker = PhaseTracker.SERVER;
        try (final CauseStackManager.StackFrame frame = tracker.pushCauseFrame();
             final PhaseContext<?> context = PlayerPhase.State.PLAYER_INTERACT.createPhaseContext(tracker)
                 .creator(player.getUUID())
                 .notifier(player.getUUID())
                 .containerLocation(ServerLocation.of((ServerWorld) level, blockPos))) {
            context.buildAndSwitch();
            frame.pushCause(event);
            frame.addContext(EventContextKeys.BLOCK_HIT, snapshot);

            return original.call(player, level, stack, hand, blockHit);
        }
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/ItemInteractionResult;"))
    private ItemInteractionResult impl$onBlockInteraction(
        final BlockState instance, final ItemStack stack, final Level level, final Player player, final InteractionHand hand, final BlockHitResult blockHit, final Operation<ItemInteractionResult> original,
        @Share("useBlock") final LocalRef<Tristate> useBlockRef, @Share("useBlockCancelled") final LocalBooleanRef useBlockCancelledRef
        ) {
        if (useBlockRef.get() == Tristate.FALSE) {
            useBlockCancelledRef.set(true);
            return ItemInteractionResult.FAIL;
        }

        final ItemInteractionResult result = original.call(instance, stack, level, player, hand, blockHit);

        // Log change in hand
        final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        if (!transactor.isEmpty()) { // TODO: API 14 composite event
            try (final EffectTransactor ignored = context.getTransactor().pushEffect(new ResultingTransactionBySideEffect(InventoryEffect.getInstance()))) {
                transactor.logPlayerInventoryChange(this.player, PlayerInventoryTransaction.EventCreator.STANDARD);
                this.player.inventoryMenu.broadcastChanges();
            }
        } else {
            transactor.logPlayerInventoryChange(this.player, PlayerInventoryTransaction.EventCreator.STANDARD);
            try (final EffectTransactor ignored = context.getTransactor().pushEffect(new ResultingTransactionBySideEffect(InventoryEffect.getInstance()))) {
                this.player.inventoryMenu.broadcastChanges();
            }
        }

        return result;
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useWithoutItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult impl$onDefaultBlockInteraction(
        final BlockState instance, final Level level, final Player player, final BlockHitResult blockHitResult, final Operation<InteractionResult> original,
        @Cancellable final CallbackInfoReturnable<InteractionResult> cir
    ) {
        final AbstractContainerMenu lastOpenContainer = player.containerMenu;
        final int containerCounter = ((ServerPlayerAccessor) player).accessor$containerCounter();

        final InteractionResult result = original.call(instance, level, player, blockHitResult);

        if (result.consumesAction() && lastOpenContainer == player.containerMenu && containerCounter != ((ServerPlayerAccessor) player).accessor$containerCounter()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
        return result;
    }

    @Inject(method = "useItemOn", cancellable = true, at = @At(value = "NEW", target = "net/minecraft/world/item/context/UseOnContext"))
    private void impl$beforeItemInteraction(final CallbackInfoReturnable<InteractionResult> cir, @Share("useItem") final LocalRef<Tristate> useItemRef) {
        if (useItemRef.get() == Tristate.FALSE) {
            ((ServerPlayerGameModeBridge) this).bridge$setInteractBlockRightClickCancelled(true);
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "useItemOn", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;",
        ordinal = 1,
        shift = At.Shift.AFTER
    ))
    private void impl$afterItemInteractionNotCreative(final CallbackInfoReturnable<InteractionResult> cir) {
        // Log change in hand
        final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        if (!transactor.isEmpty()) { // TODO: API 14 composite event
            try (final EffectTransactor ignored = context.getTransactor().pushEffect(new ResultingTransactionBySideEffect(InventoryEffect.getInstance()))) {
                transactor.logPlayerInventoryChange(this.player, PlayerInventoryTransaction.EventCreator.STANDARD);
                this.player.inventoryMenu.broadcastChanges();
            }
        } else {
            transactor.logPlayerInventoryChange(this.player, PlayerInventoryTransaction.EventCreator.STANDARD);
            try (final EffectTransactor ignored = context.getTransactor().pushEffect(new ResultingTransactionBySideEffect(InventoryEffect.getInstance()))) {
                this.player.inventoryMenu.broadcastChanges();
            }
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "FIELD", target = "Lnet/minecraft/world/InteractionResult;PASS:Lnet/minecraft/world/InteractionResult;", ordinal = 1))
    private void impl$endWithNoInteraction(final CallbackInfoReturnable<InteractionResult> cir, @Share("useBlockCancelled") final LocalBooleanRef useBlockCancelledRef) {
        if (useBlockCancelledRef.get()) {
            ((ServerPlayerGameModeBridge) this).bridge$setInteractBlockRightClickCancelled(true);
        }
    }
}

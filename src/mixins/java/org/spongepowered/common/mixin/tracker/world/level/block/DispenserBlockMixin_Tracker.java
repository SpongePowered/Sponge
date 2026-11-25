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
package org.spongepowered.common.mixin.tracker.world.level.block;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.item.util.ItemStackUtil;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin_Tracker {

    @WrapMethod(method = "dispenseFrom")
    private void tracker$createContextOnDispensing(final ServerLevel worldIn, final BlockState state, final BlockPos pos, final Operation<Void> original) {
        final SpongeBlockSnapshot spongeBlockSnapshot = ((TrackedWorldBridge) worldIn).bridge$createSnapshot(state, pos, BlockChangeFlags.ALL);
        final LevelChunkBridge mixinChunk = (LevelChunkBridge) worldIn.getChunkAt(pos);
        try (final @Nullable PhaseContext<@NonNull ?> context = BlockPhase.State.DISPENSE
                .createPhaseContext(PhaseTracker.SERVER)
                .source(spongeBlockSnapshot)
                .creator(() -> mixinChunk.bridge$getBlockCreatorUUID(pos))
                .notifier(() -> mixinChunk.bridge$getBlockNotifierUUID(pos))) {
            context.buildAndSwitch();
            original.call(worldIn, state, pos);
        }
    }


    @WrapOperation(method = "dispenseFrom",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
        ),
        slice = @Slice(
            from = @At(value = "FIELD",
                target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;NOOP:Lnet/minecraft/core/dispenser/DispenseItemBehavior;"),
            to = @At("TAIL")
        )
    )
    private ItemStack tracker$storeOriginalItem(final DispenseItemBehavior instance, final BlockSource blockSource, final ItemStack itemStack,
            final Operation<ItemStack> original, final @Share("original-item") LocalRef<ItemStack> originalItemRef) {
        originalItemRef.set(ItemStackUtil.cloneDefensiveNative(itemStack));
        return original.call(instance, blockSource, itemStack);
    }

    @WrapOperation(method = "dispenseFrom",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void tracker$setInventoryContentsCallEvent(final DispenserBlockEntity dispenserTileEntity, final int index, final ItemStack stack,
            final Operation<Void> original, final @Share("original-item") LocalRef<ItemStack> originalItemRef) {
        // DropItemEvent.Pre was thrown by DefaultDispenseItemBehaviorMixin_Tracker before anything dropped
        original.call(dispenserTileEntity, index, stack);
        if (ItemStack.matches(originalItemRef.get(), stack)) {
            return;
        }
        final PhaseContext<@NonNull ?> context = PhaseTracker.getInstance().getPhaseContext();
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(dispenserTileEntity);
            if (!InventoryEventFactory.callChangeInventoryEvent(dispenserTileEntity, index, originalItemRef.get()) && context.hasCaptures()) {
                // The item is back in its slot, so whatever was dispensed with it is undone as the phase unwinds
                context.getTransactor().forEach(GameTransaction::markCancelled);
            }
        }
    }
}

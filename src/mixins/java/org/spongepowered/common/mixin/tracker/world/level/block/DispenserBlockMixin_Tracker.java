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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin_Tracker {
    private ItemStack tracker$originalItem = ItemStack.EMPTY;
    private PhaseContext<?> tracker$context = PhaseContext.empty();

    @Inject(method = "dispenseFrom", at = @At(value = "HEAD"))
    private void tracker$createContextOnDispensing(final ServerLevel worldIn, final BlockPos pos, final CallbackInfo ci) {
        final net.minecraft.world.level.block.state.BlockState state = worldIn.getBlockState(pos);
        final SpongeBlockSnapshot spongeBlockSnapshot = ((TrackedWorldBridge) worldIn).bridge$createSnapshot(state, pos, BlockChangeFlags.ALL);
        final LevelChunkBridge mixinChunk = (LevelChunkBridge) worldIn.getChunkAt(pos);
        this.tracker$context = BlockPhase.State.DISPENSE.createPhaseContext(PhaseTracker.SERVER)
                .source(spongeBlockSnapshot)
                .creator(() -> mixinChunk.bridge$getBlockCreator(pos))
                .notifier(() -> mixinChunk.bridge$getBlockNotifier(pos))
                .buildAndSwitch();
    }


    @Inject(method = "dispenseFrom", at = @At(value = "RETURN"))
    private void tracker$closeContextOnDispensing(final ServerLevel worldIn, final BlockPos pos, final CallbackInfo ci) {
        this.tracker$context.close();
        this.tracker$context = PhaseContext.empty();
    }

    @Inject(method = "dispenseFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
            ),
            slice = @Slice(
                    from = @At(value = "FIELD",
                            target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;NOOP:Lnet/minecraft/core/dispenser/DispenseItemBehavior;"),
                    to = @At("TAIL")
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void tracker$storeOriginalItem(final ServerLevel worldIn, final BlockPos pos, final CallbackInfo ci, final BlockSourceImpl source, final DispenserBlockEntity dispenser, final int slotIndex, final ItemStack dispensedItem, final DispenseItemBehavior behavior) {
        this.tracker$originalItem = ItemStackUtil.cloneDefensiveNative(dispensedItem);
    }


    @Redirect(method = "dispenseFrom",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void tracker$setInventoryContentsCallEvent(final DispenserBlockEntity dispenserTileEntity, final int index, final ItemStack stack) {
        final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
        // If we captured nothing, simply set the slot contents and return
        // TODO - figure out how to get captured item transactions
//        if (context.getCapturedItemsOrEmptyList().isEmpty()) {
//            dispenserTileEntity.setInventorySlotContents(index, stack);
//            return;
//        }
        final ItemStack dispensedItem = ItemStack.EMPTY;
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(dispensedItem);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(dispenserTileEntity);
            final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(frame.currentCause(), ImmutableList.of(snapshot), original);
            SpongeCommon.post(dropEvent);
            if (dropEvent.isCancelled()) {
                dispenserTileEntity.setItem(index, this.tracker$originalItem);
                return;
            }

            dispenserTileEntity.setItem(index, stack);
        } finally {
            this.tracker$originalItem = ItemStack.EMPTY;
        }
    }
}

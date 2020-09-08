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
package org.spongepowered.common.mixin.tracker.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.ProxyBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin_Tracker {
    private ItemStack tracker$originalItem = ItemStack.EMPTY;
    private PhaseContext<?> tracker$context = PhaseContext.empty();

    @Inject(method = "dispense", at = @At(value = "HEAD"))
    private void tracker$createContextOnDispensing(final World worldIn, final BlockPos pos, final CallbackInfo ci) {
        final net.minecraft.block.BlockState state = worldIn.getBlockState(pos);
        final SpongeBlockSnapshot spongeBlockSnapshot = ((TrackedWorldBridge) worldIn).bridge$createSnapshot(state, pos, BlockChangeFlags.ALL);
        final ChunkBridge mixinChunk = (ChunkBridge) worldIn.getChunkAt(pos);
        this.tracker$context = BlockPhase.State.DISPENSE.createPhaseContext(PhaseTracker.SERVER)
                .source(spongeBlockSnapshot)
                .creator(() -> mixinChunk.bridge$getBlockCreator(pos))
                .notifier(() -> mixinChunk.bridge$getBlockNotifier(pos))
                .buildAndSwitch();
    }


    @Inject(method = "dispense", at = @At(value = "RETURN"))
    private void tracker$closeContextOnDispensing(final World worldIn, final BlockPos pos, final CallbackInfo ci) {
        this.tracker$context.close();
        this.tracker$context = PhaseContext.empty();
    }

    @Inject(method = "dispense",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/dispenser/IDispenseItemBehavior;dispense(Lnet/minecraft/dispenser/IBlockSource;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"
            ),
            slice = @Slice(
                    from = @At(value = "FIELD",
                            target = "Lnet/minecraft/dispenser/IDispenseItemBehavior;NOOP:Lnet/minecraft/dispenser/IDispenseItemBehavior;"),
                    to = @At("TAIL")
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void tracker$storeOriginalItem(final World worldIn, final BlockPos pos, final CallbackInfo ci, final ProxyBlockSource source, final DispenserTileEntity dispenser, final int slotIndex, final ItemStack dispensedItem, final IDispenseItemBehavior behavior) {
        this.tracker$originalItem = ItemStackUtil.cloneDefensiveNative(dispensedItem);
    }


    @Redirect(method = "dispense",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/DispenserTileEntity;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V"))
    private void tracker$setInventoryContentsCallEvent(final DispenserTileEntity dispenserTileEntity, final int index, final ItemStack stack) {
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
            final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(frame.getCurrentCause(), ImmutableList.of(snapshot), original);
            SpongeCommon.postEvent(dropEvent);
            if (dropEvent.isCancelled()) {
                dispenserTileEntity.setInventorySlotContents(index, this.tracker$originalItem);
                return;
            }

            dispenserTileEntity.setInventorySlotContents(index, stack);
        } finally {
            this.tracker$originalItem = ItemStack.EMPTY;
        }
    }
}

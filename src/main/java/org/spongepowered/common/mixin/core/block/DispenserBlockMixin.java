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
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.ProxyBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin extends ContainerBlock {

    private ItemStack impl$originalItem = ItemStack.EMPTY;
    private PhaseContext<?> impl$context = PhaseContext.empty();

    protected DispenserBlockMixin(Properties builder) {
        super(builder);
    }

    @Inject(method = "dispense", at = @At(value = "HEAD"))
    private void impl$createContextOnDispensing(final World worldIn, final BlockPos pos, final CallbackInfo ci) {
        final net.minecraft.block.BlockState state = worldIn.getBlockState(pos);
        final SpongeBlockSnapshot spongeBlockSnapshot = ((ServerWorldBridge) worldIn).bridge$createSnapshot(state, pos, BlockChangeFlags.ALL);
        final ChunkBridge mixinChunk = (ChunkBridge) worldIn.getChunkAt(pos);
        this.impl$context = BlockPhase.State.DISPENSE.createPhaseContext()
            .source(spongeBlockSnapshot)
            .owner(() -> mixinChunk.bridge$getBlockOwner(pos))
            .notifier(() -> mixinChunk.bridge$getBlockNotifier(pos))
            .buildAndSwitch();
    }

    @Inject(method = "dispense", at = @At(value = "RETURN"))
    private void impl$closeContextOnDispensing(World worldIn, BlockPos pos, CallbackInfo ci) {
        this.impl$context.close();
        this.impl$context = PhaseContext.empty();
    }

    @Inject(method = "dispense",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/DispenserBlock;dispense(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/dispenser/IDispenseItemBehavior;NOOP:Lnet/minecraft/dispenser/IDispenseItemBehavior;"),
            to = @At("TAIL")
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void impl$storeOriginalItem(World worldIn, BlockPos pos, CallbackInfo ci, ProxyBlockSource source, DispenserTileEntity dispenser, int slotIndex, ItemStack dispensedItem, IDispenseItemBehavior behavior) {
        this.impl$originalItem = ItemStackUtil.cloneDefensiveNative(dispensedItem);
    }

    @Redirect(method = "dispense", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/LockableLootTileEntity;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V"))
    private void impl$setInventoryContentsCallEvent(LockableLootTileEntity lockableLootTileEntity, int index, ItemStack stack) {
        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
        // If we captured nothing, simply set the slot contents and return
        if (context.getCapturedItemsOrEmptyList().isEmpty()) {
            lockableLootTileEntity.setInventorySlotContents(index, stack);
            return;
        }
        final ItemStack dispensedItem = context.getCapturedItems().get(0).getItem();
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(dispensedItem);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(lockableLootTileEntity);
            final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(frame.getCurrentCause(), ImmutableList.of(snapshot), original);
            SpongeImpl.postEvent(dropEvent);
            if (dropEvent.isCancelled()) {
                lockableLootTileEntity.setInventorySlotContents(index, this.impl$originalItem);
                context.getCapturedItems().clear();
                return;
            }
            if (dropEvent.getDroppedItems().isEmpty()) {
                context.getCapturedItems().clear();
            }

            lockableLootTileEntity.setInventorySlotContents(index, stack);
        }
    }
}

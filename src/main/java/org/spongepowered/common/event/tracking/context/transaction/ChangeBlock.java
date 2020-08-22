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

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.effect.BlockAddedEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.CheckBlockPostPlacementIsSameEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.ChunkChangeCompleteEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.OldBlockOnReplaceEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.RefreshOldTileEntityOnChunkChangeEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.SetBlockToChunkSectionEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateChunkLightManagerEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateHeightMapEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateOrCreateNewTileEntityPostPlacementEffect;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.ChunkPipeline;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Optional;
import java.util.function.BiConsumer;

@DefaultQualifier(NonNull.class)
public final class ChangeBlock extends BlockEventBasedTransaction {

    final SpongeBlockSnapshot original;
    final int originalOpacity;
    final BlockState newState;
    final SpongeBlockChangeFlag blockChangeFlag;
    @Nullable public TileEntity queuedRemoval;
    @Nullable public TileEntity queuedAdd;

    ChangeBlock(final SpongeBlockSnapshot attachedSnapshot, final BlockState newState,
        final SpongeBlockChangeFlag blockChange
    ) {
        super(attachedSnapshot.getBlockPos(), (BlockState) attachedSnapshot.getState());
        this.original = attachedSnapshot;
        this.newState = newState;
        this.blockChangeFlag = blockChange;
        this.originalOpacity = this.originalState.getOpacity(this.original.getServerWorld().get(), this.affectedPosition);
    }

    public BlockState getNewState() {
        return this.newState;
    }

    public SpongeBlockChangeFlag getBlockChangeFlag() {
        return this.blockChangeFlag;
    }

    public void populateChunkEffects(final ChunkPipeline.Builder builder) {

        builder.addEffect(SetBlockToChunkSectionEffect.getInstance());
        builder.addEffect(UpdateHeightMapEffect.getInstance());
        builder.addEffect(UpdateChunkLightManagerEffect.getInstance());
        builder.addEffect(OldBlockOnReplaceEffect.getInstance());
        builder.addEffect(CheckBlockPostPlacementIsSameEffect.getInstance());
        builder.addEffect(RefreshOldTileEntityOnChunkChangeEffect.getInstance());
        builder.addEffect(BlockAddedEffect.getInstance());
        builder.addEffect(UpdateOrCreateNewTileEntityPostPlacementEffect.getInstance());
        builder.addEffect(ChunkChangeCompleteEffect.getInstance());
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator() {
        return Optional.of((context, frame) -> {
            context.addCreatorAndNotifierToCauseStack(frame);
            frame.pushCause(this.original);
        });
    }

    @Override
    public boolean acceptTileAddition(final TileEntity tileEntity) {
        if (!this.affectedPosition.equals(tileEntity.getPos())) {
            return false;
        }
        if (this.queuedAdd != null) {
            return false;

        }
        this.queuedAdd = tileEntity;
        return true;
    }

    @Override
    public boolean acceptTileRemoval(final TileEntity tileentity) {
        if (!this.affectedPosition.equals(tileentity.getPos())) {
            return false;
        }
        if (this.queuedRemoval != null) {
            return false;
        }
        this.queuedRemoval = tileentity;
        return true;
    }

    @Override
    public boolean acceptTileReplacement(final @Nullable TileEntity existing, final TileEntity proposed) {
        return existing != null && this.acceptTileRemoval(existing) && this.acceptTileAddition(proposed);
    }

    @Override
    public void restore() {
        this.original.restore(true, BlockChangeFlagManager.fromNativeInt(Constants.BlockChangeFlags.FORCED_RESTORE));
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {
        printer.add("ChangeBlock")
            .add(" %s : %s", "Original Block", this.original)
            .add(" %s : %s", "New State", this.newState)
            .add(" %s : %s", "RemovedTile", this.queuedRemoval)
            .add(" %s : %s", "AddedTile", this.queuedAdd)
            .add(" %s : %s", "ChangeFlag", this.blockChangeFlag);
    }

    @Override
    protected SpongeBlockSnapshot getResultingSnapshot() {
        return SpongeBlockSnapshotBuilder.pooled()
            .world(this.original.getWorld())
            .position(this.original.getPosition())
            .blockState((org.spongepowered.api.block.BlockState) this.newState)
            .build()
            ;
    }

    @Override
    protected SpongeBlockSnapshot getOriginalSnapshot() {
        return this.original;
    }
}

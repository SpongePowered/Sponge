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
package org.spongepowered.common.event.tracking.context.transaction.block;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.level.block.entity.BlockEntityBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.BlockChange;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@DefaultQualifier(NonNull.class)
public final class AddTileEntity extends BlockEventBasedTransaction {

    final BlockEntity added;
    private @MonotonicNonNull SpongeBlockSnapshot oldSnapshot;
    private @MonotonicNonNull SpongeBlockSnapshot addedSnapshot;
    private final Supplier<ServerLevel> worldSupplier;
    private final Supplier<LevelChunk> chunkSupplier;

    public AddTileEntity(final BlockEntity blockEntity, final Supplier<ServerLevel> worldSupplier, final Supplier<LevelChunk> chunkSupplier) {
        super(blockEntity.getBlockPos().immutable(), chunkSupplier.get().getBlockState(blockEntity.getBlockPos()),
            ((ServerWorld) worldSupplier.get()).key());
        this.added = blockEntity;
        this.worldSupplier = worldSupplier;
        this.chunkSupplier = chunkSupplier;
    }

    @Override
    protected void captureState() {
        super.captureState();
        final @Nullable BlockEntity existingTile = this.chunkSupplier.get().getBlockEntity(this.affectedPosition, LevelChunk.EntityCreationType.CHECK);
        final SpongeBlockSnapshot added = TrackingUtil.createPooledSnapshot(
            this.originalState,
            this.affectedPosition,
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            this.added,
            this.worldSupplier,
            Optional::empty, Optional::empty
        );
        final SpongeBlockSnapshot existing = TrackingUtil.createPooledSnapshot(
            this.originalState,
            this.affectedPosition,
            BlockChangeFlags.NONE,
            Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT,
            existingTile,
            this.worldSupplier,
            Optional::empty,
            Optional::empty
        );
        existing.blockChange = BlockChange.MODIFY;
        this.oldSnapshot = existing;
        this.addedSnapshot = added;
    }

    @Override
    public Optional<AbsorbingFlowStep> parentAbsorber() {
        return Optional.of((ctx, tx) -> tx.acceptTileAddition(this.added));
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable final GameTransaction<@NonNull ?> parent
    ) {
        return Optional.empty();
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {
        printer.add("AddTileEntity")
            .addWrapped(120, " %s : %s", this.affectedPosition, ((BlockEntityBridge) this.added).bridge$getPrettyPrinterString());
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final ChangeBlockEvent.All event) {
        this.oldSnapshot.restore(true, BlockChangeFlags.NONE);
    }

    @Override
    protected SpongeBlockSnapshot getResultingSnapshot() {
        return this.addedSnapshot;
    }

    @Override
    protected SpongeBlockSnapshot getOriginalSnapshot() {
        return this.oldSnapshot;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AddTileEntity.class.getSimpleName() + "[", "]")
            .add("added=" + this.added)
            .add("affectedPosition=" + this.affectedPosition)
            .add("originalState=" + this.originalState)
            .add("worldKey=" + this.worldKey)
            .add("cancelled=" + this.cancelled)
            .toString();
    }
}

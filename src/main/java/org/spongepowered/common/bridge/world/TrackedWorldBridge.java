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
package org.spongepowered.common.bridge.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.bridge.world.level.block.state.BlockStateBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.UseBlockPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.UseItemOnBlockPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.UseItemPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.WorldPipeline;

import java.util.Optional;
import java.util.function.Function;

/**
 * A specialized {@link LevelBridge} or {@link ServerLevelBridge}
 * that has extra {@link org.spongepowered.common.event.tracking.PhaseTracker} related
 * methods that otherwise bear no other changes to the game.
 */
public interface TrackedWorldBridge {

    boolean bridge$forceSpawnEntity(Entity entity);

    net.minecraft.world.level.Explosion tracker$triggerInternalExplosion(Explosion explosion, Function<? super net.minecraft.world.level.Explosion,
        ? extends PhaseContext<?>> contextCreator);

    Optional<WorldPipeline.Builder> bridge$startBlockChange(BlockPos pos, BlockState state, int rawFlags);

    /**
     * Delegates to the {@link ServerLevel} to perform the lookup for a {@link LevelChunk}
     * such that if the target {@link BlockPos} results in a {@code false} for
     * {@link ServerLevel#isBlockLoaded(BlockPos)}, {@link BlockSnapshot#empty()}
     * will be returned. Likewise, optimizes the creation of the snapshot by performing
     * the {@link LevelChunk#getBlockState(BlockPos)} and {@link LevelChunk#getTileEntity(BlockPos, Chunk.CreateEntityType)}
     * lookup on the same chunk, avoiding an additional chunk lookup.
     *
     * <p>This should be used when the "known" {@link BlockState} for the target
     * position is not known. If it is known, use {@link #bridge$createSnapshot(BlockState, BlockPos, BlockChangeFlag)}</p>
     *
     * @param pos The target position to get the block snapshot for
     * @param flag The block change flag to associate with the snapshot.
     * @return The snapshot, or none if not loaded
     */
    default SpongeBlockSnapshot bridge$createSnapshot(final BlockPos pos, final BlockChangeFlag flag) {
        return this.bridge$createSnapshot(((ServerLevel) (Object) this).getBlockState(pos), pos, flag);
    }

    /**
     * Creates a {@link BlockSnapshot} but performs an additional {@link LevelChunk#getTileEntity(BlockPos, Chunk.CreateEntityType)}
     * lookup if the providing {@link BlockState#getBlock()} {@code instanceof} is
     * {@code true} for being an {@link EntityBlock} or
     * {@link BlockStateBridge#bridge$hasTileEntity()}, and associates
     * the resulting snapshot of said Tile with the snapshot. This is useful for in-progress
     * snapshot creation during transaction building for {@link TransactionalCaptureSupplier}.
     *
     * <p>If the {@link BlockEntity} is already known, and no lookups are needed, use
     * {@link #bridge$createSnapshotWithEntity(BlockState, BlockPos, BlockChangeFlag, BlockEntity)} as it avoids
     * any further chunk lookups.</p>
     *
     * @param state The block state
     * @param pos The target position
     * @param updateFlag The update flag
     * @return The snapshot, never NONE
     */
    SpongeBlockSnapshot bridge$createSnapshot(BlockState state, BlockPos pos, BlockChangeFlag updateFlag);

    /**
     * Similar to {@link #bridge$createSnapshot(BlockState, BlockPos, BlockChangeFlag)},
     * but with the added avoidance of a {@link BlockEntity} lookup during the creation of the resulting
     * {@link SpongeBlockSnapshot}.
     *
     * @param state The state
     * @param pos The position
     * @param updateFlag The update flag
     * @param tileEntity The tile entity to serialize, if available
     * @return The snapshot, never NONE
     */
    SpongeBlockSnapshot bridge$createSnapshotWithEntity(BlockState state, BlockPos pos, BlockChangeFlag updateFlag, @Nullable BlockEntity tileEntity);

    UseItemOnBlockPipeline bridge$startInteractionUseOnChange(Level worldIn, ServerPlayer playerIn, InteractionHand handIn, BlockHitResult blockRaytraceResultIn, BlockState blockstate, ItemStack copiedStack);

    UseBlockPipeline bridge$startInteractionChange(Level worldIn, ServerPlayer playerIn, InteractionHand handIn, BlockHitResult blockRaytraceResultIn, BlockState blockstate, ItemStack copiedStack);

    UseItemPipeline bridge$startItemInteractionChange(Level worldIn, ServerPlayer playerIn, InteractionHand handIn, ItemStack copiedStack, BlockHitResult blockRaytraceResult, boolean creative);
}

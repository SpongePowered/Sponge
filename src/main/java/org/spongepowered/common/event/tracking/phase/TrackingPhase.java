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
package org.spongepowered.common.event.tracking.phase;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.BlockStateTriplet;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.world.CaptureType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public abstract class TrackingPhase {

    @Nullable private final TrackingPhase parent;

    private final List<TrackingPhase> children = new ArrayList<>();

    public TrackingPhase(@Nullable TrackingPhase parent) {
        this.parent = parent;
    }

    @Nullable
    public TrackingPhase getParent() {
        return this.parent;
    }

    public List<TrackingPhase> getChildren() {
        return this.children;
    }

    public TrackingPhase addChild(TrackingPhase child) {
        this.children.add(child);
        return this;
    }

    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return true;
    }

    public BlockStateTriplet captureBlockChange(CauseTracker causeTracker, IBlockState currentState,
            IBlockState newState, Block block, BlockPos pos, int flags, PhaseContext phaseContext, IPhaseState phaseState) {
        BlockSnapshot originalBlockSnapshot = null;
        Transaction<BlockSnapshot> transaction = null;
        LinkedHashMap<Vector3i, Transaction<BlockSnapshot>> populatorSnapshotList = null;
        final IMixinWorld mixinWorld = causeTracker.getMixinWorld();
        final Map<PopulatorType, LinkedHashMap<Vector3i, Transaction<BlockSnapshot>>> capturedPopulators = phaseContext.getPopulatorMap().orElse(null);
        final PopulatorType runningGenerator = phaseContext.firstNamed(TrackingHelper.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
        if (!(((IMixinMinecraftServer) MinecraftServer.getServer()).isPreparingChunks())) {
            originalBlockSnapshot = mixinWorld.createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState,
                    causeTracker.getMinecraftWorld(), pos), pos, flags);

            if (runningGenerator != null) {
                if (capturedPopulators.get(runningGenerator) == null) {
                    capturedPopulators.put(runningGenerator, new LinkedHashMap<>());
                }

                ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.POPULATE;
                transaction = new Transaction<>(originalBlockSnapshot, originalBlockSnapshot.withState((BlockState) newState));
                populatorSnapshotList = capturedPopulators.get(runningGenerator);
                populatorSnapshotList.put(transaction.getOriginal().getPosition(), transaction);
            } else {
                final List<BlockSnapshot> capturedSpongeBlockSnapshots = phaseContext.getCapturedBlocks().orElse(new ArrayList<>());
                if (phaseState == BlockPhase.State.BLOCK_DECAY) {
                    // Only capture final state of decay, ignore the rest
                    if (block == Blocks.air) {
                        ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.DECAY;
                        capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
                    }
                } else if (block == Blocks.air) {
                    ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.BREAK;
                    capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
                } else if (block != currentState.getBlock()) {
                    ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.PLACE;
                    capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
                } else {
                    ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.MODIFY;
                    capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
                }
            }
        }
        return new BlockStateTriplet(populatorSnapshotList, originalBlockSnapshot, transaction);
    }

    public boolean ignoresEntitySpawns(IPhaseState currentState) {
        return false;
    }

    public boolean trackEntitySpawns(IPhaseState phaseState, PhaseContext context, Entity entity, Cause cause, int chunkX, int chunkZ) {
        return false;
    }

    public abstract void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext);
}

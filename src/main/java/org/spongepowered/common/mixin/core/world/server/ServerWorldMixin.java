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
package org.spongepowered.common.mixin.core.world.server;

import co.aikar.timings.Timing;
import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ServerChunkProviderBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.mixin.core.world.WorldMixin;

import java.util.Random;
import java.util.function.Consumer;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends WorldMixin implements ServerWorldBridge {

    private SpongeProxyBlockAccess impl$proxyBlockAccess = new SpongeProxyBlockAccess(this);


    @Inject(method = "onEntityAdded", at = @At("TAIL"))
    private void onEntityAddedToWorldMarkAsTracked(final net.minecraft.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake()) { // Only set the value if the entity is not fake
            ((EntityBridge) entityIn).bridge$setWorldTracked(true);
        }
    }

    @Inject(method = "onEntityRemoved", at = @At("TAIL"))
    private void onEntityRemovedFromWorldMarkAsUntracked(final net.minecraft.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake() || ((EntityBridge) entityIn).bridge$isWorldTracked()) {
            ((EntityBridge) entityIn).bridge$setWorldTracked(false);
        }
    }

    /**
     * We want to redirect the consumer caller ("updateEntity") because we need to wrap around
     * global entities being ticked, and then entities themselves being ticked is a different area/section.
     *
     * @param serverWorld
     * @param consumer
     * @param entity
     */
    @Redirect(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/server/ServerWorld;func_217390_a(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/server/ServerWorld;resetUpdateEntityTick()V"),
                    to = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;int2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;")
            )
    )
    private void impl$wrapGlobalEntityTicking(final ServerWorld serverWorld, final Consumer<Entity> consumer, final Entity entity) {
        final PhaseContext<?> currentContext = PhaseTracker.SERVER.getCurrentContext();
        if (currentContext.state.alreadyCapturingEntityTicks()) {
            this.func_217390_a(consumer, entity);
            return;
        }
        TrackingUtil.tickkGlobalEntity(consumer, entity);
        // TODO - determine if updating rotation is needed.
    }

    @Redirect(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/server/ServerWorld;func_217390_a(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/IProfiler;startSection(Ljava/lang/String;)V", args = "stringValue=tick"),
                    to = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/IProfiler;startSection(Ljava/lang/String;)V", args = "stringValue=remove")
            )
    )
    private void impl$wrapNormalEntityTick(final ServerWorld serverWorld, final Consumer<Entity> entityUpdateConsumer, final Entity entity) {
        final IPhaseState<?> currentState = PhaseTracker.SERVER.getCurrentState();
        if (currentState.alreadyCapturingEntityTicks()) {
            this.func_217390_a(entityUpdateConsumer, entity);
            return;
        }
        TrackingUtil.tickEntity(entityUpdateConsumer, entity);
    }

    @Override
    protected void impl$wrapTileEntityTick(final ITickableTileEntity tileEntity) {
        if (!SpongeImplHooks.shouldTickTile(tileEntity)) {
            return;
        }
        final IPhaseState<?> state = PhaseTracker.SERVER.getCurrentState();
        if (state.alreadyCapturingTileTicks()) {
            tileEntity.tick();
            return;
        }
        TrackingUtil.tickTileEntity(this, tileEntity);
    }

    /**
     * @author gabizou - January 11th, 2020 - Minecraft 1.14.3
     * @reason For PhaseTracking, we need to wrap around the
     * {@link BlockState#tick(World, BlockPos, Random)} method, and the ScheduledTickList uses a lambda method
     * to {@code ServerWorld#tickBlock(NextTickListEntry)}, so it's either we customize the ScheduledTickList
     * or we wrap in this method here.
     *
     * @param blockState The block state being ticked
     * @param worldIn The world (this world)
     * @param pos The position of the block state
     * @param random The random (world.rand)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "tickBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void impl$wrapBlockTick(final BlockState blockState, final World worldIn, final BlockPos pos, final Random random) {
        final PhaseContext<?> currentContext = PhaseTracker.SERVER.getCurrentContext();
        final IPhaseState currentState = currentContext.state;
        if (currentState.alreadyCapturingBlockTicks(currentContext) || currentState.ignoresBlockUpdateTick(currentContext)) {
            blockState.tick(worldIn, pos, random);
            return;
        }
        TrackingUtil.updateTickBlock(this, blockState, pos, random);
    }

    /**
     * @author gabizou - January 11th, 2020 - Minecraft 1.14.3
     * @reason For PhaseTracking, we need to wrap around the
     * {@link BlockState#tick(World, BlockPos, Random)} method, and the ScheduledTickList uses a lambda method
     * to {@code ServerWorld#tickBlock(NextTickListEntry)}, so it's either we customize the ScheduledTickList
     * or we wrap in this method here.
     *
     * @param blockState The block state being ticked
     * @param worldIn The world (this world)
     * @param pos The position of the block state
     * @param random The random (world.rand)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "tickEnvironment",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void impl$wrapBlockRandomTick(final BlockState blockState, final World worldIn, final BlockPos pos, final Random random) {
        try (final Timing timing = ((TimingBridge) blockState.getBlock()).bridge$getTimingsHandler()) {
            timing.startTiming();
            final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
            final IPhaseState phaseState = context.state;
            if (phaseState.alreadyCapturingBlockTicks(context)) {
                blockState.randomTick(worldIn, pos, this.rand);
            } else {
                TrackingUtil.randomTickBlock(this, blockState, pos, this.rand);
            }
        }
    }

    @Override
    public SpongeProxyBlockAccess bridge$getProxyAccess() {
        return this.impl$proxyBlockAccess;
    }

    /**
     * @author gabizou - August 4th, 2016
     * @author blood - May 11th, 2017 - Forces chunk requests if TE is ticking.
     * @reason Rewrites the check to be inlined to {@link BlockPosBridge}.
     *
     * @param pos The position
     * @return The block state at the desired position
     */
    @Override
    public net.minecraft.block.BlockState getBlockState(final BlockPos pos) {
        // Sponge - Replace with inlined method
        // if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((BlockPosBridge) pos).bridge$isInvalidYPosition()) {
            // Sponge end
            return Blocks.AIR.getDefaultState();
        }
        // ExtraUtilities 2 expects to get the proper chunk while mining or it gets stuck in infinite loop
        // TODO add TE config to disable/enable chunk loads
        final boolean forceChunkRequests = ((ServerChunkProviderBridge) this.getChunkProvider()).bridge$getForceChunkRequests();
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final IPhaseState<?> currentState = phaseTracker.getCurrentState();
        final boolean entered = currentState == TickPhase.Tick.TILE_ENTITY;
        if (entered) {
            ((ServerChunkProviderBridge) this.getChunkProvider()).bridge$setForceChunkRequests(true);
        }
        try {
            // Proxies have block changes for bulk special captures
            final net.minecraft.block.BlockState blockState = this.impl$proxyBlockAccess.getBlockState(pos);
            if (blockState != null) {
                return blockState;
            }
            final IChunk chunk= this.getChunk(pos);
            return chunk.getBlockState(pos);
        } finally {
            if (entered) {
                ((ServerChunkProviderBridge) this.getChunkProvider()).bridge$setForceChunkRequests(forceChunkRequests);
            }
        }
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Name", this.worldInfo.getWorldName())
                .add("DimensionId", this.dimension.getType().getId())
                .add("DimensionType", ((org.spongepowered.api.world.dimension.DimensionType) (Object) this.dimension.getType()).getKey().toString())
                .toString();
    }

}

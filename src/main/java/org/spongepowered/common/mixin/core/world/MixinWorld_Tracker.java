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
package org.spongepowered.common.mixin.core.world;

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.event.tracking.phase.PluginPhase;
import org.spongepowered.common.event.tracking.phase.SpawningPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.StaticMixinHelper;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin(value = net.minecraft.world.World.class, priority = 1001)
public abstract class MixinWorld_Tracker implements World, IMixinWorld {

    private static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    private static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(1, 1, 1);
    private static final Vector2i BIOME_MIN = BLOCK_MIN.toVector2(true);
    private static final Vector2i BIOME_MAX = BLOCK_MAX.toVector2(true);

    private final CauseTracker causeTracker = new CauseTracker((net.minecraft.world.World) (Object) this);

    @Shadow @Final public boolean isRemote;
    @Shadow @Final public Profiler theProfiler;
    @Shadow protected WorldInfo worldInfo;

    @Shadow public abstract boolean isValid(BlockPos pos);
    @Shadow public abstract void markBlockForUpdate(BlockPos pos);
    @Shadow public abstract void notifyNeighborsRespectDebug(BlockPos pos, Block blockType);
    @Shadow public abstract boolean checkLight(BlockPos pos);


    @Inject(method = "<init>", at = @At("RETURN"))
    private void onTrackerConstructed(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client,
            CallbackInfo ci) {

        // Turn on capturing
        final CauseTracker causeTracker = this.getCauseTracker();
        causeTracker.setCaptureBlocks(true);
    }

    @Inject(method = "onEntityRemoved", at = @At(value = "HEAD"))
    public void onEntityRemoval(net.minecraft.entity.Entity entityIn, CallbackInfo ci) {
        if (entityIn.isDead && this.getCauseTracker().getPhases().peek().getContext().firstNamed(TrackingHelper.TARGETED_ENTITY,
                net.minecraft.entity.Entity.class).isPresent() && !(entityIn instanceof EntityLivingBase)) {
            MessageChannel originalChannel = MessageChannel.TO_NONE;

            DestructEntityEvent event = SpongeEventFactory.createDestructEntityEvent(Cause.of(NamedCause.source(this)), originalChannel,
                    Optional.of(originalChannel), Optional.empty(), Optional.empty(), (Entity) entityIn);
            SpongeImpl.getGame().getEventManager().post(event);
            event.getMessage().ifPresent(text -> event.getChannel().ifPresent(channel -> channel.send(text)));
        }
    }

    /**
     * @author bloodmc
     *
     * Purpose: Rewritten to support capturing blocks
     */
    @Overwrite
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (!this.isValid(pos)) {
            return false;
        } else if (!this.isRemote && this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
            return false;
        } else {
            return this.getCauseTracker().setBlockState(pos, newState, flags);
        }
    }

    @Override
    public void markAndNotifyNeighbors(BlockPos pos, @Nullable net.minecraft.world.chunk.Chunk chunk, IBlockState old, IBlockState new_, int flags) {
        if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (chunk == null || chunk.isPopulated())) {
            this.markBlockForUpdate(pos);
        }

        if (!this.isRemote && (flags & 1) != 0) {
            this.notifyNeighborsRespectDebug(pos, old.getBlock());

            if (new_.getBlock().hasComparatorInputOverride()) {
                this.updateComparatorOutputLevel(pos, new_.getBlock());
            }
        }
    }

    @Redirect(method = "forceBlockUpdateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onForceBlockUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final PhaseData peek = causeTracker.getPhases().peek();
        final Optional<BlockSnapshot> currentTickingBlock;
        final boolean flag;
        if (peek != null) {
            flag = peek.getState() == SpawningPhase.State.CHUNK_SPAWNING || peek.getState() == WorldPhase.State.WORLD_SPAWNER_SPAWNING;
            currentTickingBlock = peek.getContext().firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
        } else {
            flag = false;
            currentTickingBlock = Optional.empty();
        }
        if (this.isRemote || currentTickingBlock.isPresent() || causeTracker.getPhases().peekState() == WorldPhase.State.TERRAIN_GENERATION || flag) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        TrackingHelper.updateTickBlock(causeTracker, block, pos, state, rand);
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onUpdateEntities(net.minecraft.entity.Entity entityIn) {
        final CauseTracker causeTracker = this.getCauseTracker();
        Optional<Entity> currentTickingEntity = causeTracker.getPhases().peek().getContext().firstNamed(NamedCause.SOURCE, Entity.class);
        if (this.isRemote || currentTickingEntity.isPresent()) {
            entityIn.onUpdate();
            return;
        }

        TrackingHelper.tickEntity(causeTracker, entityIn);
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    public void onUpdateTileEntities(ITickable tile) {
        final CauseTracker causeTracker = this.getCauseTracker();
        Optional<TileEntity> currentTickingTileEntity = causeTracker.getPhases().peek().getContext().firstNamed(NamedCause.SOURCE, TileEntity.class);
        if (this.isRemote || currentTickingTileEntity.isPresent()) {
            tile.update();
            return;
        }

        TrackingHelper.tickTileEntity(causeTracker, tile);


    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        final CauseTracker causeTracker = this.getCauseTracker();
        final PhaseData tuple = causeTracker.getPhases().peek();
        Optional<Entity> currentTickingEntity = tuple.getContext().firstNamed(NamedCause.SOURCE, Entity.class);
        Optional<Packet> currentPacket = tuple.getContext().firstNamed("Packet", Packet.class);
        if (this.isRemote || currentTickingEntity.isPresent() || currentPacket.isPresent()) {
            entity.onUpdate();
            return;
        }

        TrackingHelper.tickEntity(causeTracker, entity);
    }

    /**
     * @author bloodmc
     *
     * Purpose: Redirects vanilla method to our method which includes a cause.
     */
    @Overwrite
    public boolean spawnEntityInWorld(net.minecraft.entity.Entity entity) {
        return spawnEntity((Entity) entity, Cause.of(NamedCause.source(this)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean spawnEntity(Entity entity, Cause cause) {
        return this.getCauseTracker().processSpawnEntity(entity, cause);
    }


    @Overwrite
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType) {
        if (!isValid(pos)) {
            return;
        }

        final CauseTracker causeTracker = this.getCauseTracker();
        for (EnumFacing facing : EnumFacing.values()) {
            causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
        }
//
//        NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent(this, pos, java.util.EnumSet.allOf(EnumFacing.class));
//        if (event.isCancelled()) {
//            return;
//        }
//
//        for (EnumFacing facing : EnumFacing.values()) {
//            if (event.getNeighbors().keySet().contains(DirectionFacingProvider.getInstance().getKey(facing).get())) {
//                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
//            }
//        }
    }

//    /**
//     * @author bloodmc - November 15th, 2015
//     *
//     * Purpose: Rewritten to pass the source block position.
//     */
//    @SuppressWarnings("rawtypes")
//    @Overwrite
//    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
//        if (!isValid(pos)) {
//            return;
//        }
//
//        EnumSet directions = EnumSet.allOf(EnumFacing.class);
//        directions.remove(skipSide);
//
//        final CauseTracker causeTracker = this.getCauseTracker();
//        if (this.isRemote) {
//            for (Object obj : directions) {
//                EnumFacing facing = (EnumFacing) obj;
//                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
//            }
//            return;
//        }
//
//        NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent(this, pos, directions);
//        if (event.isCancelled()) {
//            return;
//        }
//
//        for (EnumFacing facing : EnumFacing.values()) {
//            if (event.getNeighbors().keySet().contains(DirectionFacingProvider.getInstance().getKey(facing).get())) {
//                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
//            }
//        }
//    }

    /**
     * @author bloodmc - November 15th, 2015
     *
     * Purpose: Redirect's vanilla method to ours that includes source block
     * position.
     */
    @Overwrite
    public void notifyBlockOfStateChange(BlockPos notifyPos, final Block blockIn) {
        this.getCauseTracker().notifyBlockOfStateChange(notifyPos, blockIn, null);
    }

    /**
     * @author bloodmc - November 15th, 2015
     *
     * Purpose: Used to track comparators when they update levels.
     */
    @Overwrite
    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
        SpongeImplHooks.updateComparatorOutputLevel((net.minecraft.world.World) (Object) this, pos, blockIn);
    }

    @Override
    public CauseTracker getCauseTracker() {
        return this.causeTracker;
    }


    @Override
    public void setBlock(int x, int y, int z, BlockState block, boolean notifyNeighbors) {
        checkBlockBounds(x, y, z);
        setBlockState(new BlockPos(x, y, z), (IBlockState) block, notifyNeighbors ? 3 : 2);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState blockState, boolean notifyNeighbors, Cause cause) {
        checkArgument(cause != null, "Cause cannot be null!");
        checkArgument(cause.root() instanceof PluginContainer, "PluginContainer must be at the ROOT of a cause!");
        final CauseTracker causeTracker = this.getCauseTracker();
        checkBlockBounds(x, y, z);
        final PhaseContext context = PhaseContext.start()
                .add(NamedCause.source(cause.root()));
        for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
            context.add(NamedCause.of(entry.getKey(), entry.getValue()));
        }
        context.complete();
        causeTracker.switchToPhase(TrackingPhases.PLUGIN, PluginPhase.State.BLOCK_WORKER, context);
        setBlockState(new BlockPos(x, y, z), (IBlockState) blockState, notifyNeighbors ? 3 : 2);
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), BLOCK_MIN, BLOCK_MAX);
        }
    }

}

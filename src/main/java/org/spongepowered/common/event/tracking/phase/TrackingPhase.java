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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.BlockStateTriplet;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        final IMixinWorld mixinWorld =  causeTracker.getMixinWorld();
        final PopulatorType runningGenerator = phaseContext.firstNamed(TrackingHelper.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
        if (!(((IMixinMinecraftServer) MinecraftServer.getServer()).isPreparingChunks()) || runningGenerator != null) {
            final IBlockState actualState = currentState.getBlock().getActualState(currentState, causeTracker.getMinecraftWorld(), pos);
            originalBlockSnapshot = mixinWorld.createSpongeBlockSnapshot(currentState, actualState, pos, flags);
            final List<BlockSnapshot> capturedSpongeBlockSnapshots = phaseContext.getCapturedBlocks().get();
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
        return new BlockStateTriplet(originalBlockSnapshot, transaction);
    }

    public boolean doesStateIgnoreEntitySpawns(IPhaseState currentState) {
        return false;
    }

    public boolean captureEntitySpawn(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final World minecraftWorld = minecraftEntity.worldObj;
        Optional<BlockSnapshot> currentTickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
        Optional<Entity> currentTickEntity = context.firstNamed(NamedCause.SOURCE, Entity.class);
        if (currentTickingBlock.isPresent()) {
            BlockPos sourcePos = VecHelper.toBlockPos(currentTickingBlock.get().getPosition());
            Block targetBlock = minecraftWorld.getBlockState(minecraftEntity.getPosition()).getBlock();
            SpongeHooks.tryToTrackBlockAndEntity(minecraftWorld, currentTickingBlock.get(), minecraftEntity, sourcePos, targetBlock, minecraftEntity.getPosition(), PlayerTracker.Type.NOTIFIER);
        }
        if (currentTickEntity.isPresent()) {
            Optional<User> creator = ((IMixinEntity) currentTickEntity.get()).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
            if (creator.isPresent()) { // transfer user to next entity. This occurs with falling blocks that change into items
                ((IMixinEntity) minecraftEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, creator.get().getUniqueId());
            }
        }
        final List<Entity> capturedEntities = context.getCapturedEntities().get();
        final List<Entity> capturedItems = context.getCapturedItems().get();
        if (minecraftEntity instanceof EntityItem) {
            capturedItems.add(entity);
        } else {
            capturedEntities.add(entity);
        }
        return true;
    }

    public abstract void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext);

    public boolean completeEntitySpawn(Entity entity, Cause cause, CauseTracker causeTracker, int chunkX, int chunkZ, IPhaseState phaseState,
            PhaseContext context) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final IMixinWorld mixinWorld = ((IMixinWorld) minecraftEntity.worldObj);
        // handle actual capturing
        final List<Entity> capturedItems = context.getCapturedItemsSupplier().get().orEmptyList();
        final List<Entity> capturedEntities = context.getCapturedEntities().get();
        final World minecraftWorld = minecraftEntity.worldObj;

        if (minecraftEntity instanceof EntityFishHook && ((EntityFishHook) minecraftEntity).angler == null) {
            // TODO MixinEntityFishHook.setShooter makes angler null
            // sometimes, but that will cause NPE when ticking
            return false;
        }

        EntityLivingBase specialCause = null;
        String causeName = "";
        // Special case for throwables
        if (minecraftEntity instanceof EntityThrowable) {
            EntityThrowable throwable = (EntityThrowable) minecraftEntity;
            specialCause = throwable.getThrower();

            if (specialCause != null) {
                causeName = NamedCause.THROWER;
                if (specialCause instanceof Player) {
                    Player player = (Player) specialCause;
                    ((IMixinEntity) minecraftEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueId());
                }
            }
        }
        // Special case for TNT
        else if (minecraftEntity instanceof EntityTNTPrimed) {
            EntityTNTPrimed tntEntity = (EntityTNTPrimed) minecraftEntity;
            specialCause = tntEntity.getTntPlacedBy();
            causeName = NamedCause.IGNITER;

            if (specialCause instanceof Player) {
                Player player = (Player) specialCause;
                ((IMixinEntity) minecraftEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueId());
            }
        }
        // Special case for Tameables
        else if (minecraftEntity instanceof EntityTameable) {
            EntityTameable tameable = (EntityTameable) minecraftEntity;
            if (tameable.getOwner() != null) {
                specialCause = tameable.getOwner();
                causeName = NamedCause.OWNER;
            }
        }

        if (specialCause != null && !cause.containsNamed(causeName)) {
            cause = cause.with(NamedCause.of(causeName, specialCause));
        }

        org.spongepowered.api.event.Event event;
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        entitySnapshotBuilder.add(((Entity) minecraftEntity).createSnapshot());

        final org.spongepowered.api.world.World spongeWorld = causeTracker.getWorld();
        if (minecraftEntity instanceof EntityItem) {
//                capturedItems.add(entity);
            event = SpongeEventFactory.createDropItemEventCustom(cause, capturedItems, entitySnapshotBuilder.build(), spongeWorld);
        } else {
//                capturedEntities.add(entity);
            event = SpongeEventFactory.createSpawnEntityEventCustom(cause, capturedEntities, entitySnapshotBuilder.build(), spongeWorld);
        }
        if (!SpongeImpl.postEvent(event) && !entity.isRemoved()) {
            if (minecraftEntity instanceof EntityWeatherEffect) {
                return TrackingHelper.addWeatherEffect(minecraftEntity, minecraftWorld);
            }

            if (minecraftEntity instanceof EntityItem) {
                capturedItems.remove(entity);
            } else {
                capturedEntities.remove(entity);
            }
            return mixinWorld.forceSpawnEntity(minecraftEntity, chunkX, chunkZ);
        }

        return false;
    }

}

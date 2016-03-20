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
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.CaptureType;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * The exit point of any phase. Every phase should have an unwinding
     * process where if anything is captured, events should be thrown and
     * processed accordingly. The outcome of each phase is dependent on
     * the {@link IPhaseState} provded, as different states require different
     * handling.
     *
     * <p>Examples of this include: {@link PacketPhase}, {@link WorldPhase}, etc.
     * </p>
     *
     * <p>Note that the {@link CauseTracker} is only provided for easy access
     * to the {@link WorldServer}, {@link IMixinWorldServer}, and
     * {@link World} instances.</p>
     *
     * @param causeTracker The cause tracker instance
     * @param state The state
     * @param phaseContext The context of the current state being unwound
     */
    public abstract void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext);

    // Default methods that are basic qualifiers, leaving up to the phase and state to decide
    // whether they perform capturing.

    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return true;
    }

    // TODO
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return true;
    }

    public boolean allowEntitySpawns(IPhaseState currentState) {
        return true;
    }

    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return false;
    }

    public boolean alreadyCapturingBlockTicks(IPhaseState phaseState, PhaseContext context) {
        return false;
    }

    public boolean ignoresScheduledUpdates(IPhaseState phaseState) {
        return false;
    }

    // Actual capture methods

    public void captureBlockChange(CauseTracker causeTracker, IBlockState currentState, IBlockState newState, Block newBlock, BlockPos pos, int flags, PhaseContext phaseContext, IPhaseState phaseState) {
        final IBlockState actualState = currentState.getBlock().getActualState(currentState,  causeTracker.getMinecraftWorld(), pos);
        final BlockSnapshot originalBlockSnapshot = causeTracker.getMixinWorld().createSpongeBlockSnapshot(currentState, actualState, pos, flags);
        final List<BlockSnapshot> capturedSpongeBlockSnapshots = phaseContext.getCapturedBlocks().get();
        if (newBlock == Blocks.air) {
            ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.BREAK;
            capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
        } else if (newBlock != currentState.getBlock()) {
            ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.PLACE;
            capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
        } else {
            ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.MODIFY;
            capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
        }

    }

    /**
     * This is Step 3 of entity spawning. It is used for the sole purpose of capturing an entity spawn
     * and doesn't actually spawn an entity into the world until the current phase is unwound.
     * The method itself should technically capture entity spawns, however, in the event it
     * is required that the entity cannot be captured, returning {@code false} will mark it
     * to spawn into the world, bypassing any of the bulk spawn events or capturing.
     *
     * <p>NOTE: This method should only be called and handled if and only if {@link #allowEntitySpawns(IPhaseState)}
     * returns {@code true}. Violation of this will have unforseen consequences.</p>
     *
     * @param phaseState The current phase state
     * @param context The current context
     * @param entity The entity being captured
     * @param chunkX The chunk x position
     * @param chunkZ The chunk z position
     * @return True if the entity was successfully captured
     */
    public boolean attemptEntitySpawnCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final WorldServer minecraftWorld = (WorldServer) minecraftEntity.worldObj;
        TrackingHelper.associateEntityCreator(context, minecraftEntity, minecraftWorld);
        if (minecraftEntity instanceof EntityItem) {
            return context.getCapturedItemsSupplier()
                    .map(supplier -> supplier.get().add(entity))
                    .orElse(false);
        } else {
            return context.getCapturedEntitySupplier()
                    .map(supplier -> supplier.get().add(entity))
                    .orElse(false);
        }
    }

    // TODO
    public boolean completeEntitySpawn(Entity entity, Cause cause, CauseTracker causeTracker, int chunkX, int chunkZ, IPhaseState phaseState,
            PhaseContext context) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();
        // handle actual capturing
        final List<Entity> capturedItems = context.getCapturedItemsSupplier().get().orEmptyList();
        final List<Entity> capturedEntities = context.getCapturedEntities().get();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();

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

        final World spongeWorld = causeTracker.getWorld();
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

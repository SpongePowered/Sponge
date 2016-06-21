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
package org.spongepowered.common.event;

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.cause.entity.teleport.PortalTeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.CreativeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.EntityPhase;
import org.spongepowered.common.event.tracking.phase.function.GeneralFunctions;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinPlayerList;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.inventory.util.ContainerUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeCommonEventFactory {

    @SuppressWarnings("unchecked")
    @Nullable
    public static CollideEntityEvent callCollideEntityEvent(net.minecraft.world.World world, @Nullable net.minecraft.entity.Entity sourceEntity,
            List<net.minecraft.entity.Entity> entities) {
        final Cause cause;
        if (sourceEntity != null) {
            cause = Cause.of(NamedCause.source(sourceEntity));
        } else {
            IMixinWorldServer spongeWorld = (IMixinWorldServer) world;
            CauseTracker causeTracker = spongeWorld.getCauseTracker();
            PhaseContext context = causeTracker.getStack().peekContext();

            final Optional<BlockSnapshot> currentTickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
            final Optional<TileEntity> currentTickingTileEntity = context.firstNamed(NamedCause.SOURCE, TileEntity.class);
            final Optional<Entity> currentTickingEntity = context.firstNamed(NamedCause.SOURCE, Entity.class);
            if (currentTickingBlock.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingBlock.get()));
            } else if (currentTickingTileEntity.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingTileEntity.get()));
            } else if (currentTickingEntity.isPresent()) {
                cause = Cause.of(NamedCause.source(currentTickingEntity.get()));
            } else {
                cause = null;
            }

            if (cause == null) {
                return null;
            }
        }

        List<Entity> spEntities = (List<Entity>) (List<?>) entities;
        CollideEntityEvent event = SpongeEventFactory.createCollideEntityEvent(cause, spEntities, (World) world);
        SpongeImpl.postEvent(event);
        return event;
    }

    @SuppressWarnings("rawtypes")
    public static NotifyNeighborBlockEvent callNotifyNeighborEvent(World world, BlockPos pos, EnumSet notifiedSides) {
        final CauseTracker causeTracker = ((IMixinWorldServer) world).getCauseTracker();
        final PhaseData peek = causeTracker.getStack().peek();
        final PhaseContext context = peek.getContext();
        final BlockSnapshot snapshot = world.createSnapshot(pos.getX(), pos.getY(), pos.getZ());
        final Map<Direction, BlockState> neighbors = new HashMap<>();

        for (Object obj : notifiedSides) {
            EnumFacing notifiedSide = (EnumFacing) obj;
            BlockPos offset = pos.offset(notifiedSide);
            if (!causeTracker.getMinecraftWorld().isBlockLoaded(offset)) {
                continue;
            }
            Direction direction = DirectionFacingProvider.getInstance().getKey(notifiedSide).get();
            Location<World> location = new Location<>(world, VecHelper.toVector3i(offset));
            if (location.getBlockY() >= 0 && location.getBlockY() <= 255) {
                neighbors.put(direction, location.getBlock());
            }
        }

        ImmutableMap<Direction, BlockState> originalNeighbors = ImmutableMap.copyOf(neighbors);
        // Determine cause
        final Cause.Builder builder = Cause.source(snapshot);
        final IMixinChunk mixinChunk = (IMixinChunk) causeTracker.getMinecraftWorld().getChunkFromBlockCoords(pos);

        peek.getState().getPhase().populateCauseForNotifyNeighborEvent(peek.getState(), context, builder, causeTracker, mixinChunk, pos);

        NotifyNeighborBlockEvent event = SpongeEventFactory.createNotifyNeighborBlockEvent(builder.build(), originalNeighbors, neighbors);
        StaticMixinHelper.processingInternalForgeEvent = true;
        SpongeImpl.postEvent(event);
        StaticMixinHelper.processingInternalForgeEvent = false;
        return event;
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(Cause cause, Optional<Vector3d> interactionPoint, BlockSnapshot targetBlock, Direction targetSide, EnumHand hand) {
        return callInteractBlockEventSecondary(cause, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, Tristate.UNDEFINED, interactionPoint, targetBlock, targetSide, hand);
    }

    public static InteractBlockEvent.Secondary callInteractBlockEventSecondary(Cause cause, Tristate originalUseBlockResult, Tristate useBlockResult, Tristate originalUseItemResult, Tristate useItemResult, Optional<Vector3d> interactionPoint, BlockSnapshot targetBlock, Direction targetSide, EnumHand hand) {
        InteractBlockEvent.Secondary event;
        if (hand == EnumHand.MAIN_HAND) {
            event = SpongeEventFactory.createInteractBlockEventSecondaryMainHand(cause, originalUseBlockResult, useBlockResult, originalUseItemResult, useItemResult, interactionPoint, targetBlock, targetSide);
        } else {
            event = SpongeEventFactory.createInteractBlockEventSecondaryOffHand(cause, originalUseBlockResult, useBlockResult, originalUseItemResult, useItemResult, interactionPoint, targetBlock, targetSide);
        }
        SpongeImpl.postEvent(event);
        return event;
    }

    public static boolean callDestructEntityEventDeath(EntityLivingBase entity, DamageSource source) {
        final MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter();
        MessageChannel originalChannel;
        MessageChannel channel;
        Text originalMessage;
        Optional<User> sourceCreator = Optional.empty();
        boolean messageCancelled = false;

        if (entity instanceof EntityPlayerMP) {
            Player player = (Player) entity;
            originalChannel = player.getMessageChannel();
            channel = player.getMessageChannel();
        } else {
            originalChannel = MessageChannel.TO_NONE;
            channel = MessageChannel.TO_NONE;
        }
        if (source instanceof EntityDamageSource) {
            EntityDamageSource damageSource = (EntityDamageSource) source;
            IMixinEntity spongeEntity = (IMixinEntity) damageSource.getSourceOfDamage();
            if (spongeEntity != null) {
                sourceCreator = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
            }
        }

        originalMessage = SpongeTexts.toText(entity.getCombatTracker().getDeathMessage());
        formatter.getBody().add(new MessageEvent.DefaultBodyApplier(originalMessage));
        List<NamedCause> causes = new ArrayList<>();
        causes.add(NamedCause.of("Attacker", source));
        if (sourceCreator.isPresent()) {
            causes.add(NamedCause.owner(sourceCreator.get()));
        }

        Cause cause = Cause.of(causes);
        DestructEntityEvent.Death event = SpongeEventFactory.createDestructEntityEventDeath(cause, originalChannel, Optional.of(channel), formatter, (Living) entity, messageCancelled);
        SpongeImpl.postEvent(event);
        Text message = event.getMessage();
        if (!event.isMessageCancelled() && !message.isEmpty()) {
            event.getChannel().ifPresent(eventChannel -> eventChannel.send(entity, event.getMessage()));
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static DropItemEvent.Destruct callDropItemEventDestruct(net.minecraft.entity.Entity entity, DamageSource source, List<EntityItem> itemDrops) {
        Optional<User> sourceCreator = Optional.empty();

        if (source instanceof EntityDamageSource) {
            EntityDamageSource damageSource = (EntityDamageSource) source;
            IMixinEntity spongeEntity = (IMixinEntity) damageSource.getSourceOfDamage();
            if (spongeEntity != null) {
                sourceCreator = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
            }
        }

        List<NamedCause> causes = new ArrayList<>();
        causes.add(NamedCause.source(EntitySpawnCause.builder()
                .entity((Entity) entity)
                .type(SpawnTypes.DROPPED_ITEM)
                .build()));
        causes.add(NamedCause.of("Attacker", source));
        causes.add(NamedCause.of("Victim", entity));
        if (sourceCreator.isPresent()) {
            causes.add(NamedCause.owner(sourceCreator.get()));
        }
        DropItemEvent.Destruct event = SpongeEventFactory.createDropItemEventDestruct(Cause.of(causes), (List<org.spongepowered.api.entity.Entity>)(List<?>) itemDrops, (World) entity.worldObj);
        SpongeImpl.postEvent(event);
        return event;
    }

    @SuppressWarnings("unchecked")
    public static DropItemEvent.Dispense callDropItemEventDispenseSingle(net.minecraft.entity.Entity entity, EntityItem droppedItem) {
        List<EntityItem> droppedItems = new ArrayList<>();
        droppedItems.add(droppedItem);

        SpawnCause spawnCause = EntitySpawnCause.builder()
                .entity((Entity) entity)
                .type(SpawnTypes.DROPPED_ITEM)
                .build();
        DropItemEvent.Dispense event = SpongeEventFactory.createDropItemEventDispense(Cause.of(NamedCause.source(spawnCause)), (List<org.spongepowered.api.entity.Entity>)(List<?>) droppedItems, (World) entity.worldObj);
        SpongeImpl.postEvent(event);
        return event;
    }

    public static boolean handleCollideBlockEvent(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, net.minecraft.entity.Entity entity, Direction direction) {
        final WorldServer worldServer = (WorldServer) world;
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldServer;
        final CauseTracker causeTracker = mixinWorldServer.getCauseTracker();
        final Cause.Builder builder = Cause.source(entity);
        builder.named(NamedCause.of(NamedCause.PHYSICAL, entity));

        if (!(entity instanceof EntityPlayer)) {
            IMixinEntity spongeEntity = (IMixinEntity) entity;
            Optional<User> user = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
            if (user.isPresent()) {
                builder.named(NamedCause.owner(user.get()));
            }
        }

        // TODO: Add target side support
        CollideBlockEvent event = SpongeEventFactory.createCollideBlockEvent(builder.build(), (BlockState) state,
                new Location<>((World) world, VecHelper.toVector3d(pos)), direction);
        boolean cancelled = SpongeImpl.postEvent(event);
        if (!cancelled) {
            IMixinEntity spongeEntity = (IMixinEntity) entity;
            if (!pos.equals(spongeEntity.getLastCollidedBlockPos())) {
                final PhaseData peek = causeTracker.getStack().peek();
                final Optional<User> notifier = peek.getContext().firstNamed(NamedCause.NOTIFIER, User.class);
                if (notifier.isPresent()) {
                    IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(pos);
                    spongeChunk.addTrackedBlockPosition(block, pos, notifier.get(), PlayerTracker.Type.NOTIFIER);
                }
            }
        }

        return cancelled;
    }

    public static boolean handleCollideImpactEvent(net.minecraft.entity.Entity projectile, @Nullable ProjectileSource projectileSource,
            RayTraceResult movingObjectPosition) {
        final WorldServer worldServer = (WorldServer) projectile.worldObj;
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldServer;
        final CauseTracker causeTracker = mixinWorldServer.getCauseTracker();
        RayTraceResult.Type movingObjectType = movingObjectPosition.typeOfHit;
        final Cause.Builder builder = Cause.source(projectile).named("ProjectileSource", projectileSource == null
                                                                                         ? ProjectileSource.UNKNOWN
                                                                                         : projectileSource);
        final Optional<User> notifier = causeTracker.getStack().peek()
                .getContext()
                .firstNamed(NamedCause.NOTIFIER, User.class);
        notifier.ifPresent(user -> builder.named(NamedCause.OWNER, user));

        Location<World> impactPoint = new Location<>((World) projectile.worldObj, VecHelper.toVector3d(movingObjectPosition.hitVec));
        boolean cancelled = false;

        if (movingObjectType == RayTraceResult.Type.BLOCK) {
            BlockSnapshot targetBlock = ((World) projectile.worldObj).createSnapshot(VecHelper.toVector3i(movingObjectPosition.getBlockPos()));
            Direction side = Direction.NONE;
            if (movingObjectPosition.sideHit != null) {
                side = DirectionFacingProvider.getInstance().getKey(movingObjectPosition.sideHit).get();
            }

            CollideBlockEvent.Impact event = SpongeEventFactory.createCollideBlockEventImpact(builder.build(), impactPoint, targetBlock.getState(),
                    targetBlock.getLocation().get(), side);
            cancelled = SpongeImpl.postEvent(event);
            // Track impact block if event is not cancelled
            if (!cancelled && notifier.isPresent()) {
                BlockPos targetPos = VecHelper.toBlockPos(impactPoint.getBlockPosition());
                IMixinChunk spongeChunk = (IMixinChunk) projectile.worldObj.getChunkFromBlockCoords(targetPos);
                spongeChunk.addTrackedBlockPosition((Block) targetBlock.getState().getType(), targetPos, notifier.get(), PlayerTracker.Type.NOTIFIER);
            }
        } else if (movingObjectPosition.entityHit != null) { // entity
            ArrayList<Entity> entityList = new ArrayList<>();
            entityList.add((Entity) movingObjectPosition.entityHit);
            CollideEntityEvent.Impact event = SpongeEventFactory.createCollideEntityEventImpact(builder.build(), entityList, impactPoint,
                    (World) projectile.worldObj);
            return SpongeImpl.postEvent(event);
        }

        return cancelled;
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(net.minecraft.entity.Entity entityIn, Location<World> location) {
        Transform<World> fromTransform = ((IMixinEntity) entityIn).getTransform();
        Transform<World> toTransform = fromTransform.setLocation(location).setRotation(new Vector3d(entityIn.rotationPitch, entityIn.rotationYaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform, false);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(net.minecraft.entity.Entity entityIn, double posX, double posY, double posZ, float yaw, float pitch) {
        Transform<World> fromTransform = ((IMixinEntity) entityIn).getTransform();
        Transform<World> toTransform = fromTransform.setPosition(new Vector3d(posX, posY, posZ)).setRotation(new Vector3d(pitch, yaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform, false);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(net.minecraft.entity.Entity entityIn, Transform<World> fromTransform, Transform<World> toTransform, boolean apiCall) {

        // Use origin world to get correct cause
        IMixinWorldServer spongeWorld = (IMixinWorldServer) fromTransform.getExtent();
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        final PhaseData peek = causeTracker.getStack().peek();
        final IPhaseState state = peek.getState();
        final PhaseContext context = peek.getContext();

        final Cause teleportCause = state.getPhase().generateTeleportCause(state, context);

        MoveEntityEvent.Teleport event = SpongeEventFactory.createMoveEntityEventTeleport(teleportCause, fromTransform, toTransform, (Entity) entityIn);
        SpongeImpl.postEvent(event);
        return event;
    }

    @Nullable
    public static MoveEntityEvent.Teleport.Portal handleDisplaceEntityPortalEvent(net.minecraft.entity.Entity entityIn, int targetDimensionId, @Nullable Teleporter teleporter) {
        SpongeImplHooks.registerPortalAgentType(teleporter);
        final MinecraftServer mcServer = SpongeImpl.getServer();
        final IMixinPlayerList mixinPlayerList = (IMixinPlayerList) mcServer.getPlayerList();
        final IMixinEntity mixinEntity = (IMixinEntity) entityIn;
        final Transform<World> fromTransform = mixinEntity.getTransform();
        final int fromDimensionId = entityIn.dimension;
        final WorldServer fromWorld = ((WorldServer) entityIn.worldObj);
        final IMixinWorldServer fromMixinWorld = (IMixinWorldServer) fromWorld;
        // handle the end
        if (targetDimensionId == 1 && fromWorld.provider instanceof WorldProviderEnd) {
            targetDimensionId = 0;
        }
        WorldServer toWorld = mcServer.worldServerForDimension(targetDimensionId);
        final IMixinWorldServer toMixinWorld = (IMixinWorldServer) toWorld;
        if (teleporter == null) {
            teleporter = toWorld.getDefaultTeleporter();
        }
        final SpongeConfig<?> activeConfig = fromMixinWorld.getActiveConfig();
        String worldName = "";
        String teleporterClassName = teleporter.getClass().getName();

        // check for new destination in config
        if (teleporterClassName.equals("net.minecraft.world.Teleporter")) {
            if (toWorld.provider instanceof WorldProviderHell) {
                worldName = activeConfig.getConfig().getWorld().getPortalAgents().get("minecraft:default_nether");
            } else if (toWorld.provider instanceof WorldProviderEnd) {
                worldName = activeConfig.getConfig().getWorld().getPortalAgents().get("minecraft:default_the_end");
            }
        } else { // custom
            worldName = activeConfig.getConfig().getWorld().getPortalAgents().get("minecraft:" + teleporter.getClass().getSimpleName());
        }

        if (worldName != null && !worldName.equals("")) {
            for (WorldProperties worldProperties : Sponge.getServer().getAllWorldProperties()) {
                if (worldProperties.getWorldName().equalsIgnoreCase(worldName)) {
                    Optional<World> spongeWorld = Sponge.getServer().loadWorld(worldProperties);
                    if (spongeWorld.isPresent()) {
                        toWorld = (WorldServer) spongeWorld.get();
                        teleporter = toWorld.getDefaultTeleporter();
                        ((IMixinTeleporter) teleporter).setPortalType(targetDimensionId);
                    }
                }
            }
        }

        EntityUtil.adjustEntityPostionForTeleport(mixinPlayerList, entityIn, fromWorld, toWorld);
        if (entityIn instanceof EntityPlayerMP) {
            // disable packets from being sent to clients to avoid syncing issues, this is re-enabled before the event
            ((IMixinNetHandlerPlayServer) ((EntityPlayerMP) entityIn).connection).setAllowClientLocationUpdate(false);
        }

        final PhaseContext context = PhaseContext.start();
        context.add(NamedCause.source(mixinEntity))
                .add(NamedCause.of(InternalNamedCauses.Teleporting.FROM_WORLD, fromWorld))
                .add(NamedCause.of(InternalNamedCauses.Teleporting.TARGET_WORLD, toWorld))
                .add(NamedCause.of(InternalNamedCauses.Teleporting.TARGET_TELEPORTER, teleporter))
                .add(NamedCause.of(InternalNamedCauses.Teleporting.FROM_TRANSFORM, fromTransform))
                .addBlockCaptures()
                .addEntityCaptures();
        final Cause teleportCause = Cause.of(NamedCause.source(PortalTeleportCause.builder()
                        .agent((PortalAgent) teleporter)
                        .type(TeleportTypes.PORTAL)
                        .build()
                )
        );
        context.complete();
        fromMixinWorld.getCauseTracker().switchToPhase(EntityPhase.State.LEAVING_DIMENSION, context);

        final CauseTracker toCauseTracker = toMixinWorld.getCauseTracker();
        toCauseTracker.switchToPhase(EntityPhase.State.CHANGING_TO_DIMENSION, context);

        if (entityIn.isEntityAlive() && !(fromWorld.provider instanceof WorldProviderEnd)) {
            fromWorld.theProfiler.startSection("placing");
            // need to use placeInPortal to support mods
            teleporter.placeInPortal(entityIn, entityIn.rotationYaw);
            fromWorld.theProfiler.endSection();
        }

        // Complete phases, just because we need to. The phases don't actually do anything, because the processing resides here.
        toCauseTracker.completePhase();
        fromMixinWorld.getCauseTracker().completePhase();
        // Grab the exit location of entity after being placed into portal
        final Transform<World> portalExitTransform = mixinEntity.getTransform().setExtent((World) toWorld);
        final MoveEntityEvent.Teleport.Portal event = SpongeEventFactory.createMoveEntityEventTeleportPortal(teleportCause, fromTransform, portalExitTransform, (PortalAgent) teleporter, mixinEntity, true);

        SpongeImpl.postEvent(event);
        if (entityIn instanceof EntityPlayerMP) {
            ((IMixinNetHandlerPlayServer) ((EntityPlayerMP) entityIn).connection).setAllowClientLocationUpdate(true);
        }
        final Vector3i chunkPosition = mixinEntity.getLocation().getChunkPosition();
        final IMixinTeleporter toMixinTeleporter = (IMixinTeleporter) teleporter;

        if (event.isCancelled()) {
            // update cache
            ((IMixinTeleporter) teleporter).removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
            mixinEntity.setLocationAndAngles(fromTransform);
            return event;
        }

        final Transform<World> toTransform = event.getToTransform();
        final List<BlockSnapshot> capturedBlocks = context.getCapturedBlocks();

        if (!portalExitTransform.equals(toTransform)) {
            // if plugin set to same world, just set the transform
            if (fromWorld == toTransform.getExtent()) {
                // force cancel so we know to skip remaining logic
                event.setCancelled(true);
                // update cache
                toMixinTeleporter.removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
                mixinEntity.setLocationAndAngles(toTransform);
                if (entityIn instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) entityIn;
                    // close any open inventory
                    player.closeScreen();
                    // notify client
                    player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                }
                return event;
            }
        } else {
            if (toWorld.provider instanceof WorldProviderEnd) {
                BlockPos blockpos = entityIn.worldObj.getTopSolidOrLiquidBlock(toWorld.getSpawnPoint());
                entityIn.moveToBlockPosAndAngles(blockpos, entityIn.rotationYaw, entityIn.rotationPitch);
            }
        }

        // Attempt to create the portal
        if (event.isCancelled()) {
            return null;
        }

        if (!capturedBlocks.isEmpty()
            && !GeneralFunctions.processBlockCaptures(capturedBlocks, toCauseTracker, EntityPhase.State.CHANGING_TO_DIMENSION, context)) {
            toMixinTeleporter.removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
        }

        if (!event.getKeepsVelocity()) {
            entityIn.motionX = 0;
            entityIn.motionY = 0;
            entityIn.motionZ = 0;
        }
        return event;
    }


    public static void checkSpawnEvent(Entity entity, Cause cause) {
        checkArgument(cause.root() instanceof SpawnCause, "The cause does not have a SpawnCause! It has instead: {}", cause.root().toString());
        checkArgument(cause.containsNamed(NamedCause.SOURCE), "The cause does not have a \"Source\" named object!");
        checkArgument(cause.get(NamedCause.SOURCE, SpawnCause.class).isPresent(), "The SpawnCause is not the \"Source\" of the cause!");

    }


    public static CreativeInventoryEvent.Click callCreativeClickInventoryEvent(EntityPlayerMP player, CPacketCreativeInventoryAction packetIn) {
        Cause cause = Cause.of(NamedCause.owner(player));
        // Creative doesn't inform server of cursor status
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
        if (((IMixinContainer) player.openContainer).getCapturedTransactions().size() == 0 && packetIn.getSlotId() >= 0
            && packetIn.getSlotId() < player.openContainer.inventorySlots.size()) {
            Slot slot = player.openContainer.getSlot(packetIn.getSlotId());
            if (slot != null) {
                SlotTransaction slotTransaction =
                        new SlotTransaction(new SlotAdapter(slot), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);
                ((IMixinContainer) player.openContainer).getCapturedTransactions().add(slotTransaction);
            }
        }
        CreativeInventoryEvent.Click event = SpongeEventFactory.createCreativeInventoryEventClick(cause, cursorTransaction,
                (org.spongepowered.api.item.inventory.Container) player.openContainer,
                ((IMixinContainer) player.openContainer).getCapturedTransactions());
        SpongeImpl.postEvent(event);
        return event;
    }

    public static CreativeInventoryEvent.Drop callCreativeDropInventoryEvent(EntityPlayerMP player, ItemStack itemstack, CPacketCreativeInventoryAction packetIn) {
        // Creative doesn't inform server of cursor status
        Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(ItemStackSnapshot.NONE, ItemStackSnapshot.NONE);

        EntityItem entityitem;
        if (itemstack == null) {
            // create dummy item
            entityitem = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, (net.minecraft.item.ItemStack) ItemStackSnapshot.NONE.createStack());
        } else {
            entityitem = player.dropItem(itemstack, true);
        }

        List<Entity> entityList = new ArrayList<>();
        entityList.add((Entity) entityitem);

        SpawnCause spawnCause = EntitySpawnCause.builder()
                .entity((Entity) player)
                .type(SpawnTypes.DROPPED_ITEM)
                .build();
        final Cause cause = Cause.source(spawnCause).owner(player).build();
        CreativeInventoryEvent.Drop event = SpongeEventFactory.createCreativeInventoryEventDrop(cause, cursorTransaction, entityList,
                ContainerUtil.fromNative(player.openContainer), (World) player.worldObj,
                ContainerUtil.toMixin(player.openContainer).getCapturedTransactions());
        SpongeImpl.postEvent(event);
        return event;
    }
}
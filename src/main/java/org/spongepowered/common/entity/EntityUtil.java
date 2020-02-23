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
package org.spongepowered.common.entity;

import static net.minecraft.util.EntityPredicates.NOT_SPECTATING;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SEntityPropertiesPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SPlayerAbilitiesPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.explosive.fused.FusedExplosive;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.teleport.PortalAgent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.InvokingTeleporterContext;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.accessor.entity.EntityAccessor;
import org.spongepowered.common.mixin.accessor.entity.LivingEntityAccessor;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class EntityUtil {

    public static final Function<PhaseContext<?>, Supplier<Optional<User>>> ENTITY_CREATOR_FUNCTION = (context) ->
        () -> Stream.<Supplier<Optional<User>>>builder()
            .add(() -> context.getSource(User.class))
            .add(context::getNotifier)
            .add(context::getOwner)
            .build()
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    private EntityUtil() {
    }

    @SuppressWarnings("Guava") private static final Predicate<Entity> TRACEABLE = Predicates.and(NOT_SPECTATING,
      entity -> entity != null && entity.canBeCollidedWith());

    @Nullable
    public static Entity transferEntityToWorld(final Entity entity, @Nullable MoveEntityEvent.Teleport event,
        @Nullable ServerWorld toWorld,  @Nullable final ForgeITeleporterBridge teleporter, final boolean recreate) {

        if (entity.world.isRemote || entity.removed) {
            return null;
        }

        org.spongepowered.api.entity.Entity sEntity = (org.spongepowered.api.entity.Entity) entity;
        final EntityBridge mEntity = (EntityBridge) entity;
        final Transform fromTransform = sEntity.getTransform();
        final ServerWorld fromWorld = (ServerWorld) sEntity.getWorld();

        fromWorld.profiler.startSection("changeDimension");

        boolean loadChunks = true;

        // use the world from event
        final Transform toTransform;

        // Assume portal
        if (event == null) {
            if (toWorld == null || teleporter == null) {
                return null;
            }

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                final InvokingTeleporterContext context = createInvokingTeleporterPhase(entity, toWorld, teleporter)) {

                if (!context.getDidPort()) {
                    return entity;
                }

                frame.pushCause(context.getTeleporter());
                frame.pushCause(entity);

                // TODO All of this code assumes that all teleports outside of the API are portals...need to re-think this and somehow correctly
                // TODO determine what it truly is for API 8
                frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.PORTAL);

                event = SpongeEventFactory.createMoveEntityEventTeleportPortal(frame.getCurrentCause(), fromTransform, context.getExitTransform(),
                        (World) fromWorld, (World) toWorld, sEntity, context.getTeleporter(), true, true);

                if (SpongeImpl.postEvent(event)) {
                    // Mods may cancel this event in order to run custom transfer logic
                    // We need to make sure to only rollback if they completely changed the world
                    if (event.getFromWorld() != sEntity.getWorld()) {
                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = Location.of((World) context.getTargetWorld(), context.getExitTransform().getPosition()).getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        mEntity.bridge$setLocationAndAngles(fromTransform);
                    }

                    return null;
                } else {
                    toTransform = event.getToTransform();
                    toWorld = (ServerWorld) event.getToWorld();

                    // Handle plugins setting changing to a different world than the teleporter said so. This is considered a cancellation
                    if (context.getTargetWorld() != toWorld) {
                        event.setCancelled(true);

                        if (teleporter instanceof TeleporterBridge) {

                            final Vector3i chunkPosition = Location.of((World) context.getTargetWorld(), context.getExitTransform().getPosition()).getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        mEntity.bridge$setLocationAndAngles(toTransform);
                        return null;
                    }

                    // If we don't use the portal agent clear out the portal blocks that were created
                    if (!((MoveEntityEvent.Teleport.Portal) event).getUsePortalAgent()) {
                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = Location.of((World) context.getTargetWorld(), context.getExitTransform().getPosition()).getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }
                        context.getCapturedBlockSupplier().restoreOriginals();
                    } else {

                        // Unwind PhaseTracker captured blocks here, the actual position placement of the entity is common code below
                        if (teleporter instanceof TeleporterBridge && !context.getCapturedBlockSupplier().isEmpty() && !TrackingUtil
                            .processBlockCaptures(context)) {
                            // Transactions were rolled back, the portal wasn't made. We need to bomb the dimension change and clear portal cache
                            final Vector3i chunkPosition = Location.of((World) context.getTargetWorld(), context.getExitTransform().getPosition()).getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));

                            return null;
                        }
                    }

                    // If we got here, the event went completely through and the portal logic will load chunks on the other end, don't reload chunks
                    loadChunks = false;
                }
            }
            // Make sure no one else besides me is stupid enough to pass a cancelled event to this....
        } else if (event.isCancelled()) {
            return null;
        } else {
            toTransform = event.getToTransform();
            toWorld = (ServerWorld) event.getToWorld();
        }

        fromWorld.profiler.endStartSection("reloading");
        final Entity toReturn;

        if (recreate) {
            toReturn = entity.getType().create(toWorld);
            sEntity = (org.spongepowered.api.entity.Entity) toReturn;
            if (toReturn == null) {
                return entity;
            }

            ((EntityAccessor) toReturn).accessor$copyDataFromOld(entity);
        } else {
            toReturn = entity;
        }

        if (!event.getKeepsVelocity()) {
            toReturn.motionX = 0;
            toReturn.motionY = 0;
            toReturn.motionZ = 0;
        }

        if (loadChunks) {
            final Vector3i toChunkPosition = Location.of((World) toWorld, toTransform.getPosition()).getChunkPosition();
            toWorld.getChunkProvider().loadChunk(toChunkPosition.getX(), toChunkPosition.getZ());
        }

        fromWorld.profiler.startSection("moving");
        ((EntityBridge) toReturn).bridge$setLocationAndAngles(toTransform);
        fromWorld.profiler.endSection();

        try (final PhaseContext<?> ignored = EntityPhase.State.CHANGING_DIMENSION.createPhaseContext(PhaseTracker.SERVER).setTargetWorld(toWorld).buildAndSwitch()) {
            if (recreate) {
                final boolean flag = toReturn.forceSpawn;
                toReturn.forceSpawn = true;
                toWorld.addEntity0(toReturn);
                toReturn.forceSpawn = flag;
                toWorld.updateEntityWithOptionalForce(toReturn, false);
            } else {
                toWorld.addEntity0(toReturn);
                toWorld.updateEntityWithOptionalForce(toReturn, false);
            }
        }

        // Fix Vanilla bug where primed minecart TNTs don't keep state through a portal
        if (toReturn instanceof TNTMinecartEntity) {
            if (((FusedExplosive) sEntity).isPrimed()) {
                toReturn.world.setEntityState(toReturn, (byte) 10);
            }
        }

        entity.removed = true;
        fromWorld.profiler.endSection();
        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
        fromWorld.profiler.endSection();

        return toReturn;
    }

    @Nullable
    public static ServerPlayerEntity transferPlayerToWorld(final ServerPlayerEntity player, @Nullable MoveEntityEvent.Teleport event,
        @Nullable ServerWorld toWorld,  @Nullable final ForgeITeleporterBridge teleporter) {

        if (player.world.isRemote || player.removed) {
            return null;
        }

        final PlayerList playerList = SpongeImpl.getServer().getPlayerList();
        final Player sPlayer = (Player) player;
        final Transform fromTransform = sPlayer.getTransform();
        final ServerWorld fromWorld = (ServerWorld) sPlayer.getWorld();

        fromWorld.profiler.startSection("changeDimension");

        // use the world from event
        final Transform toTransform;

        // Assume portal
        if (event == null) {
            if (toWorld == null || teleporter == null) {
                return null;
            }

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                final InvokingTeleporterContext context = createInvokingTeleporterPhase(player, toWorld, teleporter)) {

                if (!context.getDidPort()) {
                    return null;
                }

                frame.pushCause(context.getTeleporter());
                frame.pushCause(player);

                // TODO All of this code assumes that all teleports outside of the API are portals...need to re-think this and somehow correctly
                // TODO determine what it truly is for API 8
                frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.PORTAL);

                event = SpongeEventFactory.createMoveEntityEventTeleportPortal(frame.getCurrentCause(), fromTransform, context.getExitTransform(),
                        (World) fromWorld, (World) toWorld, sPlayer, context.getTeleporter(), true, true);

                if (SpongeImpl.postEvent(event)) {
                    // Mods may cancel this event in order to run custom transfer logic
                    // We need to make sure to only rollback if they completely changed the world
                    if (event.getFromWorld() != sPlayer.getWorld()) {
                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = Location.of((World) context.getTargetWorld(), context.getExitTransform().getPosition()).getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        ((EntityBridge) player).bridge$setLocationAndAngles(fromTransform);
                    }

                    return null;
                } else {
                    toTransform = event.getToTransform();
                    toWorld = (ServerWorld) event.getToWorld();

                    // Handle plugins setting changing to a different world than the teleporter said so. This is considered a cancellation
                    if (context.getTargetWorld() != toWorld) {
                        event.setCancelled(true);

                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = Location.of((World) context.getTargetWorld(), context.getExitTransform().getPosition()).getChunkPosition();
                            ((TeleporterBridge) teleporter).bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        ((EntityBridge) player).bridge$setLocationAndAngles(toTransform);
                        return null;
                    }

                    // If we don't use the portal agent clear out the portal blocks that
                    if (!((MoveEntityEvent.Teleport.Portal) event).getUsePortalAgent()) {
                        final Vector3i chunkPosition = Location.of((World) context.getTargetWorld(), context.getExitTransform().getPosition()).getChunkPosition();
                        if (teleporter instanceof TeleporterBridge) {
                            ((TeleporterBridge) teleporter).bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }
                        context.getCapturedBlockSupplier().restoreOriginals();
                    } else {

                        // Unwind PhaseTracker captured blocks here, the actual position placement of the entity is common code below
                        if (teleporter instanceof TeleporterBridge && !context.getCapturedBlockSupplier().isEmpty() && !TrackingUtil
                            .processBlockCaptures(context)) {
                            // Transactions were rolled back, the portal wasn't made. We need to bomb the dimension change and clear portal cache
                            final Vector3i chunkPosition = Location.of((World) context.getTargetWorld(), context.getExitTransform().getPosition()).getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));

                            return null;
                        }
                    }
                }
            }
            // Make sure no one else besides me is stupid enough to pass a cancelled event to this....
        } else if (event.isCancelled()) {
            return null;
        } else {
            toTransform = event.getToTransform();
            toWorld = (ServerWorld) event.getToWorld();
        }

        final int dimensionId;

        if (!((ServerPlayerEntityBridge) player).bridge$usesCustomClient()) {

            // Check if the world we're going to matches our provider type. if so, we need to send a fake respawn packet to clear chunks
            final int fromClientDimId = WorldManager.getClientDimensionId(player, fromWorld);
            final int toClientDimId = WorldManager.getClientDimensionId(player, toWorld);

            if (fromClientDimId == toClientDimId) {
                final int fakeDimId;
                switch (fromClientDimId) {
                    case 1:
                        fakeDimId = -1;
                        break;
                    case 0:
                        fakeDimId = 1;
                        break;
                    default:
                        fakeDimId = 0;
                        break;
                }

                player.connection.sendPacket(new SRespawnPacket(fakeDimId, toWorld.getDifficulty(), toWorld.getWorldType(),
                        player.interactionManager.getGameType()));
            }

            dimensionId = toClientDimId;
        } else {
            // We're a custom client, their problem to handle the client provider
            WorldManager.sendDimensionRegistration(player, toWorld.dimension);

            dimensionId = ((ServerWorldBridge) toWorld).bridge$getDimensionId();
        }

        player.connection.sendPacket(new SRespawnPacket(dimensionId, toWorld.getDifficulty(), toWorld.getWorldType(),
            player.interactionManager.getGameType()));

        player.dimension = ((ServerWorldBridge) toWorld).bridge$getDimensionId(); // If a Vanilla client, dimensionId could be a provider id.
        player.setWorld(toWorld);

        playerList.updatePermissionLevel(player);

        fromWorld.removeEntityDangerously(player);
        player.removed = false;

        final Vector3d position = toTransform.getPosition();
        player.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) toTransform.getYaw(), (float) toTransform.getPitch());

        try (final PhaseContext<?> ignored = EntityPhase.State.CHANGING_DIMENSION.createPhaseContext(PhaseTracker.SERVER).setTargetWorld(toWorld).buildAndSwitch()) {
            toWorld.addEntity0(player);
            toWorld.updateEntityWithOptionalForce(player, false);
        }

        // preparePlayer
        fromWorld.getPlayerChunkMap().removePlayer(player);
        toWorld.getPlayerChunkMap().addPlayer(player);

        final Vector3i toChunkPosition = toTransform.getLocation().getChunkPosition();
        toWorld.getChunkProvider().provideChunk(toChunkPosition.getX(), toChunkPosition.getZ());

        if (event instanceof MoveEntityEvent.Teleport.Portal) {
            CriteriaTriggers.CHANGED_DIMENSION.trigger(player, fromWorld.dimension.getType(), toWorld.dimension.getType());

            if (fromWorld.dimension.getType() == DimensionType.NETHER && toWorld.dimension.getType() == DimensionType.OVERWORLD
                && player.getEnteredNetherPosition() != null) {
                CriteriaTriggers.NETHER_TRAVEL.trigger(player, player.getEnteredNetherPosition());
            }
        }
        //

        player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

        player.interactionManager.setWorld(toWorld);
        playerList.sendWorldInfo(player, toWorld);
        playerList.sendInventory(player);

        player.connection.sendPacket(new SPlayerAbilitiesPacket(player.abilities));

        for (final EffectInstance potioneffect : player.getActivePotionEffects()) {
            player.connection.sendPacket(new SPlayEntityEffectPacket(player.getEntityId(), potioneffect));
        }

        // Fix MC-88179: on non-death SPacketRespawn, also resend attributes
        final AttributeMap attributemap = (AttributeMap) player.getAttributes();
        final Collection<IAttributeInstance> watchedAttribs = attributemap.getWatchedAttributes();
        if (!watchedAttribs.isEmpty()) {
            player.connection.sendPacket(new SEntityPropertiesPacket(player.getEntityId(), watchedAttribs));
        }

        player.connection.sendPacket(new SServerDifficultyPacket(toWorld.getDifficulty(), toWorld.getWorldInfo().isDifficultyLocked()));
        player.connection.sendPacket(new SEntityStatusPacket(player, toWorld.getGameRules().getBoolean(GameRules.REDUCED_DEBUG_INFO) ?
            (byte) 22 : 23));

        if (!event.getKeepsVelocity()) {
            player.motionX = 0;
            player.motionY = 0;
            player.motionZ = 0;
        }

        SpongeImplHooks.handlePostChangeDimensionEvent(player, fromWorld, toWorld);

        ((ServerPlayerEntityBridge) player).bridge$refreshExp();
        return player;
    }

    // Teleporter code is extremely stupid
    private static InvokingTeleporterContext createInvokingTeleporterPhase(final Entity entity, ServerWorld toWorld, ForgeITeleporterBridge teleporter) {
        SpongeImplHooks.registerPortalAgentType(teleporter);

        final MinecraftServer mcServer = SpongeImpl.getServer();
        final org.spongepowered.api.entity.Entity sEntity = (org.spongepowered.api.entity.Entity) entity;
        final Transform fromTransform = sEntity.getTransform();
        final ServerWorld fromWorld = ((ServerWorld) entity.world);

        int toDimensionId = ((ServerWorldBridge) toWorld).bridge$getDimensionId();

        // Entering End Portal in End goes to Overworld in Vanilla
        if (toDimensionId == 1 && fromWorld.dimension instanceof EndDimension) {
            toDimensionId = 0;
        }

        toWorld = mcServer.getWorld(toDimensionId);

        final Map<String, String> portalAgents =
            ((WorldInfoBridge) fromWorld.getWorldInfo()).bridge$getConfigAdapter().getConfig().getWorld().getPortalAgents();
        final String worldName;

        // Check if we're to use a different teleporter for this world
        if (teleporter.getClass().getName().equals("net.minecraft.world.Teleporter")) {
            worldName = portalAgents.get("minecraft:default_" + toWorld.dimension.getType().getName().toLowerCase(Locale.ENGLISH));
        } else {
            worldName = portalAgents.get("minecraft:" + teleporter.getClass().getSimpleName());
        }

        if (worldName != null) {
            for (final WorldProperties properties : Sponge.getServer().getAllWorldProperties()) {
                if (properties.getName().equalsIgnoreCase(worldName)) {
                    final Optional<World> spongeWorld = Sponge.getServer().loadWorld(properties);
                    if (spongeWorld.isPresent()) {
                        toWorld = (ServerWorld) spongeWorld.get();
                        teleporter = (ForgeITeleporterBridge) toWorld.getDefaultTeleporter();
                        if (teleporter instanceof TeleporterBridge) {
                            if (!((fromWorld.dimension.isNether() || toWorld.dimension.isNether()))) {
                                ((TeleporterBridge) teleporter).bridge$setNetherPortalType(false);
                            }
                        }
                    }
                }
            }
        }

        fromWorld.profiler.startSection("reposition");
        final Transform<World> toTransform = getPortalExitTransform(entity, fromWorld, toWorld);
        fromWorld.profiler.endSection();

        // Portals create blocks and the PhaseTracker is known to capture blocks..
        final InvokingTeleporterContext context = EntityPhase.State.INVOKING_TELEPORTER.createPhaseContext(PhaseTracker.SERVER)
            .setTargetWorld(toWorld)
            .setTeleporter((PortalAgent) teleporter)
            .setExitTransform(toTransform)
            .buildAndSwitch();

        if (!(fromWorld.dimension instanceof EndDimension)) {

            // Only place entity in portal if one of the following are true :
            // 1. The teleporter is custom. (not vanilla)
            // 2. The last known portal vec is known. (Usually set after block collision)
            // 3. The entity is traveling to end from a non-end world.
            // Note: We must always use placeInPortal to support mods.
            if (!teleporter.bridge$isVanilla() || entity.getLastPortalVec() != null || toWorld.dimension instanceof EndDimension) {
                // In Forge, the entity dimension is already set by this point.
                // To maintain compatibility with Forge mods, we temporarily
                // set the entity's dimension to the current target dimension
                // when calling Teleporter#bridge$placeEntity.

                Vector3d position = toTransform.getPosition();
                entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) toTransform.getYaw(),
                    (float) toTransform.getPitch());

                fromWorld.profiler.startSection("placing");
                if (!teleporter.bridge$isVanilla() || toWorld.dimension instanceof EndDimension) {
                    // Have to assume mod teleporters or end -> overworld always port. We set this state for nether ports in
                    // TeleporterMixin#bridge$placeEntity
                    context.setDidPort(true);
                }

                teleporter.bridge$placeEntity(toWorld, entity, (float) fromTransform.getRotation().getY());

                fromWorld.profiler.endSection();

                context.setTargetWorld(toWorld);
                context.setExitTransform(sEntity.getTransform());

                // Roll back Entity transform
                position = fromTransform.getPosition();
                entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) fromTransform.getYaw(),
                    (float) fromTransform.getPitch());
            }
        } else {
            context.setDidPort(true);
        }

        return context;
    }

    private static Transform getPortalExitTransform(final Entity entity, final ServerWorld fromWorld, final ServerWorld toWorld) {
        final Dimension fromWorldProvider = fromWorld.dimension;
        final Dimension toWorldProvider = toWorld.dimension;

        double x;
        final double y;
        double z;

        final Transform transform;

        if (toWorldProvider instanceof EndDimension) {
            final BlockPos coordinate = toWorld.getSpawnCoordinate();
            x = coordinate.getX();
            y = coordinate.getY();
            z = coordinate.getZ();
        } else if (fromWorldProvider instanceof EndDimension && toWorldProvider instanceof OverworldDimension) {
            final BlockPos coordinate = toWorld.getSpawnPoint();
            x = coordinate.getX();
            y = coordinate.getY();
            z = coordinate.getZ();
        }
        else {

            final double moveFactor = ((DimensionBridge) fromWorldProvider).bridge$getMovementFactor() / ((DimensionBridge) toWorldProvider).bridge$getMovementFactor();

            x = MathHelper.clamp(entity.posX * moveFactor, toWorld.getWorldBorder().minX() + 16.0D, toWorld.getWorldBorder().maxX() - 16.0D);
            y = entity.posY;
            z = MathHelper.clamp(entity.posZ * moveFactor, toWorld.getWorldBorder().minZ() + 16.0D, toWorld.getWorldBorder().maxZ() - 16.0D);
            entity.rotationYaw = 90f;
            entity.rotationPitch = 0f;

            x = (double) MathHelper.clamp((int) x, -29999872, 29999872);
            z = (double) MathHelper.clamp((int) z, -29999872, 29999872);
        }

        transform = new Transform<>((World) toWorld, new Vector3d(x, y, z), new Vector3d(entity.rotationPitch, entity.rotationYaw, 0f));

        return transform;
    }

    public static boolean isEntityDead(final net.minecraft.entity.Entity entity) {
        if (entity instanceof LivingEntity) {
            final LivingEntity base = (LivingEntity) entity;
            return base.getHealth() <= 0 || base.deathTime > 0 || ((LivingEntityAccessor) entity).accessor$getDead();
        }
        return entity.removed;
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(final Entity entityIn, final Location<World> location) {
        final Transform<World> fromTransform = ((org.spongepowered.api.entity.Entity) entityIn).getTransform();
        final Transform<World> toTransform = fromTransform.setLocation(location).setRotation(new Vector3d(entityIn.rotationPitch, entityIn.rotationYaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(final Entity entityIn, final double posX, final double posY, final double posZ, final float yaw, final float pitch) {
        final Transform<World> fromTransform = ((org.spongepowered.api.entity.Entity) entityIn).getTransform();
        final Transform<World> toTransform = fromTransform.setPosition(new Vector3d(posX, posY, posZ)).setRotation(new Vector3d(pitch, yaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(
        final Entity entityIn, final Transform<World> fromTransform, final Transform<World> toTransform) {

        // Use origin world to get correct cause
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entityIn);

            final MoveEntityEvent.Teleport event = SpongeEventFactory.createMoveEntityEventTeleport(Sponge.getCauseStackManager().getCurrentCause(),
                fromTransform, toTransform, (org.spongepowered.api.entity.Entity) entityIn, false);
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static boolean processEntitySpawnsFromEvent(final SpawnEntityEvent event, final Supplier<Optional<User>> entityCreatorSupplier) {
        boolean spawnedAny = false;
        for (final org.spongepowered.api.entity.Entity entity : event.getEntities()) {
            // Here is where we need to handle the custom items potentially having custom entities
            spawnedAny = processEntitySpawn(entity, entityCreatorSupplier);
        }
        return spawnedAny;
    }

    public static boolean processEntitySpawnsFromEvent(final PhaseContext<?> context, final SpawnEntityEvent destruct) {
        return processEntitySpawnsFromEvent(destruct, ENTITY_CREATOR_FUNCTION.apply(context));
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean processEntitySpawn(final org.spongepowered.api.entity.Entity entity, final Supplier<Optional<User>> supplier) {
        final Entity minecraftEntity = (Entity) entity;
        if (minecraftEntity instanceof ItemEntity) {
            final ItemStack item = ((ItemEntity) minecraftEntity).getItem();
            if (!item.isEmpty()) {
                final Optional<Entity> customEntityItem = Optional.ofNullable(SpongeImplHooks.getCustomEntityIfItem(minecraftEntity));
                if (customEntityItem.isPresent()) {
                    // Bypass spawning the entity item, since it is established that the custom entity is spawned.
                    final Entity entityToSpawn = customEntityItem.get();
                    supplier.get()
                        .ifPresent(spawned -> {
                            if (entityToSpawn instanceof OwnershipTrackedBridge) {
                                ((OwnershipTrackedBridge) entityToSpawn).tracked$setOwnerReference(spawned);
                            }
                        });
                    if (entityToSpawn.removed) {
                        entityToSpawn.removed = false;
                    }
                    // Since forge already has a new event thrown for the entity, we don't need to throw
                    // the event anymore as sponge plugins getting the event after forge mods will
                    // have the modified entity list for entities, so no need to re-capture the entities.
                    ((ServerWorldBridge) entityToSpawn.world).bridge$forceSpawnEntity(entityToSpawn);
                    return true;
                }
            }
        }

        supplier.get()
            .ifPresent(spawned -> {
                if (entity instanceof OwnershipTrackedBridge) {
                    ((OwnershipTrackedBridge) entity).tracked$setOwnerReference(spawned);
                }
            });
        // Allowed to call force spawn directly since we've applied creator and custom item logic already
        ((ServerWorldBridge) entity.getWorld()).bridge$forceSpawnEntity((Entity) entity);
        return true;
    }

    static final class EntityTrace {

        Entity entity;
        Vec3d location;
        double distance;

        EntityTrace(final double entityDistance) {
            this.distance = entityDistance;
        }

        RayTraceResult asRayTraceResult() {
            return new RayTraceResult(this.entity, this.location);
        }
    }

    public static RayTraceResult rayTraceFromEntity(final Entity source, final double traceDistance, final float partialTicks, final boolean includeEntities) {
        final RayTraceResult blockRay = EntityUtil.rayTraceFromEntity(source, traceDistance, partialTicks);

        if (!includeEntities) {
            return blockRay;
        }

        final Vec3d traceStart = EntityUtil.getPositionEyes(source, partialTicks);
        final double blockDistance = (blockRay != null) ? blockRay.hitResult.distanceTo(traceStart) : traceDistance;
        final EntityTrace trace = new EntityTrace(blockDistance);

        final Vec3d lookDir = source.getLook(partialTicks).scale(traceDistance);
        final Vec3d traceEnd = traceStart.add(lookDir);

        final AxisAlignedBB boundingBox = source.getBoundingBox();
        final AxisAlignedBB traceBox = boundingBox.expand(lookDir.x, lookDir.y, lookDir.z);
        final List<Entity> entities = source.world.getEntitiesInAABBexcluding(source, traceBox.grow(1.0F, 1.0F, 1.0F), EntityUtil.TRACEABLE);
        for (final Entity entity : entities) {
            final AxisAlignedBB entityBB = entity.getBoundingBox().grow(entity.getCollisionBorderSize());
            final RayTraceResult entityRay1 = entityBB.calculateIntercept(traceStart, traceEnd);

            if (entityBB.contains(traceStart)) {
                if (trace.distance >= 0.0D) {
                    trace.entity = entity;
                    trace.location = entityRay1 == null ? traceStart : entityRay1.hitResult;
                    trace.distance = 0.0D;
                }
                continue;
            }

            if (entityRay1 == null) {
                continue;
            }

            final double distanceToEntity = traceStart.distanceTo(entityRay1.hitResult);

            if (distanceToEntity < trace.distance || trace.distance == 0.0D) {
                if (entity.getLowestRidingEntity() == source.getLowestRidingEntity()) {
                    if (trace.distance == 0.0D) {
                        trace.entity = entity;
                        trace.location = entityRay1.hitResult;
                    }
                } else {
                    trace.entity = entity;
                    trace.location = entityRay1.hitResult;
                    trace.distance = distanceToEntity;
                }
            }
        }

        if (trace.entity != null && (trace.distance < blockDistance || blockRay == null)) {
            return trace.asRayTraceResult();
        }

        return blockRay;
    }

    @Nullable
    public static RayTraceResult rayTraceFromEntity(final Entity source, final double traceDistance, final float partialTicks) {
        final Vec3d traceStart = EntityUtil.getPositionEyes(source, partialTicks);
        final Vec3d lookDir = source.getLook(partialTicks).scale(traceDistance);
        final Vec3d traceEnd = traceStart.add(lookDir);
        return source.world.rayTraceBlocks(traceStart, traceEnd, false, false, true);
    }

    private static Vec3d getPositionEyes(final Entity entity, final float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        }

        final double interpX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        final double interpY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + entity.getEyeHeight();
        final double interpZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        return new Vec3d(interpX, interpY, interpZ);
    }

    /**
     * A simple redirected static util method for {@link Entity#entityDropItem(ItemStack, float)}.
     * What this does is ensures that any possibly required wrapping of captured drops is performed.
     * Likewise, it ensures that the phase state is set up appropriately.
     *
     * @param entity The entity dropping the item
     * @param itemStack The itemstack to spawn
     * @param offsetY The offset y coordinate
     * @return The item entity
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public static ItemEntity entityOnDropItem(final Entity entity, final ItemStack itemStack, final float offsetY, final double xPos, final double zPos) {
        if (itemStack.isEmpty()) {
            // Sanity check, just like vanilla
            return null;
        }
        // Now the real fun begins.
        final ItemStack item;
        final double posX = xPos;
        final double posY = entity.posY + offsetY;
        final double posZ = zPos;

        // FIRST we want to throw the DropItemEvent.PRE
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        // Gather phase states to determine whether we're merging or capturing later
        final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = phaseContext.state;

        // We want to frame ourselves here, because of the two events we have to throw, first for the drop item event, then the constructentityevent.
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // Perform the event throws first, if they return false, return null
            item = SpongeCommonEventFactory.throwDropItemAndConstructEvent(entity, posX, posY, posZ, snapshot, original, frame);

            if (item == null || item.isEmpty()) {
                return null;
            }


            // This is where we could perform item pre merging, and cancel before we create a new entity.
            // For now, we aren't performing pre merging.

            final ItemEntity entityitem = new ItemEntity(entity.world, posX, posY, posZ, item);
            entityitem.setDefaultPickupDelay();

            // FIFTH - Capture the entity maybe?
            if (((IPhaseState) currentState).spawnItemOrCapture(phaseContext, entity, entityitem)) {
                return entityitem;
            }
            // FINALLY - Spawn the entity in the world if all else didn't fail
            EntityUtil.processEntitySpawn((org.spongepowered.api.entity.Entity) entityitem, Optional::empty);
            return entityitem;
        }
    }


    /**
     * This is used to create the "dropping" motion for items caused by players. This
     * specifically was being used (and should be the correct math) to drop from the
     * player, when we do item stack captures preventing entity items being created.
     *
     * @param dropAround True if it's being "dropped around the player like dying"
     * @param player The player to drop around from
     * @param random The random instance
     * @return The motion vector
     */
    @SuppressWarnings("unused")
    private static Vector3d createDropMotion(final boolean dropAround, final PlayerEntity player, final Random random) {
        double x;
        double y;
        double z;
        if (dropAround) {
            final float f = random.nextFloat() * 0.5F;
            final float f1 = random.nextFloat() * ((float) Math.PI * 2F);
            x = -MathHelper.sin(f1) * f;
            z = MathHelper.cos(f1) * f;
            y = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            x = -MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
            z = MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
            y = - MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F;
            final float f3 = random.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * random.nextFloat();
            x += Math.cos(f3) * f2;
            y += (random.nextFloat() - random.nextFloat()) * 0.1F;
            z += Math.sin(f3) * f2;
        }
        return new Vector3d(x, y, z);
    }


    public static boolean isUntargetable(Entity from, Entity target) {
        if (((VanishableBridge) target).bridge$isVanished() && ((VanishableBridge) target).bridge$isUntargetable()) {
            return true;
        }
        // Temporary fix for https://bugs.mojang.com/browse/MC-149563
        if (from.world != target.world) {
            return true;
        }
        return false;
    }

}

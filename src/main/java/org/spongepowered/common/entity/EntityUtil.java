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

import static net.minecraft.util.EntitySelectors.NOT_SPECTATING;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.explosive.FusedExplosive;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldProviderBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.InvokingTeleporterContext;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;

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

    private static final Predicate<Entity> TRACEABLE = Predicates.and(NOT_SPECTATING,
      entity -> entity != null && entity.canBeCollidedWith());

    public static final Function<Humanoid, EntityPlayer> HUMANOID_TO_PLAYER = (humanoid) -> humanoid instanceof EntityPlayer ? (EntityPlayer) humanoid : null;

    @Nullable
    public static Entity transferEntityToWorld(Entity entity, @Nullable MoveEntityEvent.Teleport event,
        @Nullable WorldServer toWorld,  @Nullable ForgeITeleporterBridge teleporter, boolean recreate) {

        if (entity.world.isRemote || entity.isDead) {
            return null;
        }

        org.spongepowered.api.entity.Entity sEntity = (org.spongepowered.api.entity.Entity) entity;
        final EntityBridge mEntity = (EntityBridge) entity;
        final Transform<World> fromTransform = sEntity.getTransform();
        final WorldServer fromWorld = (WorldServer) fromTransform.getExtent();

        fromWorld.profiler.startSection("changeDimension");

        boolean loadChunks = true;

        // use the world from event
        final Transform<World> toTransform;

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
                    context.getTeleporter(), sEntity, true, true);

                if (SpongeImpl.postEvent(event)) {
                    // Mods may cancel this event in order to run custom transfer logic
                    // We need to make sure to only rollback if they completely changed the world
                    if (event.getFromTransform().getExtent() != sEntity.getTransform().getExtent()) {
                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        mEntity.bridge$setLocationAndAngles(fromTransform);
                    }

                    return null;
                } else {
                    toTransform = event.getToTransform();
                    toWorld = (WorldServer) toTransform.getExtent();

                    // Handle plugins setting changing to a different world than the teleporter said so. This is considered a cancellation
                    if (context.getExitTransform().getExtent() != toTransform.getExtent()) {
                        event.setCancelled(true);

                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
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
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }
                        context.getCapturedBlockSupplier().restoreOriginals();
                    } else {

                        // Unwind PhaseTracker captured blocks here, the actual position placement of the entity is common code below
                        if (teleporter instanceof TeleporterBridge && !context.getCapturedBlockSupplier().isEmpty() && !TrackingUtil
                            .processBlockCaptures(EntityPhase.State.INVOKING_TELEPORTER, context)) {
                            // Transactions were rolled back, the portal wasn't made. We need to bomb the dimension change and clear portal cache
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
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
            toWorld = (WorldServer) toTransform.getExtent();
        }

        fromWorld.profiler.endStartSection("reloading");
        final Entity toReturn;

        if (recreate) {
            toReturn = EntityList.newEntity(entity.getClass(), toWorld);
            sEntity = (org.spongepowered.api.entity.Entity) toReturn;
            if (toReturn == null) {
                return entity;
            }

            ((EntityAccessor) toReturn).accessor$CopyDataFromOldEntity(entity);
        } else {
            toReturn = entity;
        }

        if (!event.getKeepsVelocity()) {
            toReturn.motionX = 0;
            toReturn.motionY = 0;
            toReturn.motionZ = 0;
        }

        if (loadChunks) {
            final Vector3i toChunkPosition = toTransform.getLocation().getChunkPosition();
            toWorld.getChunkProvider().loadChunk(toChunkPosition.getX(), toChunkPosition.getZ());
        }

        fromWorld.profiler.startSection("moving");
        ((EntityBridge) toReturn).bridge$setLocationAndAngles(toTransform);
        fromWorld.profiler.endSection();

        try (final PhaseContext<?> ignored = EntityPhase.State.CHANGING_DIMENSION.createPhaseContext().setTargetWorld(toWorld).buildAndSwitch()) {
            if (recreate) {
                boolean flag = toReturn.forceSpawn;
                toReturn.forceSpawn = true;
                toWorld.spawnEntity(toReturn);
                toReturn.forceSpawn = flag;
                toWorld.updateEntityWithOptionalForce(toReturn, false);
            } else {
                toWorld.spawnEntity(toReturn);
                toWorld.updateEntityWithOptionalForce(toReturn, false);
            }
        }

        // Fix Vanilla bug where primed minecart TNTs don't keep state through a portal
        if (toReturn instanceof EntityMinecartTNT) {
            if (((FusedExplosive) sEntity).isPrimed()) {
                toReturn.world.setEntityState(toReturn, (byte) 10);
            }
        }

        entity.isDead = true;
        fromWorld.profiler.endSection();
        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
        fromWorld.profiler.endSection();

        return toReturn;
    }

    public static EntityPlayerMP transferPlayerToWorld(EntityPlayerMP player, @Nullable MoveEntityEvent.Teleport event,
        @Nullable WorldServer toWorld,  @Nullable ForgeITeleporterBridge teleporter) {

        if (player.world.isRemote || player.isDead) {
            return player;
        }

        final PlayerList playerList = SpongeImpl.getServer().getPlayerList();
        final Player sPlayer = (Player) player;
        final Transform<World> fromTransform = sPlayer.getTransform();
        final WorldServer fromWorld = (WorldServer) fromTransform.getExtent();

        fromWorld.profiler.startSection("changeDimension");

        // use the world from event
        final Transform<World> toTransform;

        // Assume portal
        if (event == null) {
            if (toWorld == null || teleporter == null) {
                return player;
            }

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                final InvokingTeleporterContext context = createInvokingTeleporterPhase(player, toWorld, teleporter)) {

                if (!context.getDidPort()) {
                    return player;
                }

                frame.pushCause(context.getTeleporter());
                frame.pushCause(player);

                // TODO All of this code assumes that all teleports outside of the API are portals...need to re-think this and somehow correctly
                // TODO determine what it truly is for API 8
                frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.PORTAL);

                event = SpongeEventFactory.createMoveEntityEventTeleportPortal(frame.getCurrentCause(), fromTransform, context.getExitTransform(),
                    context.getTeleporter(), sPlayer, true, true);

                if (SpongeImpl.postEvent(event)) {
                    // Mods may cancel this event in order to run custom transfer logic
                    // We need to make sure to only rollback if they completely changed the world
                    if (event.getFromTransform().getExtent() != sPlayer.getTransform().getExtent()) {
                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        ((EntityBridge) player).bridge$setLocationAndAngles(fromTransform);
                    }

                    return player;
                } else {
                    toTransform = event.getToTransform();
                    toWorld = (WorldServer) toTransform.getExtent();

                    // Handle plugins setting changing to a different world than the teleporter said so. This is considered a cancellation
                    if (context.getExitTransform().getExtent() != toTransform.getExtent()) {
                        event.setCancelled(true);

                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        ((EntityBridge) player).bridge$setLocationAndAngles(toTransform);
                        return player;
                    }

                    // If we don't use the portal agent clear out the portal blocks that
                    if (!((MoveEntityEvent.Teleport.Portal) event).getUsePortalAgent()) {
                        final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                        if (teleporter instanceof TeleporterBridge) {
                            ((TeleporterBridge) teleporter).bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                        }
                        context.getCapturedBlockSupplier().restoreOriginals();
                    } else {

                        // Unwind PhaseTracker captured blocks here, the actual position placement of the entity is common code below
                        if (teleporter instanceof TeleporterBridge && !context.getCapturedBlockSupplier().isEmpty() && !TrackingUtil
                            .processBlockCaptures(EntityPhase.State.INVOKING_TELEPORTER, context)) {
                            // Transactions were rolled back, the portal wasn't made. We need to bomb the dimension change and clear portal cache
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));

                            return player;
                        }
                    }
                }
            }
            // Make sure no one else besides me is stupid enough to pass a cancelled event to this....
        } else if (event.isCancelled()) {
            return player;
        } else {
            toTransform = event.getToTransform();
            toWorld = (WorldServer) toTransform.getExtent();
        }

        final int fromClientDimId = WorldManager.getClientDimensionId(player, fromWorld);
        final int toClientDimId = WorldManager.getClientDimensionId(player, toWorld);

        if (fromClientDimId == toClientDimId) {
            int fakeDimId;
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

            player.connection.sendPacket(new SPacketRespawn(fakeDimId, toWorld.getDifficulty(), toWorld.getWorldType(),
                player.interactionManager.getGameType()));
        }

        // TODO I can easily support switching gamemodes per world here but need to actually have worlds inherit server gamemode
        // TODO if they have none set..
        player.connection.sendPacket(new SPacketRespawn(toClientDimId, toWorld.getDifficulty(), toWorld.getWorldType(),
            player.interactionManager.getGameType()));

        playerList.updatePermissionLevel(player);

        fromWorld.removeEntityDangerously(player);
        player.isDead = false;

        final Vector3d position = toTransform.getPosition();
        player.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) toTransform.getYaw(), (float) toTransform.getPitch());

        try (final PhaseContext<?> ignored = EntityPhase.State.CHANGING_DIMENSION.createPhaseContext().setTargetWorld(toWorld).buildAndSwitch()) {
            toWorld.spawnEntity(player);
            toWorld.updateEntityWithOptionalForce(player, false);
        }

        player.dimension = ((ServerWorldBridge) toWorld).bridge$getDimensionId();
        player.setWorld(toWorld);

        // preparePlayer
        fromWorld.getPlayerChunkMap().removePlayer(player);
        toWorld.getPlayerChunkMap().addPlayer(player);

        final Vector3i toChunkPosition = toTransform.getLocation().getChunkPosition();
        toWorld.getChunkProvider().provideChunk(toChunkPosition.getX(), toChunkPosition.getZ());

        if (event instanceof MoveEntityEvent.Teleport.Portal) {
            CriteriaTriggers.CHANGED_DIMENSION.trigger(player, fromWorld.provider.getDimensionType(), toWorld.provider.getDimensionType());

            if (fromWorld.provider.getDimensionType() == DimensionType.NETHER && toWorld.provider.getDimensionType() == DimensionType.OVERWORLD
                && player.getEnteredNetherPosition() != null) {
                CriteriaTriggers.NETHER_TRAVEL.trigger(player, player.getEnteredNetherPosition());
            }
        }
        //

        player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

        player.interactionManager.setWorld(toWorld);
        playerList.updateTimeAndWeatherForPlayer(player, toWorld);
        playerList.syncPlayerInventory(player);

        for (PotionEffect potioneffect : player.getActivePotionEffects()) {
            player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
        }

        // Fix MC-88179: on non-death SPacketRespawn, also resend attributes
        final AttributeMap attributemap = (AttributeMap) player.getAttributeMap();
        final Collection<IAttributeInstance> watchedAttribs = attributemap.getWatchedAttributes();
        if (!watchedAttribs.isEmpty()) {
            player.connection.sendPacket(new SPacketEntityProperties(player.getEntityId(), watchedAttribs));
        }

        player.connection.sendPacket(new SPacketServerDifficulty(toWorld.getDifficulty(), toWorld.getWorldInfo().isDifficultyLocked()));
        player.connection.sendPacket(new SPacketEntityStatus(player, toWorld.getGameRules().getBoolean(DefaultGameRules.REDUCED_DEBUG_INFO) ?
            (byte) 22 : 23));

        if (!event.getKeepsVelocity()) {
            player.motionX = 0;
            player.motionY = 0;
            player.motionZ = 0;
        }

        SpongeImplHooks.handlePostChangeDimensionEvent(player, fromWorld, toWorld);

        return player;
    }

    // Teleporter code is extremely stupid
    private static InvokingTeleporterContext createInvokingTeleporterPhase(Entity entity, WorldServer toWorld, ForgeITeleporterBridge teleporter) {
        SpongeImplHooks.registerPortalAgentType(teleporter);

        final MinecraftServer mcServer = SpongeImpl.getServer();
        final org.spongepowered.api.entity.Entity sEntity = (org.spongepowered.api.entity.Entity) entity;
        final Transform<World> fromTransform = sEntity.getTransform();
        final WorldServer fromWorld = ((WorldServer) entity.world);

        int toDimensionId = ((ServerWorldBridge) toWorld).bridge$getDimensionId();

        // Entering End Portal in End goes to Overworld in Vanilla
        if (toDimensionId == 1 && fromWorld.provider instanceof WorldProviderEnd) {
            toDimensionId = 0;
        }

        toWorld = mcServer.getWorld(toDimensionId);

        final Map<String, String> portalAgents =
            ((WorldInfoBridge) fromWorld.getWorldInfo()).getConfigAdapter().getConfig().getWorld().getPortalAgents();
        String worldName;

        // Check if we're to use a different teleporter for this world
        if (teleporter.getClass().getName().equals("net.minecraft.world.Teleporter")) {
            worldName = portalAgents.get("minecraft:default_" + toWorld.provider.getDimensionType().getName().toLowerCase(Locale.ENGLISH));
        } else {
            worldName = portalAgents.get("minecraft:" + teleporter.getClass().getSimpleName());
        }

        if (worldName != null) {
            for (WorldProperties properties : Sponge.getServer().getAllWorldProperties()) {
                if (properties.getWorldName().equalsIgnoreCase(worldName)) {
                    Optional<World> spongeWorld = Sponge.getServer().loadWorld(properties);
                    if (spongeWorld.isPresent()) {
                        toWorld = (WorldServer) spongeWorld.get();
                        teleporter = (ForgeITeleporterBridge) toWorld.getDefaultTeleporter();
                        if (teleporter instanceof TeleporterBridge) {
                            if (!((fromWorld.provider.isNether() || toWorld.provider.isNether()))) {
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
        final InvokingTeleporterContext context = EntityPhase.State.INVOKING_TELEPORTER.createPhaseContext()
            .setTargetWorld(toWorld)
            .setTeleporter((PortalAgent) teleporter)
            .setExitTransform(toTransform)
            .buildAndSwitch();

        if (!(fromWorld.provider instanceof WorldProviderEnd)) {

            // Only place entity in portal if one of the following are true :
            // 1. The teleporter is custom. (not vanilla)
            // 2. The last known portal vec is known. (Usually set after block collision)
            // 3. The entity is traveling to end from a non-end world.
            // Note: We must always use placeInPortal to support mods.
            if (!teleporter.bridge$isVanilla() || entity.getLastPortalVec() != null || toWorld.provider instanceof WorldProviderEnd) {
                // In Forge, the entity dimension is already set by this point.
                // To maintain compatibility with Forge mods, we temporarily
                // set the entity's dimension to the current target dimension
                // when calling Teleporter#bridge$placeEntity.

                Vector3d position = toTransform.getPosition();
                entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) toTransform.getYaw(),
                    (float) toTransform.getPitch());

                fromWorld.profiler.startSection("placing");
                if (!teleporter.bridge$isVanilla() || toWorld.provider instanceof WorldProviderEnd) {
                    // Have to assume mod teleporters or end -> overworld always port. We set this state for nether ports in
                    // TeleporterMixin#bridge$placeEntity
                    context.setDidPort(true);
                }

                teleporter.bridge$placeEntity(toWorld, entity, (float) fromTransform.getRotation().getY());

                fromWorld.profiler.endSection();

                context.setExitTransform(sEntity.getTransform().setExtent((World) toWorld));

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

    private static Transform<World> getPortalExitTransform(Entity entity, WorldServer fromWorld, WorldServer toWorld) {
        final WorldProvider fromWorldProvider = fromWorld.provider;
        final WorldProvider toWorldProvider = toWorld.provider;

        double x;
        double y;
        double z;

        final Transform<World> transform;

        if (toWorldProvider instanceof WorldProviderEnd) {
            final BlockPos coordinate = toWorld.getSpawnCoordinate();
            x = coordinate.getX();
            y = coordinate.getY();
            z = coordinate.getZ();
        } else if (fromWorldProvider instanceof WorldProviderEnd && toWorldProvider instanceof WorldProviderSurface) {
            final BlockPos coordinate = toWorld.getSpawnPoint();
            x = coordinate.getX();
            y = coordinate.getY();
            z = coordinate.getZ();
        }
        else {

            final double moveFactor = ((WorldProviderBridge) fromWorldProvider).bridge$getMovementFactor() / ((WorldProviderBridge) toWorldProvider).bridge$getMovementFactor();

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

    public static boolean isEntityDead(net.minecraft.entity.Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase base = (EntityLivingBase) entity;
            return base.getHealth() <= 0 || base.deathTime > 0 || base.dead;
        }
        return entity.isDead;
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, Location<World> location) {
        Transform<World> fromTransform = ((org.spongepowered.api.entity.Entity) entityIn).getTransform();
        Transform<World> toTransform = fromTransform.setLocation(location).setRotation(new Vector3d(entityIn.rotationPitch, entityIn.rotationYaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, double posX, double posY, double posZ, float yaw, float pitch) {
        Transform<World> fromTransform = ((org.spongepowered.api.entity.Entity) entityIn).getTransform();
        Transform<World> toTransform = fromTransform.setPosition(new Vector3d(posX, posY, posZ)).setRotation(new Vector3d(pitch, yaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, Transform<World> fromTransform, Transform<World> toTransform) {

        // Use origin world to get correct cause
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entityIn);

            MoveEntityEvent.Teleport event = SpongeEventFactory.createMoveEntityEventTeleport(Sponge.getCauseStackManager().getCurrentCause(),
                fromTransform, toTransform, (org.spongepowered.api.entity.Entity) entityIn, false);
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    public static int getHorseInternalVariant(SpongeHorseColor color, SpongeHorseStyle style) {
        return color.getBitMask() | style.getBitMask();
    }

    public static boolean processEntitySpawnsFromEvent(SpawnEntityEvent event, Supplier<Optional<User>> entityCreatorSupplier) {
        boolean spawnedAny = false;
        for (org.spongepowered.api.entity.Entity entity : event.getEntities()) {
            // Here is where we need to handle the custom items potentially having custom entities
            spawnedAny = processEntitySpawn(entity, entityCreatorSupplier);
        }
        return spawnedAny;
    }

    public static boolean processEntitySpawnsFromEvent(PhaseContext<?> context, SpawnEntityEvent destruct) {
        return processEntitySpawnsFromEvent(destruct, ENTITY_CREATOR_FUNCTION.apply(context));
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean processEntitySpawn(org.spongepowered.api.entity.Entity entity, Supplier<Optional<User>> supplier) {
        final Entity minecraftEntity = (Entity) entity;
        if (minecraftEntity instanceof EntityItem) {
            final ItemStack item = ((EntityItem) minecraftEntity).getItem();
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
                    if (entityToSpawn.isDead) {
                        entityToSpawn.isDead = false;
                    }
                    // Since forge already has a new event thrown for the entity, we don't need to throw
                    // the event anymore as sponge plugins getting the event after forge mods will
                    // have the modified entity list for entities, so no need to re-capture the entities.
                    ((ServerWorldBridge) entityToSpawn.world).bridge$forceSpawnEntity(entity);
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
        ((ServerWorldBridge) entity.getWorld()).bridge$forceSpawnEntity(entity);
        return true;
    }

    static final class EntityTrace {

        Entity entity;
        Vec3d location;
        double distance;

        EntityTrace(double entityDistance) {
            this.distance = entityDistance;
        }

        RayTraceResult asRayTraceResult() {
            return new RayTraceResult(this.entity, this.location);
        }
    }

    public static RayTraceResult rayTraceFromEntity(Entity source, double traceDistance, float partialTicks, boolean includeEntities) {
        RayTraceResult blockRay = EntityUtil.rayTraceFromEntity(source, traceDistance, partialTicks);

        if (!includeEntities) {
            return blockRay;
        }

        Vec3d traceStart = EntityUtil.getPositionEyes(source, partialTicks);
        double blockDistance = (blockRay != null) ? blockRay.hitVec.distanceTo(traceStart) : traceDistance;
        EntityUtil.EntityTrace entityRay = EntityUtil.rayTraceEntities(source, traceDistance, partialTicks, blockDistance, traceStart);

        if (entityRay.entity != null && (entityRay.distance < blockDistance || blockRay == null)) {
            return entityRay.asRayTraceResult();
        }

        return blockRay;
    }

    private static EntityUtil.EntityTrace rayTraceEntities(Entity source, double traceDistance, float partialTicks, double blockDistance, Vec3d traceStart) {
        EntityUtil.EntityTrace trace = new EntityUtil.EntityTrace(blockDistance);

        Vec3d lookDir = source.getLook(partialTicks).scale(traceDistance);
        Vec3d traceEnd = traceStart.add(lookDir);

        for (final Entity entity : EntityUtil.getTraceEntities(source, traceDistance, lookDir, EntityUtil.TRACEABLE)) {
            AxisAlignedBB entityBB = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            RayTraceResult entityRay = entityBB.calculateIntercept(traceStart, traceEnd);

            if (entityBB.contains(traceStart)) {
                if (trace.distance >= 0.0D) {
                    trace.entity = entity;
                    trace.location = entityRay == null ? traceStart : entityRay.hitVec;
                    trace.distance = 0.0D;
                }
                continue;
            }

            if (entityRay == null) {
                continue;
            }

            double distanceToEntity = traceStart.distanceTo(entityRay.hitVec);

            if (distanceToEntity < trace.distance || trace.distance == 0.0D) {
                if (entity.getLowestRidingEntity() == source.getLowestRidingEntity()) {
                    if (trace.distance == 0.0D) {
                        trace.entity = entity;
                        trace.location = entityRay.hitVec;
                    }
                } else {
                    trace.entity = entity;
                    trace.location = entityRay.hitVec;
                    trace.distance = distanceToEntity;
                }
            }
        }

        return trace;
    }

    private static List<Entity> getTraceEntities(Entity source, double traceDistance, Vec3d dir, Predicate<Entity> filter) {
        AxisAlignedBB boundingBox = source.getEntityBoundingBox();
        AxisAlignedBB traceBox = boundingBox.expand(dir.x, dir.y, dir.z);
        List<Entity> entities = source.world.getEntitiesInAABBexcluding(source, traceBox.grow(1.0F, 1.0F, 1.0F), filter);
        return entities;
    }

    @Nullable
    public static RayTraceResult rayTraceFromEntity(Entity source, double traceDistance, float partialTicks) {
        Vec3d traceStart = EntityUtil.getPositionEyes(source, partialTicks);
        Vec3d lookDir = source.getLook(partialTicks).scale(traceDistance);
        Vec3d traceEnd = traceStart.add(lookDir);
        return source.world.rayTraceBlocks(traceStart, traceEnd, false, false, true);
    }

    private static Vec3d getPositionEyes(Entity entity, float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        }

        double interpX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double interpY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks + entity.getEyeHeight();
        double interpZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        return new Vec3d(interpX, interpY, interpZ);
    }

    public static boolean refreshPainting(EntityPainting painting, EntityPainting.EnumArt art) {
        EntityPainting.EnumArt oldArt = painting.art;
        painting.art = art;
        painting.updateFacingWithBoundingBox(painting.facingDirection);
        if (!painting.onValidSurface()) {
            painting.art = oldArt;
            painting.updateFacingWithBoundingBox(painting.facingDirection);
            return false;
        }

        final EntityTracker paintingTracker = ((WorldServer) painting.world).getEntityTracker();
        EntityTrackerEntry paintingEntry = paintingTracker.trackedEntityHashTable.lookup(painting.getEntityId());
        List<EntityPlayerMP> playerMPs = new ArrayList<>();
        for (EntityPlayerMP player : paintingEntry.trackingPlayers) {
            SPacketDestroyEntities packet = new SPacketDestroyEntities(painting.getEntityId());
            player.connection.sendPacket(packet);
            playerMPs.add(player);
        }
        for (EntityPlayerMP playerMP : playerMPs) {
            SpongeImpl.getGame().getScheduler().createTaskBuilder()
                    .delayTicks(SpongeImpl.getGlobalConfigAdapter().getConfig().getEntity().getPaintingRespawnDelaly())
                    .execute(() -> {
                        final SPacketSpawnPainting packet = new SPacketSpawnPainting(painting);
                        playerMP.connection.sendPacket(packet);
                    })
                    .submit(SpongeImpl.getPlugin());
        }
        return true;
    }

    public static List<EntityHanging> findHangingEntities(WorldServer worldIn, BlockPos pos) {
        return worldIn.getEntitiesWithinAABB(EntityHanging.class, new AxisAlignedBB(pos, pos).grow(1.1D, 1.1D, 1.1D),
                entityIn -> {
                    if (entityIn == null) {
                        return false;
                    }

                    BlockPos entityPos = entityIn.getPosition();
                    // Hanging Neighbor Entity
                    if (entityPos.equals(pos.add(0, 1, 0))) {
                        return true;
                    }

                    // Check around source block
                    EnumFacing entityFacing = entityIn.getHorizontalFacing();

                    if (entityFacing == EnumFacing.NORTH) {
                        return entityPos.equals(pos.add(Constants.Entity.HANGING_OFFSET_NORTH));
                    } else if (entityFacing == EnumFacing.SOUTH) {
                        return entityIn.getPosition().equals(pos.add(Constants.Entity.HANGING_OFFSET_SOUTH));
                    } else if (entityFacing == EnumFacing.WEST) {
                        return entityIn.getPosition().equals(pos.add(Constants.Entity.HANGING_OFFSET_WEST));
                    } else if (entityFacing == EnumFacing.EAST) {
                        return entityIn.getPosition().equals(pos.add(Constants.Entity.HANGING_OFFSET_EAST));
                    }
                    return false;
                });
    }

    // A temporary variable to transfer the 'isBedSpawn' variable between
    // getPlayerRespawnLocation and recreatePlayerEntity
    public static boolean tempIsBedSpawn = false;

    // Internal to MixinPlayerList. has side effects
    public static Location<World> getPlayerRespawnLocation(EntityPlayerMP playerIn, @Nullable WorldServer targetWorld) {
        final Location<World> location = ((World) playerIn.world).getSpawnLocation();
        tempIsBedSpawn = false;
        if (targetWorld == null) { // Target world doesn't exist? Use global
            return location;
        }

        final Dimension toDimension = (Dimension) targetWorld.provider;
        int toDimensionId = ((ServerWorldBridge) targetWorld).bridge$getDimensionId();
        // Cannot respawn in requested world, use the fallback dimension for
        // that world. (Usually overworld unless a mod says otherwise).
        if (!toDimension.allowsPlayerRespawns()) {
            toDimensionId = SpongeImplHooks.getRespawnDimension((WorldProvider) toDimension, playerIn);
            targetWorld = targetWorld.getMinecraftServer().getWorld(toDimensionId);
        }

        Vector3d targetSpawnVec = VecHelper.toVector3d(targetWorld.getSpawnPoint());
        BlockPos bedPos = SpongeImplHooks.getBedLocation(playerIn, toDimensionId);
        if (bedPos != null) { // Player has a bed
            boolean forceBedSpawn = SpongeImplHooks.isSpawnForced(playerIn, toDimensionId);
            BlockPos bedSpawnLoc = EntityPlayer.getBedSpawnLocation(targetWorld, bedPos, forceBedSpawn);
            if (bedSpawnLoc != null) { // The bed exists and is not obstructed
                tempIsBedSpawn = true;
                targetSpawnVec = new Vector3d(bedSpawnLoc.getX() + 0.5D, bedSpawnLoc.getY() + 0.1D, bedSpawnLoc.getZ() + 0.5D);
            } else { // Bed invalid
                playerIn.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
            }
        }
        return new Location<>((World) targetWorld, targetSpawnVec);
    }

    public static Living fromNativeToLiving(Entity entity) {
        if (!(entity instanceof Living)) {
            throw new IllegalArgumentException("Entity is incompatible with SpongeAPI Living interface: " + entity);
        }
        return (Living) entity;
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
    @Nullable
    public static EntityItem entityOnDropItem(Entity entity, ItemStack itemStack, float offsetY) {
        return entityOnDropItem(entity, itemStack, offsetY, entity.posX, entity.posZ);
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
    public static EntityItem entityOnDropItem(Entity entity, ItemStack itemStack, float offsetY, double xPos, double zPos) {
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
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // Perform the event throws first, if they return false, return null
            item = throwDropItemAndConstructEvent(entity, posX, posY, posZ, snapshot, original, frame);

            if (item == null || item.isEmpty()) {
                return null;
            }


            // This is where we could perform item pre merging, and cancel before we create a new entity.
            // For now, we aren't performing pre merging.

            final EntityItem entityitem = new EntityItem(entity.world, posX, posY, posZ, item);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public static EntityItem playerDropItem(PlayerEntityBridge mixinPlayer, ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        mixinPlayer.shouldRestoreInventory(false);
        final EntityPlayer player = (EntityPlayer) mixinPlayer;

        final double posX = player.posX;
        final double posY = player.posY - 0.3 + player.getEyeHeight();
        final double posZ = player.posZ;
        // Now the real fun begins.
        final ItemStack item;
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(droppedItem);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getCurrentContext();
        @SuppressWarnings("RawTypeCanBeGeneric") final IPhaseState currentState = phaseContext.state;

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

            item = throwDropItemAndConstructEvent((EntityPlayer) mixinPlayer, posX, posY, posZ, snapshot, original, frame);

            if (item == null || item.isEmpty()) {
                return null;
            }


            // Here is where we would potentially perform item pre-merging (merge the item stacks with previously captured item stacks
            // and only if those stacks can be stacked (count increased). Otherwise, we'll just continue to throw the entity item.
            // For now, due to refactoring a majority of all of this code, pre-merging is disabled entirely.

            EntityItem entityitem = new EntityItem(player.world, posX, posY, posZ, droppedItem);
            entityitem.setPickupDelay(40);

            if (traceItem) {
                entityitem.setThrower(player.getName());
            }

            final Random random = player.getRNG();
            if (dropAround) {
                float f = random.nextFloat() * 0.5F;
                float f1 = random.nextFloat() * ((float) Math.PI * 2F);
                entityitem.motionX = -MathHelper.sin(f1) * f;
                entityitem.motionZ = MathHelper.cos(f1) * f;
                entityitem.motionY = 0.20000000298023224D;
            } else {
                float f2 = 0.3F;
                entityitem.motionX = -MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
                entityitem.motionZ = MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
                entityitem.motionY = - MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F;
                float f3 = random.nextFloat() * ((float) Math.PI * 2F);
                f2 = 0.02F * random.nextFloat();
                entityitem.motionX += Math.cos(f3) * f2;
                entityitem.motionY += (random.nextFloat() - random.nextFloat()) * 0.1F;
                entityitem.motionZ += Math.sin(f3) * f2;
            }
            // FIFTH - Capture the entity maybe?
            if (currentState.spawnItemOrCapture(phaseContext, (EntityPlayer) mixinPlayer, entityitem)) {
                return entityitem;
            }
            // TODO - Investigate whether player drops are adding to the stat list in captures.
            ItemStack itemstack = dropItemAndGetStack(player, entityitem);

            if (traceItem) {
                if (!itemstack.isEmpty()) {
                    player.addStat(StatList.getDroppedObjectStats(itemstack.getItem()), droppedItem.getCount());
                }

                player.addStat(StatList.DROP);
            }

            return entityitem;
        }
    }

    /**
     * @author gabizou - April 19th, 2018
     * Creates two events here:
     * - {@link DropItemEvent}
     * - {@link ConstructEntityEvent}
     *
     * This is to reduce the code size from normal entity drops and player drops.
     * While player drops usually require performing position and motion modifications,
     * we return the item stack if it is to be thrown (this allows the event to have a
     * say in what item is dropped).
     *
     * @param entity The entity throwing the item
     * @param posX The position x for the item stack to spawn
     * @param posY The position y for the item stack to spawn
     * @param posZ The position z for the item stack to spawn
     * @param snapshot The item snapshot of the item to drop
     * @param original The original list to be used
     * @param frame
     * @return The item if it is to be spawned, null if to be ignored
     */
    @Nullable
    public static ItemStack throwDropItemAndConstructEvent(Entity entity, double posX, double posY,
        double posZ, ItemStackSnapshot snapshot, List<ItemStackSnapshot> original, CauseStackManager.StackFrame frame) {
        final PlayerEntityBridge mixinPlayer;
        if (entity instanceof PlayerEntityBridge) {
            mixinPlayer = (PlayerEntityBridge) entity;
        } else {
            mixinPlayer = null;
        }
        ItemStack item;

        frame.pushCause(entity);

        // FIRST we want to throw the DropItemEvent.PRE
        final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(frame.getCurrentCause(),
            ImmutableList.of(snapshot), original);
        SpongeImpl.postEvent(dropEvent);
        if (dropEvent.isCancelled()) {
            if (mixinPlayer != null) {
                mixinPlayer.shouldRestoreInventory(true);
            }
            return null;
        }
        if (dropEvent.getDroppedItems().isEmpty()) {
            return null;
        }

        // SECOND throw the ConstructEntityEvent
        Transform<World> suggested = new Transform<>((World) entity.world, new Vector3d(posX, posY, posZ));
        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), EntityTypes.ITEM, suggested);
        frame.removeContext(EventContextKeys.SPAWN_TYPE);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            // Make sure the player is restoring inventories
            if (mixinPlayer != null) {
                mixinPlayer.shouldRestoreInventory(true);
            }
            return null;
        }

        item = event.isCancelled() ? null : ItemStackUtil.fromSnapshotToNative(dropEvent.getDroppedItems().get(0));
        if (item == null) {
            // Make sure the player is restoring inventories
            if (mixinPlayer != null) {
                mixinPlayer.shouldRestoreInventory(true);
            }
            return null;
        }
        return item;
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
    private static Vector3d createDropMotion(boolean dropAround, EntityPlayer player, Random random) {
        double x;
        double y;
        double z;
        if (dropAround) {
            float f = random.nextFloat() * 0.5F;
            float f1 = random.nextFloat() * ((float) Math.PI * 2F);
            x = -MathHelper.sin(f1) * f;
            z = MathHelper.cos(f1) * f;
            y = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            x = -MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
            z = MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
            y = - MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F;
            float f3 = random.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * random.nextFloat();
            x += Math.cos(f3) * f2;
            y += (random.nextFloat() - random.nextFloat()) * 0.1F;
            z += Math.sin(f3) * f2;
        }
        return new Vector3d(x, y, z);
    }

    private static ItemStack dropItemAndGetStack(EntityPlayer player, EntityItem item) {
        final ItemStack stack = item.getItem();
        player.world.spawnEntity(item);
        return stack;
    }

    @SuppressWarnings("unchecked")
    public static Optional<EntityType> fromNameToType(String name) {
        // EntityList includes all forge mods with *unedited* entity names
        Class<?> clazz = SpongeImplHooks.getEntityClass(new ResourceLocation(name));
        if(clazz == null) {
            return Optional.empty();
        }

        return Optional.of(EntityTypeRegistryModule.getInstance().getForClass((Class<? extends Entity>) clazz));
    }

    @SuppressWarnings("unchecked")
    public static Optional<EntityType> fromLocationToType(ResourceLocation location) {
        Class<?> clazz = SpongeImplHooks.getEntityClass(location);
        if (clazz == null) {
            return Optional.empty();
        }
        return Optional.of(EntityTypeRegistryModule.getInstance().getForClass((Class<? extends Entity>) clazz));
    }

    // I'm lazy, but this is better than using the convenience method
    public static EntityArchetype archetype(EntityType type) {
        return new SpongeEntityArchetypeBuilder().type(type).build();
    }
}

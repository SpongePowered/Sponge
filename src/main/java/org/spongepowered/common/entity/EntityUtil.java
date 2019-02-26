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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.EndDimension;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.teleport.PortalAgent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.TeleportingContext;
import org.spongepowered.common.interfaces.IMixinPlayerList;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.item.IMixinItem;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.interfaces.world.IMixinITeleporter;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.registry.type.world.dimension.GlobalDimensionType;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import static net.minecraft.util.EntitySelectors.NOT_SPECTATING;

public final class EntityUtil {

    private static final BlockPos HANGING_OFFSET_EAST = new BlockPos(1, 1, 0);
    private static final BlockPos HANGING_OFFSET_WEST = new BlockPos(-1, 1, 0);
    private static final BlockPos HANGING_OFFSET_NORTH = new BlockPos(0, 1, -1);
    private static final BlockPos HANGING_OFFSET_SOUTH = new BlockPos(0, 1, 1);

    public static final Function<PhaseContext<?>, Supplier<Optional<UUID>>> ENTITY_CREATOR_FUNCTION = (context) ->
        () -> Stream.<Supplier<Optional<User>>>builder()
            .add(() -> context.getSource(User.class))
            .add(context::getNotifier)
            .add(context::getOwner)
            .build()
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(User::getUniqueId)
            .findFirst();

    private EntityUtil() {
    }

    private static final Predicate<Entity> TRACEABLE = NOT_SPECTATING.and(entity -> entity != null && entity.canBeCollidedWith());

    public static final Function<Humanoid, EntityPlayer> HUMANOID_TO_PLAYER = (humanoid) -> humanoid instanceof EntityPlayer ? (EntityPlayer) humanoid : null;

    /**
     * This is mostly for debugging purposes, but as well as ensuring that the phases are entered and exited correctly.
     *
     *  <p>Note that this is called only in SpongeVanilla or SpongeForge directly due to changes in signatures
     *  from Forge.</p>
     *
     * @param entity The entity
     * @param toDimension The target dimension type suggested by mods and vanilla alike. The suggested
     *     dimension itypecan be erroneous and Vanilla will re-assign the variable to the overworld for
     *     silly things like entering an end portal while in the end.
     * @return The entity, if the teleport was not cancelled or something.
     */
    @Nullable
    public static Entity transferEntityToDimension(Entity entity, DimensionType toDimension, IMixinITeleporter teleporter) {
        final MoveEntityEvent.Teleport.Portal event = handleDisplaceEntityPortalEvent(entity, toDimension, teleporter);
        if (event == null || event.isCancelled()) {
            return null;
        }

        entity.world.profiler.startSection("changeDimension");
        final Transform toTransform = event.getToTransform();
        final WorldServer toWorld = (WorldServer) toTransform.getWorld();
        entity.world.removeEntity(entity);
        entity.removed = false;
        entity.world.profiler.startSection("reposition");

        final Vector3i toChunkVec = toTransform.getLocation().getChunkPosition();
        toWorld.getChunkProvider().getChunk(toChunkVec.getX(), toChunkVec.getZ(), true, true);
        final Vector3d toPosition = toTransform.getPosition();
        entity.setLocationAndAngles(toPosition.getX(), toPosition.getY(), toPosition.getZ(), (float) toTransform.getYaw(), (float) toTransform.getPitch());
        entity.world = toWorld;
        try (PhaseContext<?> ignored = EntityPhase.State.CHANGING_DIMENSION.createPhaseContext().setTargetWorld(toWorld).buildAndSwitch()) {
            toWorld.spawnEntity(entity);
            toWorld.tickEntity(entity, false);
        }
        entity.world.profiler.endSection();
        entity.world.profiler.endSection();
        entity.world.profiler.endSection();
        return entity;
    }

    // Used by PlayerList#transferPlayerToDimension and EntityPlayerMP#changeDimension.
    // This method should NOT fire a teleport event as that should always be handled by the caller.
    public static void transferPlayerToDimension(MoveEntityEvent.Teleport.Portal event, EntityPlayerMP playerIn) {
        final Location toLocation = event.getToTransform().getLocation();

        final WorldServer fromWorld = (WorldServer) event.getFromTransform().getWorld();
        final WorldServer toWorld = (WorldServer) toLocation.getWorld();

        fromWorld.removeEntityDangerously(playerIn);
        fromWorld.getPlayerChunkMap().removePlayer(playerIn);
        playerIn.removed = false;
        playerIn.dimension = toWorld.getDimension().getType();

        toWorld.getChunkProvider().getChunk(toLocation.getChunkPosition().getX(), toLocation.getChunkPosition().getZ(), true, true);

        final DimensionType clientDimension = ((IMixinDimensionType) toWorld.getDimension().getType()).asClientDimensionType();

        if (!((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            final GlobalDimensionType fromGlobalDimension = ((IMixinDimensionType) fromWorld.getDimension().getType()).getGlobalDimensionType();
            final GlobalDimensionType toGlobalDimension = ((IMixinDimensionType) clientDimension).getGlobalDimensionType();

            if (fromGlobalDimension != toGlobalDimension) {
                playerIn.connection.sendPacket(new SPacketRespawn((clientDimension.getId() >= DimensionType.OVERWORLD.getId() ? DimensionType.NETHER :
                    DimensionType.OVERWORLD), toWorld.getDifficulty(), toWorld.getWorldInfo().getGenerator(), playerIn.interactionManager
                    .getGameType()));
            }
        }

        playerIn.connection.sendPacket(new SPacketRespawn(clientDimension, toWorld.getDifficulty(), toWorld.getWorldInfo().getGenerator(),
            playerIn.interactionManager.getGameType()));
        playerIn.connection.sendPacket(new SPacketServerDifficulty(toWorld.getDifficulty(), toWorld.getWorldInfo().isDifficultyLocked()));
        SpongeImpl.getServer().getPlayerList().updatePermissionLevel(playerIn);
        ((IMixinEntity) playerIn).setLocationAndAngles(event.getToTransform());
        playerIn.setWorld(toWorld);
        playerIn.interactionManager.setWorld(toWorld);
        toWorld.spawnEntity(playerIn);
        toWorld.tickEntity(playerIn, false);
        toWorld.getPlayerChunkMap().addPlayer(playerIn);

        CriteriaTriggers.CHANGED_DIMENSION.trigger(playerIn, fromWorld.dimension.getType(), toWorld.dimension.getType());

        if (fromWorld.dimension.getType() == DimensionType.NETHER && playerIn.world.dimension.getType() == DimensionType.OVERWORLD
            && playerIn.getEnteredNetherPosition() != null) {
            CriteriaTriggers.NETHER_TRAVEL.trigger(playerIn, playerIn.getEnteredNetherPosition());
        }

        SpongeImpl.getServer().getPlayerList().sendWorldInfo(playerIn, toWorld);
        SpongeImpl.getServer().getPlayerList().sendInventory(playerIn);

        playerIn.connection.sendPacket(new SPacketEntityStatus(playerIn, toWorld.getGameRules().getBoolean("reducedDebugInfo") ? (byte) 22 : 23));

        for (PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            playerIn.connection.sendPacket(new SPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }

        ((IMixinEntityPlayerMP) playerIn).refreshXpHealthAndFood();

        // Fix MC-88179: Resend attributes when switching dimensions. Code taken from Forge.
        final AttributeMap attributemap = (AttributeMap) playerIn.getAttributeMap();
        final Collection<IAttributeInstance> watchedAttribs = attributemap.getWatchedAttributes();
        if (!watchedAttribs.isEmpty()) {
            playerIn.connection.sendPacket(new SPacketEntityProperties(playerIn.getEntityId(), watchedAttribs));
        }

        SpongeImplHooks.handlePostChangeDimensionEvent(playerIn, fromWorld, toWorld);
    }

    /**
     * A relative copy paste of {@link EntityPlayerMP#func_212321_a(DimensionType)} where instead we direct all processing
     * to the appropriate areas for throwing events and capturing world changes during the transfer.
     *
     * <p>Note that this is called only in SpongeVanilla or SpongeForge directly due to changes in signatures
     * from Forge.</p>
     *
     * @param playerIn The player being teleported
     * @param toDimension The dimension type
     * @return The player object, not re-created
     */
    @Nullable
    public static EntityPlayerMP teleportPlayerToDimension(EntityPlayerMP playerIn, DimensionType toDimension, IMixinITeleporter teleporter) {
        // Fire teleport event here to support Forge's EntityTravelDimensionEvent
        // This also prevents sending client wrong data if event is cancelled
        final MoveEntityEvent.Teleport.Portal event = EntityUtil.handleDisplaceEntityPortalEvent(playerIn, toDimension, teleporter);
        if (event == null || event.isCancelled()) {
            return playerIn;
        }
        playerIn.invulnerableDimensionChange = true;

        final boolean sameDimension = playerIn.dimension == toDimension;

        // If leaving The End via End's Portal
        // Sponge Start - Check the provider, not the world's dimension
        final WorldServer fromWorld = (WorldServer) event.getFromTransform().getWorld();
        if (fromWorld.dimension instanceof EndDimension && toDimension == DimensionType.THE_END) {
            // Sponge End
            fromWorld.removeEntity(playerIn);
            if (!playerIn.queuedEndExit) {
                playerIn.queuedEndExit = true;
                playerIn.connection.sendPacket(new SPacketChangeGameState(4, playerIn.seenCredits ? 0.0F : 1.0F));
                playerIn.seenCredits = true;
            }
            return playerIn;
        } // else { // Sponge - Remove unecessary

        final WorldServer toWorld = (WorldServer) event.getToTransform().getWorld();
        // If we attempted to travel a new dimension but were denied due to some reason such as world
        // not being loaded then short-circuit to prevent unnecessary logic from running
        if (!sameDimension && fromWorld == toWorld) {
            return playerIn;
        }

        transferPlayerToDimension(event, playerIn);
        playerIn.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));
        return playerIn;
    }

    public static boolean isEntityDead(org.spongepowered.api.entity.Entity entity) {
        return isEntityDead((net.minecraft.entity.Entity) entity);
    }

    private static boolean isEntityDead(net.minecraft.entity.Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase base = (EntityLivingBase) entity;
            return base.getHealth() <= 0 || base.deathTime > 0 || base.dead;
        }
        return entity.removed;
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, Location location) {
        Transform fromTransform = ((IMixinEntity) entityIn).getTransform();
        Transform toTransform = fromTransform.setLocation(location).setRotation(new Vector3d(entityIn.rotationPitch, entityIn.rotationYaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, double posX, double posY, double posZ, float yaw, float pitch) {
        Transform fromTransform = ((IMixinEntity) entityIn).getTransform();
        Transform toTransform = fromTransform.setPosition(new Vector3d(posX, posY, posZ)).setRotation(new Vector3d(pitch, yaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, Transform fromTransform, Transform toTransform) {

        // Use origin world to get correct cause
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseData peek = phaseTracker.getCurrentPhaseData();

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entityIn);

            MoveEntityEvent.Teleport event = SpongeEventFactory.createMoveEntityEventTeleport(Sponge.getCauseStackManager().getCurrentCause(), fromTransform, toTransform, (org.spongepowered.api.entity.Entity) entityIn);
            SpongeImpl.postEvent(event);
            return event;
        }
    }

    @Nullable
    public static MoveEntityEvent.Teleport.Portal handleDisplaceEntityPortalEvent(Entity entityIn, DimensionType targetDimensionType,
        IMixinITeleporter teleporter) {
        SpongeImplHooks.registerPortalAgentType(teleporter);
        final MinecraftServer mcServer = SpongeImpl.getServer();
        final IMixinPlayerList mixinPlayerList = (IMixinPlayerList) mcServer.getPlayerList();
        final IMixinEntity mixinEntity = (IMixinEntity) entityIn;
        final Transform fromTransform = mixinEntity.getTransform();
        final WorldServer fromWorld = ((WorldServer) entityIn.world);
        final IMixinWorldServer fromMixinWorld = (IMixinWorldServer) fromWorld;
        boolean sameDimension = entityIn.dimension == targetDimensionType;
        // handle the end
        if (targetDimensionType == DimensionType.THE_END && fromWorld.dimension instanceof EndDimension) {
            targetDimensionType = DimensionType.OVERWORLD;
        }
        WorldServer toWorld = mcServer.getWorld(targetDimensionType);
        // If we attempted to travel a new dimension but were denied due to some reason such as world
        // not being loaded then short-circuit to prevent unnecessary logic from running
        if (!sameDimension && fromWorld == toWorld) {
            return null;
        }

        final Map<String, String> portalAgents = fromMixinWorld.getActiveConfig().getConfig().getWorld().getPortalAgents();
        String worldName;

        // Check if we're to use a different teleporter for this world
        if (teleporter.getClass().getName().equals("net.minecraft.world.Teleporter")) {
            worldName = portalAgents.get("minecraft:default_" + toWorld.dimension.getType().getSuffix().toLowerCase(Locale.ENGLISH));
        } else { // custom
            worldName = portalAgents.get("minecraft:" + teleporter.getClass().getSimpleName());
        }

        if (worldName != null) {
            for (WorldProperties worldProperties : Sponge.getServer().getAllWorldProperties()) {
                if (worldProperties.getWorldName().equalsIgnoreCase(worldName)) {
                    Optional<World> spongeWorld = Sponge.getServer().loadWorld(worldProperties);
                    if (spongeWorld.isPresent()) {
                        toWorld = (WorldServer) spongeWorld.get();
                        teleporter = (IMixinITeleporter) toWorld.getDefaultTeleporter();
                        if (teleporter instanceof IMixinTeleporter) {
                            if ((fromWorld.dimension.isNether() || toWorld.dimension.isNether())) {
                                ((IMixinTeleporter) teleporter).setNetherPortalType(true);
                            } else {
                                ((IMixinTeleporter) teleporter).setNetherPortalType(false);
                            }
                        }
                    }
                }
            }
        }

        adjustEntityPostionForTeleport(mixinPlayerList, entityIn, fromWorld, toWorld);

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
             TeleportingContext context = EntityPhase.State.CHANGING_DIMENSION.createPhaseContext().setTargetWorld(toWorld)
                     .buildAndSwitch()
            ) {
            frame.pushCause(teleporter);
            frame.pushCause(mixinEntity);

            frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.PORTAL);


            if (entityIn.isAlive() && (teleporter instanceof IMixinTeleporter && !(fromWorld.dimension instanceof EndDimension))) {
                fromWorld.profiler.startSection("placing");
                // Only place entity in portal if one of the following are true :
                // 1. The teleporter is custom. (not vanilla)
                // 2. The last known portal vec is known. (Usually set after block collision)
                // 3. The entity is traveling to end from a non-end world.
                // Note: We must always use placeInPortal to support mods.
                if (!teleporter.isVanilla() || entityIn.getLastPortalVec() != null || (!(fromWorld.dimension instanceof EndDimension) && toWorld.dimension instanceof EndDimension)) {
                    // In Forge, the entity dimension is already set by this point.
                    // To maintain compatibility with Forge mods, we temporarily
                    // set the entity's dimension to the current target dimension
                    // when calling Teleporter#placeEntity.

                    // When MoveEntityEvent.Teleport.Portal is fully implemented,
                    // this logic should be reworked.
                    DimensionType oldDimension = entityIn.dimension;
                    entityIn.dimension = targetDimensionType;
                    teleporter.placeEntity(toWorld, entityIn, entityIn.rotationYaw);
                    entityIn.dimension = oldDimension;
                }
                fromWorld.profiler.endSection();
            }

            // Complete phases, just because we need to. The phases don't actually do anything, because the processing resides here.

            // Grab the exit location of entity after being placed into portal
            final Transform portalExitTransform = mixinEntity.getTransform().setWorld((World) toWorld);
            // Use setLocationAndAngles to avoid firing MoveEntityEvent to plugins
            mixinEntity.setLocationAndAngles(fromTransform);
            final MoveEntityEvent.Teleport.Portal event = SpongeEventFactory.createMoveEntityEventTeleportPortal(frame.getCurrentCause(), fromTransform, portalExitTransform, mixinEntity, (PortalAgent) teleporter, true);
            SpongeImpl.postEvent(event);
            final Vector3i chunkPosition = mixinEntity.getLocation().getChunkPosition();
            final List<BlockSnapshot> capturedBlocks = context.getCapturedBlocks();
            final Transform toTransform = event.getToTransform();

            if (event.isCancelled()) {
                // Mods may cancel this event in order to run custom transfer logic
                // We need to make sure to only restore the location if
                if (!portalExitTransform.getWorld().getUniqueId().equals(mixinEntity.getLocation().getWorld().getUniqueId())) {
                    // update cache
                    if (teleporter instanceof IMixinTeleporter) {
                        ((IMixinTeleporter) teleporter).removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                    }
                    if (!capturedBlocks.isEmpty()) {
                        for (BlockSnapshot original : Lists.reverse(capturedBlocks)) {
                            original.restore(true, BlockChangeFlags.NONE);
                        }
                        capturedBlocks.clear();
                    }
                    mixinEntity.setLocationAndAngles(fromTransform);
                } else {
                    // Call setTransform to let plugins know mods changed the position
                    // Guarantees plugins such as Nucleus can track changed locations properly
                    mixinEntity.setTransform(mixinEntity.getTransform());
                }
                return event;
            }

            if (!portalExitTransform.equals(toTransform)) {
                // if plugin set to same world, just set the transform
                if (((World) fromWorld).getUniqueId().equals(toTransform.getWorld().getUniqueId())) {
                    // force cancel so we know to skip remaining logic
                    event.setCancelled(true);

                    if (teleporter instanceof IMixinTeleporter) {
                        // update cache
                        ((IMixinTeleporter) teleporter).removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                    }

                    // Undo created portal
                    if (!capturedBlocks.isEmpty()) {
                        for (BlockSnapshot original : Lists.reverse(capturedBlocks)) {
                            original.restore(true, BlockChangeFlags.NONE);
                        }
                    }
                    capturedBlocks.clear();
                    mixinEntity.setLocationAndAngles(toTransform);
                    return event;
                }
            } else {
                if (toWorld.dimension instanceof EndDimension) {
                    BlockPos blockpos = entityIn.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, toWorld.getSpawnPoint());
                    entityIn.moveToBlockPosAndAngles(blockpos, entityIn.rotationYaw, entityIn.rotationPitch);
                }
            }

            if (teleporter instanceof IMixinTeleporter && !capturedBlocks.isEmpty() && !TrackingUtil.processBlockCaptures(capturedBlocks, EntityPhase.State.CHANGING_DIMENSION, context)) {
                ((IMixinTeleporter) teleporter).removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
            }

            if (!event.getKeepsVelocity()) {
                entityIn.motionX = 0;
                entityIn.motionY = 0;
                entityIn.motionZ = 0;
            }
            return event;

        }
    }

    public static IMixinWorldServer getMixinWorld(org.spongepowered.api.entity.Entity entity) {
        return (IMixinWorldServer) entity.getWorld();
    }

    public static IMixinWorldServer getMixinWorld(Entity entity) {
        return (IMixinWorldServer) entity.world;
    }

    public static WorldServer getMinecraftWorld(org.spongepowered.api.entity.Entity entity) {
        return (WorldServer) entity.getWorld();
    }

    public static World getSpongeWorld(Entity player) {
        return (World) player.world;
    }

    public static Player toPlayer(EntityPlayer player) {
        return (Player) player;
    }

    public static int getHorseInternalVariant(SpongeHorseColor color, SpongeHorseStyle style) {
        return color.getBitMask() | style.getBitMask();
    }

    public static boolean processEntitySpawnsFromEvent(SpawnEntityEvent event, Supplier<Optional<UUID>> entityCreatorSupplier) {
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

    public static boolean processEntitySpawn(org.spongepowered.api.entity.Entity entity, Supplier<Optional<UUID>> supplier) {
        final Entity minecraftEntity = toNative(entity);
        if (minecraftEntity instanceof EntityItem) {
            final ItemStack item = ((EntityItem) minecraftEntity).getItem();
            if (!item.isEmpty()) {
                final Optional<Entity>
                    customEntityItem =
                    ((IMixinItem) item.getItem()).getCustomEntityItem(minecraftEntity.getEntityWorld(), minecraftEntity, item);
                if (customEntityItem.isPresent()) {
                    // Bypass spawning the entity item, since it is established that the custom entity is spawned.
                    final Entity entityToSpawn = customEntityItem.get();
                    supplier.get()
                        .ifPresent(creator -> toMixin(entityToSpawn).setCreator(creator));
                    // Since forge already has a new event thrown for the entity, we don't need to throw
                    // the event anymore as sponge plugins getting the event after forge mods will
                    // have the modified entity list for entities, so no need to re-capture the entities.
                    getMixinWorld(entity).forceSpawnEntity(entity);
                    return true;
                }
            }
        }
        supplier.get()
            .ifPresent(creator -> toMixin(entity).setCreator(creator));
        // Allowed to call force spawn directly since we've applied creator and custom item logic already
        getMixinWorld(entity).forceSpawnEntity(entity);
        return true;
    }

    public static ItemStack getItem(Entity entity) {
        return entity instanceof EntityItem ? ((EntityItem) entity).getItem() : ItemStack.EMPTY;
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
        EntityTrace entityRay = EntityUtil.rayTraceEntities(source, traceDistance, partialTicks, blockDistance, traceStart);

        if (entityRay.entity != null && (entityRay.distance < blockDistance || blockRay == null)) {
            return entityRay.asRayTraceResult();
        }

        return blockRay;
    }

    private static EntityTrace rayTraceEntities(Entity source, double traceDistance, float partialTicks, double blockDistance, Vec3d traceStart) {
        EntityTrace trace = new EntityTrace(blockDistance);

        Vec3d lookDir = source.getLook(partialTicks).scale(traceDistance);
        Vec3d traceEnd = traceStart.add(lookDir);

        for (final Entity entity : EntityUtil.getTraceEntities(source, traceDistance, lookDir, EntityUtil.TRACEABLE)) {
            AxisAlignedBB entityBB = entity.getBoundingBox().grow(entity.getCollisionBorderSize());
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
        AxisAlignedBB boundingBox = source.getBoundingBox();
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

    public static boolean refreshPainting(EntityPainting painting, PaintingType art) {
        PaintingType oldArt = painting.art;
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
            SpongeImpl.getGame().getServer().getScheduler().submit(
              Task.builder()
                .delayTicks(SpongeImpl.getGlobalConfig().getConfig().getEntity().getPaintingRespawnDelaly())
                .execute(() -> {
                    final SPacketSpawnPainting packet = new SPacketSpawnPainting(painting);
                    playerMP.connection.sendPacket(packet);
                })
              .build()
            );
        }
        return true;
    }

    public static Profession validateProfession(int professionId) {
        List<Profession> professions = (List<Profession>) ProfessionRegistryModule.getInstance().getAll();
        for (Profession profession : professions) {
            if (profession instanceof SpongeProfession) {
                if (professionId == ((SpongeProfession) profession).type) {
                    return profession;
                }
            }
        }
        throw new IllegalStateException("Invalid Villager profession id is present! Found: " + professionId
                                        + " when the expected contain: " + professions);
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
                        return entityPos.equals(pos.add(HANGING_OFFSET_NORTH));
                    } else if (entityFacing == EnumFacing.SOUTH) {
                        return entityIn.getPosition().equals(pos.add(HANGING_OFFSET_SOUTH));
                    } else if (entityFacing == EnumFacing.WEST) {
                        return entityIn.getPosition().equals(pos.add(HANGING_OFFSET_WEST));
                    } else if (entityFacing == EnumFacing.EAST) {
                        return entityIn.getPosition().equals(pos.add(HANGING_OFFSET_EAST));
                    }
                    return false;
                });
    }

    // A temporary variable to transfer the 'isBedSpawn' variable between
    // getPlayerRespawnLocation and recreatePlayerEntity
    public static boolean tempIsBedSpawn = false;

    // Internal to MixinPlayerList. has side effects
    public static Location getPlayerRespawnLocation(EntityPlayerMP playerIn, @Nullable WorldServer toWorld) {
        final Location location = ((World) playerIn.world).getSpawnLocation();
        tempIsBedSpawn = false;
        if (toWorld == null) { // Target world doesn't exist? Use global
            return location;
        }

        final Dimension toDimension = (Dimension) toWorld.dimension;
        DimensionType toDimensionType = ((net.minecraft.world.dimension.Dimension) toDimension).getType();
        // Cannot respawn in requested world, use the fallback dimension for
        // that world. (Usually overworld unless a mod says otherwise).
        if (!toDimension.allowsPlayerRespawns()) {
            toDimensionType = SpongeImplHooks.getRespawnDimension((net.minecraft.world.dimension.Dimension) toDimension, playerIn);
            toWorld = playerIn.server.getWorld(toDimensionType);
        }

        BlockPos spawnPos = toWorld.getSpawnPoint();
        BlockPos bedPos = SpongeImplHooks.getBedLocation(playerIn, toDimensionType);
        if (bedPos != null) { // Player has a bed
            final boolean forceBedSpawn = SpongeImplHooks.isSpawnForced(playerIn, toDimensionType);
            final BlockPos offsetToBedPos = EntityPlayer.getBedSpawnLocation(toWorld, bedPos, forceBedSpawn);
            if (offsetToBedPos != null) { // The bed exists and is not obstructed
                tempIsBedSpawn = true;
                spawnPos = new BlockPos(offsetToBedPos.getX() + 0.5D, offsetToBedPos.getY() + 0.1D, offsetToBedPos.getZ() + 0.5D);
            } else { // Bed invalid
                playerIn.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
                // Vanilla behaviour - Delete the known bed location if invalid
                bedPos = null; // null = remove location
            }
            // Set the new bed location for the new dimension
            DimensionType prevDim = playerIn.dimension; // Temporarily for setSpawnPoint
            playerIn.dimension = toDimensionType;
            playerIn.setSpawnPoint(bedPos, forceBedSpawn);
            playerIn.dimension = prevDim;
        }
        return new Location((World) toWorld, VecHelper.toVector3d(spawnPos));
    }

    public static Entity toNative(org.spongepowered.api.entity.Entity tickingEntity) {
        if (!(tickingEntity instanceof Entity)) {
            throw new IllegalArgumentException("Not a native Entity for this implementation!");
        }
        return (Entity) tickingEntity;
    }

    public static EntityLivingBase toNative(IMixinEntityLivingBase entityLivingBase) {
        if (!(entityLivingBase instanceof EntityLivingBase)) {
            throw new IllegalArgumentException("Not a native EntityLivingBase for this implementation!");
        }
        return (EntityLivingBase) entityLivingBase;
    }

    public static EntityPlayer toNative(IMixinEntityPlayer player) {
        if (!(player instanceof EntityPlayer)) {
            throw new IllegalArgumentException("Not a native EntityPlayer for this implementation!");
        }
        return (EntityPlayer) player;
    }

    @Nullable
    public static Entity toNullableNative(@Nullable org.spongepowered.api.entity.Entity entity) {
        if (entity == null) {
            return null;
        }
        if (!(entity instanceof Entity)) {
            throw new IllegalArgumentException("Not a native Entity for this implementation!");
        }
        return (Entity) entity;
    }

    public static org.spongepowered.api.entity.Entity fromNative(Entity entity) {
        return (org.spongepowered.api.entity.Entity) entity;
    }

    public static Living fromNativeToLiving(Entity entity) {
        if (!(entity instanceof Living)) {
            throw new IllegalArgumentException("Entity is incompatible with SpongeAPI Living interface: " + entity);
        }
        return (Living) entity;
    }

    public static EntityLivingBase toNative(Living entity) {
        if (!(entity instanceof EntityLivingBase)) {
            throw new IllegalArgumentException("Living entity is not compatible with this implementation: " + entity);
        }
        return (EntityLivingBase) entity;
    }

    public static EntityPlayerMP toNative(Player player) {
        if (!(player instanceof EntityPlayerMP)) {
            throw new IllegalArgumentException("Player entity is not compatible with this implementation: " + player);
        }
        return (EntityPlayerMP) player;
    }

    public static EntityPlayerMP toNative(IMixinEntityPlayerMP playerMP) {
        if (!(playerMP instanceof EntityPlayerMP)) {
            throw new IllegalArgumentException("Player entity is not compatible with this implementation: " + playerMP);
        }
        return (EntityPlayerMP) playerMP;
    }

    public static IMixinEntity toMixin(Entity entity) {
        if (!(entity instanceof IMixinEntity)) {
            throw new IllegalArgumentException("Not a mixin Entity for this implementation!");
        }
        return (IMixinEntity) entity;
    }

    public static IMixinEntity toMixin(org.spongepowered.api.entity.Entity entity) {
        if (!(entity instanceof IMixinEntity)) {
            throw new IllegalArgumentException("Not a mixin Entity for this implementation!");
        }
        return (IMixinEntity) entity;
    }

    public static EntitySnapshot createSnapshot(Entity entity) {
        return fromNative(entity).createSnapshot();
    }

    public static void changeWorld(MinecraftServer server, Entity entity, Location location, DimensionType fromDimension, DimensionType toDimension) {
        final WorldServer fromWorld = server.getWorld(fromDimension);
        final WorldServer toWorld = server.getWorld(toDimension);
        if (entity instanceof EntityPlayer) {
            fromWorld.getEntityTracker().removePlayerFromTrackers((EntityPlayerMP) entity);
            fromWorld.getPlayerChunkMap().removePlayer((EntityPlayerMP) entity);
            server.getPlayerList().getPlayers().remove(entity);
        } else {
            fromWorld.getEntityTracker().untrack(entity);
        }

        entity.stopRiding();
        entity.world.removeEntityDangerously(entity);
        entity.removed = false;
        entity.dimension = toDimension;
        entity.setPositionAndRotation(location.getX(), location.getY(), location.getZ(), 0, 0);
        while (toWorld.isCollisionBoxesEmpty(entity, entity.getBoundingBox()) && location.getY() < 256.0D) {
            entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
        }

        toWorld.getChunkProvider().getChunk((int) entity.posX >> 4, (int) entity.posZ >> 4, true, true);

        if (entity instanceof EntityPlayerMP && ((EntityPlayerMP) entity).connection != null) {
            final EntityPlayerMP player = (EntityPlayerMP) entity;

            toDimension = ((IMixinDimensionType) toWorld.getDimension().getType()).asClientDimensionType();

            if (!((IMixinEntityPlayerMP) player).usesCustomClient()) {
                final GlobalDimensionType fromGlobalDimension = ((IMixinDimensionType) fromWorld.getDimension().getType()).getGlobalDimensionType();
                final GlobalDimensionType toGlobalDimension = ((IMixinDimensionType) toDimension).getGlobalDimensionType();

                if (fromGlobalDimension != toGlobalDimension) {
                    player.connection.sendPacket(new SPacketRespawn((toDimension.getId() >= DimensionType.OVERWORLD.getId() ? DimensionType.NETHER :
                        DimensionType.OVERWORLD), toWorld.getDifficulty(), toWorld.getWorldInfo().getGenerator(), player.interactionManager
                        .getGameType()));
                }
            }

            // Prevent 'lastMoveLocation' from being set to the previous world.
            ((IMixinNetHandlerPlayServer) player.connection).setLastMoveLocation(null);

            player.connection.sendPacket(
                    new SPacketRespawn(toDimension, toWorld.getDifficulty(), toWorld.getWorldInfo().getGenerator(),
                            player.interactionManager.getGameType()));
            player.connection.sendPacket(new SPacketServerDifficulty(toWorld.getDifficulty(), toWorld.getWorldInfo().isDifficultyLocked()));
            server.getPlayerList().updatePermissionLevel(player);

            entity.setWorld(toWorld);
            player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
            player.setSneaking(false);
            server.getPlayerList().sendWorldInfo(player, toWorld);
            toWorld.getPlayerChunkMap().addPlayer(player);
            toWorld.spawnEntity(player);
            server.getPlayerList().getPlayers().add(player);
            player.interactionManager.setWorld(toWorld);
            player.addSelfToInternalCraftingInventory();

            player.connection.sendPacket(new SPacketEntityStatus(player, toWorld.getGameRules().getBoolean("reducedDebugInfo") ? (byte) 22 : 23));
            ((IMixinEntityPlayerMP) player).refreshXpHealthAndFood();
            for (Object effect : player.getActivePotionEffects()) {
                player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), (PotionEffect) effect));
            }


            // Fix MC-88179: Resend attributes when switching dimensions. Code taken from Forge.
            final AttributeMap attributemap = (AttributeMap) player.getAttributeMap();
            final Collection<IAttributeInstance> watchedAttribs = attributemap.getWatchedAttributes();
            if (!watchedAttribs.isEmpty()) {
                player.connection.sendPacket(new SPacketEntityProperties(player.getEntityId(), watchedAttribs));
            }
        } else {
            entity.setWorld(toWorld);
            toWorld.spawnEntity(entity);
        }

        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
    }

    private static void adjustEntityPostionForTeleport(IMixinPlayerList playerList, Entity entity, WorldServer fromWorld, WorldServer toWorld) {
        fromWorld.profiler.startSection("moving");
        net.minecraft.world.dimension.Dimension dOld = fromWorld.dimension;
        net.minecraft.world.dimension.Dimension dNew = toWorld.dimension;
        double moveFactor = playerList.getMovementFactor(dOld) / playerList.getMovementFactor(dNew);
        double x = entity.posX * moveFactor;
        double y = entity.posY;
        double z = entity.posZ * moveFactor;

        if (dNew instanceof EndDimension) {
            BlockPos blockpos;

            if (dOld instanceof EndDimension) {
                blockpos = toWorld.getSpawnPoint();
            } else {
                blockpos = toWorld.getSpawnCoordinate();
            }

            x = blockpos.getX();
            y = blockpos.getY();
            z = blockpos.getZ();
            entity.setLocationAndAngles(x, y, z, 90.0F, 0.0F);
        }

        if (!(dOld instanceof EndDimension)) {
            fromWorld.profiler.startSection("placing");
            x = MathHelper.clamp((int)x, -29999872, 29999872);
            z = MathHelper.clamp((int)z, -29999872, 29999872);

            if (entity.isAlive()) {
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
            fromWorld.profiler.endSection();
        }

        if (entity.isAlive()) {
            fromWorld.tickEntity(entity, false);
        }

        fromWorld.profiler.endSection();
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
        final IMixinEntity mixinEntity = EntityUtil.toMixin(entity);
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
        final PhaseData peek = PhaseTracker.getInstance().getCurrentPhaseData();
        final IPhaseState<?> currentState = peek.state;
        final PhaseContext<?> phaseContext = peek.context;

        // We want to frame ourselves here, because of the two events we have to throw, first for the drop item event, then the constructentityevent.
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // Perform the event throws first, if they return false, return null
            item = throwDropItemAndConstructEvent(mixinEntity, posX, posY, posZ, snapshot, original, frame);

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
            EntityUtil.processEntitySpawn(fromNative(entityitem), Optional::empty);
            return entityitem;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public static EntityItem playerDropItem(IMixinEntityPlayer mixinPlayer, ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        mixinPlayer.shouldRestoreInventory(false);
        final EntityPlayer player = EntityUtil.toNative(mixinPlayer);

        final double posX = player.posX;
        final double posY = player.posY - 0.3 + player.getEyeHeight();
        final double posZ = player.posZ;
        // Now the real fun begins.
        final ItemStack item;
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(droppedItem);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        final PhaseData peek = PhaseTracker.getInstance().getCurrentPhaseData();
        final IPhaseState currentState = peek.state;
        final PhaseContext<?> phaseContext = peek.context;

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

            item = throwDropItemAndConstructEvent(mixinPlayer, posX, posY, posZ, snapshot, original, frame);

            if (item == null || item.isEmpty()) {
                return null;
            }


            // Here is where we would potentially perform item pre-merging (merge the item stacks with previously captured item stacks
            // and only if those stacks can be stacked (count increased). Otherwise, we'll just continue to throw the entity item.
            // For now, due to refactoring a majority of all of this code, pre-merging is disabled entirely.

            EntityItem entityitem = new EntityItem(player.world, posX, posY, posZ, droppedItem);
            entityitem.setPickupDelay(40);

            if (traceItem) {
                entityitem.setThrowerId(player.getUniqueID());
            }

            final Random random = mixinPlayer.getRandom();
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
            if (currentState.spawnItemOrCapture(phaseContext, EntityUtil.toNative(mixinPlayer), entityitem)) {
                return entityitem;
            }
            // TODO - Investigate whether player drops are adding to the stat list in captures.
            ItemStack itemstack = dropItemAndGetStack(player, entityitem);

            if (traceItem) {
                if (!itemstack.isEmpty()) {
                    player.addStat(StatList.ITEM_DROPPED.get(itemstack.getItem()), droppedItem.getCount());
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
    public static ItemStack throwDropItemAndConstructEvent(IMixinEntity entity, double posX, double posY,
        double posZ, ItemStackSnapshot snapshot, List<ItemStackSnapshot> original, CauseStackManager.StackFrame frame) {
        final IMixinEntityPlayer mixinPlayer;
        if (entity instanceof IMixinEntityPlayer) {
            mixinPlayer = (IMixinEntityPlayer) entity;
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
        Transform suggested = new Transform(entity.getWorld(), new Vector3d(posX, posY, posZ));
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

    public static Optional<EntityType> fromNameToType(String name) {
        return fromLocationToType(new ResourceLocation(name));
    }

    public static Optional<EntityType> fromLocationToType(ResourceLocation location) {
        return Optional.ofNullable((EntityType) net.minecraft.entity.EntityType.field_200787_a.get(location));
    }

    // I'm lazy, but this is better than using the convenience method
    public static EntityArchetype archetype(EntityType type) {
        return new SpongeEntityArchetypeBuilder().type(type).build();
    }
}

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
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
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
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.cause.entity.teleport.PortalTeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
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
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.interfaces.IMixinPlayerList;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.MixinEntity;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

public final class EntityUtil {

    public static final BlockPos HANGING_OFFSET_EAST = new BlockPos(1, 1, 0);
    public static final BlockPos HANGING_OFFSET_WEST = new BlockPos(-1, 1, 0);
    public static final BlockPos HANGING_OFFSET_NORTH = new BlockPos(0, 1, -1);
    public static final BlockPos HANGING_OFFSET_SOUTH = new BlockPos(0, 1, 1);

    private EntityUtil() {
    }

    static final Predicate<Entity> TRACEABLE = Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
        @Override
        public boolean apply(Entity entity) {
            return entity != null && entity.canBeCollidedWith();
        }
    });

    public static final Function<Humanoid, EntityPlayer> HUMANOID_TO_PLAYER = (humanoid) -> humanoid instanceof EntityPlayer ? (EntityPlayer) humanoid : null;

    /**
     * Called specifically from {@link MixinEntity#changeDimension(int)} to overwrite
     * {@link Entity#changeDimension(int)}. This is mostly for debugging
     * purposes, but as well as ensuring that the phases are entered and exited correctly.
     *
     * @param mixinEntity The mixin entity being called
     * @param toSuggestedDimension The target dimension id suggested by mods and vanilla alike. The suggested
     *     dimension id can be erroneous and Vanilla will re-assign the variable to the overworld for
     *     silly things like entering an end portal while in the end.
     * @return The entity, if the teleport was not cancelled or something.
     */
    @Nullable
    public static Entity transferEntityToDimension(IMixinEntity mixinEntity, int toSuggestedDimension) {
        final Entity entity = toNative(mixinEntity);
        // handle portal event
        MoveEntityEvent.Teleport.Portal event = handleDisplaceEntityPortalEvent(entity, toSuggestedDimension, null);
        if (event == null || event.isCancelled()) {
            return null;
        }

        entity.world.profiler.startSection("changeDimension");
        // use the world from event
        final Transform<World> toTransform = event.getToTransform();
        WorldServer toWorld = (WorldServer) toTransform.getExtent();
        entity.world.removeEntity(entity);
        entity.isDead = false;
        entity.world.profiler.startSection("reposition");

        final Vector3i toChunkPosition = toTransform.getLocation().getChunkPosition();
        toWorld.getChunkProvider().loadChunk(toChunkPosition.getX(), toChunkPosition.getZ());
        // Only need to update the entity location here as the portal is handled in SpongeCommonEventFactory
        final Vector3d toPosition = toTransform.getPosition();
        entity.setLocationAndAngles(toPosition.getX(), toPosition.getY(), toPosition.getZ(), (float) toTransform.getYaw(), (float) toTransform.getPitch());
        entity.world = toWorld;
        toWorld.spawnEntity(entity);
        toWorld.updateEntityWithOptionalForce(entity, false);
        entity.world.profiler.endSection();

        entity.world.profiler.endSection();
        entity.world.profiler.endSection();
        return entity;
    }

    /**
     * A relative copy paste of {@link EntityPlayerMP#changeDimension(int)} where instead we direct all processing
     * to the appropriate areas for throwing events and capturing world changes during the transfer.
     *
     * @param mixinEntityPlayerMP The player being teleported
     * @param suggestedDimensionId The suggested dimension
     * @return The player object, not re-created
     */
    @Nullable
    public static Entity teleportPlayerToDimension(EntityPlayerMP entityPlayerMP, int suggestedDimensionId) {
        // Fire teleport event here to support Forge's EntityTravelDimensionEvent
        // This also prevents sending client wrong data if event is cancelled
        WorldServer toWorld = SpongeImpl.getServer().getWorld(suggestedDimensionId);
        MoveEntityEvent.Teleport.Portal event = EntityUtil.handleDisplaceEntityPortalEvent(entityPlayerMP, suggestedDimensionId, toWorld.getDefaultTeleporter());
        if (event == null || event.isCancelled()) {
            return entityPlayerMP;
        }

        boolean sameDimension = entityPlayerMP.dimension == suggestedDimensionId;
        // If leaving The End via End's Portal
        // Sponge Start - Check the provider, not the world's dimension
        final WorldServer fromWorldServer = (WorldServer) event.getFromTransform().getExtent();
        if (fromWorldServer.provider instanceof WorldProviderEnd && suggestedDimensionId == 1) { // if (this.dimension == 1 && dimensionIn == 1)
            // Sponge End
            fromWorldServer.removeEntity(entityPlayerMP);
            if (!entityPlayerMP.queuedEndExit) {
                entityPlayerMP.queuedEndExit = true;
                if (entityPlayerMP.hasAchievement(AchievementList.THE_END2)) {
                    entityPlayerMP.connection.sendPacket(new SPacketChangeGameState(4, 0.0F));
                } else {
                    entityPlayerMP.addStat(AchievementList.THE_END2);
                    entityPlayerMP.connection.sendPacket(new SPacketChangeGameState(4, 1.0F));
                }
            }
            return entityPlayerMP;
        } // else { // Sponge - Remove unecessary

        final WorldServer toWorldServer = (WorldServer) event.getToTransform().getExtent();
        // If we attempted to travel a new dimension but were denied due to some reason such as world
        // not being loaded then short-circuit to prevent unnecessary logic from running
        if (!sameDimension && fromWorldServer == toWorldServer) {
            return entityPlayerMP;
        }
        int targetDimensionId = ((IMixinWorldServer) toWorldServer).getDimensionId();

        // Sponge Start - Rewrite for vanilla mechanics since multiworlds can change world providers and
        // dimension id's
        if (fromWorldServer.provider instanceof WorldProviderSurface) {
            if (targetDimensionId == 1) {
                entityPlayerMP.addStat(AchievementList.THE_END);
            } else if (targetDimensionId == -1) {
                entityPlayerMP.addStat(AchievementList.PORTAL);
            }
        }
        // Sponge End

        transferPlayerToDimension(event, entityPlayerMP);
        entityPlayerMP.connection.sendPacket(new SPacketEffect(1032, BlockPos.ORIGIN, 0, false));

        // Sponge Start - entityPlayerMP has been moved below to refreshXpHealthAndFood
        /*
        entityPlayerMP.lastExperience = -1;
        entityPlayerMP.lastHealth = -1.0F;
        entityPlayerMP.lastFoodLevel = -1;
        */
        // Sponge End
        return entityPlayerMP;
    }

    // Used by PlayerList#transferPlayerToDimension and EntityPlayerMP#changeDimension.
    // This method should NOT fire a teleport event as that should always be handled by the caller.
    public static void transferPlayerToDimension(MoveEntityEvent.Teleport.Portal event, EntityPlayerMP playerIn) {
        WorldServer fromWorld = (WorldServer) event.getFromTransform().getExtent();
        WorldServer toWorld = (WorldServer) event.getToTransform().getExtent();
        playerIn.dimension = WorldManager.getClientDimensionId(playerIn, toWorld);
        toWorld.getChunkProvider().loadChunk(event.getToTransform().getLocation().getChunkPosition().getX(), event.getToTransform().getLocation().getChunkPosition().getZ());
        // Support vanilla clients teleporting to custom dimensions
        final int dimensionId = playerIn.dimension;

        // Send dimension registration
        if (((IMixinEntityPlayerMP) playerIn).usesCustomClient()) {
            WorldManager.sendDimensionRegistration(playerIn, toWorld.provider);
        } else {
            // Force vanilla client to refresh its chunk cache if same dimension type
            if (fromWorld != toWorld && fromWorld.provider.getDimensionType() == toWorld.provider.getDimensionType()) {
                playerIn.connection.sendPacket(new SPacketRespawn((dimensionId >= 0 ? -1 : 0), toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(), playerIn.interactionManager.getGameType()));
            }
        }
        playerIn.connection.sendPacket(new SPacketRespawn(dimensionId, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(), playerIn.interactionManager.getGameType()));
        fromWorld.removeEntityDangerously(playerIn);
        playerIn.isDead = false;
        // we do not need to call transferEntityToWorld as we already have the correct transform and created the portal in handleDisplaceEntityPortalEvent
        ((IMixinEntity) playerIn).setLocationAndAngles(event.getToTransform());
        playerIn.setWorld(toWorld);
        toWorld.spawnEntity(playerIn);
        toWorld.updateEntityWithOptionalForce(playerIn, false);
        SpongeImpl.getServer().getPlayerList().preparePlayer(playerIn, fromWorld);
        playerIn.connection.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
        playerIn.interactionManager.setWorld(toWorld);
        SpongeImpl.getServer().getPlayerList().updateTimeAndWeatherForPlayer(playerIn, toWorld);
        SpongeImpl.getServer().getPlayerList().syncPlayerInventory(playerIn);

        // Update reducedDebugInfo game rule
        playerIn.connection.sendPacket(new SPacketEntityStatus(playerIn,
                toWorld.getGameRules().getBoolean(DefaultGameRules.REDUCED_DEBUG_INFO) ? (byte) 22 : 23));

        for (PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
            playerIn.connection.sendPacket(new SPacketEntityEffect(playerIn.getEntityId(), potioneffect));
        }
        ((IMixinEntityPlayerMP) playerIn).refreshXpHealthAndFood();

        SpongeImplHooks.handlePostChangeDimensionEvent(playerIn, fromWorld, toWorld);
    }

    /*public static boolean isNative(org.spongepowered.api.data.type.ZombieType type, @Nullable Profession profession) {
        // No profession means native, husk means native, otherwise check the map (forge uses NORMAL + Profession for its types)
        return profession == null || type == ZombieTypes.HUSK || ZOMBIE_TYPE_MAP.containsKey(profession);
    }*/

    public static boolean isEntityDead(org.spongepowered.api.entity.Entity entity) {
        return isEntityDead((net.minecraft.entity.Entity) entity);
    }

    public static boolean isEntityDead(net.minecraft.entity.Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase base = (EntityLivingBase) entity;
            return base.getHealth() <= 0 || base.deathTime > 0 || base.dead;
        } else {
            return entity.isDead;
        }
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, Location<World> location) {
        Transform<World> fromTransform = ((IMixinEntity) entityIn).getTransform();
        Transform<World> toTransform = fromTransform.setLocation(location).setRotation(new Vector3d(entityIn.rotationPitch, entityIn.rotationYaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform, false);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, double posX, double posY, double posZ, float yaw, float pitch) {
        Transform<World> fromTransform = ((IMixinEntity) entityIn).getTransform();
        Transform<World> toTransform = fromTransform.setPosition(new Vector3d(posX, posY, posZ)).setRotation(new Vector3d(pitch, yaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform, false);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(Entity entityIn, Transform<World> fromTransform, Transform<World> toTransform, boolean apiCall) {

        // Use origin world to get correct cause
        final CauseTracker causeTracker = CauseTracker.getInstance();
        final PhaseData peek = causeTracker.getCurrentPhaseData();
        final IPhaseState state = peek.state;
        final PhaseContext context = peek.context;

        final Cause teleportCause = state.getPhase().generateTeleportCause(state, context);

        MoveEntityEvent.Teleport event = SpongeEventFactory.createMoveEntityEventTeleport(teleportCause, fromTransform, toTransform, (org.spongepowered.api.entity.Entity) entityIn);
        SpongeImpl.postEvent(event);
        return event;
    }

    @Nullable
    public static MoveEntityEvent.Teleport.Portal handleDisplaceEntityPortalEvent(Entity entityIn, int targetDimensionId, @Nullable Teleporter teleporter) {
        SpongeImplHooks.registerPortalAgentType(teleporter);
        final MinecraftServer mcServer = SpongeImpl.getServer();
        final IMixinPlayerList mixinPlayerList = (IMixinPlayerList) mcServer.getPlayerList();
        final IMixinEntity mixinEntity = (IMixinEntity) entityIn;
        final Transform<World> fromTransform = mixinEntity.getTransform();
        final WorldServer fromWorld = ((WorldServer) entityIn.world);
        final IMixinWorldServer fromMixinWorld = (IMixinWorldServer) fromWorld;
        boolean sameDimension = entityIn.dimension == targetDimensionId;
        // handle the end
        if (targetDimensionId == 1 && fromWorld.provider instanceof WorldProviderEnd) {
            targetDimensionId = 0;
        }
        WorldServer toWorld = mcServer.getWorld(targetDimensionId);
        // If we attempted to travel a new dimension but were denied due to some reason such as world
        // not being loaded then short-circuit to prevent unnecessary logic from running
        if (!sameDimension && fromWorld == toWorld) {
            return null;
        }

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

        adjustEntityPostionForTeleport(mixinPlayerList, entityIn, fromWorld, toWorld);
        final PhaseContext context = PhaseContext.start();
        context.add(NamedCause.source(mixinEntity))
                // unused, to be removed and re-located when phase context is cleaned up
                //.add(NamedCause.of(InternalNamedCauses.Teleporting.FROM_WORLD, fromWorld))
                //.add(NamedCause.of(InternalNamedCauses.Teleporting.TARGET_TELEPORTER, teleporter))
                //.add(NamedCause.of(InternalNamedCauses.Teleporting.FROM_TRANSFORM, fromTransform))
                .add(NamedCause.of(InternalNamedCauses.Teleporting.TARGET_WORLD, toWorld))
                .addBlockCaptures()
                .addEntityCaptures();
        final Cause teleportCause = Cause.of(NamedCause.source(PortalTeleportCause.builder()
                        .agent((PortalAgent) teleporter)
                        .type(TeleportTypes.PORTAL)
                        .build()
                )
        );
        context.complete();
        final CauseTracker causeTracker = CauseTracker.getInstance();
        causeTracker.switchToPhase(EntityPhase.State.CHANGING_DIMENSION, context);


        if (entityIn.isEntityAlive() && !(fromWorld.provider instanceof WorldProviderEnd)) {
            fromWorld.profiler.startSection("placing");
            // need to use placeInPortal to support mods
            teleporter.placeInPortal(entityIn, entityIn.rotationYaw);
            fromWorld.profiler.endSection();
        }

        // Complete phases, just because we need to. The phases don't actually do anything, because the processing resides here.
        causeTracker.completePhase(EntityPhase.State.CHANGING_DIMENSION);

        // Grab the exit location of entity after being placed into portal
        final Transform<World> portalExitTransform = mixinEntity.getTransform().setExtent((World) toWorld);
        // Use setLocationAndAngles to avoid firing MoveEntityEvent to plugins
        mixinEntity.setLocationAndAngles(fromTransform);
        final MoveEntityEvent.Teleport.Portal event = SpongeEventFactory.createMoveEntityEventTeleportPortal(teleportCause, fromTransform, portalExitTransform, (PortalAgent) teleporter, mixinEntity, true);
        SpongeImpl.postEvent(event);
        final Vector3i chunkPosition = mixinEntity.getLocation().getChunkPosition();
        final IMixinTeleporter toMixinTeleporter = (IMixinTeleporter) teleporter;

        if (event.isCancelled()) {
            // Mods may cancel this event in order to run custom transfer logic
            // We need to make sure to only restore the location if 
            if (!portalExitTransform.getExtent().getUniqueId().equals(mixinEntity.getLocation().getExtent().getUniqueId())) {
                // update cache
                ((IMixinTeleporter) teleporter).removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
                mixinEntity.setLocationAndAngles(fromTransform);
            } else {
                // Call setTransform to let plugins know mods changed the position
                // Guarantees plugins such as Nucleus can track changed locations properly
                mixinEntity.setTransform(mixinEntity.getTransform());
            }
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
                toMixinTeleporter.removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
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
                BlockPos blockpos = entityIn.world.getTopSolidOrLiquidBlock(toWorld.getSpawnPoint());
                entityIn.moveToBlockPosAndAngles(blockpos, entityIn.rotationYaw, entityIn.rotationPitch);
            }
        }

        // Attempt to create the portal
        if (event.isCancelled()) {
            return null;
        }

        if (!capturedBlocks.isEmpty()
            && !TrackingUtil.processBlockCaptures(capturedBlocks, EntityPhase.State.CHANGING_DIMENSION, context)) {
            toMixinTeleporter.removePortalPositionFromCache(ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ()));
        }

        if (!event.getKeepsVelocity()) {
            entityIn.motionX = 0;
            entityIn.motionY = 0;
            entityIn.motionZ = 0;
        }
        return event;
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

    public static RayTraceResult rayTraceFromEntity(Entity source, double traceDistance, float partialTicks) {
        Vec3d traceStart = EntityUtil.getPositionEyes(source, partialTicks);
        Vec3d lookDir = source.getLook(partialTicks).scale(traceDistance);
        Vec3d traceEnd = traceStart.add(lookDir);
        return source.world.rayTraceBlocks(traceStart, traceEnd, false, false, true);
    }

    public static Vec3d getPositionEyes(Entity entity, float partialTicks)
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
    @SuppressWarnings("unchecked")
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
                    .delayTicks(SpongeImpl.getGlobalConfig().getConfig().getEntity().getPaintingRespawnDelaly())
                    .execute(() -> {
                        final SPacketSpawnPainting packet = new SPacketSpawnPainting(painting);
                        playerMP.connection.sendPacket(packet);
                    })
                    .submit(SpongeImpl.getPlugin());
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
    public static Location<World> getPlayerRespawnLocation(EntityPlayerMP playerIn, @Nullable WorldServer targetWorld) {
        final Location<World> location = ((World) playerIn.world).getSpawnLocation();
        tempIsBedSpawn = false;
        if (targetWorld == null) { // Target world doesn't exist? Use global
            return location;
        }

        final Dimension targetDimension = (Dimension) targetWorld.provider;
        int targetDimensionId = ((IMixinWorldServer) targetWorld).getDimensionId();
        // Cannot respawn in requested world, use the fallback dimension for
        // that world. (Usually overworld unless a mod says otherwise).
        if (!targetDimension.allowsPlayerRespawns()) {
            targetDimensionId = SpongeImplHooks.getRespawnDimension((WorldProvider) targetDimension, playerIn);
            targetWorld = targetWorld.getMinecraftServer().getWorld(targetDimensionId);
        }

        Vector3d targetSpawnVec = VecHelper.toVector3d(targetWorld.getSpawnPoint());
        BlockPos bedPos = ((IMixinEntityPlayer) playerIn).getBedLocation(targetDimensionId);
        if (bedPos != null) { // Player has a bed
            boolean forceBedSpawn = ((IMixinEntityPlayer) playerIn).isSpawnForced(targetDimensionId);
            BlockPos bedSpawnLoc = EntityPlayer.getBedSpawnLocation(targetWorld, bedPos, forceBedSpawn);
            if (bedSpawnLoc != null) { // The bed exists and is not obstructed
                tempIsBedSpawn = true;
                targetSpawnVec = new Vector3d(bedSpawnLoc.getX() + 0.5D, bedSpawnLoc.getY() + 0.1D, bedSpawnLoc.getZ() + 0.5D);
            } else { // Bed invalid
                playerIn.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
                // Vanilla behaviour - Delete the known bed location if invalid
                bedPos = null; // null = remove location
            }
            // Set the new bed location for the new dimension
            int prevDim = playerIn.dimension; // Temporarily for setSpawnPoint
            playerIn.dimension = targetDimensionId;
            playerIn.setSpawnPoint(bedPos, forceBedSpawn);
            playerIn.dimension = prevDim;
        }
        return new Location<>((World) targetWorld, targetSpawnVec);
    }

    public static Entity toNative(org.spongepowered.api.entity.Entity tickingEntity) {
        if (!(tickingEntity instanceof Entity)) {
            throw new IllegalArgumentException("Not a native Entity for this implementation!");
        }
        return (Entity) tickingEntity;
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

    public static org.spongepowered.api.entity.Entity fromMixin(IMixinEntity mixinEntity) {
        if (!(mixinEntity instanceof org.spongepowered.api.entity.Entity)) {
            throw new IllegalArgumentException("Not a native SpongeAPI entity!");
        }
        return (org.spongepowered.api.entity.Entity) mixinEntity;
    }
    public static EntitySnapshot createSnapshot(Entity entity) {
        return fromNative(entity).createSnapshot();
    }

    public static boolean changeWorld(net.minecraft.entity.Entity entity, Location<World> location, int currentDim, int targetDim) {
        final MinecraftServer mcServer = SpongeImpl.getServer();
        final WorldServer fromWorld = mcServer.getWorld(currentDim);
        final WorldServer toWorld = mcServer.getWorld(targetDim);
        if (entity instanceof EntityPlayer) {
            fromWorld.getEntityTracker().removePlayerFromTrackers((EntityPlayerMP) entity);
            fromWorld.getPlayerChunkMap().removePlayer((EntityPlayerMP) entity);
            mcServer.getPlayerList().getPlayers().remove(entity);
        } else {
            fromWorld.getEntityTracker().untrack(entity);
        }

        entity.world.removeEntityDangerously(entity);
        entity.isDead = false;
        entity.dimension = targetDim;
        entity.setPositionAndRotation(location.getX(), location.getY(), location.getZ(), 0, 0);
        while (!toWorld.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && entity.posY < 256.0D) {
            entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
        }

        toWorld.getChunkProvider().loadChunk((int) entity.posX >> 4, (int) entity.posZ >> 4);

        if (entity instanceof EntityPlayerMP && ((EntityPlayerMP) entity).connection != null) {
            EntityPlayerMP entityPlayerMP = (EntityPlayerMP) entity;
            // Support vanilla clients going into custom dimensions
            final int toDimensionId = WorldManager.getClientDimensionId(entityPlayerMP, toWorld);
            if (((IMixinEntityPlayerMP) entityPlayerMP).usesCustomClient()) {
                WorldManager.sendDimensionRegistration(entityPlayerMP, toWorld.provider);
            } else {
                // Force vanilla client to refresh its chunk cache if same dimension type
                if (fromWorld != toWorld && fromWorld.provider.getDimensionType() == toWorld.provider.getDimensionType()) {
                    entityPlayerMP.connection.sendPacket(
                            new SPacketRespawn(toDimensionId >= 0 ? -1 : 0, toWorld.getDifficulty(),
                                    toWorld.getWorldInfo().getTerrainType(), entityPlayerMP.interactionManager.getGameType()));
                }
            }

            // Prevent 'lastMoveLocation' from being set to the previous world.
            ((IMixinNetHandlerPlayServer) entityPlayerMP.connection).setLastMoveLocation(null);

            entityPlayerMP.connection.sendPacket(
                    new SPacketRespawn(toDimensionId, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(),
                            entityPlayerMP.interactionManager.getGameType()));
            entity.setWorld(toWorld);
            entityPlayerMP.connection.setPlayerLocation(entityPlayerMP.posX, entityPlayerMP.posY, entityPlayerMP.posZ,
                    entityPlayerMP.rotationYaw, entityPlayerMP.rotationPitch);
            entityPlayerMP.setSneaking(false);
            mcServer.getPlayerList().updateTimeAndWeatherForPlayer(entityPlayerMP, toWorld);
            toWorld.getPlayerChunkMap().addPlayer(entityPlayerMP);
            toWorld.spawnEntity(entityPlayerMP);
            mcServer.getPlayerList().getPlayers().add(entityPlayerMP);
            entityPlayerMP.interactionManager.setWorld(toWorld);
            entityPlayerMP.addSelfToInternalCraftingInventory();
            ((IMixinEntityPlayerMP) entityPlayerMP).refreshXpHealthAndFood();
            for (Object effect : entityPlayerMP.getActivePotionEffects()) {
                entityPlayerMP.connection.sendPacket(new SPacketEntityEffect(entityPlayerMP.getEntityId(), (PotionEffect) effect));
            }
            entityPlayerMP.sendPlayerAbilities();
        } else {
            entity.setWorld(toWorld);
            toWorld.spawnEntity(entity);
        }

        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
        return true;
    }

    public static void adjustEntityPostionForTeleport(IMixinPlayerList playerList, Entity entity, WorldServer fromWorld, WorldServer toWorld) {
        fromWorld.profiler.startSection("moving");
        WorldProvider pOld = fromWorld.provider;
        WorldProvider pNew = toWorld.provider;
        double moveFactor = playerList.getMovementFactor(pOld) / playerList.getMovementFactor(pNew);
        double x = entity.posX * moveFactor;
        double y = entity.posY;
        double z = entity.posZ * moveFactor;

//        if (!(pNew instanceof WorldProviderEnd)) {
//            x = MathHelper.clamp_double(x, toWorldIn.getWorldBorder().minX() + 16.0D, toWorldIn.getWorldBorder().maxX() - 16.0D);
//            z = MathHelper.clamp_double(z, toWorldIn.getWorldBorder().minZ() + 16.0D, toWorldIn.getWorldBorder().maxZ() - 16.0D);
//            entityIn.setLocationAndAngles(x, entityIn.posY, z, entityIn.rotationYaw, entityIn.rotationPitch);
//        }

        if (pNew instanceof WorldProviderEnd) {
            BlockPos blockpos;

            if (pOld instanceof WorldProviderEnd) {
                blockpos = toWorld.getSpawnPoint();
            } else {
                blockpos = toWorld.getSpawnCoordinate();
            }

            x = (double)blockpos.getX();
            y = (double)blockpos.getY();
            z = (double)blockpos.getZ();
            entity.setLocationAndAngles(x, y, z, 90.0F, 0.0F);
        }

        if (!(pOld instanceof WorldProviderEnd)) {
            fromWorld.profiler.startSection("placing");
            x = (double) MathHelper.clamp((int)x, -29999872, 29999872);
            z = (double)MathHelper.clamp((int)z, -29999872, 29999872);

            if (entity.isEntityAlive()) {
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
            fromWorld.profiler.endSection();
        }

        if (entity.isEntityAlive()) {
            fromWorld.updateEntityWithOptionalForce(entity, false);
        }

        fromWorld.profiler.endSection();
    }

    /**
     * A simple redirected static util method for {@link Entity#entityDropItem(ItemStack, float)}
     * for easy debugging.
     * @param entity
     * @param itemStack
     * @param offsetY
     * @return
     */
    public static EntityItem entityOnDropItem(Entity entity, ItemStack itemStack, float offsetY) {
        final IMixinEntity mixinEntity = EntityUtil.toMixin(entity);

        // Now the real fun begins.
        final ItemStack item;
        final double posX = entity.posX;
        final double posY = entity.posY + offsetY;
        final double posZ = entity.posZ;
        if (itemStack.isEmpty()) {
            return null;
        }
        // FIRST we want to throw the DropItemEvent.PRE
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);
        final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(Cause.of(NamedCause.source(entity)),
                ImmutableList.of(snapshot), original);
        SpongeImpl.postEvent(dropEvent);
        if (dropEvent.isCancelled()) {
            return null;
        }

        // SECOND throw the ConstructEntityEvent
        Transform<World> suggested = new Transform<>(mixinEntity.getWorld(), new Vector3d(posX, entity.posY + (double) offsetY, posZ));
        SpawnCause cause = EntitySpawnCause.builder().entity(mixinEntity).type(SpawnTypes.DROPPED_ITEM).build();
        ConstructEntityEvent.Pre event = SpongeEventFactory
                .createConstructEntityEventPre(Cause.of(NamedCause.source(cause)), EntityTypes.ITEM, suggested);
        SpongeImpl.postEvent(event);
        item = event.isCancelled() ? null : ItemStackUtil.fromSnapshotToNative(dropEvent.getDroppedItems().get(0));
        if (item == null) {
            return null;
        }
        final PhaseData peek = CauseTracker.getInstance().getCurrentPhaseData();
        final IPhaseState currentState = peek.state;
        final PhaseContext phaseContext = peek.context;

        if (!item.isEmpty()) {
            if (CauseTracker.ENABLED && !currentState.getPhase().ignoresItemPreMerging(currentState) && SpongeImpl.getGlobalConfig().getConfig().getOptimizations().doDropsPreMergeItemDrops()) {
                if (currentState.tracksEntitySpecificDrops()) {
                    final Multimap<UUID, ItemDropData> multimap = phaseContext.getCapturedEntityDropSupplier().get();
                    final Collection<ItemDropData> itemStacks = multimap.get(entity.getUniqueID());
                    SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.item(item)
                            .position(new Vector3d(posX, posY, posZ))
                            .build());
                    return null;
                } else {
                    final List<ItemDropData> itemStacks = phaseContext.getCapturedItemStackSupplier().get();
                    SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.item(item)
                            .position(new Vector3d(posX, posY, posZ))
                            .build());
                    return null;
                }
            }
            EntityItem entityitem = new EntityItem(entity.world, posX, posY, posZ, item);
            entityitem.setDefaultPickupDelay();

            // FIFTH - Capture the entity maybe?
            if (CauseTracker.ENABLED && currentState.getPhase().doesCaptureEntityDrops(currentState)) {
                if (currentState.tracksEntitySpecificDrops()) {
                    // We are capturing per entity drop
                    phaseContext.getCapturedEntityItemDropSupplier().get().put(entity.getUniqueID(), entityitem);
                } else {
                    // We are adding to a general list - usually for EntityPhase.State.DEATH
                    phaseContext.getCapturedItemsSupplier().get().add(entityitem);
                }
                // Return the item, even if it wasn't spawned in the world.
                return entityitem;
            }
            // FINALLY - Spawn the entity in the world if all else didn't fail
            entity.world.spawnEntity(entityitem);
            return entityitem;
        }
        return null;
    }

    @Nullable
    public static EntityItem playerDropItem(IMixinEntityPlayer mixinPlayer, ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        final EntityPlayer player = EntityUtil.toNative(mixinPlayer);

        final double posX = player.posX;
        final double adjustedPosY = player.posY - 0.30000001192092896D + (double) player.getEyeHeight();
        final double posZ = player.posZ;
        // Now the real fun begins.
        final ItemStack item;

        // FIRST we want to throw the DropItemEvent.PRE
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(droppedItem);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);
        final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(Cause.of(NamedCause.source(player)),
                ImmutableList.of(snapshot), original);
        SpongeImpl.postEvent(dropEvent);
        if (dropEvent.isCancelled()) {
            return null;
        }

        // SECOND throw the ConstructEntityEvent
        Transform<World> suggested = new Transform<>(mixinPlayer.getWorld(), new Vector3d(posX, adjustedPosY, posZ));
        SpawnCause cause = EntitySpawnCause.builder().entity(mixinPlayer).type(SpawnTypes.DROPPED_ITEM).build();
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)), EntityTypes.ITEM, suggested);
        SpongeImpl.postEvent(event);
        item = event.isCancelled() ? null : ItemStackUtil.fromSnapshotToNative(dropEvent.getDroppedItems().get(0));
        if (item == null) {
            return null;
        }
        final PhaseData peek = CauseTracker.getInstance().getCurrentPhaseData();
        final IPhaseState currentState = peek.state;
        final PhaseContext phaseContext = peek.context;

        if (CauseTracker.ENABLED && !currentState.getPhase().ignoresItemPreMerging(currentState) && SpongeImpl.getGlobalConfig().getConfig().getOptimizations().doDropsPreMergeItemDrops()) {
            if (currentState.tracksEntitySpecificDrops()) {
                final Multimap<UUID, ItemDropData> multimap = phaseContext.getCapturedEntityDropSupplier().get();
                final Collection<ItemDropData> itemStacks = multimap.get(player.getUniqueID());
                SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.Player.player(player)
                        .stack(item)
                        .trace(traceItem)
                        .motion(createDropMotion(dropAround, player, mixinPlayer.getRandom()))
                        .dropAround(dropAround)
                        .position(new Vector3d(posX, adjustedPosY, posZ))
                        .build());
                return null;
            } else {
                final List<ItemDropData> itemStacks = phaseContext.getCapturedItemStackSupplier().get();
                SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.Player.player(player)
                        .stack(item)
                        .trace(traceItem)
                        .motion(createDropMotion(dropAround, player, mixinPlayer.getRandom()))
                        .dropAround(dropAround)
                        .position(new Vector3d(posX, adjustedPosY, posZ))
                        .build());
                return null;
            }
        }

        EntityItem entityitem = new EntityItem(player.world, posX, adjustedPosY, posZ, droppedItem);
        entityitem.setPickupDelay(40);

        if (traceItem) {
            entityitem.setThrower(player.getName());
        }

        final Random random = mixinPlayer.getRandom();
        if (dropAround) {
            float f = random.nextFloat() * 0.5F;
            float f1 = random.nextFloat() * ((float) Math.PI * 2F);
            entityitem.motionX = (double) (-MathHelper.sin(f1) * f);
            entityitem.motionZ = (double) (MathHelper.cos(f1) * f);
            entityitem.motionY = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            entityitem.motionX = (double) (-MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2);
            entityitem.motionZ = (double) (MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2);
            entityitem.motionY = (double) ( - MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F);
            float f3 = random.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * random.nextFloat();
            entityitem.motionX += Math.cos((double) f3) * (double) f2;
            entityitem.motionY += (double) ((random.nextFloat() - random.nextFloat()) * 0.1F);
            entityitem.motionZ += Math.sin((double) f3) * (double) f2;
        }
        // FIFTH - Capture the entity maybe?
        if (CauseTracker.ENABLED && currentState.getPhase().doesCaptureEntityDrops(currentState)) {
            if (currentState.tracksEntitySpecificDrops()) {
                // We are capturing per entity drop
                phaseContext.getCapturedEntityItemDropSupplier().get().put(player.getUniqueID(), entityitem);
            } else {
                // We are adding to a general list - usually for EntityPhase.State.DEATH
                phaseContext.getCapturedItemsSupplier().get().add(entityitem);
            }
            // Return the item, even if it wasn't spawned in the world.
            return entityitem;
        }
        ItemStack itemstack = dropItemAndGetStack(player, entityitem);

        if (traceItem) {
            if (!itemstack.isEmpty()) {
                player.addStat(StatList.getDroppedObjectStats(itemstack.getItem()), droppedItem.getCount());
            }

            player.addStat(StatList.DROP);
        }

        return entityitem;
    }

    private static Vector3d createDropMotion(boolean dropAround, EntityPlayer player, Random random) {
        double x;
        double y;
        double z;
        if (dropAround) {
            float f = random.nextFloat() * 0.5F;
            float f1 = random.nextFloat() * ((float) Math.PI * 2F);
            x = (double) (-MathHelper.sin(f1) * f);
            z = (double) (MathHelper.cos(f1) * f);
            y = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            x = (double) (-MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2);
            z = (double) (MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2);
            y = (double) ( - MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F);
            float f3 = random.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * random.nextFloat();
            x += Math.cos((double) f3) * (double) f2;
            y += (double) ((random.nextFloat() - random.nextFloat()) * 0.1F);
            z += Math.sin((double) f3) * (double) f2;
        }
        return new Vector3d(x, y, z);
    }

    private static ItemStack dropItemAndGetStack(EntityPlayer player, EntityItem item) {
        final ItemStack stack = item.getItem();
        if (stack != null) {
            player.world.spawnEntity(item);
            return stack;
        }
        return null;
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

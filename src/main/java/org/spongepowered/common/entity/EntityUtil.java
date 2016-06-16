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
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.ItemDropData;
import org.spongepowered.common.interfaces.IMixinPlayerList;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.MixinEntity;
import org.spongepowered.common.registry.type.entity.ProfessionRegistryModule;
import org.spongepowered.common.world.WorldManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

public final class EntityUtil {

    /**
     * This is a dummy entity that can be used for various mixins where a null
     * check on an entity is required. Note that this entity SHOULD NEVER BE
     * USED FOR OTHER PURPOSES AT ALL.
     */
    public static final Entity USELESS_ENTITY_FOR_MIXINS = new EntityDummy(null);

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
        MoveEntityEvent.Teleport.Portal event = SpongeCommonEventFactory.handleDisplaceEntityPortalEvent(entity, toSuggestedDimension, null);
        if (event == null || event.isCancelled()) {
            return null;
        }

        entity.worldObj.theProfiler.startSection("changeDimension");
        // use the world from event
        WorldServer toWorld = (WorldServer) event.getToTransform().getExtent();
        entity.worldObj.removeEntity(entity);
        entity.isDead = false;
        entity.worldObj.theProfiler.startSection("reposition");
        // Only need to update the entity location here as the portal is handled in SpongeCommonEventFactory
        entity.setLocationAndAngles(event.getToTransform().getPosition().getX(), event.getToTransform().getPosition().getY(), event.getToTransform().getPosition().getZ(), (float) event.getToTransform().getYaw(), (float) event.getToTransform().getPitch());
        entity.worldObj = toWorld;
        toWorld.spawnEntityInWorld(entity);
        toWorld.updateEntityWithOptionalForce(entity, false);
        entity.worldObj.theProfiler.endSection();

        entity.worldObj.theProfiler.endSection();
        entity.worldObj.theProfiler.endSection();
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
    public static Entity teleportPlayerToDimension(IMixinEntityPlayerMP mixinEntityPlayerMP, int suggestedDimensionId) {
        final EntityPlayerMP entityPlayerMP = toNative(mixinEntityPlayerMP);
        // If leaving The End via End's Portal
        // Sponge Start - Check the provider, not the world's dimension
        final WorldServer fromWorldServer = ((WorldServer) entityPlayerMP.worldObj);
        final IMixinWorldServer fromMixinWorldServer = (IMixinWorldServer) fromWorldServer;
        if (fromWorldServer.provider instanceof WorldProviderEnd && suggestedDimensionId == 1) { // if (this.dimension == 1 && dimensionIn == 1)
            // Sponge End
            fromWorldServer.removeEntity(entityPlayerMP);
            if (!entityPlayerMP.playerConqueredTheEnd) {
                entityPlayerMP.playerConqueredTheEnd = true;
                if (entityPlayerMP.hasAchievement(AchievementList.THE_END2)) {
                    entityPlayerMP.connection.sendPacket(new SPacketChangeGameState(4, 0.0F));
                } else {
                    entityPlayerMP.addStat(AchievementList.THE_END2);
                    entityPlayerMP.connection.sendPacket(new SPacketChangeGameState(4, 1.0F));
                }
            }
            return entityPlayerMP;
        } // else { // Sponge - Remove unecessary

        // Sponge Start - Rewrite for vanilla mechanics since multiworlds can change world providers and
        // dimension id's
        if (fromWorldServer.provider instanceof WorldProviderSurface) {
            if (suggestedDimensionId == 1) {
                entityPlayerMP.addStat(AchievementList.THE_END);
            } else if (suggestedDimensionId == -1) {
                entityPlayerMP.addStat(AchievementList.PORTAL);
            }
        }
        // Sponge End

        final WorldServer toWorldServer = SpongeImpl.getServer().worldServerForDimension(suggestedDimensionId);

        ((IMixinPlayerList) entityPlayerMP.mcServer.getPlayerList()).transferPlayerToDimension(entityPlayerMP, suggestedDimensionId, toWorldServer.getDefaultTeleporter());
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
            AxisAlignedBB entityBB = entity.getEntityBoundingBox().expandXyz(entity.getCollisionBorderSize());
            RayTraceResult entityRay = entityBB.calculateIntercept(traceStart, traceEnd);

            if (entityBB.isVecInside(traceStart)) {
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
        AxisAlignedBB traceBox = boundingBox.addCoord(dir.xCoord, dir.yCoord, dir.zCoord);
        List<Entity> entities = source.worldObj.getEntitiesInAABBexcluding(source, traceBox.expand(1.0F, 1.0F, 1.0F), filter);
        return entities;
    }

    public static RayTraceResult rayTraceFromEntity(Entity source, double traceDistance, float partialTicks) {
        Vec3d traceStart = EntityUtil.getPositionEyes(source, partialTicks);
        Vec3d lookDir = source.getLook(partialTicks).scale(traceDistance);
        Vec3d traceEnd = traceStart.add(lookDir);
        return source.worldObj.rayTraceBlocks(traceStart, traceEnd, false, false, true);
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
        final EntityTracker paintingTracker = ((WorldServer) painting.worldObj).getEntityTracker();
        EntityTrackerEntry paintingEntry = paintingTracker.trackedEntityHashTable.lookup(painting.getEntityId());
        List<EntityPlayerMP> playerMPs = new ArrayList<>();
        for (EntityPlayerMP player : paintingEntry.trackingPlayers) {
            SPacketDestroyEntities packet = new SPacketDestroyEntities(painting.getEntityId());
            player.connection.sendPacket(packet);
            playerMPs.add(player);
        }
        painting.art = art;
        painting.updateFacingWithBoundingBox(painting.facingDirection);
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
        return worldIn.getEntitiesWithinAABB(EntityHanging.class, new AxisAlignedBB(pos, pos).expand(1.1D, 1.1D, 1.1D),
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

    public static boolean changeWorld(net.minecraft.entity.Entity entity, Location<World> location, int currentDim, int targetDim) {
        final MinecraftServer mcServer = SpongeImpl.getServer();
        final WorldServer fromWorld = mcServer.worldServerForDimension(currentDim);
        final WorldServer toWorld = mcServer.worldServerForDimension(targetDim);
        if (entity instanceof EntityPlayer) {
            fromWorld.getEntityTracker().removePlayerFromTrackers((EntityPlayerMP) entity);
            fromWorld.getPlayerChunkMap().removePlayer((EntityPlayerMP) entity);
            mcServer.getPlayerList().getPlayerList().remove(entity);
        } else {
            fromWorld.getEntityTracker().untrackEntity(entity);
        }

        entity.worldObj.removeEntityDangerously(entity);
        entity.isDead = false;
        entity.dimension = targetDim;
        entity.setPositionAndRotation(location.getX(), location.getY(), location.getZ(), 0, 0);
        while (!toWorld.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && entity.posY < 256.0D) {
            entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
        }

        toWorld.getChunkProvider().loadChunk((int) entity.posX >> 4, (int) entity.posZ >> 4);

        if (entity instanceof EntityPlayerMP && ((EntityPlayerMP) entity).connection != null) {
            EntityPlayerMP entityplayermp1 = (EntityPlayerMP) entity;
            final IMixinWorldServer toMixinWorld = (IMixinWorldServer) toWorld;
            final IMixinWorldServer fromMixinWorld = (IMixinWorldServer) fromWorld;
            // Support vanilla clients going into custom dimensions
            final DimensionType fromClientDimensionType = WorldManager.getClientDimensionType(fromWorld.provider.getDimensionType());
            final DimensionType toClientDimensionType = WorldManager.getClientDimensionType(toWorld.provider.getDimensionType());
            // Support vanilla clients going into custom dimensions
            final Integer worldDimensionId = ((IMixinWorldServer) toWorld).getDimensionId();
            if (((IMixinEntityPlayerMP) entityplayermp1).usesCustomClient()) {
                WorldManager.sendDimensionRegistration(entityplayermp1, toWorld.provider);
            } else {
                final int fromClientDimensionTypeId = fromClientDimensionType.getId();
                final int toClientDimensionTypeId = toClientDimensionType.getId();
                // Force vanilla client to refresh their chunk cache if same dimension
                if (currentDim != targetDim && fromClientDimensionTypeId == toClientDimensionTypeId) {
                    entityplayermp1.connection.sendPacket(
                            new SPacketRespawn(toClientDimensionTypeId >= 0 ? -1 : 0, toWorld.getDifficulty(),
                                    toWorld.getWorldInfo().getTerrainType(), entityplayermp1.interactionManager.getGameType()));
                }
            }

            entityplayermp1.connection.sendPacket(
                    new SPacketRespawn(toClientDimensionType.getId(), toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(),
                            entityplayermp1.interactionManager.getGameType()));
            entity.setWorld(toWorld);
            entityplayermp1.connection.setPlayerLocation(entityplayermp1.posX, entityplayermp1.posY, entityplayermp1.posZ,
                    entityplayermp1.rotationYaw, entityplayermp1.rotationPitch);
            entityplayermp1.setSneaking(false);
            mcServer.getPlayerList().updateTimeAndWeatherForPlayer(entityplayermp1, toWorld);
            toWorld.getPlayerChunkMap().addPlayer(entityplayermp1);
            toWorld.spawnEntityInWorld(entityplayermp1);
            mcServer.getPlayerList().getPlayerList().add(entityplayermp1);
            entityplayermp1.interactionManager.setWorld(toWorld);
            entityplayermp1.addSelfToInternalCraftingInventory();
            entityplayermp1.setHealth(entityplayermp1.getHealth());
            for (Object effect : entityplayermp1.getActivePotionEffects()) {
                entityplayermp1.connection.sendPacket(new SPacketEntityEffect(entityplayermp1.getEntityId(), (PotionEffect) effect));
            }
        } else {
            entity.setWorld(toWorld);
            toWorld.spawnEntityInWorld(entity);
        }

        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
        return true;
    }

    public static void adjustEntityPostionForTeleport(IMixinPlayerList playerList, Entity entity, WorldServer fromWorld, WorldServer toWorld) {
        fromWorld.theProfiler.startSection("moving");
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
            fromWorld.theProfiler.startSection("placing");
            x = (double) MathHelper.clamp_int((int)x, -29999872, 29999872);
            z = (double)MathHelper.clamp_int((int)z, -29999872, 29999872);

            if (entity.isEntityAlive()) {
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
            fromWorld.theProfiler.endSection();
        }

        if (entity.isEntityAlive()) {
            fromWorld.updateEntityWithOptionalForce(entity, false);
        }

        fromWorld.theProfiler.endSection();
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
        if (itemStack.getItem() != null) {
            // FIRST we want to throw the DropItemEvent.PRE
            final ItemStackSnapshot snapshot = ItemStackUtil.createSnapshot(itemStack);
            final List<ItemStackSnapshot> original = new ArrayList<>();
            original.add(snapshot);
            final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(Cause.of(NamedCause.source(entity)),
                    ImmutableList.of(snapshot), original);
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
        } else {
            return null;
        }
        if (item == null) {
            return null;
        }
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) entity.worldObj;
        final PhaseData peek = mixinWorldServer.getCauseTracker().getStack().peek();
        final IPhaseState currentState = peek.getState();
        final PhaseContext phaseContext = peek.getContext();

        if (item.stackSize != 0 && item.getItem() != null) {
            if (!currentState.getPhase().ignoresItemPreMerging(currentState) && SpongeImpl.getGlobalConfig().getConfig().getOptimizations().doDropsPreMergeItemDrops()) {
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
            EntityItem entityitem = new EntityItem(entity.worldObj, posX, posY, posZ, itemStack);
            entityitem.setDefaultPickupDelay();

            // FIFTH - Capture the entity maybe?
            if (currentState.getPhase().doesCaptureEntityDrops(currentState)) {
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
            entity.worldObj.spawnEntityInWorld(entityitem);
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
        if (droppedItem.getItem() != null) {
            // FIRST we want to throw the DropItemEvent.PRE
            final ItemStackSnapshot snapshot = ItemStackUtil.createSnapshot(droppedItem);
            final List<ItemStackSnapshot> original = new ArrayList<>();
            original.add(snapshot);
            final DropItemEvent.Pre dropEvent = SpongeEventFactory.createDropItemEventPre(Cause.of(NamedCause.source(player)),
                    ImmutableList.of(snapshot), original);
            if (dropEvent.isCancelled()) {
                return null;
            }

            // SECOND throw the ConstructEntityEvent
            Transform<World> suggested = new Transform<>(mixinPlayer.getWorld(), new Vector3d(posX, adjustedPosY, posZ));
            SpawnCause cause = EntitySpawnCause.builder().entity(mixinPlayer).type(SpawnTypes.DROPPED_ITEM).build();
            ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)), EntityTypes.ITEM, suggested);
            SpongeImpl.postEvent(event);
            item = event.isCancelled() ? null : ItemStackUtil.fromSnapshotToNative(dropEvent.getDroppedItems().get(0));
        } else {
            return null;
        }
        if (item == null) {
            return null;
        }
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) player.worldObj;
        final PhaseData peek = mixinWorldServer.getCauseTracker().getStack().peek();
        final IPhaseState currentState = peek.getState();
        final PhaseContext phaseContext = peek.getContext();

        if (!currentState.getPhase().ignoresItemPreMerging(currentState) && SpongeImpl.getGlobalConfig().getConfig().getOptimizations().doDropsPreMergeItemDrops()) {
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

        EntityItem entityitem = new EntityItem(player.worldObj, posX, adjustedPosY, posZ, droppedItem);
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
        if (currentState.getPhase().doesCaptureEntityDrops(currentState)) {
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
            if (itemstack != null) {
                player.addStat(StatList.getDroppedObjectStats(itemstack.getItem()), droppedItem.stackSize);
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
        final ItemStack stack = item.getEntityItem();
        if (stack != null) {
            player.worldObj.spawnEntityInWorld(item);
            return stack;
        }
        return null;
    }
}

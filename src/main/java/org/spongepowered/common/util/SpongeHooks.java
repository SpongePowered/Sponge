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
package org.spongepowered.common.util;

import com.flowpowered.math.vector.Vector3i;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.LoggingCategory;
import org.spongepowered.common.config.type.DimensionConfig;
import org.spongepowered.common.config.type.GeneralConfigBase;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.interfaces.IMixinTrackable;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.IModData_Collisions;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.teleport.ConfigTeleportHelperFilter;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.management.MBeanServer;

public class SpongeHooks {

    private static Object2LongMap<CollisionWarning> recentWarnings = new Object2LongOpenHashMap<>();

    public static void logInfo(String msg, Object... args) {
        SpongeImpl.getLogger().info(MessageFormat.format(msg, args));
    }

    public static void logWarning(String msg, Object... args) {
        SpongeImpl.getLogger().warn(MessageFormat.format(msg, args));
    }

    public static void logSevere(String msg, Object... args) {
        SpongeImpl.getLogger().fatal(MessageFormat.format(msg, args));
    }

    public static void logStack(SpongeConfig<? extends GeneralConfigBase> config) {
        if (config.getConfig().getLogging().logWithStackTraces()) {
            Throwable ex = new Throwable();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
    }

    public static void logEntityDeath(Entity entity) {
        if (entity == null || entity.world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) entity.world);
        if (config.getConfig().getLogging().entityDeathLogging()) {
            logInfo("Dim: {0} setDead(): {1}", ((IMixinWorldServer) entity.world).getDimensionId(), entity);
            logStack(config);
        }
    }

    public static void logEntityDespawn(Entity entity, String reason) {
        if (entity == null || entity.world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) entity.world);
        if (config.getConfig().getLogging().entityDespawnLogging()) {
            logInfo("Dim: {0} Despawning ({1}): {2}", ((IMixinWorldServer) entity.world).getDimensionId(), reason, entity);
            logStack(config);
        }
    }

    public static void logEntitySpawn(Cause cause, Entity entity) {
        if (entity == null || entity.world.isRemote) {
            return;
        }

        String spawnName = entity.getName();
        if (entity instanceof EntityItem) {
            spawnName = ((EntityItem) entity).getItem().getDisplayName();
        }

        Optional<User> user = cause.first(User.class);
        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) entity.world);
        if (config.getConfig().getLogging().entitySpawnLogging()) {
            logInfo("SPAWNED " + spawnName + " [RootCause: {0}][User: {1}][World: {2}][DimId: {3}]",
                    getFriendlyCauseName(cause),
                    user.isPresent() ? user.get().getName() : "None",
                    entity.world.getWorldInfo().getWorldName(),
                    ((IMixinWorldServer) entity.world).getDimensionId());
            logStack(config);
        }
    }

    public static void logBlockTrack(World world, Block block, BlockPos pos, User user, boolean allowed) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) world);
        if (config.getConfig().getLogging().blockTrackLogging() && allowed) {
            logInfo("Tracking Block " + "[RootCause: {0}][World: {1}][Block: {2}][Pos: {3}]",
                    user.getName(),
                    world.getWorldInfo().getWorldName() + "(" + ((IMixinWorldServer) world).getDimensionId() + ")",
                    ((BlockType) block).getId(),
                    pos);
            logStack(config);
        } else if (config.getConfig().getLogging().blockTrackLogging() && !allowed) {
            logInfo("Blacklisted! Unable to track Block " + "[RootCause: {0}][World: {1}][DimId: {2}][Block: {3}][Pos: {4}]",
                    user.getName(),
                    world.getWorldInfo().getWorldName(),
                    ((IMixinWorldServer) world).getDimensionId(),
                    ((BlockType) block).getId(),
                    pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
    }

    public static void logBlockAction(World world, @Nullable BlockChange type, Transaction<BlockSnapshot> transaction) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) world);
        Optional<User> user = Sponge.getCauseStackManager().getCurrentCause().first(User.class);
        LoggingCategory logging = config.getConfig().getLogging();
        if (type != null && type.allowsLogging(logging)) {
            logInfo("Block " + type.name() + " [RootCause: {0}][User: {1}][World: {2}][DimId: {3}][OriginalState: {4}][NewState: {5}]",
                    getFriendlyCauseName(Sponge.getCauseStackManager().getCurrentCause()),
                    user.isPresent() ? user.get().getName() : "None",
                    world.getWorldInfo().getWorldName(),
                    ((IMixinWorldServer) world).getDimensionId(),
                    transaction.getOriginal().getState(),
                    transaction.getFinal().getState());
            logStack(config);
        }
    }

    public static void logChunkLoad(World world, Vector3i chunkPos) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) world);
        if (config.getConfig().getLogging().chunkLoadLogging()) {
            logInfo("Load Chunk At [{0}] ({1}, {2})", ((IMixinWorldServer) world).getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(config);
        }
    }

    public static void logChunkUnload(World world, Vector3i chunkPos) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) world);
        if (config.getConfig().getLogging().chunkUnloadLogging()) {
            logInfo("Unload Chunk At [{0}] ({1}, {2})", ((IMixinWorldServer) world).getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(config);
        }
    }

    public static void logChunkGCQueueUnload(WorldServer world, Chunk chunk) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig(world);
        if (config.getConfig().getLogging().chunkGCQueueUnloadLogging()) {
            logInfo("Chunk GC Queued Chunk At [{0}] ({1}, {2} for unload)", ((IMixinWorldServer) world).getDimensionId(), chunk.x, chunk.z);
            logStack(config);
        }
    }

    public static void logExploitSignCommandUpdates(EntityPlayer player, TileEntity te, String command) {
        if (player.world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) player.world);
        if (config.getConfig().getLogging().logExploitSignCommandUpdates) {
            logInfo("[EXPLOIT] Player ''{0}'' attempted to exploit sign in world ''{1}'' located at ''{2}'' with command ''{3}''",
                    player.getName(),
                    te.getWorld().getWorldInfo().getWorldName(),
                    te.getPos().getX() + ", " + te.getPos().getY() + ", " + te.getPos().getZ(),
                    command);
            logStack(config);
        }
    }

    public static void logExploitItemNameOverflow(EntityPlayer player, int length) {
        if (player.world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) player.world);
        if (config.getConfig().getLogging().logExploitItemStackNameOverflow) {
            logInfo("[EXPLOIT] Player ''{0}'' attempted to send a creative itemstack update with a display name length of ''{1}'' (Max allowed length is 32767). This has been blocked to avoid server overflow.",
                    player.getName(),
                    length);
            logStack(config);
        }
    }

    public static void logExploitRespawnInvisibility(EntityPlayer player) {
        if (player.world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) player.world);
        if (config.getConfig().getLogging().logExploitRespawnInvisibility) {
            logInfo("[EXPLOIT] Player ''{0}'' attempted to perform a respawn invisibility exploit to surrounding players.",
                    player.getName());
            logStack(config);
        }
    }

    public static boolean checkBoundingBoxSize(Entity entity, AxisAlignedBB aabb) {
        if (entity == null || entity.world.isRemote) {
            return false;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) entity.world);
        if (!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer || entity instanceof IEntityMultiPart) {
            return false; // only check living entities, so long as they are not a player or multipart entity
        }

        int maxBoundingBoxSize = config.getConfig().getEntity().getMaxBoundingBoxSize();
        if (maxBoundingBoxSize <= 0) {
            return false;
        }
        int x = MathHelper.floor(aabb.minX);
        int x1 = MathHelper.floor(aabb.maxX + 1.0D);
        int y = MathHelper.floor(aabb.minY);
        int y1 = MathHelper.floor(aabb.maxY + 1.0D);
        int z = MathHelper.floor(aabb.minZ);
        int z1 = MathHelper.floor(aabb.maxZ + 1.0D);

        int size = Math.abs(x1 - x) * Math.abs(y1 - y) * Math.abs(z1 - z);
        if (size > maxBoundingBoxSize) {
            logWarning("Entity being removed for bounding box restrictions");
            logWarning("BB Size: {0} > {1} avg edge: {2}", size, maxBoundingBoxSize, aabb.getAverageEdgeLength());
            logWarning("Motion: ({0}, {1}, {2})", entity.motionX, entity.motionY, entity.motionZ);
            logWarning("Calculated bounding box: {0}", aabb);
            logWarning("Entity bounding box: {0}", entity.getCollisionBoundingBox());
            logWarning("Entity: {0}", entity);
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBT(tag);
            logWarning("Entity NBT: {0}", tag);
            logStack(config);
            entity.setDead();
            return true;
        }
        return false;
    }

    public static boolean checkEntitySpeed(Entity entity, double x, double y, double z) {
        if (entity == null || entity.world.isRemote) {
            return false;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) entity.world);
        int maxSpeed = config.getConfig().getEntity().getMaxSpeed();
        if (maxSpeed > 0) {
            double distance = x * x + z * z;
            if (distance > maxSpeed && !entity.isRiding()) {
                if (config.getConfig().getLogging().logEntitySpeedRemoval()) {
                    logInfo("Speed violation: {0} was over {1} - Removing Entity: {2}", distance, maxSpeed, entity);
                    if (entity instanceof EntityLivingBase) {
                        EntityLivingBase livingBase = (EntityLivingBase) entity;
                        logInfo("Entity Motion: ({0}, {1}, {2}) Move Strafing: {3} Move Forward: {4}",
                                entity.motionX, entity.motionY,
                                entity.motionZ,
                                livingBase.moveStrafing, livingBase.moveForward);
                    }

                    if (config.getConfig().getLogging().logWithStackTraces()) {
                        logInfo("Move offset: ({0}, {1}, {2})", x, y, z);
                        logInfo("Motion: ({0}, {1}, {2})", entity.motionX, entity.motionY, entity.motionZ);
                        logInfo("Entity: {0}", entity);
                        NBTTagCompound tag = new NBTTagCompound();
                        entity.writeToNBT(tag);
                        logInfo("Entity NBT: {0}", tag);
                        logStack(config);
                    }
                }
                if (entity instanceof EntityPlayer) { // Skip killing players
                    entity.motionX = 0;
                    entity.motionY = 0;
                    entity.motionZ = 0;
                    return false;
                }
                // Remove the entity;
                entity.isDead = true;
                return false;
            }
        }
        return true;
    }

    // TODO - needs to be hooked
    @SuppressWarnings("rawtypes")
    public static void logEntitySize(Entity entity, List list) {
        if (entity == null || entity.world.isRemote) {
            return;
        }

        SpongeConfig<? extends GeneralConfigBase> config = getActiveConfig((WorldServer) entity.world);
        if (!config.getConfig().getLogging().logEntityCollisionChecks()) {
            return;
        }
        int collisionWarnSize = config.getConfig().getEntity().getMaxCollisionSize();

        if (list == null) {
            return;
        }

        if (collisionWarnSize > 0 && (entity.getEntityWorld().getMinecraftServer().getTickCounter() % 10) == 0 && list.size() >= collisionWarnSize) {
            SpongeHooks.CollisionWarning warning = new SpongeHooks.CollisionWarning(entity.world, entity);
            if (SpongeHooks.recentWarnings.containsKey(warning)) {
                long lastWarned = SpongeHooks.recentWarnings.get(warning);
                if ((MinecraftServer.getCurrentTimeMillis() - lastWarned) < 30000) {
                    return;
                }
            }
            SpongeHooks.recentWarnings.put(warning, System.currentTimeMillis());
            logWarning("Entity collision > {0, number} at: {1}", collisionWarnSize, entity);
        }
    }

    private static class CollisionWarning {

        public BlockPos blockPos;
        public int dimensionId;

        public CollisionWarning(World world, Entity entity) {
            this.dimensionId = ((IMixinWorldServer) world).getDimensionId();
            this.blockPos = new BlockPos(entity.chunkCoordX, entity.chunkCoordY, entity.chunkCoordZ);
        }

        @Override
        public boolean equals(Object otherObj) {
            if (!(otherObj instanceof CollisionWarning) || (otherObj == null)) {
                return false;
            }
            CollisionWarning other = (CollisionWarning) otherObj;
            return (other.dimensionId == this.dimensionId) && other.blockPos.equals(this.blockPos);
        }

        @Override
        public int hashCode() {
            return this.blockPos.hashCode() + this.dimensionId;
        }
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void dumpHeap(File file, boolean live) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            m.invoke(hotspotMBean, file.getPath(), live);
        } catch (Throwable t) {
            logSevere("Could not write heap to {0}", file);
        }
    }

    public static void enableThreadContentionMonitoring() {
        if (!SpongeImpl.getGlobalConfig().getConfig().getDebug().isEnableThreadContentionMonitoring()) {
            return;
        }
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        mbean.setThreadContentionMonitoringEnabled(true);
    }

    public static SpongeConfig<? extends GeneralConfigBase> getActiveConfig(WorldServer world, boolean refresh) {
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) world;
        SpongeConfig<? extends GeneralConfigBase> activeConfig = mixinWorldServer.getActiveConfig();
        if (activeConfig == null || refresh) {
            activeConfig = mixinWorldServer.getWorldConfig();
            mixinWorldServer.setActiveConfig(activeConfig);
        }

        return activeConfig;
    }

    public static SpongeConfig<? extends GeneralConfigBase> getActiveConfig(WorldServer world) {
        return getActiveConfig(world, false);
    }

    public static SpongeConfig<? extends GeneralConfigBase> getActiveConfig(Path dimensionPath, String worldFolder) {
        if (dimensionPath == null) {
            // If no dimension type, go global
            return SpongeImpl.getGlobalConfig();
        }

        if (worldFolder != null) {
            final Optional<org.spongepowered.api.world.World> optWorld = SpongeImpl.getGame().getServer().getWorld(worldFolder);

            // If this is a loaded world then we only return configs on the loaded objects. Don't go to disk.
            if (optWorld.isPresent()) {
                return ((IMixinWorldServer) optWorld.get()).getActiveConfig();
            }
        }

        // No in-memory config objects, lookup from disk.
        final Path dimConfPath = dimensionPath.resolve("dimension.conf");
        final SpongeConfig<DimensionConfig> dimConfig = new SpongeConfig<>(SpongeConfig.Type.DIMENSION, dimConfPath, SpongeImpl.ECOSYSTEM_ID, SpongeImpl.getGlobalConfig());

        if (worldFolder != null) {
            final Path worldConfPath = dimensionPath.resolve(worldFolder).resolve("world.conf");
            return new SpongeConfig<>(SpongeConfig.Type.WORLD, worldConfPath, SpongeImpl.ECOSYSTEM_ID, dimConfig);
        }

        return dimConfig;
    }

    public static void refreshActiveConfigs() {
        for (BlockType blockType : BlockTypeRegistryModule.getInstance().getAll()) {
            if (blockType instanceof IModData_Collisions) {
                ((IModData_Collisions) blockType).requiresCollisionsCacheRefresh(true);
            }
            if (blockType instanceof IMixinTrackable) {
                ((IMixinBlock) blockType).initializeTrackerState();
            }
        }
        for (TileEntityType tileEntityType : TileEntityTypeRegistryModule.getInstance().getAll()) {
            ((SpongeTileEntityType) tileEntityType).initializeTrackerState();
        }
        for (EntityType entityType : EntityTypeRegistryModule.getInstance().getAll()) {
            ((SpongeEntityType) entityType).initializeTrackerState();
        }

        for (WorldServer world : WorldManager.getWorlds()) {
            ((IMixinWorldServer) world).setActiveConfig(SpongeHooks.getActiveConfig(world, true));
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof IModData_Activation) {
                    ((IModData_Activation) entity).requiresActivationCacheRefresh(true);
                }
                if (entity instanceof IModData_Collisions) {
                    ((IModData_Collisions) entity).requiresCollisionsCacheRefresh(true);
                }
                if (entity instanceof IMixinTrackable) {
                    ((IMixinTrackable) entity).refreshCache();
                }
            }
            for (TileEntity tileEntity : world.loadedTileEntityList) {
                if (tileEntity instanceof IModData_Activation) {
                    ((IModData_Activation) tileEntity).requiresActivationCacheRefresh(true);
                }
                if (tileEntity instanceof IMixinTrackable) {
                    ((IMixinTrackable) tileEntity).refreshCache();
                }
            }
        }
        ConfigTeleportHelperFilter.invalidateCache();
    }


    public static String getFriendlyCauseName(Cause cause) {
        String causedBy = "Unknown";
        Object rootCause = cause.root();
        if (rootCause instanceof User) {
            User user = (User) rootCause;
            causedBy = user.getName();
        } else if (rootCause instanceof EntityItem) {
            EntityItem item = (EntityItem) rootCause;
            causedBy = item.getItem().getDisplayName();
        }
        else if (rootCause instanceof Entity) {
            Entity causeEntity = (Entity) rootCause;
            causedBy = causeEntity.getName();
        }else if (rootCause instanceof BlockSnapshot) {
            BlockSnapshot snapshot = (BlockSnapshot) rootCause;
            causedBy = snapshot.getState().getType().getId();
        } else if (rootCause instanceof CatalogType) {
            CatalogType type = (CatalogType) rootCause;
            causedBy = type.getId();
        } else if (rootCause instanceof PluginContainer) {
            PluginContainer plugin = (PluginContainer) rootCause;
            causedBy = plugin.getId();
        } else {
            causedBy = rootCause.getClass().getName();
        }
        return causedBy;
    }
}

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
import com.google.common.base.Predicate;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.SpongeConfig.DimensionConfig;
import org.spongepowered.common.config.SpongeConfig.WorldConfig;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.MinecraftBlockDamageSource;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.mixin.plugin.interfaces.IModData;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.world.DimensionRegistryModule;
import org.spongepowered.common.world.CaptureType;
import org.spongepowered.common.world.DimensionManager;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.management.MBeanServer;

public class SpongeHooks {

    public static int tickingDimension = 0;
    public static ChunkCoordIntPair tickingChunk = null;

    private static TObjectLongHashMap<CollisionWarning> recentWarnings = new TObjectLongHashMap<>();

    public static void logInfo(String msg, Object... args) {
        SpongeImpl.getLogger().info(MessageFormat.format(msg, args));
    }

    public static void logWarning(String msg, Object... args) {
        SpongeImpl.getLogger().warn(MessageFormat.format(msg, args));
    }

    public static void logSevere(String msg, Object... args) {
        SpongeImpl.getLogger().fatal(MessageFormat.format(msg, args));
    }

    public static void logStack(SpongeConfig<?> config) {
        if (config.getConfig().getLogging().logWithStackTraces()) {
            Throwable ex = new Throwable();
            ex.fillInStackTrace();
            ex.printStackTrace();
        }
    }

    public static void logEntityDeath(Entity entity) {
        if (entity == null || entity.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entityDeathLogging()) {
            logInfo("Dim: {0} setDead(): {1}",
                    entity.worldObj.provider.getDimensionId(), entity);
            logStack(config);
        }
    }

    public static void logEntityDespawn(Entity entity, String reason) {
        if (entity == null || entity.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entityDespawnLogging()) {
            logInfo("Dim: {0} Despawning ({1}): {2}", entity.worldObj.provider.getDimensionId(), reason, entity);
            logStack(config);
        }
    }

    public static void logEntitySpawn(Cause cause, Entity entity) {
        if (entity == null || entity.worldObj.isRemote) {
            return;
        }

        String spawnName = entity.getName();
        if (entity instanceof EntityItem) {
            spawnName = ((EntityItem) entity).getEntityItem().getDisplayName();
        }

        Optional<User> user = cause.first(User.class);
        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (config.getConfig().getLogging().entitySpawnLogging()) {
            logInfo("SPAWNED " + spawnName + " [RootCause: {0}][User: {1}][World: {2}][DimId: {3}]",
                    getFriendlyCauseName(cause),
                    user.isPresent() ? user.get().getName() : "None",
                    entity.worldObj.getWorldInfo().getWorldName(),
                    entity.worldObj.provider.getDimensionId());
            logStack(config);
        }
    }

    public static void logBlockTrack(World world, Block block, BlockPos pos, User user, boolean allowed) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(world);
        if (config.getConfig().getLogging().blockTrackLogging() && allowed) {
            logInfo("Tracking Block " + "[RootCause: {0}][World: {1}][Block: {2}][Pos: {3}]",
                    user.getName(),
                    world.getWorldInfo().getWorldName() + "(" + world.provider.getDimensionId() + ")",
                    ((BlockType) block).getId(),
                    pos);
            logStack(config);
        } else if (config.getConfig().getLogging().blockTrackLogging() && !allowed) {
            logInfo("Blacklisted! Unable to track Block " + "[RootCause: {0}][World: {1}][DimId: {2}][Block: {3}][Pos: {4}]",
                    user.getName(),
                    world.getWorldInfo().getWorldName(),
                    world.provider.getDimensionId(),
                    ((BlockType) block).getId(),
                    pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
    }

    public static void logBlockAction(Cause cause, World world, CaptureType type, Transaction<BlockSnapshot> transaction) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(world);
        Optional<User> user = cause.first(User.class);
        SpongeConfig.LoggingCategory logging = config.getConfig().getLogging();
        if (logging.blockBreakLogging() && type == CaptureType.BREAK
            || logging.blockModifyLogging() && type == CaptureType.MODIFY
            || logging.blockPlaceLogging() && type == CaptureType.PLACE
            || logging.blockPopulateLogging() && type == CaptureType.POPULATE) {

            logInfo("Block " + type.name() + " [RootCause: {0}][User: {1}][World: {2}][DimId: {3}][OriginalState: {4}][NewState: {5}]",
                    getFriendlyCauseName(cause),
                    user.isPresent() ? user.get().getName() : "None",
                    world.getWorldInfo().getWorldName(),
                    world.provider.getDimensionId(),
                    transaction.getOriginal().getState(),
                    transaction.getFinal().getState());
            logStack(config);
        }
    }

    public static void logChunkLoad(World world, Vector3i chunkPos) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(world);
        if (config.getConfig().getLogging().chunkLoadLogging()) {
            logInfo("Load Chunk At [{0}] ({1}, {2})", world.provider.getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(config);
        }
    }

    public static void logChunkUnload(World world, Vector3i chunkPos) {
        if (world.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(world);
        if (config.getConfig().getLogging().chunkUnloadLogging()) {
            logInfo("Unload Chunk At [{0}] ({1}, {2})", world.provider.getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(config);
        }
    }

    public static void logExploitSignCommandUpdates(EntityPlayer player, TileEntity te, String command) {
        if (player.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(player.worldObj);
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
        if (player.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(player.worldObj);
        if (config.getConfig().getLogging().logExploitItemStackNameOverflow) {
            logInfo("[EXPLOIT] Player ''{0}'' attempted to send a creative itemstack update with a display name length of ''{1}'' (Max allowed length is 32767). This has been blocked to avoid server overflow.",
                    player.getName(),
                    length);
            logStack(config);
        }
    }

    public static void logExploitRespawnInvisibility(EntityPlayer player) {
        if (player.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(player.worldObj);
        if (config.getConfig().getLogging().logExploitRespawnInvisibility) {
            logInfo("[EXPLOIT] Player ''{0}'' attempted to perform a respawn invisibility exploit to surrounding players.",
                    player.getName());
            logStack(config);
        }
    }

    @SuppressWarnings("unused")
    private static void logChunkLoadOverride(ChunkProviderServer provider, int x, int z) {
        SpongeConfig<?> config = getActiveConfig(provider.worldObj);
        logInfo("Chunk Load Override: {0}, Dimension ID: {1}", provider.chunkLoadOverride,
                provider.worldObj.provider.getDimensionId());
    }

    public static boolean checkBoundingBoxSize(Entity entity, AxisAlignedBB aabb) {
        if (entity == null || entity.worldObj.isRemote) {
            return false;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer) {
            return false; // only check living entities that are not players
        }

        int maxBoundingBoxSize = config.getConfig().getEntity().getMaxBoundingBoxSize();
        if (maxBoundingBoxSize <= 0) {
            return false;
        }
        int x = MathHelper.floor_double(aabb.minX);
        int x1 = MathHelper.floor_double(aabb.maxX + 1.0D);
        int y = MathHelper.floor_double(aabb.minY);
        int y1 = MathHelper.floor_double(aabb.maxY + 1.0D);
        int z = MathHelper.floor_double(aabb.minZ);
        int z1 = MathHelper.floor_double(aabb.maxZ + 1.0D);

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
        if (entity == null || entity.worldObj.isRemote) {
            return false;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        int maxSpeed = config.getConfig().getEntity().getMaxSpeed();
        if (maxSpeed > 0) {
            double distance = x * x + z * z;
            if (distance > maxSpeed) {
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
        if (entity == null || entity.worldObj.isRemote) {
            return;
        }

        SpongeConfig<?> config = getActiveConfig(entity.worldObj);
        if (!config.getConfig().getLogging().logEntityCollisionChecks()) {
            return;
        }
        int collisionWarnSize = config.getConfig().getEntity().getMaxCollisionSize();

        if (list == null) {
            return;
        }

        if (collisionWarnSize > 0 && (MinecraftServer.getServer().getTickCounter() % 10) == 0 && list.size() >= collisionWarnSize) {
            SpongeHooks.CollisionWarning warning = new SpongeHooks.CollisionWarning(entity.worldObj, entity);
            if (SpongeHooks.recentWarnings.contains(warning)) {
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
            this.dimensionId = world.provider.getDimensionId();
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

    public static SpongeConfig<?> getActiveConfig(World world, boolean refresh) {
        SpongeConfig<?> activeConfig = ((IMixinWorld) world).getActiveConfig();
        if (activeConfig == null || refresh) {
            final SpongeConfig<WorldConfig> worldConfig = ((IMixinWorld) world).getWorldConfig();
            final SpongeConfig<DimensionConfig> dimensionConfig = ((IMixinWorldProvider) world.provider).getDimensionConfig();
            if (worldConfig != null && worldConfig.getConfig().isConfigEnabled()) {
                activeConfig = worldConfig;
            } else if (dimensionConfig != null && dimensionConfig.getConfig().isConfigEnabled()) {
                activeConfig = ((IMixinWorldProvider) world.provider).getDimensionConfig();
            } else {
                activeConfig = SpongeImpl.getGlobalConfig();
            }
            ((IMixinWorld) world).setActiveConfig(activeConfig);
        }

        return activeConfig;
    }

    public static SpongeConfig<?> getActiveConfig(World world) {
        return getActiveConfig(world, false);
    }

    public static SpongeConfig<?> getActiveConfig(String dimensionType, String worldFolder) {
        if (dimensionType == null) {
            // If no dimension type, go global
            return SpongeImpl.getGlobalConfig();
        }

        if (worldFolder != null) {
            final Optional<org.spongepowered.api.world.World> optWorld = SpongeImpl.getGame().getServer().getWorld(worldFolder);

            // If this is a loaded world then we only return configs on the loaded objects. Don't go to disk.
            if (optWorld.isPresent()) {
                final World world = (World) optWorld.get();
                final SpongeConfig<WorldConfig> worldConfig = ((IMixinWorld) world).getWorldConfig();

                if (worldConfig != null) {
                    // Return world config if its present and enabled
                    if (worldConfig.getConfig().isConfigEnabled()) {
                        return worldConfig;
                    }

                    // If we've gotten here, see if this world's dimension's config is enabled.
                    final SpongeConfig<DimensionConfig> dimensionConfig =
                            DimensionRegistryModule.getInstance().getConfig(world.provider.getDimensionId());
                    if (dimensionConfig != null && dimensionConfig.getConfig().isConfigEnabled()) {
                        return dimensionConfig;
                    }

                    // Default to global
                    return SpongeImpl.getGlobalConfig();
                } else {
                    // If we've gotten here, we have no world config so see if this world's dimension's config is enabled.
                    final SpongeConfig<DimensionConfig> dimensionConfig =
                            DimensionRegistryModule.getInstance().getConfig(world.provider.getDimensionId());
                    if (dimensionConfig != null && dimensionConfig.getConfig().isConfigEnabled()) {
                        return dimensionConfig;
                    }

                    // Default to global
                    return SpongeImpl.getGlobalConfig();
                }
            }
        }

        // No in-memory config objects, lookup from disk.
        final Path dimFolderPath = SpongeImpl.getSpongeConfigDir().resolve("worlds").resolve(dimensionType);
        final Path dimConfPath = dimFolderPath.resolve("dimension.conf");

        if (worldFolder != null) {
            final Path worldConfPath = dimFolderPath.resolve(worldFolder).resolve("world.conf");

            final SpongeConfig<WorldConfig> worldConfig = new SpongeConfig<>(SpongeConfig.Type.WORLD, worldConfPath, SpongeImpl.ECOSYSTEM_ID);
            if (worldConfig.getConfig().isConfigEnabled()) {
                return worldConfig;
            }
        }

        final SpongeConfig<DimensionConfig> dimConfig = new SpongeConfig<>(SpongeConfig.Type.DIMENSION, dimConfPath, SpongeImpl.ECOSYSTEM_ID);
        if (dimConfig.getConfig().isConfigEnabled()) {
            return dimConfig;
        }

        // Neither in-memory or on-disk enabled configs. Go global.
        return SpongeImpl.getGlobalConfig();
    }

    public static void refreshActiveConfigs() {
        for (WorldServer world : DimensionManager.getWorlds()) {
            ((IMixinWorld) world).setActiveConfig(SpongeHooks.getActiveConfig(world, true));
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof IModData) {
                    IModData spongeEntity = (IModData) entity;
                    spongeEntity.requiresCacheRefresh(true);
                }
            }
        }
        for (BlockType blockType : BlockTypeRegistryModule.getInstance().getAll()) {
            if (blockType instanceof IModData) {
                IModData spongeBlock = (IModData) blockType;
                spongeBlock.requiresCacheRefresh(true);
            }
        }
    }

    public static void setBlockState(World world, int x, int y, int z, BlockState state, boolean notifyNeighbors) {
        setBlockState(world, new BlockPos(x, y, z), state, notifyNeighbors);
    }

    public static void setBlockState(World world, BlockPos position, BlockState state, boolean notifyNeighbors) {
        world.setBlockState(position, toBlockState(state), notifyNeighbors ? 3 : 2);
    }

    public static void setBlockState(Chunk chunk, int x, int y, int z, BlockState state, boolean notifyNeighbors) {
        setBlockState(chunk, new BlockPos(x, y, z), state, notifyNeighbors);
    }

    public static void setBlockState(Chunk chunk, BlockPos position, BlockState state, boolean notifyNeighbors) {
        if (notifyNeighbors) { // delegate to world
            setBlockState(chunk.getWorld(), position, state, true);
            return;
        }
        chunk.setBlockState(position, toBlockState(state));
    }

    private static IBlockState toBlockState(BlockState state) {
        if (state instanceof IBlockState) {
            return (IBlockState) state;
        } else {
            // TODO: Need to figure out what is sensible for other BlockState
            // implementing classes.
            throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
        }
    }

    public static String getFriendlyCauseName(Cause cause) {
        String causedBy = "Unknown";
        Object rootCause = cause.root();
        if (rootCause instanceof User) {
            User user = (User) rootCause;
            causedBy = user.getName();
        } else if (rootCause instanceof EntityItem) {
            EntityItem item = (EntityItem) rootCause;
            causedBy = item.getEntityItem().getDisplayName();
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

    public static List<EntityHanging> findHangingEntities(World worldIn, BlockPos pos) {
        List<EntityHanging> list = worldIn.getEntitiesWithinAABB(EntityHanging.class, new AxisAlignedBB(pos, new BlockPos(pos.getX(), pos.getY(), pos.getZ())).expand(1.1D, 1.1D, 1.1D), new Predicate<EntityHanging>() {
            @Override
            public boolean apply(EntityHanging entityIn) {
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

                switch(entityFacing) {
                    case NORTH:
                        return entityPos.equals(pos.add(StaticMixinHelper.HANGING_OFFSET_NORTH));
                    case SOUTH:
                        return entityIn.getPosition().equals(pos.add(StaticMixinHelper.HANGING_OFFSET_SOUTH));
                    case WEST:
                        return entityIn.getPosition().equals(pos.add(StaticMixinHelper.HANGING_OFFSET_WEST));
                    case EAST:
                        return entityIn.getPosition().equals(pos.add(StaticMixinHelper.HANGING_OFFSET_EAST));
                    default:
                        return false;
                }
            }
        });

        return list;
    }

    public static Optional<User> tryToTrackBlock(World world, Object source, BlockPos sourcePos, Block targetBlock, BlockPos targetPos, PlayerTracker.Type type) {
        if (!world.isBlockLoaded(sourcePos) || !world.isBlockLoaded(targetPos)) {
            return Optional.empty();
        }

        IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(sourcePos);
        if (spongeChunk != null) {
            Optional<User> owner = spongeChunk.getBlockOwner(sourcePos);
            Optional<User> notifier = spongeChunk.getBlockNotifier(sourcePos);
            if (notifier.isPresent()) {
                spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(targetPos);
                spongeChunk.addTrackedBlockPosition(world.getBlockState(targetPos).getBlock(), targetPos, notifier.get(), type);
                return notifier;
            } else if (owner.isPresent()) {
                spongeChunk.addTrackedBlockPosition(world.getBlockState(targetPos).getBlock(), targetPos, owner.get(), type);
                return owner;
            }
        }
        return Optional.empty();
    }

    public static void tryToTrackBlockAndEntity(World world, Object source, Entity entity, BlockPos sourcePos, Block targetBlock, BlockPos targetPos, PlayerTracker.Type type) {
        Optional<User> user = tryToTrackBlock(world, source, sourcePos, targetBlock, targetPos, type);
        if (user.isPresent()) {
            ((IMixinEntity) entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.get().getUniqueId());
        }
    }

    // Remove below after update to 1.8.8
    private static final Pattern formattingCodePattern = Pattern.compile("(?i)" + String.valueOf('\u00a7') + "[0-9A-FK-OR]");

    public static String getTextWithoutFormattingCodes(String text) {
        return text == null ? null : formattingCodePattern.matcher(text).replaceAll("");
    }

    public static DamageSource getEntityDamageSource(Entity entity) {
        if (entity == null) {
            return null;
        }

        IMixinWorld spongeWorld = (IMixinWorld) entity.worldObj;
        if (spongeWorld.getCauseTracker().hasTickingEntity()) {
            Entity lastAttacker = (Entity) spongeWorld.getCauseTracker().getCurrentTickEntity().get();
            if (lastAttacker instanceof Player) {
                EntityPlayerMP player = (EntityPlayerMP) lastAttacker;
                return DamageSource.causePlayerDamage(player);
            } else if (lastAttacker instanceof Projectile) {
                Projectile projectile = (Projectile) lastAttacker;
                net.minecraft.entity.Entity shooter = null;
                if (projectile.getShooter() instanceof Entity) {
                    shooter = (net.minecraft.entity.Entity) projectile.getShooter();
                }

                if (projectile instanceof EntityArrow) {
                    return DamageSource.causeArrowDamage((EntityArrow) lastAttacker, shooter);
                } else if (projectile instanceof EntityFireball) {
                    return DamageSource.causeFireballDamage((EntityFireball) lastAttacker, shooter);
                }
            } else if ((lastAttacker instanceof EntityMob || lastAttacker instanceof IMob) && lastAttacker instanceof EntityLivingBase) {
                return DamageSource.causeMobDamage((EntityLivingBase) lastAttacker);
            } else {
                String damageType = ((IMixinEntity) lastAttacker).getType() == null ? lastAttacker.getClass().getSimpleName() : ((IMixinEntity) lastAttacker).getType().getId();
                return new EntityDamageSource(damageType, lastAttacker);
            }
        } else if (spongeWorld.getCauseTracker().hasTickingBlock()) {
            BlockSnapshot blockSnapshot = spongeWorld.getCauseTracker().getCurrentTickBlock().get();
            if (blockSnapshot.getLocation().isPresent()) {
                return new MinecraftBlockDamageSource(blockSnapshot.getState().getType().getId(), blockSnapshot.getLocation().get());
            }
        } else if (StaticMixinHelper.packetPlayer != null) {
            return DamageSource.causePlayerDamage(StaticMixinHelper.packetPlayer);
        }

        return null;
    }
}

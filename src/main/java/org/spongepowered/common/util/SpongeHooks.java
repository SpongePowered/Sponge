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

import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.LoggingCategory;
import org.spongepowered.common.config.type.DimensionConfig;
import org.spongepowered.common.config.type.GeneralConfigBase;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.math.vector.Vector3i;

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class SpongeHooks {

    private static Object2LongMap<CollisionWarning> recentWarnings = new Object2LongOpenHashMap<>();

    public static void logInfo(final String msg, final Object... args) {
        SpongeImpl.getLogger().info(MessageFormat.format(msg, args));
    }

    public static void logWarning(final String msg, final Object... args) {
        SpongeImpl.getLogger().warn(MessageFormat.format(msg, args));
    }

    public static void logSevere(final String msg, final Object... args) {
        SpongeImpl.getLogger().fatal(MessageFormat.format(msg, args));
    }

    public static void logStack(final SpongeConfig<? extends GeneralConfigBase> config) {
        if (config.getConfig().getLogging().logWithStackTraces()) {
            final Throwable ex = new Throwable();
            ex.fillInStackTrace();
            SpongeImpl.getLogger().catching(Level.INFO, ex);
        }
    }

    public static void logEntityDeath(@Nullable final Entity entity) {
        if (entity == null || entity.getEntityWorld().isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().entityDeathLogging()) {
            logInfo("Dimension: {0} setDead(): {1}", entity.getEntityWorld().getDimension().getType(), entity);
            logStack(configAdapter);
        }
    }

    public static void logEntityDespawn(@Nullable final Entity entity, final String reason) {
        if (entity == null || entity.getEntityWorld().isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().entityDespawnLogging()) {
            logInfo("Dimension: {0} Despawning ({1}): {2}", entity.getEntityWorld().getDimension().getType(), reason, entity);
            logStack(configAdapter);
        }
    }

    public static void logEntitySpawn(@Nullable final Entity entity) {
        if (entity == null) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        final ITextComponent spawnName = entity.getName();

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().entitySpawnLogging()) {
            logInfo("SPAWNED " + spawnName.getUnformattedComponentText() + " [Dimension: {1}]", entity.getEntityWorld().dimension.getType());
            logStack(configAdapter);
        }
    }

    public static void logBlockTrack(
        final World world, final Block block, final BlockPos pos, final User user, final boolean allowed) {
        if (world.isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().blockTrackLogging() && allowed) {
            logInfo("Tracking Block " + "[RootCause: {0}][Dimension: {1}][Block: {2}][Pos: {3}]",
                    user.getName(), world.getDimension().getType(), ((BlockType) block).getKey(), pos);
            logStack(configAdapter);
        } else if (configAdapter.getConfig().getLogging().blockTrackLogging() && !allowed) {
            logInfo("Blacklisted! Unable to track Block " + "[RootCause: {0}][Dimension: {1}][Block: {2}][Pos: {3}]",
                    user.getName(),
                    world.getDimension().getType(),
                    ((BlockType) block).getKey(),
                    pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
    }

    public static void logBlockAction(final World world, @Nullable final BlockChange type, final Transaction<BlockSnapshot> transaction) {
        if (world.isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();

        final LoggingCategory logging = configAdapter.getConfig().getLogging();
        if (type != null && type.allowsLogging(logging)) {
            logInfo("Block " + type.name() + " [Dimension: {0}][OriginalState: {1}][NewState: {2}]",
                    world.getDimension().getType(),
                    transaction.getOriginal().getState(),
                    transaction.getFinal().getState());
            logStack(configAdapter);
        }
    }

    public static void logChunkLoad(final World world, final Vector3i chunkPos) {
        if (world.isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().chunkLoadLogging()) {
            logInfo("Load Chunk in Dimension [{0}] ({1}, {2})", world.getDimension().getType(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(configAdapter);
        }
    }

    public static void logChunkUnload(final World world, final Vector3i chunkPos) {
        if (world.isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().chunkUnloadLogging()) {
            logInfo("Unload Chunk in Dimension [{0}] ({1}, {2})", world.getDimension().getType(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(configAdapter);
        }
    }

    public static void logChunkGCQueueUnload(final World world, final Chunk chunk) {
        if (world.isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().chunkGCQueueUnloadLogging()) {
            logInfo("Chunk GC Queued Chunk in Dimension '{0}' ({2}, {3} for unload)", world.getDimension().getType(), chunk.getPos().x,
                    chunk.getPos().z);
            logStack(configAdapter);
        }
    }

    public static void logExploitSignCommandUpdates(final PlayerEntity player, final TileEntity tileEntity, final String command) {
        if (player.getEntityWorld().isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) player.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().logExploitSignCommandUpdates) {
            logInfo("[EXPLOIT] Player '{0}' attempted to exploit sign in dimension '{1}' located at '{2}' with command '{3}'",
                    player.getName(),
                    tileEntity.getWorld().getDimension().getType(),
                    tileEntity.getPos().getX() + ", " + tileEntity.getPos().getY() + ", " + tileEntity.getPos().getZ(),
                    command);
            logStack(configAdapter);
        }
    }

    public static void logExploitItemNameOverflow(final PlayerEntity player, final int length) {
        if (player.getEntityWorld().isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) player.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().logExploitItemStackNameOverflow) {
            logInfo("[EXPLOIT] Player '{0}' attempted to send a creative itemstack update with a display name length of '{1}' (Max allowed "
                            + "length is 32767). This has been blocked to avoid server overflow.",
                    player.getName(),
                    length);
            logStack(configAdapter);
        }
    }

    public static void logExploitRespawnInvisibility(final PlayerEntity player) {
        if (player.getEntityWorld().isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) player.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().logExploitRespawnInvisibility) {
            logInfo("[EXPLOIT] Player '{0}' attempted to perform a respawn invisibility exploit to surrounding players.",
                    player.getName());
            logStack(configAdapter);
        }
    }

    public static boolean checkBoundingBoxSize(@Nullable final Entity entity, final AxisAlignedBB aabb) {
        if (entity == null || entity.getEntityWorld().isRemote()) {
            return false;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (!(entity instanceof LivingEntity) || entity instanceof PlayerEntity || entity instanceof EnderDragonPartEntity) {
            return false; // only check living entities, so long as they are not a player or multipart entity
        }

        final int maxBoundingBoxSize = configAdapter.getConfig().getEntity().getMaxBoundingBoxSize();
        if (maxBoundingBoxSize <= 0) {
            return false;
        }
        final int x = MathHelper.floor(aabb.minX);
        final int x1 = MathHelper.floor(aabb.maxX + 1.0D);
        final int y = MathHelper.floor(aabb.minY);
        final int y1 = MathHelper.floor(aabb.maxY + 1.0D);
        final int z = MathHelper.floor(aabb.minZ);
        final int z1 = MathHelper.floor(aabb.maxZ + 1.0D);

        final int size = Math.abs(x1 - x) * Math.abs(y1 - y) * Math.abs(z1 - z);
        if (size > maxBoundingBoxSize) {
            logWarning("Entity being removed for bounding box restrictions");
            logWarning("BB Size: {0} > {1} avg edge: {2}", size, maxBoundingBoxSize, aabb.getAverageEdgeLength());
            logWarning("Motion: ({0}, {1}, {2})", entity.getMotion().x, entity.getMotion().y, entity.getMotion().z);
            logWarning("Calculated bounding box: {0}", aabb);
            logWarning("Entity bounding box: {0}", entity.getCollisionBoundingBox());
            logWarning("Entity: {0}", entity);
            final CompoundNBT compound = new CompoundNBT();
            entity.writeWithoutTypeId(compound);
            logWarning("Entity NBT: {0}", compound);
            logStack(configAdapter);
            entity.remove();
            return true;
        }
        return false;
    }

    public static boolean checkEntitySpeed(@Nullable final Entity entity, final double x, final double y, final double z) {
        if (entity == null || entity.getEntityWorld().isRemote()) {
            return false;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.world.getWorldInfo()).bridge$getConfigAdapter();
        final int maxSpeed = configAdapter.getConfig().getEntity().getMaxSpeed();
        if (maxSpeed > 0) {
            final double distance = x * x + z * z;
            if (distance > maxSpeed && !entity.isPassenger()) {
                if (configAdapter.getConfig().getLogging().logEntitySpeedRemoval()) {
                    logInfo("Speed violation: {0} was over {1} - Removing Entity: {2}", distance, maxSpeed, entity);
                    if (entity instanceof LivingEntity) {
                        final LivingEntity livingEntity = (LivingEntity) entity;
                        logInfo("Entity Motion: ({0}, {1}, {2}) Move Strafing: {3} Move Forward: {4}",
                                entity.getMotion().x, entity.getMotion().y,
                                entity.getMotion().z,
                                livingEntity.moveStrafing, livingEntity.moveForward);
                    }

                    if (configAdapter.getConfig().getLogging().logWithStackTraces()) {
                        logInfo("Move offset: ({0}, {1}, {2})", x, y, z);
                        logInfo("Motion: ({0}, {1}, {2})", entity.getMotion().x, entity.getMotion().y, entity.getMotion().z);
                        logInfo("Entity: {0}", entity);
                        final CompoundNBT compound = new CompoundNBT();
                        entity.writeWithoutTypeId(compound);
                        logInfo("Entity NBT: {0}", compound);
                        logStack(configAdapter);
                    }
                }
                if (entity instanceof PlayerEntity) { // Skip killing players
                    entity.setMotion(Vec3d.ZERO);
                    return false;
                }
                // Remove the entity;
                entity.removed = true;
                return false;
            }
        }
        return true;
    }

    public static void logEntitySize(@Nullable final Entity entity, final List list) {
        if (entity == null || list == null || entity.getEntityWorld().isRemote()) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (!configAdapter.getConfig().getLogging().logEntityCollisionChecks()) {
            return;
        }
        final int collisionWarnSize = configAdapter.getConfig().getEntity().getMaxCollisionSize();

        if (collisionWarnSize > 0 && (entity.getServer().getTickCounter() % 10) == 0 && list.size() >= collisionWarnSize) {
            final SpongeHooks.CollisionWarning warning = new SpongeHooks.CollisionWarning(entity.getEntityWorld().getDimension().getType(), entity);
            if (SpongeHooks.recentWarnings.containsKey(warning)) {
                final long lastWarned = SpongeHooks.recentWarnings.get(warning);
                if ((entity.getServer().getServerTime() - lastWarned) < 30000) {
                    return;
                }
            }
            SpongeHooks.recentWarnings.put(warning, System.currentTimeMillis());
            logWarning("Entity collision > {0, number} at: {1}", collisionWarnSize, entity);
        }
    }

    private static class CollisionWarning {

        public BlockPos blockPos;
        public DimensionType dimensionType;

        public CollisionWarning(final DimensionType dimensionType, final Entity entity) {
            this.dimensionType = dimensionType;
            this.blockPos = new BlockPos(entity.chunkCoordX, entity.chunkCoordY, entity.chunkCoordZ);
        }

        @Override
        public boolean equals(final Object otherObj) {
            if (!(otherObj instanceof CollisionWarning)) {
                return false;
            }
            final CollisionWarning other = (CollisionWarning) otherObj;
            return (other.dimensionType == this.dimensionType) && other.blockPos.equals(this.blockPos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.blockPos, this.dimensionType);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void dumpHeap(final File file, final boolean live) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            final Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            final Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            final Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            m.invoke(hotspotMBean, file.getPath(), live);
        } catch (final Throwable t) {
            logSevere("Could not write heap to {0}", file);
        }
    }

    public static void enableThreadContentionMonitoring() {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getDebug().isEnableThreadContentionMonitoring()) {
            return;
        }
        final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        mbean.setThreadContentionMonitoringEnabled(true);
    }

    public static SpongeConfig<? extends GeneralConfigBase> getOrLoadConfigAdapter(@Nullable final Path dimensionPath, @Nullable
    final String worldDirectory) {
        if (worldDirectory != null) {
            final org.spongepowered.api.world.World apiWorld = SpongeImpl.getGame().getServer().getWorldManager().getWorld(worldDirectory)
                    .orElse(null);
            if (apiWorld != null) {
                return ((WorldInfoBridge) apiWorld.getProperties()).bridge$getConfigAdapter();
            }
        }

        if (dimensionPath == null) {
            // If no dimension type, go global
            return SpongeImpl.getGlobalConfigAdapter();
        }

        // No in-memory config objects, lookup from disk.
        final SpongeConfig<DimensionConfig> dimensionConfigAdapter = new SpongeConfig<>(SpongeConfig.Type.DIMENSION, dimensionPath
            .resolve("dimension.conf"), SpongeImpl.ECOSYSTEM_ID, SpongeImpl.getGlobalConfigAdapter(), false);

        if (worldDirectory != null) {
            return new SpongeConfig<>(SpongeConfig.Type.WORLD, dimensionPath.resolve(worldDirectory).resolve("world.conf"), SpongeImpl.ECOSYSTEM_ID,
                dimensionConfigAdapter, false);
        }

        return dimensionConfigAdapter;
    }

    public static CompletableFuture<CommentedConfigurationNode> savePluginsInMetricsConfig(final Map<String, Tristate> entries) {
        return SpongeImpl.getGlobalConfigAdapter()
            .updateSetting("metrics.plugin-states", entries, new TypeToken<Map<String, Tristate>>() {
                private static final long serialVersionUID = 190617916448550012L;
            });
    }
}

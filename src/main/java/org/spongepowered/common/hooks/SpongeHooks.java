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
package org.spongepowered.common.hooks;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.inheritable.BaseConfig;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.LoggingCategory;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.math.vector.Vector3i;

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

public final class SpongeHooks {

    private static Object2LongMap<CollisionWarning> recentWarnings = new Object2LongOpenHashMap<>();

    public static void logInfo(final String msg, final Object... args) {
        SpongeCommon.getLogger().info(MessageFormat.format(msg, args));
    }

    public static void logWarning(final String msg, final Object... args) {
        SpongeCommon.getLogger().warn(MessageFormat.format(msg, args));
    }

    public static void logSevere(final String msg, final Object... args) {
        SpongeCommon.getLogger().fatal(MessageFormat.format(msg, args));
    }

    public static void logStack(final InheritableConfigHandle<? extends BaseConfig> config) {
        if (config.get().getLogging().logWithStackTraces()) {
            final Throwable ex = new Throwable();
            ex.fillInStackTrace();
            SpongeCommon.getLogger().catching(Level.INFO, ex);
        }
    }

    public static void logEntityDeath(@Nullable final Entity entity) {
        if (entity == null || entity.getEntityWorld().isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().entityDeathLogging()) {
            SpongeHooks.logInfo("Dimension: {0} setDead(): {1}", entity.getEntityWorld().getDimension().getType(), entity);
            SpongeHooks.logStack(configAdapter);
        }
    }

    public static void logEntityDespawn(@Nullable final Entity entity, final String reason) {
        if (entity == null || entity.getEntityWorld().isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().entityDespawnLogging()) {
            SpongeHooks.logInfo("Dimension: {0} Despawning ({1}): {2}", entity.getEntityWorld().getDimension().getType(), reason, entity);
            SpongeHooks.logStack(configAdapter);
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

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().entitySpawnLogging()) {
            SpongeHooks.logInfo("SPAWNED " + spawnName.getUnformattedComponentText() + " [Dimension: {1}]", entity.getEntityWorld().dimension.getType());
            SpongeHooks.logStack(configAdapter);
        }
    }

    public static void logBlockTrack(
        final net.minecraft.world.World world, final Block block, final BlockPos pos, final User user, final boolean allowed) {
        if (world.isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().blockTrackLogging() && allowed) {
            SpongeHooks.logInfo("Tracking Block " + "[RootCause: {0}][Dimension: {1}][Block: {2}][Pos: {3}]",
                    user.getName(), world.getDimension().getType(), ((BlockType) block).getKey(), pos);
            SpongeHooks.logStack(configAdapter);
        } else if (configAdapter.get().getLogging().blockTrackLogging() && !allowed) {
            SpongeHooks.logInfo("Blacklisted! Unable to track Block " + "[RootCause: {0}][Dimension: {1}][Block: {2}][Pos: {3}]",
                    user.getName(),
                    world.getDimension().getType(),
                    ((BlockType) block).getKey(),
                    pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
    }

    public static void logBlockAction(final net.minecraft.world.World world, @Nullable final BlockChange type, final Transaction<BlockSnapshot> transaction) {
        if (world.isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();

        final LoggingCategory logging = configAdapter.get().getLogging();
        if (type != null && type.allowsLogging(logging)) {
            SpongeHooks.logInfo("Block " + type.name() + " [Dimension: {0}][OriginalState: {1}][NewState: {2}]",
                    world.getDimension().getType(),
                    transaction.getOriginal().getState(),
                    transaction.getFinal().getState());
            SpongeHooks.logStack(configAdapter);
        }
    }

    public static void logChunkQueueLoad(final net.minecraft.world.World world, final Vector3i chunkPos) {
        if (world.isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().chunkLoadLogging()) {
            SpongeHooks.logInfo("Queue Chunk At [{0}] ({1}, {2})", world.getDimension().getType(), chunkPos.getX(),
                    chunkPos.getZ());
            SpongeHooks.logStack(configAdapter);
        }
    }

    public static void logChunkLoad(final net.minecraft.world.World world, final Vector3i chunkPos) {
        if (world.isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().chunkLoadLogging()) {
            SpongeHooks.logInfo("Load Chunk in Dimension [{0}] ({1}, {2})", world.getDimension().getType(), chunkPos.getX(),
                    chunkPos.getZ());
            SpongeHooks.logStack(configAdapter);
        }
    }

    public static void logChunkUnload(final net.minecraft.world.World world, final Vector3i chunkPos) {
        if (world.isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().chunkUnloadLogging()) {
            SpongeHooks.logInfo("Unload Chunk in Dimension [{0}] ({1}, {2})", world.getDimension().getType(), chunkPos.getX(),
                    chunkPos.getZ());
            SpongeHooks.logStack(configAdapter);
        }
    }

    public static void logChunkGCQueueUnload(final net.minecraft.world.World world, final Chunk chunk) {
        if (world.isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().chunkGCQueueUnloadLogging()) {
            SpongeHooks.logInfo("Chunk GC Queued Chunk in Dimension '{0}' ({2}, {3} for unload)", world.getDimension().getType(), chunk.getPos().x,
                    chunk.getPos().z);
            SpongeHooks.logStack(configAdapter);
        }
    }

    public static void logExploitItemNameOverflow(final PlayerEntity player, final int length) {
        if (player.getEntityWorld().isRemote()) {
            return;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) player.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().logExploitItemStackNameOverflow) {
            SpongeHooks.logInfo("[EXPLOIT] Player '{0}' attempted to send a creative itemstack update with a display name length of '{1}' (Max allowed "
                            + "length is 32767). This has been blocked to avoid server overflow.",
                    player.getName(),
                    length);
            SpongeHooks.logStack(configAdapter);
        }
    }

    public static boolean checkBoundingBoxSize(@Nullable final Entity entity, final AxisAlignedBB aabb) {
        if (entity == null || entity.getEntityWorld().isRemote()) {
            return false;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (!(entity instanceof LivingEntity) || entity instanceof PlayerEntity || entity instanceof EnderDragonPartEntity) {
            return false; // only check living entities, so long as they are not a player or multipart entity
        }

        final int maxBoundingBoxSize = configAdapter.get().getEntity().getMaxBoundingBoxSize();
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
            SpongeHooks.logWarning("Entity being removed for bounding box restrictions");
            SpongeHooks.logWarning("BB Size: {0} > {1} avg edge: {2}", size, maxBoundingBoxSize, aabb.getAverageEdgeLength());
            SpongeHooks.logWarning("Motion: ({0}, {1}, {2})", entity.getMotion().x, entity.getMotion().y, entity.getMotion().z);
            SpongeHooks.logWarning("Calculated bounding box: {0}", aabb);
            SpongeHooks.logWarning("Entity bounding box: {0}", entity.getCollisionBoundingBox());
            SpongeHooks.logWarning("Entity: {0}", entity);
            final CompoundNBT compound = new CompoundNBT();
            entity.writeWithoutTypeId(compound);
            SpongeHooks.logWarning("Entity NBT: {0}", compound);
            SpongeHooks.logStack(configAdapter);
            entity.remove();
            return true;
        }
        return false;
    }

    public static boolean checkEntitySpeed(@Nullable final Entity entity, final double x, final double y, final double z) {
        if (entity == null || entity.getEntityWorld().isRemote()) {
            return false;
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) entity.world.getWorldInfo()).bridge$getConfigAdapter();
        final int maxSpeed = configAdapter.get().getEntity().getMaxSpeed();
        if (maxSpeed > 0) {
            final double distance = x * x + z * z;
            if (distance > maxSpeed && !entity.isPassenger()) {
                if (configAdapter.get().getLogging().logEntitySpeedRemoval()) {
                    SpongeHooks.logInfo("Speed violation: {0} was over {1} - Removing Entity: {2}", distance, maxSpeed, entity);
                    if (entity instanceof LivingEntity) {
                        final LivingEntity livingEntity = (LivingEntity) entity;
                        SpongeHooks.logInfo("Entity Motion: ({0}, {1}, {2}) Move Strafing: {3} Move Forward: {4}",
                                entity.getMotion().x, entity.getMotion().y,
                                entity.getMotion().z,
                                livingEntity.moveStrafing, livingEntity.moveForward);
                    }

                    if (configAdapter.get().getLogging().logWithStackTraces()) {
                        SpongeHooks.logInfo("Move offset: ({0}, {1}, {2})", x, y, z);
                        SpongeHooks.logInfo("Motion: ({0}, {1}, {2})", entity.getMotion().x, entity.getMotion().y, entity.getMotion().z);
                        SpongeHooks.logInfo("Entity: {0}", entity);
                        final CompoundNBT compound = new CompoundNBT();
                        entity.writeWithoutTypeId(compound);
                        SpongeHooks.logInfo("Entity NBT: {0}", compound);
                        SpongeHooks.logStack(configAdapter);
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

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) entity.getEntityWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (!configAdapter.get().getLogging().logEntityCollisionChecks()) {
            return;
        }
        final int collisionWarnSize = configAdapter.get().getEntity().getMaxCollisionSize();

        if (collisionWarnSize > 0 && (entity.getServer().getTickCounter() % 10) == 0 && list.size() >= collisionWarnSize) {
            final SpongeHooks.CollisionWarning warning = new SpongeHooks.CollisionWarning(entity.getEntityWorld().getDimension().getType(), entity);
            if (SpongeHooks.recentWarnings.containsKey(warning)) {
                final long lastWarned = SpongeHooks.recentWarnings.get(warning);
                if ((entity.getServer().getServerTime() - lastWarned) < 30000) {
                    return;
                }
            }
            SpongeHooks.recentWarnings.put(warning, System.currentTimeMillis());
            SpongeHooks.logWarning("Entity collision > {0, number} at: {1}", collisionWarnSize, entity);
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
            SpongeHooks.logSevere("Could not write heap to {0}", file);
        }
    }

    public static void enableThreadContentionMonitoring() {
        if (!SpongeConfigs.getCommon().get().getDebug().isEnableThreadContentionMonitoring()) {
            return;
        }
        final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        mbean.setThreadContentionMonitoringEnabled(true);
    }

}

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
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
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
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.LoggingCategory;
import org.spongepowered.common.config.type.DimensionConfig;
import org.spongepowered.common.config.type.GeneralConfigBase;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.ActivationCapability;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.CollisionsCapability;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.management.MBeanServer;

public class SpongeHooks {

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

    public static void logEntityDeath(final Entity entity) {
        if (entity == null || entity.field_70170_p.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().entityDeathLogging()) {
            logInfo("Dim: {0} setDead(): {1}", ((WorldServerBridge) entity.field_70170_p).bridge$getDimensionId(), entity);
            logStack(configAdapter);
        }
    }

    public static void logEntityDespawn(final Entity entity, final String reason) {
        if (entity == null || entity.field_70170_p.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().entityDespawnLogging()) {
            logInfo("Dim: {0} Despawning ({1}): {2}", ((WorldServerBridge) entity.field_70170_p).bridge$getDimensionId(), reason, entity);
            logStack(configAdapter);
        }
    }

    public static void logEntitySpawn(final Entity entity) {
        if (entity == null) {
            return;
        }

        if (!(entity instanceof EntityLivingBase)) {
            return;
        }

        final String spawnName = entity.func_70005_c_();

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().entitySpawnLogging()) {
            logInfo("SPAWNED " + spawnName + " [World: {2}][DimId: {3}]",
                    entity.field_70170_p.func_72912_H().func_76065_j(),
                    ((WorldServerBridge) entity.field_70170_p).bridge$getDimensionId());
            logStack(configAdapter);
        }
    }

    public static void logBlockTrack(final World world, final Block block, final BlockPos pos, final User user, final boolean allowed) {
        if (world.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().blockTrackLogging() && allowed) {
            logInfo("Tracking Block " + "[RootCause: {0}][World: {1}][Block: {2}][Pos: {3}]",
                    user.getName(),
                world.func_72912_H().func_76065_j() + "(" + ((WorldServerBridge) world).bridge$getDimensionId() + ")",
                    ((BlockType) block).getId(),
                    pos);
            logStack(configAdapter);
        } else if (configAdapter.getConfig().getLogging().blockTrackLogging() && !allowed) {
            logInfo("Blacklisted! Unable to track Block " + "[RootCause: {0}][World: {1}][DimId: {2}][Block: {3}][Pos: {4}]",
                    user.getName(),
                    world.func_72912_H().func_76065_j(),
                    ((WorldServerBridge) world).bridge$getDimensionId(),
                    ((BlockType) block).getId(),
                    pos.func_177958_n() + ", " + pos.func_177956_o() + ", " + pos.func_177952_p());
        }
    }

    public static void logBlockAction(final World world, @Nullable final BlockChange type, final Transaction<BlockSnapshot> transaction) {
        if (world.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.func_72912_H()).bridge$getConfigAdapter();

        final LoggingCategory logging = configAdapter.getConfig().getLogging();
        if (type != null && type.allowsLogging(logging)) {
            logInfo("Block " + type.name() + " [World: {2}][DimId: {3}][OriginalState: {4}][NewState: {5}]",
                    world.func_72912_H().func_76065_j(),
                    ((WorldServerBridge) world).bridge$getDimensionId(),
                    transaction.getOriginal().getState(),
                    transaction.getFinal().getState());
            logStack(configAdapter);
        }
    }

    public static void logChunkLoad(final World world, final Vector3i chunkPos) {
        if (world.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().chunkLoadLogging()) {
            logInfo("Load Chunk At [{0}] ({1}, {2})", ((WorldServerBridge) world).bridge$getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(configAdapter);
        }
    }

    public static void logChunkUnload(final World world, final Vector3i chunkPos) {
        if (world.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().chunkUnloadLogging()) {
            logInfo("Unload Chunk At [{0}] ({1}, {2})", ((WorldServerBridge) world).bridge$getDimensionId(), chunkPos.getX(),
                    chunkPos.getZ());
            logStack(configAdapter);
        }
    }

    public static void logChunkGCQueueUnload(final WorldServer world, final Chunk chunk) {
        if (world.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().chunkGCQueueUnloadLogging()) {
            logInfo("Chunk GC Queued Chunk At [{0}] ({1}, {2} for unload)", ((WorldServerBridge) world).bridge$getDimensionId(), chunk.field_76635_g, chunk.field_76647_h);
            logStack(configAdapter);
        }
    }

    public static void logExploitSignCommandUpdates(final EntityPlayer player, final TileEntity te, final String command) {
        if (player.field_70170_p.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) player.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().logExploitSignCommandUpdates) {
            logInfo("[EXPLOIT] Player ''{0}'' attempted to exploit sign in world ''{1}'' located at ''{2}'' with command ''{3}''",
                    player.func_70005_c_(),
                    te.func_145831_w().func_72912_H().func_76065_j(),
                    te.func_174877_v().func_177958_n() + ", " + te.func_174877_v().func_177956_o() + ", " + te.func_174877_v().func_177952_p(),
                    command);
            logStack(configAdapter);
        }
    }

    public static void logExploitItemNameOverflow(final EntityPlayer player, final int length) {
        if (player.field_70170_p.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) player.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().logExploitItemStackNameOverflow) {
            logInfo("[EXPLOIT] Player ''{0}'' attempted to send a creative itemstack update with a display name length of ''{1}'' (Max allowed length is 32767). This has been blocked to avoid server overflow.",
                    player.func_70005_c_(),
                    length);
            logStack(configAdapter);
        }
    }

    public static void logExploitRespawnInvisibility(final EntityPlayer player) {
        if (player.field_70170_p.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) player.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().logExploitRespawnInvisibility) {
            logInfo("[EXPLOIT] Player ''{0}'' attempted to perform a respawn invisibility exploit to surrounding players.",
                    player.func_70005_c_());
            logStack(configAdapter);
        }
    }

    public static boolean checkBoundingBoxSize(final Entity entity, final AxisAlignedBB aabb) {
        if (entity == null || entity.field_70170_p.field_72995_K) {
            return false;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        if (!(entity instanceof EntityLivingBase) || entity instanceof EntityPlayer || entity instanceof IEntityMultiPart) {
            return false; // only check living entities, so long as they are not a player or multipart entity
        }

        final int maxBoundingBoxSize = configAdapter.getConfig().getEntity().getMaxBoundingBoxSize();
        if (maxBoundingBoxSize <= 0) {
            return false;
        }
        final int x = MathHelper.func_76128_c(aabb.field_72340_a);
        final int x1 = MathHelper.func_76128_c(aabb.field_72336_d + 1.0D);
        final int y = MathHelper.func_76128_c(aabb.field_72338_b);
        final int y1 = MathHelper.func_76128_c(aabb.field_72337_e + 1.0D);
        final int z = MathHelper.func_76128_c(aabb.field_72339_c);
        final int z1 = MathHelper.func_76128_c(aabb.field_72334_f + 1.0D);

        final int size = Math.abs(x1 - x) * Math.abs(y1 - y) * Math.abs(z1 - z);
        if (size > maxBoundingBoxSize) {
            logWarning("Entity being removed for bounding box restrictions");
            logWarning("BB Size: {0} > {1} avg edge: {2}", size, maxBoundingBoxSize, aabb.func_72320_b());
            logWarning("Motion: ({0}, {1}, {2})", entity.field_70159_w, entity.field_70181_x, entity.field_70179_y);
            logWarning("Calculated bounding box: {0}", aabb);
            logWarning("Entity bounding box: {0}", entity.func_70046_E());
            logWarning("Entity: {0}", entity);
            final NBTTagCompound tag = new NBTTagCompound();
            entity.func_189511_e(tag);
            logWarning("Entity NBT: {0}", tag);
            logStack(configAdapter);
            entity.func_70106_y();
            return true;
        }
        return false;
    }

    public static boolean checkEntitySpeed(final Entity entity, final double x, final double y, final double z) {
        if (entity == null || entity.field_70170_p.field_72995_K) {
            return false;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        final int maxSpeed = configAdapter.getConfig().getEntity().getMaxSpeed();
        if (maxSpeed > 0) {
            final double distance = x * x + z * z;
            if (distance > maxSpeed && !entity.func_184218_aH()) {
                if (configAdapter.getConfig().getLogging().logEntitySpeedRemoval()) {
                    logInfo("Speed violation: {0} was over {1} - Removing Entity: {2}", distance, maxSpeed, entity);
                    if (entity instanceof EntityLivingBase) {
                        final EntityLivingBase livingBase = (EntityLivingBase) entity;
                        logInfo("Entity Motion: ({0}, {1}, {2}) Move Strafing: {3} Move Forward: {4}",
                                entity.field_70159_w, entity.field_70181_x,
                                entity.field_70179_y,
                                livingBase.field_70702_br, livingBase.field_191988_bg);
                    }

                    if (configAdapter.getConfig().getLogging().logWithStackTraces()) {
                        logInfo("Move offset: ({0}, {1}, {2})", x, y, z);
                        logInfo("Motion: ({0}, {1}, {2})", entity.field_70159_w, entity.field_70181_x, entity.field_70179_y);
                        logInfo("Entity: {0}", entity);
                        final NBTTagCompound tag = new NBTTagCompound();
                        entity.func_189511_e(tag);
                        logInfo("Entity NBT: {0}", tag);
                        logStack(configAdapter);
                    }
                }
                if (entity instanceof EntityPlayer) { // Skip killing players
                    entity.field_70159_w = 0;
                    entity.field_70181_x = 0;
                    entity.field_70179_y = 0;
                    return false;
                }
                // Remove the entity;
                entity.field_70128_L = true;
                return false;
            }
        }
        return true;
    }

    // TODO - needs to be hooked
    @SuppressWarnings("rawtypes")
    public static void logEntitySize(final Entity entity, final List list) {
        if (entity == null || entity.field_70170_p.field_72995_K) {
            return;
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) entity.field_70170_p.func_72912_H()).bridge$getConfigAdapter();
        if (!configAdapter.getConfig().getLogging().logEntityCollisionChecks()) {
            return;
        }
        final int collisionWarnSize = configAdapter.getConfig().getEntity().getMaxCollisionSize();

        if (list == null) {
            return;
        }

        if (collisionWarnSize > 0 && (entity.func_130014_f_().func_73046_m().func_71259_af() % 10) == 0 && list.size() >= collisionWarnSize) {
            final SpongeHooks.CollisionWarning warning = new SpongeHooks.CollisionWarning(entity.field_70170_p, entity);
            if (SpongeHooks.recentWarnings.containsKey(warning)) {
                final long lastWarned = SpongeHooks.recentWarnings.get(warning);
                if ((MinecraftServer.func_130071_aq() - lastWarned) < 30000) {
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

        public CollisionWarning(final World world, final Entity entity) {
            this.dimensionId = ((WorldServerBridge) world).bridge$getDimensionId();
            this.blockPos = new BlockPos(entity.field_70176_ah, entity.field_70162_ai, entity.field_70164_aj);
        }

        @Override
        public boolean equals(final Object otherObj) {
            if (!(otherObj instanceof CollisionWarning) || (otherObj == null)) {
                return false;
            }
            final CollisionWarning other = (CollisionWarning) otherObj;
            return (other.dimensionId == this.dimensionId) && other.blockPos.equals(this.blockPos);
        }

        @Override
        public int hashCode() {
            return this.blockPos.hashCode() + this.dimensionId;
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
        } catch (Throwable t) {
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

    public static SpongeConfig<? extends GeneralConfigBase> getConfigAdapter(final Path dimensionPath, final String worldFolder) {
        if (worldFolder != null) {
            final org.spongepowered.api.world.World sWorld = SpongeImpl.getGame().getServer().getWorld(worldFolder).orElse(null);
            if (sWorld != null) {
                return ((WorldInfoBridge) sWorld.getProperties()).bridge$getConfigAdapter();
            }
        }

        if (dimensionPath == null) {
            // If no dimension type, go global
            return SpongeImpl.getGlobalConfigAdapter();
        }

        // No in-memory config objects, lookup from disk.
        final SpongeConfig<DimensionConfig> dimensionConfigAdapter = new SpongeConfig<>(SpongeConfig.Type.DIMENSION, dimensionPath
            .resolve("dimension.conf"), SpongeImpl.ECOSYSTEM_ID, SpongeImpl.getGlobalConfigAdapter(), false);

        if (worldFolder != null) {
            return new SpongeConfig<>(SpongeConfig.Type.WORLD, dimensionPath.resolve(worldFolder).resolve("world.conf"), SpongeImpl.ECOSYSTEM_ID,
                dimensionConfigAdapter, false);
        }

        return dimensionConfigAdapter;
    }

    public static void refreshActiveConfigs() {
        for (final BlockType blockType : BlockTypeRegistryModule.getInstance().getAll()) {
            if (blockType instanceof CollisionsCapability) {
                ((CollisionsCapability) blockType).collision$requiresCollisionsCacheRefresh(true);
            }
            if (blockType instanceof TrackableBridge) {
                ((BlockBridge) blockType).bridge$initializeTrackerState();
            }
        }
        for (final TileEntityType tileEntityType : TileEntityTypeRegistryModule.getInstance().getAll()) {
            ((SpongeTileEntityType) tileEntityType).initializeTrackerState();
        }
        for (final EntityType entityType : EntityTypeRegistryModule.getInstance().getAll()) {
            ((SpongeEntityType) entityType).initializeTrackerState();
        }

        for (final WorldServer world : WorldManager.getWorlds()) {
            final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.func_72912_H()).bridge$getConfigAdapter();
            // Reload before updating world config cache
            configAdapter.load();
            ((WorldServerBridge) world).bridge$updateConfigCache();
            for (final Entity entity : world.field_72996_f) {
                if (entity instanceof ActivationCapability) {
                    ((ActivationCapability) entity).activation$requiresActivationCacheRefresh(true);
                }
                if (entity instanceof CollisionsCapability) {
                    ((CollisionsCapability) entity).collision$requiresCollisionsCacheRefresh(true);
                }
                if (entity instanceof TrackableBridge) {
                    ((TrackableBridge) entity).bridge$refreshTrackerStates();
                }
            }
            for (final TileEntity tileEntity : world.field_147482_g) {
                if (tileEntity instanceof ActivationCapability) {
                    ((ActivationCapability) tileEntity).activation$requiresActivationCacheRefresh(true);
                }
                if (tileEntity instanceof TrackableBridge) {
                    ((TrackableBridge) tileEntity).bridge$refreshTrackerStates();
                }
            }
        }
        ConfigTeleportHelperFilter.invalidateCache();
    }

    public static CompletableFuture<CommentedConfigurationNode> savePluginsInMetricsConfig(final Map<String, Tristate> entries) {
        return SpongeImpl.getGlobalConfigAdapter()
            .updateSetting("metrics.plugin-states", entries, new TypeToken<Map<String, Tristate>>() {
                private static final long serialVersionUID = 190617916448550012L;
            });
    }
}

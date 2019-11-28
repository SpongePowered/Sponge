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
package org.spongepowered.common.mixin.plugin.tileentityactivation;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.TileEntityActivationCategory;
import org.spongepowered.common.config.category.TileEntityActivationModCategory;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.mixin.core.server.management.PlayerChunkMapAccessor;
import org.spongepowered.common.mixin.core.server.management.PlayerchunkMapEntryAccessor;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.ActivationCapability;
import org.spongepowered.common.util.VecHelper;

import java.util.Map;

public class TileEntityActivation {

    /**
     * Initialize tileentity activation state.
     *
     * @param tileEntity The tileentity to check
     */
    public static void initializeTileEntityActivationState(final TileEntity tileEntity) {
        if (tileEntity.getWorld() == null || tileEntity.getWorld().isRemote || !(tileEntity instanceof ITickable)) {
            return;
        }

        final TileEntityActivationCategory tileEntityActCat = ((WorldInfoBridge) tileEntity.getWorld().getWorldInfo()).bridge$getConfigAdapter().getConfig().getTileEntityActivationRange();
        final TileEntityType type = ((org.spongepowered.api.block.tileentity.TileEntity) tileEntity).getType();

        final ActivationCapability spongeTileEntity = (ActivationCapability) tileEntity;
        final SpongeTileEntityType spongeType = (SpongeTileEntityType) type;
        if (spongeType == null || spongeType.getModId() == null) {
            return;
        }
        final TileEntityActivationModCategory tileEntityActModCat = tileEntityActCat.getModList().get(spongeType.getModId().toLowerCase());
        final int defaultActivationRange = tileEntityActCat.getDefaultBlockRange();
        final int defaultTickRate = tileEntityActCat.getDefaultTickRate();
        if (tileEntityActModCat == null) {
            // use default activation range
            spongeTileEntity.activation$setActivationRange(defaultActivationRange);
            spongeTileEntity.activation$setSpongeTickRate(defaultTickRate);
            if (defaultTickRate <= 0) {
                spongeTileEntity.activation$setDefaultActivationState(false);
            }
            if (defaultActivationRange > 0) {
                spongeTileEntity.activation$setDefaultActivationState(false);
            }
        } else {
            if (!tileEntityActModCat.isEnabled()) {
                spongeTileEntity.activation$setDefaultActivationState(true);
                return;
            }

            final Integer defaultModActivationRange = tileEntityActModCat.getDefaultBlockRange();
            final Integer tileEntityActivationRange = tileEntityActModCat.getTileEntityRangeList().get(type.getName().toLowerCase());
            if (defaultModActivationRange != null && tileEntityActivationRange == null) {
                spongeTileEntity.activation$setActivationRange(defaultModActivationRange);
                if (defaultModActivationRange > 0) {
                    spongeTileEntity.activation$setDefaultActivationState(false);
                }
            } else if (tileEntityActivationRange != null) {
                spongeTileEntity.activation$setActivationRange(tileEntityActivationRange);
                if (tileEntityActivationRange > 0) {
                    spongeTileEntity.activation$setDefaultActivationState(false);
                }
            }

            final Integer defaultModTickRate = tileEntityActModCat.getDefaultTickRate();
            final Integer tileEntityTickRate = tileEntityActModCat.getTileEntityTickRateList().get(type.getName().toLowerCase());
            if (defaultModTickRate != null && tileEntityTickRate == null) {
                spongeTileEntity.activation$setSpongeTickRate(defaultModTickRate);
                if (defaultModTickRate <= 0) {
                    spongeTileEntity.activation$setDefaultActivationState(false);
                }
            } else if (tileEntityTickRate != null) {
                spongeTileEntity.activation$setSpongeTickRate(tileEntityTickRate);
                if (tileEntityTickRate <= 0) {
                    spongeTileEntity.activation$setDefaultActivationState(false);
                }
            }
        }
    }

    /**
    * Find what tileentities are in range of the players in the world and set
    * active if in range.
    *
    * @param world The world to perform activation checks in
    */
    public static void activateTileEntities(final ServerWorld world) {
        final PlayerChunkMap playerChunkMap = world.func_184164_w();
        for (final PlayerChunkMapEntry playerChunkMapEntry : ((PlayerChunkMapAccessor) playerChunkMap).accessor$getEntries()) {
            for (final PlayerEntity player : ((PlayerchunkMapEntryAccessor) playerChunkMapEntry).accessor$getPlayers()) {
                final Chunk chunk = ((PlayerchunkMapEntryAccessor) playerChunkMapEntry).accessor$getChunk();
                if (chunk == null || chunk.field_189550_d || ((ChunkBridge) chunk).bridge$isPersistedChunk()) {
                    continue;
                }

                activateChunkTileEntities(player, chunk);
            }
        }
    }


    /**
     * Checks for the activation state of all tileentities in this chunk.
     *
     * @param chunk Chunk to check for activation
     */
    private static void activateChunkTileEntities(final PlayerEntity player, final Chunk chunk) {
        final Vector3i playerPos = VecHelper.toVector3i(player.getPosition());
        final long currentTick = SpongeImpl.getServer().getTickCounter();
        for (final Map.Entry<BlockPos, TileEntity> mapEntry : chunk.getTileEntityMap().entrySet()) {
            final TileEntity tileEntity = mapEntry.getValue();
            final ActivationCapability spongeTileEntity = (ActivationCapability) tileEntity;
            if (spongeTileEntity.activation$getSpongeTickRate() <= 0 || !((TileEntityBridge) tileEntity).bridge$shouldTick()) {
                // never activate
                continue;
            }
            if (!(tileEntity instanceof ITickable) || spongeTileEntity.activation$getActivatedTick() == currentTick) {
                // already activated
                continue;
            }

            final Vector3i tilePos = VecHelper.toVector3i(tileEntity.getPos());
            if (currentTick > ((ActivationCapability) tileEntity).activation$getActivatedTick()) {
                if (spongeTileEntity.activation$getDefaultActivationState()) {
                    ((ActivationCapability) tileEntity).activation$setActivatedTick(currentTick);
                    continue;
                }

                // check if activation cache needs to be updated
                if (spongeTileEntity.activation$requiresActivationCacheRefresh()) {
                    TileEntityActivation.initializeTileEntityActivationState(tileEntity);
                    spongeTileEntity.activation$requiresActivationCacheRefresh(false);
                }

                final int bbActivationRange = ((ActivationCapability) tileEntity).activation$getActivationRange();
                final int blockDistance = Math.round(tilePos.distance(playerPos));
                if (blockDistance <= bbActivationRange) {
                    ((ActivationCapability) tileEntity).activation$setActivatedTick(currentTick);
                }
            }
        }
    }

    /**
     * Checks if the tileentity is active for this tick.
     *
     * @param tileEntity The tileentity to check for activity
     * @return Whether the given tileentity should be active
     */
    public static boolean checkIfActive(final TileEntity tileEntity) {
        if (tileEntity.getWorld() == null || tileEntity.getWorld().isRemote || !(tileEntity instanceof ITickable)) {
            return true;
        }

        final World world = tileEntity.getWorld();
        final ChunkBridge activeChunk = ((ActiveChunkReferantBridge) tileEntity).bridge$getActiveChunk();
        if (activeChunk == null) {
            // Should never happen but just in case for mods, always tick
            return true;
        }

        if (!activeChunk.bridge$isActive()) {
            return false;
        }

        final long currentTick = SpongeImpl.getServer().getTickCounter();
        final ActivationCapability spongeTileEntity = (ActivationCapability) tileEntity;
        boolean isActive = activeChunk.bridge$isPersistedChunk() || spongeTileEntity.activation$getActivatedTick() >= currentTick || spongeTileEntity.activation$getDefaultActivationState();

        // Should this tileentity tick?
        if (!isActive) {
            if (spongeTileEntity.activation$getActivatedTick() == Integer.MIN_VALUE) {
                // Has not come across a player
                return false;
            }
        }

        // check tick rate
        if (isActive && world.getWorldInfo().getGameTime() % spongeTileEntity.activation$getSpongeTickRate() != 0L) {
            isActive = false;
        }

        return isActive;
    }

    public static void addTileEntityToConfig(final World world, final SpongeTileEntityType type) {
        final SpongeConfig<WorldConfig> worldConfigAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        final SpongeConfig<GlobalConfig> globalConfigAdapter = SpongeImpl.getGlobalConfigAdapter();
        if (!worldConfigAdapter.getConfig().getTileEntityActivationRange().autoPopulateData()) {
            return;
        }

        boolean requiresSave = false;
        final String tileModId = type.getModId().toLowerCase();
        final TileEntityActivationCategory activationCategory = globalConfigAdapter.getConfig().getTileEntityActivationRange();
        TileEntityActivationModCategory tileEntityMod = activationCategory.getModList().get(tileModId);
        final int defaultRange = activationCategory.getDefaultBlockRange();
        final int defaultTickRate = activationCategory.getDefaultTickRate();
        if (tileEntityMod == null) {
            tileEntityMod = new TileEntityActivationModCategory(tileModId);
            activationCategory.getModList().put(tileModId, tileEntityMod);
            requiresSave = true;
        }

        if (tileEntityMod != null) {
            // check for tileentity range overrides
            final String tileId = type.getName().toLowerCase();
            final Integer tileEntityActivationRange = tileEntityMod.getTileEntityRangeList().get(tileId);
            Integer modDefaultRange = tileEntityMod.getDefaultBlockRange();
            if (modDefaultRange == null) {
                modDefaultRange = defaultRange;
            }
            if (tileEntityActivationRange == null) {
                tileEntityMod.getTileEntityRangeList().put(tileId, modDefaultRange);
                requiresSave = true;
            }

            // check for tileentity tick rate overrides
            Integer modDefaultTickRate = tileEntityMod.getDefaultTickRate();
            if (modDefaultTickRate == null) {
                modDefaultTickRate = defaultTickRate;
            }
            final Integer tileEntityActivationTickRate = tileEntityMod.getTileEntityTickRateList().get(tileId);
            if (tileEntityActivationTickRate == null) {
                tileEntityMod.getTileEntityTickRateList().put(tileId, modDefaultTickRate);
                requiresSave = true;
            }
        }

        if (requiresSave) {
            globalConfigAdapter.save();
        }
    }
}

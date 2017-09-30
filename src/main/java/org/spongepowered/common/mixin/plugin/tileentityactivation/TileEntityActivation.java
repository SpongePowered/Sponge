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

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.TileEntityActivationModCategory;
import org.spongepowered.common.config.category.TileEntityActivationCategory;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;
import org.spongepowered.common.util.VecHelper;

import java.util.Map;

public class TileEntityActivation {

    /**
     * These tileentities are excluded from Activation range checks.
     *
     * @param tileEntity The tileentity to check
     * @return boolean If it should always tick.
     */
    public static boolean initializeTileEntityActivationState(TileEntity tileEntity) {
        if (tileEntity.getWorld() == null || tileEntity.getWorld().isRemote || !(tileEntity instanceof ITickable)) {
            return true;
        }

        TileEntityActivationCategory config = ((IMixinWorldServer) tileEntity.getWorld()).getActiveConfig().getConfig().getTileEntityActivationRange();
        TileEntityType type = ((org.spongepowered.api.block.tileentity.TileEntity) tileEntity).getType();

        IModData_Activation spongeTileEntity = (IModData_Activation) tileEntity;
        SpongeTileEntityType spongeType = (SpongeTileEntityType) type;
        if (spongeType == null || spongeType.getModId() == null) {
            return true;
        }
        TileEntityActivationModCategory tileEntityMod = config.getModList().get(spongeType.getModId().toLowerCase());
        int defaultActivationRange = config.getDefaultBlockRange();
        int defaultTickRate = config.getDefaultTickRate();
        if (tileEntityMod == null) {
            // use default activation range
            spongeTileEntity.setActivationRange(defaultActivationRange);
            if (defaultActivationRange <= 0) {
                return true;
            }
            return false;
        } else if (!tileEntityMod.isEnabled()) {
            spongeTileEntity.setActivationRange(defaultActivationRange);
            spongeTileEntity.setSpongeTickRate(defaultTickRate);
            return true;
        }

        Integer defaultModActivationRange = tileEntityMod.getDefaultBlockRange();
        Integer tileEntityActivationRange = tileEntityMod.getTileEntityRangeList().get(type.getName().toLowerCase());
        if (defaultModActivationRange != null && tileEntityActivationRange == null) {
            spongeTileEntity.setActivationRange(defaultModActivationRange);
            if (defaultModActivationRange <= 0) {
                return true;
            }
        } else if (tileEntityActivationRange != null) {
            spongeTileEntity.setActivationRange(tileEntityActivationRange);
            if (tileEntityActivationRange <= 0) {
                return true;
            }
        }

        Integer defaultModTickRate = tileEntityMod.getDefaultTickRate();
        Integer tileEntityTickRate = tileEntityMod.getTileEntityTickRateList().get(type.getName().toLowerCase());
        if (defaultModTickRate != null && tileEntityTickRate == null) {
            spongeTileEntity.setSpongeTickRate(defaultModTickRate);
            if (defaultModTickRate <= 0) {
                return true;
            }
            return false;
        } else if (tileEntityTickRate != null) {
            spongeTileEntity.setSpongeTickRate(tileEntityTickRate);
            if (tileEntityTickRate <= 0) {
                return true;
            }
        }

        return false;
    }

    /**
    * Find what tileentities are in range of the players in the world and set
    * active if in range.
    *
    * @param world The world to perform activation checks in
    */
    public static void activateTileEntities(WorldServer world) {
        final PlayerChunkMap playerChunkMap = world.getPlayerChunkMap();
        for (PlayerChunkMapEntry playerChunkMapEntry : playerChunkMap.entries) {
            for (EntityPlayer player : playerChunkMapEntry.players) {
                final Chunk chunk = playerChunkMapEntry.chunk;
                if (chunk == null || chunk.unloadQueued) {
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
    private static void activateChunkTileEntities(EntityPlayer player, Chunk chunk) {
        final Vector3i playerPos = VecHelper.toVector3i(player.getPosition());
        final long currentTick = SpongeImpl.getServer().getTickCounter();
        for (Map.Entry<BlockPos, TileEntity> mapEntry : chunk.getTileEntityMap().entrySet()) {
            final TileEntity tileEntity = mapEntry.getValue();
            if (!(tileEntity instanceof ITickable) || ((IModData_Activation) tileEntity).getActivatedTick() == currentTick) {
                // already activated
                continue;
            }

            final Vector3i tilePos = VecHelper.toVector3i(tileEntity.getPos());
            if (currentTick > ((IModData_Activation) tileEntity).getActivatedTick()) {
                if (((IModData_Activation) tileEntity).getDefaultActivationState()) {
                    ((IModData_Activation) tileEntity).setActivatedTick(currentTick);
                    continue;
                }

                IModData_Activation spongeEntity = (IModData_Activation) tileEntity;
                // check if activation cache needs to be updated
                if (spongeEntity.requiresActivationCacheRefresh()) {
                    TileEntityActivation.initializeTileEntityActivationState(tileEntity);
                    spongeEntity.requiresActivationCacheRefresh(false);
                }

                int bbActivationRange = ((IModData_Activation) tileEntity).getActivationRange();
                int blockDistance = Math.round(tilePos.distance(playerPos));
                if (blockDistance <= bbActivationRange) {
                    ((IModData_Activation) tileEntity).setActivatedTick(currentTick);
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
    public static boolean checkIfActive(TileEntity tileEntity) {
        if (tileEntity.getWorld() == null || tileEntity.getWorld().isRemote || !(tileEntity instanceof ITickable)) {
            return true;
        }

        final World world = tileEntity.getWorld();
        final IMixinChunk activeChunk = ((IMixinTileEntity) tileEntity).getActiveChunk();
        if (activeChunk == null) {
            // Should never happen but just in case for mods, always tick
            return true;
        }

        long currentTick = SpongeImpl.getServer().getTickCounter();
        IModData_Activation spongeTileEntity = (IModData_Activation) tileEntity;
        boolean isActive = activeChunk.isPersistedChunk() || spongeTileEntity.getActivatedTick() >= currentTick || spongeTileEntity.getDefaultActivationState();

        // Should this entity tick?
        if (!isActive) {
            if (spongeTileEntity.getActivatedTick() == Integer.MIN_VALUE) {
                // Has not come across a player
                return false;
            }
        }

        // check tick rate
        if (isActive && world.getWorldInfo().getWorldTotalTime() % spongeTileEntity.getSpongeTickRate() != 0L) {
            isActive = false;
        }

        return isActive;
    }

    public static void addTileEntityToConfig(World world, SpongeTileEntityType type) {
        checkNotNull(world, "world");
        checkNotNull(type, "type");

        SpongeConfig<?> config = ((IMixinWorldServer) world).getActiveConfig();
        if (config == null || type == null || !config.getConfig().getTileEntityActivationRange().autoPopulateData()) {
            return;
        }

        boolean requiresSave = false;
        final String tileModId = type.getModId().toLowerCase();
        TileEntityActivationCategory activationCategory = config.getConfig().getTileEntityActivationRange();
        TileEntityActivationModCategory tileEntityMod = activationCategory.getModList().get(tileModId);
        int defaultRange = activationCategory.getDefaultBlockRange();
        int defaultTickRate = activationCategory.getDefaultTickRate();
        if (tileEntityMod == null) {
            tileEntityMod = new TileEntityActivationModCategory(tileModId);
            activationCategory.getModList().put(tileModId, tileEntityMod);
            requiresSave = true;
        }

        if (tileEntityMod != null) {
            // check for tileentity range overrides
            final String tileId = type.getName().toLowerCase();
            Integer tileEntityActivationRange = tileEntityMod.getTileEntityRangeList().get(tileId);
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
            Integer tileEntityActivationTickRate = tileEntityMod.getTileEntityTickRateList().get(tileId);
            if (tileEntityActivationTickRate == null) {
                tileEntityMod.getTileEntityTickRateList().put(tileId, modDefaultTickRate);
                requiresSave = true;
            }
        }

        if (requiresSave) {
            config.save();
        }
    }
}

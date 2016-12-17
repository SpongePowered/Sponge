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
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
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
        if (tileEntity.getWorld() == null || tileEntity.getWorld().isRemote) {
            return true;
        }

        TileEntityActivationCategory config = ((IMixinWorldServer) tileEntity.getWorld()).getActiveConfig().getConfig().getTileEntityActivationRange();
        TileEntityType type = ((org.spongepowered.api.block.tileentity.TileEntity) tileEntity).getType();

        IModData_Activation spongeEntity = (IModData_Activation) tileEntity;
        SpongeTileEntityType spongeType = (SpongeTileEntityType) type;
        TileEntityActivationModCategory tileEntityMod = config.getModList().get(spongeType.getModId());
        int defaultActivationRange = config.getDefaultBlockRange();
        if (tileEntityMod == null) {
            // use default activation range
            spongeEntity.setActivationRange(defaultActivationRange);
            if (defaultActivationRange <= 0) {
                return true;
            }
            return false;
        } else if (!tileEntityMod.isEnabled()) {
            spongeEntity.setActivationRange(defaultActivationRange);
            return true;
        }

        Integer defaultModActivationRange = tileEntityMod.getDefaultBlockRange();
        Integer tileEntityActivationRange = tileEntityMod.getTileEntityRangeList().get(type.getName());
        if (defaultModActivationRange != null && tileEntityActivationRange == null) {
            spongeEntity.setActivationRange(defaultModActivationRange);
            if (defaultModActivationRange <= 0) {
                return true;
            }
            return false;
        } else if (tileEntityActivationRange != null) {
            spongeEntity.setActivationRange(tileEntityActivationRange);
            if (tileEntityActivationRange <= 0) {
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
                if (chunk == null || chunk.unloaded) {
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
            if (((IModData_Activation) tileEntity).getActivatedTick() == currentTick) {
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
        if (tileEntity.getWorld() == null || tileEntity.getWorld().isRemote) {
            return true;
        }

        final World world = tileEntity.getWorld();
        long currentTick = SpongeImpl.getServer().getTickCounter();
        IModData_Activation spongeTileEntity = (IModData_Activation) tileEntity;
        boolean isActive = spongeTileEntity.getActivatedTick() >= currentTick || spongeTileEntity.getDefaultActivationState();

        // Should this entity tick?
        if (!isActive) {
            if (spongeTileEntity.getActivatedTick() == Integer.MIN_VALUE) {
                // Has not come across a player
                return false;
            }

        // Add a little performance juice to active entities. Skip 1/4 if not immune.
        } else if (!spongeTileEntity.getDefaultActivationState() && spongeTileEntity.getTicksExisted() % 4 == 0) {
            isActive = false;
        }

        // Make sure not on edge of unloaded chunk
        int x = tileEntity.getPos().getX();
        int z = tileEntity.getPos().getZ();
        IMixinChunk spongeChunk = (IMixinChunk)((IMixinChunkProviderServer) world.getChunkProvider()).getLoadedChunkWithoutMarkingActive(x >> 4, z >> 4);
        if (isActive && (spongeChunk == null || (!spongeChunk.isPersistedChunk() && !spongeChunk.areNeighborsLoaded()))) {
            isActive = false;
        }

        if (!isActive && spongeChunk != null && spongeChunk.isPersistedChunk()) {
            isActive = true;
        }

        // check tick rate
        if (isActive && world.getWorldInfo().getWorldTotalTime() % spongeTileEntity.getTickRate() != 0L) {
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
        TileEntityActivationCategory activationCategory = config.getConfig().getTileEntityActivationRange();
        TileEntityActivationModCategory tileEntityMod = activationCategory.getModList().get(type.getModId());
        int defaultRange = activationCategory.getDefaultBlockRange();
        int defaultTickRate = activationCategory.getDefaultTickRate();
        if (tileEntityMod == null) {
            tileEntityMod = new TileEntityActivationModCategory(type.getModId());
            activationCategory.getModList().put(type.getModId(), tileEntityMod);
            requiresSave = true;
        }

        if (tileEntityMod != null) {
            // check for tileentity range overrides
            Integer tileEntityActivationRange = tileEntityMod.getTileEntityRangeList().get(type.getName());
            Integer modDefaultRange = tileEntityMod.getDefaultBlockRange();
            if (modDefaultRange == null) {
                modDefaultRange = defaultRange;
            }
            if (tileEntityActivationRange == null) {
                tileEntityMod.getTileEntityRangeList().put(type.getName(), modDefaultRange);
                requiresSave = true;
            }

            // check for tileentity tick rate overrides
            Integer modDefaultTickRate = tileEntityMod.getDefaultTickRate();
            if (modDefaultTickRate == null) {
                modDefaultTickRate = defaultTickRate;
            }
            Integer tileEntityActivationTickRate = tileEntityMod.getTileEntityTickRateList().get(type.getName());
            if (tileEntityActivationTickRate == null) {
                tileEntityMod.getTileEntityTickRateList().put(type.getName(), modDefaultTickRate);
                requiresSave = true;
            }
        }

        if (requiresSave) {
            config.save();
        }
    }
}

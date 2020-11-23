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
package org.spongepowered.common.mixin.tracker.world;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.profile.SpongeGameProfileManager;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeHooks;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin_Tracker implements ChunkBridge {

    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;

    @Shadow public abstract World shadow$getWorld();

    private Map<Integer, PlayerTracker> trackertracker$trackedIntBlockPositions = new HashMap<>();
    private Map<Short, PlayerTracker> trackertracker$trackedShortBlockPositions = new HashMap<>();

    @Override
    public void bridge$addTrackedBlockPosition(final Block block, final BlockPos pos, final User user, final PlayerTracker.Type trackerType) {
        if (((WorldBridge) this.shadow$getWorld()).bridge$isFake()) {
            return;
        }
        if (!PhaseTracker.getInstance().getCurrentState().tracksCreatorsAndNotifiers()) {
            // Don't track chunk gen
            return;
        }
        // Don't track fake players
        if (user instanceof ServerPlayerEntity && SpongeImplHooks.isFakePlayer((ServerPlayerEntity) user.getPlayer().orElse(null))) {
            return;
        }
        // Update TE tracking cache
        // We must always check for a TE as a mod block may not implement ITileEntityProvider if a TE exists
        // Note: We do not check SpongeImplHooks.hasBlockTileEntity(block, state) as neighbor notifications do not
        //       include blockstate.
        final TileEntity tileEntity = this.tileEntities.get(pos);
        if (tileEntity != null) {
            if (tileEntity instanceof CreatorTrackedBridge) {
                final CreatorTrackedBridge creatorBridge = (CreatorTrackedBridge) tileEntity;
                if (trackerType == PlayerTracker.Type.NOTIFIER) {
                    if (creatorBridge.tracked$getNotifierReference().orElse(null) == user) {
                        return;
                    }
                    creatorBridge.tracked$setNotifier(user);
                } else {
                    if (creatorBridge.tracked$getCreatorReference().orElse(null) == user) {
                        return;
                    }
                    creatorBridge.tracked$setCreatorReference(user);
                }
            }
        }

        final InheritableConfigHandle<WorldConfig> configAdapter = ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.get().getLogging().blockTrackLogging()) {
            SpongeHooks.logBlockTrack(this.shadow$getWorld(), block, pos, user,
                    !configAdapter.get().getBlockTracking().getBlockBlacklist().contains(((BlockType) block).getKey().toString()));
        }

        final WorldInfoBridge infoBridge = (WorldInfoBridge) this.shadow$getWorld().getWorldInfo();
        final int indexForUniqueId = infoBridge.bridge$getIndexForUniqueId(user.getUniqueId());
        if (pos.getY() <= 255) {
            final short blockPos = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker playerTracker = this.trackertracker$trackedShortBlockPositions.get(blockPos);
            if (playerTracker != null) {
                if (trackerType == PlayerTracker.Type.CREATOR) {
                    playerTracker.creatorindex = indexForUniqueId;
                    playerTracker.notifierIndex = indexForUniqueId;
                } else {
                    playerTracker.notifierIndex = indexForUniqueId;
                }
            } else {
                this.trackertracker$trackedShortBlockPositions.put(blockPos, new PlayerTracker(indexForUniqueId, trackerType));
            }
        } else {
            final int blockPos = Constants.Sponge.blockPosToInt(pos);
            final PlayerTracker playerTracker = this.trackertracker$trackedIntBlockPositions.get(blockPos);
            if (playerTracker != null) {
                if (trackerType == PlayerTracker.Type.CREATOR) {
                    playerTracker.creatorindex = indexForUniqueId;
                    playerTracker.notifierIndex = indexForUniqueId;
                } else {
                    playerTracker.notifierIndex = indexForUniqueId;
                }
            } else {
                this.trackertracker$trackedIntBlockPositions.put(blockPos, new PlayerTracker(indexForUniqueId, trackerType));
            }
        }
    }

    @Override
    public Map<Integer, PlayerTracker> bridge$getTrackedIntPlayerPositions() {
        return this.trackertracker$trackedIntBlockPositions;
    }

    @Override
    public Map<Short, PlayerTracker> bridge$getTrackedShortPlayerPositions() {
        return this.trackertracker$trackedShortBlockPositions;
    }

    @Override
    public Optional<User> bridge$getBlockCreator(final BlockPos pos) {
        if (((WorldBridge) this.shadow$getWorld()).bridge$isFake()) {
            return Optional.empty();
        }
        final int intKey = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackertracker$trackedIntBlockPositions.get(intKey);
        if (intTracker != null) {
            final int notifierIndex = intTracker.creatorindex;
            return this.tracker$getValidatedUser(intKey, notifierIndex);
        } else {
            final short shortKey = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackertracker$trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                final int notifierIndex = shortTracker.creatorindex;
                return this.tracker$getValidatedUser(shortKey, notifierIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<UUID> bridge$getBlockCreatorUUID(final BlockPos pos) {
        if (((WorldBridge) this.shadow$getWorld()).bridge$isFake()) {
            return Optional.empty();
        }
        final int key = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackertracker$trackedIntBlockPositions.get(key);
        if (intTracker != null) {
            final int creatorIndex = intTracker.creatorindex;
            return this.tracker$getValidatedUUID(key, creatorIndex);
        } else {
            final short shortKey = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackertracker$trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                final int creatorIndex = shortTracker.creatorindex;
                return this.tracker$getValidatedUUID(shortKey, creatorIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> bridge$getBlockNotifier(final BlockPos pos) {
        if (((WorldBridge) this.shadow$getWorld()).bridge$isFake()) {
            return Optional.empty();
        }
        final int intKey = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackertracker$trackedIntBlockPositions.get(intKey);
        if (intTracker != null) {
            return this.tracker$getValidatedUser(intKey, intTracker.notifierIndex);
        } else {
            final short shortKey = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackertracker$trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                return this.tracker$getValidatedUser(shortKey, shortTracker.notifierIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<UUID> bridge$getBlockNotifierUUID(final BlockPos pos) {
        if (((WorldBridge) this.shadow$getWorld()).bridge$isFake()) {
            return Optional.empty();
        }
        final int key = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackertracker$trackedIntBlockPositions.get(key);
        if (intTracker != null) {
            return this.tracker$getValidatedUUID(key, intTracker.notifierIndex);
        } else {
            final short shortKey = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackertracker$trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                return this.tracker$getValidatedUUID(shortKey, shortTracker.notifierIndex);
            }
        }

        return Optional.empty();
    }

    private Optional<User> tracker$getValidatedUser(final int key, final int creatorIndex) {
        final Optional<UUID> uuid = this.tracker$getValidatedUUID(key, creatorIndex);
        if (uuid.isPresent()) {
            final UUID userUniqueId = uuid.get();
            // get player if online
            final PlayerEntity player = this.shadow$getWorld().getPlayerByUuid(userUniqueId);
            if (player != null) {
                return Optional.ofNullable(((ServerPlayerEntityBridge) player).bridge$getUser());
            }
            // player is not online, get or create user from storage
            return this.tracker$getUserFromId(userUniqueId);
        }
        return Optional.empty();
    }

    private Optional<UUID> tracker$getValidatedUUID(final int key, final int creatorIndex) {
        final UUID uuid = (((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$getUniqueIdForIndex(creatorIndex)).orElse(null);
        if (uuid != null) {
            // Verify id is valid and not invalid
            if (SpongeConfigs.getCommon().get().getWorld().getInvalidLookupUuids().contains(uuid)) {
                this.trackertracker$trackedIntBlockPositions.remove(key);
                return Optional.empty();
            }
            // player is not online, get or create user from storage
            return Optional.of(uuid);
        }
        return Optional.empty();
    }

    private Optional<User> tracker$getUserFromId(final UUID uuid) {
        // check username cache
        final SpongeUserManager userManager = this.getUserManager();
        final Server server = (Server) this.shadow$getWorld().getServer();
        final String username = ((SpongeServer) server).getUsernameCache().getLastKnownUsername(uuid);
        if (username != null && userManager != null) {
            return userManager.get(GameProfile.of(uuid, username));
        }

        // check mojang cache
        final GameProfile profile = server.getGameProfileManager().getCache().getById(uuid).orElse(null);
        if (profile != null && userManager != null) {
            return userManager.get(profile);
        }

        // If we reach this point, queue UUID for async lookup and return empty
        ((SpongeGameProfileManager) server.getGameProfileManager()).lookupUserAsync(uuid);
        return Optional.empty();
    }

    // Special setter used by API
    @Override
    public void bridge$setBlockNotifier(final BlockPos pos, @Nullable final UUID uuid) {
        if (((WorldBridge) this.shadow$getWorld()).bridge$isFake()) {
            return;
        }
        if (pos.getY() <= 255) {
            final short blockPos = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackertracker$trackedShortBlockPositions.get(blockPos);
            if (shortTracker != null) {
                shortTracker.notifierIndex = uuid == null ? -1 : ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$getIndexForUniqueId(uuid);
            } else {
                this.trackertracker$trackedShortBlockPositions.put(blockPos,
                        new PlayerTracker(uuid == null ? -1 : ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$getIndexForUniqueId(uuid),
                                PlayerTracker.Type.NOTIFIER));
            }
        } else {
            final int blockPos = Constants.Sponge.blockPosToInt(pos);
            final PlayerTracker intTracker = this.trackertracker$trackedIntBlockPositions.get(blockPos);
            if (intTracker != null) {
                intTracker.notifierIndex = uuid == null ? -1 : ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$getIndexForUniqueId(uuid);
            } else {
                this.trackertracker$trackedIntBlockPositions.put(blockPos,
                        new PlayerTracker(uuid == null ? -1 : ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$getIndexForUniqueId(uuid),
                                PlayerTracker.Type.NOTIFIER));
            }
        }
    }

    // Special setter used by API
    @Override
    public void bridge$setBlockCreator(final BlockPos pos, @Nullable final UUID uuid) {
        if (((WorldBridge) this.shadow$getWorld()).bridge$isFake()) {
            return;
        }
        if (pos.getY() <= 255) {
            final short blockPos = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackertracker$trackedShortBlockPositions.get(blockPos);
            if (shortTracker != null) {
                shortTracker.creatorindex = uuid == null ? -1 : ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$getIndexForUniqueId(uuid);
            } else {
                this.trackertracker$trackedShortBlockPositions.put(blockPos, new PlayerTracker(uuid == null ? -1 : ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo())
                        .bridge$getIndexForUniqueId(uuid), PlayerTracker.Type.CREATOR));
            }
        } else {
            final int blockPos = Constants.Sponge.blockPosToInt(pos);
            final PlayerTracker intTracker = this.trackertracker$trackedIntBlockPositions.get(blockPos);
            if (intTracker != null) {
                intTracker.creatorindex = uuid == null ? -1 : ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo()).bridge$getIndexForUniqueId(uuid);
            } else {
                this.trackertracker$trackedIntBlockPositions.put(blockPos, new PlayerTracker(uuid == null ? -1 : ((WorldInfoBridge) this.shadow$getWorld().getWorldInfo())
                        .bridge$getIndexForUniqueId(uuid), PlayerTracker.Type.CREATOR));
            }
        }
    }

    @Override
    public void bridge$setTrackedIntPlayerPositions(final Map<Integer, PlayerTracker> trackedPositions) {
        this.trackertracker$trackedIntBlockPositions = trackedPositions;
    }

    @Override
    public void bridge$setTrackedShortPlayerPositions(final Map<Short, PlayerTracker> trackedPositions) {
        this.trackertracker$trackedShortBlockPositions = trackedPositions;
    }

    private SpongeUserManager getUserManager() {
        final World world = this.shadow$getWorld();
        if (world == null || ((WorldBridge) world).bridge$isFake()) {
            return null;
        }

        return (SpongeUserManager) ((Server) world.getServer()).getUserManager();
    }
}

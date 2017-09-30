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
package org.spongepowered.common.mixin.tracking.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(value = net.minecraft.world.chunk.Chunk.class, priority = 1111)
public abstract class MixinChunk_Tracker implements Chunk, IMixinChunk {

    private static final int NUM_XZ_BITS = 4;
    private static final int NUM_SHORT_Y_BITS = 8;
    private static final int NUM_INT_Y_BITS = 24;
    private static final int Y_SHIFT = NUM_XZ_BITS;
    private static final int Z_SHORT_SHIFT = Y_SHIFT + NUM_SHORT_Y_BITS;
    private static final int Z_INT_SHIFT = Y_SHIFT + NUM_INT_Y_BITS;
    private static final short XZ_MASK = 0xF;
    private static final short Y_SHORT_MASK = 0xFF;
    private static final int Y_INT_MASK = 0xFFFFFF;
    private SpongeProfileManager spongeProfileManager;
    private UserStorageService userStorageService;

    @Shadow @Final private World world;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private ExtendedBlockStorage[] storageArrays;
    @Shadow @Final private int[] precipitationHeightMap;
    @Shadow @Final private int[] heightMap;
    @Shadow private boolean dirty;

    public Map<Integer, PlayerTracker> trackedIntBlockPositions = new Int2ObjectArrayMap<>();
    public Map<Short, PlayerTracker> trackedShortBlockPositions = new Short2ObjectArrayMap<>();

    @Final // need this constructor to never be overwritten by anything.
    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"), remap = false)
    public void onConstructedTracker(World world, int x, int z, CallbackInfo ci) {
        if (!world.isRemote) {
            this.spongeProfileManager = ((SpongeProfileManager) Sponge.getServer().getGameProfileManager());
            this.userStorageService = SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get();
        }
    }

    @Override
    public void addTrackedBlockPosition(Block block, BlockPos pos, User user, PlayerTracker.Type trackerType) {
        if (this.world.isRemote) {
            return;
        }
        if (PhaseTracker.getInstance().getCurrentState().ignoresBlockTracking()) {
            // Don't track chunk gen
            return;
        }
        // Don't track fake players
        if (user instanceof EntityPlayerMP && SpongeImplHooks.isFakePlayer((EntityPlayerMP) user)) {
            return;
        }

        if (!SpongeHooks.getActiveConfig((WorldServer) this.world).getConfig().getBlockTracking().getBlockBlacklist().contains(((BlockType) block).getId())) {
            SpongeHooks.logBlockTrack(this.world, block, pos, user, true);
        } else {
            SpongeHooks.logBlockTrack(this.world, block, pos, user, false);
        }

        final IMixinWorldInfo worldInfo = (IMixinWorldInfo) this.world.getWorldInfo();
        final int indexForUniqueId = worldInfo.getIndexForUniqueId(user.getUniqueId());
        if (pos.getY() <= 255) {
            short blockPos = blockPosToShort(pos);
            final PlayerTracker playerTracker = this.trackedShortBlockPositions.get(blockPos);
            if (playerTracker != null) {
                if (trackerType == PlayerTracker.Type.OWNER) {
                    playerTracker.ownerIndex = indexForUniqueId;
                    playerTracker.notifierIndex = indexForUniqueId;
                } else {
                    playerTracker.notifierIndex = indexForUniqueId;
                }
            } else {
                this.trackedShortBlockPositions.put(blockPos, new PlayerTracker(indexForUniqueId, trackerType));
            }
        } else {
            int blockPos = blockPosToInt(pos);
            final PlayerTracker playerTracker = this.trackedIntBlockPositions.get(blockPos);
            if (playerTracker != null) {
                if (trackerType == PlayerTracker.Type.OWNER) {
                    playerTracker.ownerIndex = indexForUniqueId;
                } else {
                    playerTracker.notifierIndex = indexForUniqueId;
                }
            } else {
                this.trackedIntBlockPositions.put(blockPos, new PlayerTracker(indexForUniqueId, trackerType));
            }
        }
    }

    @Override
    public Map<Integer, PlayerTracker> getTrackedIntPlayerPositions() {
        return this.trackedIntBlockPositions;
    }

    @Override
    public Map<Short, PlayerTracker> getTrackedShortPlayerPositions() {
        return this.trackedShortBlockPositions;
    }

    @Override
    public Optional<User> getBlockOwner(BlockPos pos) {
        final int intKey = blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackedIntBlockPositions.get(intKey);
        if (intTracker != null) {
            final int notifierIndex = intTracker.ownerIndex;
            return getValidatedUser(intKey, notifierIndex);
        } else {
            final short shortKey = blockPosToShort(pos);
            if (this.trackedShortBlockPositions.get(shortKey) != null) {
                PlayerTracker tracker = this.trackedShortBlockPositions.get(shortKey);
                final int notifierIndex = tracker.ownerIndex;
                return getValidatedUser(shortKey, notifierIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<UUID> getBlockOwnerUUID(BlockPos pos) {
        final int key = blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackedIntBlockPositions.get(key);
        if (intTracker != null) {
            final int ownerIndex = intTracker.ownerIndex;
            return getValidatedUUID(key, ownerIndex);
        } else {
            final short shortKey = blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                final int ownerIndex = shortTracker.ownerIndex;
                return getValidatedUUID(shortKey, ownerIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> getBlockNotifier(BlockPos pos) {
        final int intKey = blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackedIntBlockPositions.get(intKey);
        if (intTracker != null) {
            final int notifierIndex = intTracker.notifierIndex;
            return getValidatedUser(intKey, notifierIndex);
        } else {
            final short shortKey = blockPosToShort(pos);
            if (this.trackedShortBlockPositions.get(shortKey) != null) {
                PlayerTracker tracker = this.trackedShortBlockPositions.get(shortKey);
                final int notifierIndex = tracker.notifierIndex;
                return getValidatedUser(shortKey, notifierIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<UUID> getBlockNotifierUUID(BlockPos pos) {
        final int key = blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackedIntBlockPositions.get(key);
        if (intTracker != null) {
            final int ownerIndex = intTracker.notifierIndex;
            return getValidatedUUID(key, ownerIndex);
        } else {
            final short shortKey = blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                final int ownerIndex = shortTracker.notifierIndex;
                return getValidatedUUID(shortKey, ownerIndex);
            }
        }

        return Optional.empty();
    }

    private Optional<User> getValidatedUser(int key, int ownerIndex) {
        Optional<UUID> uuid = getValidatedUUID(key, ownerIndex);
        if (uuid.isPresent()) {
            UUID userUniqueId = uuid.get();
            // get player if online
            EntityPlayer player = this.world.getPlayerEntityByUUID(userUniqueId);
            if (player != null) {
                return Optional.of((User) player);
            }
            // player is not online, get or create user from storage
            return this.getUserFromId(userUniqueId);
        }
        return Optional.empty();
    }

    private Optional<UUID> getValidatedUUID(int key, int ownerIndex) {
        UUID uuid = (((IMixinWorldInfo) this.world.getWorldInfo()).getUniqueIdForIndex(ownerIndex)).orElse(null);
        if (uuid != null) {
            UUID userUniqueId = uuid;
            // Verify id is valid and not invalid
            if (SpongeImpl.getGlobalConfig().getConfig().getWorld().getInvalidLookupUuids().contains(userUniqueId)) {
                this.trackedIntBlockPositions.remove(key);
                return Optional.empty();
            }
            // player is not online, get or create user from storage
            return Optional.of(userUniqueId);
        }
        return Optional.empty();
    }

    private Optional<User> getUserFromId(UUID uuid) {
        // check username cache
        String username = SpongeUsernameCache.getLastKnownUsername(uuid);
        if (username != null) {
            return this.userStorageService.get(GameProfile.of(uuid, username));
        }

        // check mojang cache
        GameProfile profile = this.spongeProfileManager.getCache().getById(uuid).orElse(null);
        if (profile != null) {
            return this.userStorageService.get(profile);
        }

        // If we reach this point, queue UUID for async lookup and return empty
        this.spongeProfileManager.lookupUserAsync(uuid);
        return Optional.empty();
    }

    // Special setter used by API
    @Override
    public void setBlockNotifier(BlockPos pos, @Nullable UUID uuid) {
        if (pos.getY() <= 255) {
            short blockPos = blockPosToShort(pos);
            if (this.trackedShortBlockPositions.get(blockPos) != null) {
                this.trackedShortBlockPositions.get(blockPos).notifierIndex = uuid == null ? -1 :
                                                                              ((IMixinWorldInfo) this.world.getWorldInfo()).getIndexForUniqueId(uuid);
            } else {
                this.trackedShortBlockPositions.put(blockPos,
                        new PlayerTracker(uuid == null ? -1 : ((IMixinWorldInfo) this.world.getWorldInfo()).getIndexForUniqueId(uuid),
                                PlayerTracker.Type.NOTIFIER));
            }
        } else {
            int blockPos = blockPosToInt(pos);
            if (this.trackedIntBlockPositions.get(blockPos) != null) {
                this.trackedIntBlockPositions.get(blockPos).notifierIndex = uuid == null ? -1 :
                                                                            ((IMixinWorldInfo) this.world.getWorldInfo()).getIndexForUniqueId(uuid);
            } else {
                this.trackedIntBlockPositions.put(blockPos,
                        new PlayerTracker(uuid == null ? -1 : ((IMixinWorldInfo) this.world.getWorldInfo()).getIndexForUniqueId(uuid),
                                PlayerTracker.Type.NOTIFIER));
            }
        }
    }

    // Special setter used by API
    @Override
    public void setBlockCreator(BlockPos pos, @Nullable UUID uuid) {
        if (pos.getY() <= 255) {
            short blockPos = blockPosToShort(pos);
            if (this.trackedShortBlockPositions.get(blockPos) != null) {
                this.trackedShortBlockPositions.get(blockPos).ownerIndex = uuid == null ? -1 : ((IMixinWorldInfo) this.world.getWorldInfo())
                        .getIndexForUniqueId(uuid);
            } else {
                this.trackedShortBlockPositions.put(blockPos, new PlayerTracker(uuid == null ? -1 : ((IMixinWorldInfo) this.world.getWorldInfo())
                        .getIndexForUniqueId(uuid), PlayerTracker.Type.OWNER));
            }
        } else {
            int blockPos = blockPosToInt(pos);
            if (this.trackedIntBlockPositions.get(blockPos) != null) {
                this.trackedIntBlockPositions.get(blockPos).ownerIndex = uuid == null ? -1 : ((IMixinWorldInfo) this.world.getWorldInfo())
                        .getIndexForUniqueId(uuid);
            } else {
                this.trackedIntBlockPositions.put(blockPos, new PlayerTracker(uuid == null ? -1 : ((IMixinWorldInfo) this.world.getWorldInfo())
                        .getIndexForUniqueId(uuid), PlayerTracker.Type.OWNER));
            }
        }
    }

    @Override
    public void setTrackedIntPlayerPositions(Map<Integer, PlayerTracker> trackedPositions) {
        this.trackedIntBlockPositions = trackedPositions;
    }

    @Override
    public void setTrackedShortPlayerPositions(Map<Short, PlayerTracker> trackedPositions) {
        this.trackedShortBlockPositions = trackedPositions;
    }

    /**
     * Modifies bits in an integer.
     *
     * @param num Integer to modify
     * @param data Bits of data to add
     * @param which Index of nibble to start at
     * @param bitsToReplace The number of bits to replace starting from nibble index
     * @return The modified integer
     */
    private int setNibble(int num, int data, int which, int bitsToReplace) {
        return (num & ~(bitsToReplace << (which * 4)) | (data << (which * 4)));
    }

    @Inject(method = "onLoad", at = @At("HEAD"))
    private void startLoad(CallbackInfo callbackInfo) {
        if (!this.world.isRemote) {
            GenerationPhase.State.CHUNK_LOADING.createPhaseContext()
                    .source(this)
                    .world(this.world)
                    .buildAndSwitch();
        }
    }

    @Inject(method = "onLoad", at = @At("RETURN"))
    private void endLoad(CallbackInfo callbackInfo) {
        if (!this.world.isRemote) {
            PhaseTracker.getInstance().completePhase(GenerationPhase.State.CHUNK_LOADING);
        }
    }

    /**
     * Serialize this BlockPos into a short value
     */
    private short blockPosToShort(BlockPos pos) {
        short serialized = (short) setNibble(0, pos.getX() & XZ_MASK, 0, NUM_XZ_BITS);
        serialized = (short) setNibble(serialized, pos.getY() & Y_SHORT_MASK, 1, NUM_SHORT_Y_BITS);
        serialized = (short) setNibble(serialized, pos.getZ() & XZ_MASK, 3, NUM_XZ_BITS);
        return serialized;
    }

    /**
     * Create a BlockPos from a serialized chunk position
     */
    private BlockPos blockPosFromShort(short serialized) {
        int x = this.x * 16 + (serialized & XZ_MASK);
        int y = (serialized >> Y_SHIFT) & Y_SHORT_MASK;
        int z = this.z * 16 + ((serialized >> Z_SHORT_SHIFT) & XZ_MASK);
        return new BlockPos(x, y, z);
    }

    /**
     * Serialize this BlockPos into an int value
     */
    private int blockPosToInt(BlockPos pos) {
        int serialized = setNibble(0, pos.getX() & XZ_MASK, 0, NUM_XZ_BITS);
        serialized = setNibble(serialized, pos.getY() & Y_INT_MASK, 1, NUM_INT_Y_BITS);
        serialized = setNibble(serialized, pos.getZ() & XZ_MASK, 7, NUM_XZ_BITS);
        return serialized;
    }

    /**
     * Create a BlockPos from a serialized chunk position
     */
    private BlockPos blockPosFromInt(int serialized) {
        int x = this.x * 16 + (serialized & XZ_MASK);
        int y = (serialized >> Y_SHIFT) & Y_INT_MASK;
        int z = this.z * 16 + ((serialized >> Z_INT_SHIFT) & XZ_MASK);
        return new BlockPos(x, y, z);
    }

}

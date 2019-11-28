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

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(value = net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin_Tracker implements ChunkBridge {


    @Shadow @Final private World world;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;


    @Nullable private UserStorageService trackerImpl$userStorageService;
    private Map<Integer, PlayerTracker> trackerImpl$trackedIntBlockPositions = new HashMap<>();
    private Map<Short, PlayerTracker> trackerImpl$trackedShortBlockPositions = new HashMap<>();

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"))
    private void tracker$setUpUserService(@Nullable final World worldIn, final int x, final int z, final CallbackInfo ci) {
        this.trackerImpl$userStorageService = worldIn != null && !((WorldBridge) worldIn).bridge$isFake()
                                  ? null
                                  : SpongeImpl.getGame().getServiceManager().provideUnchecked(UserStorageService.class);

    }

    @Override
    public void bridge$addTrackedBlockPosition(final Block block, final BlockPos pos, final User user, final PlayerTracker.Type trackerType) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return;
        }
        if (!PhaseTracker.getInstance().getCurrentState().tracksOwnersAndNotifiers()) {
            // Don't track chunk gen
            return;
        }
        // Don't track fake players
        if (user instanceof ServerPlayerEntity && SpongeImplHooks.isFakePlayer((ServerPlayerEntity) user)) {
            return;
        }
        // Update TE tracking cache
        if (block instanceof ITileEntityProvider) {
            final TileEntity tileEntity = this.tileEntities.get(pos);
            if (tileEntity instanceof OwnershipTrackedBridge) {
                final OwnershipTrackedBridge ownerBridge = (OwnershipTrackedBridge) tileEntity;
                if (trackerType == PlayerTracker.Type.NOTIFIER) {
                    if (ownerBridge.tracked$getNotifierReference().orElse(null) == user) {
                        return;
                    }
                    ownerBridge.tracked$setNotifier(user);
                } else {
                    if (ownerBridge.tracked$getOwnerReference().orElse(null) == user) {
                        return;
                    }
                    ownerBridge.tracked$setOwnerReference(user);
                }
            }
        }

        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getConfigAdapter();
        if (configAdapter.getConfig().getLogging().blockTrackLogging()) {
            if (!configAdapter.getConfig().getBlockTracking().getBlockBlacklist().contains(((BlockType) block).getId())) {
                SpongeHooks.logBlockTrack(this.world, block, pos, user, true);
            } else {
                SpongeHooks.logBlockTrack(this.world, block, pos, user, false);
            }
        }

        final WorldInfoBridge worldInfo = (WorldInfoBridge) this.world.getWorldInfo();
        final int indexForUniqueId = worldInfo.bridge$getIndexForUniqueId(user.getUniqueId());
        if (pos.getY() <= 255) {
            final short blockPos = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker playerTracker = this.trackerImpl$trackedShortBlockPositions.get(blockPos);
            if (playerTracker != null) {
                if (trackerType == PlayerTracker.Type.OWNER) {
                    playerTracker.ownerIndex = indexForUniqueId;
                    playerTracker.notifierIndex = indexForUniqueId;
                } else {
                    playerTracker.notifierIndex = indexForUniqueId;
                }
            } else {
                this.trackerImpl$trackedShortBlockPositions.put(blockPos, new PlayerTracker(indexForUniqueId, trackerType));
            }
        } else {
            final int blockPos = Constants.Sponge.blockPosToInt(pos);
            final PlayerTracker playerTracker = this.trackerImpl$trackedIntBlockPositions.get(blockPos);
            if (playerTracker != null) {
                if (trackerType == PlayerTracker.Type.OWNER) {
                    playerTracker.ownerIndex = indexForUniqueId;
                    playerTracker.notifierIndex = indexForUniqueId;
                } else {
                    playerTracker.notifierIndex = indexForUniqueId;
                }
            } else {
                this.trackerImpl$trackedIntBlockPositions.put(blockPos, new PlayerTracker(indexForUniqueId, trackerType));
            }
        }
    }

    @Override
    public Map<Integer, PlayerTracker> bridge$getTrackedIntPlayerPositions() {
        return this.trackerImpl$trackedIntBlockPositions;
    }

    @Override
    public Map<Short, PlayerTracker> bridge$getTrackedShortPlayerPositions() {
        return this.trackerImpl$trackedShortBlockPositions;
    }

    @Override
    public Optional<User> bridge$getBlockOwner(final BlockPos pos) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return Optional.empty();
        }
        final int intKey = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackerImpl$trackedIntBlockPositions.get(intKey);
        if (intTracker != null) {
            final int notifierIndex = intTracker.ownerIndex;
            return this.tracker$getValidatedUser(intKey, notifierIndex);
        } else {
            final short shortKey = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackerImpl$trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                final int notifierIndex = shortTracker.ownerIndex;
                return this.tracker$getValidatedUser(shortKey, notifierIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<UUID> bridge$getBlockOwnerUUID(final BlockPos pos) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return Optional.empty();
        }
        final int key = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackerImpl$trackedIntBlockPositions.get(key);
        if (intTracker != null) {
            final int ownerIndex = intTracker.ownerIndex;
            return this.tracker$getValidatedUUID(key, ownerIndex);
        } else {
            final short shortKey = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackerImpl$trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                final int ownerIndex = shortTracker.ownerIndex;
                return this.tracker$getValidatedUUID(shortKey, ownerIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> bridge$getBlockNotifier(final BlockPos pos) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return Optional.empty();
        }
        final int intKey = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackerImpl$trackedIntBlockPositions.get(intKey);
        if (intTracker != null) {
            return this.tracker$getValidatedUser(intKey, intTracker.notifierIndex);
        } else {
            final short shortKey = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackerImpl$trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                return this.tracker$getValidatedUser(shortKey, shortTracker.notifierIndex);
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<UUID> bridge$getBlockNotifierUUID(final BlockPos pos) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return Optional.empty();
        }
        final int key = Constants.Sponge.blockPosToInt(pos);
        final PlayerTracker intTracker = this.trackerImpl$trackedIntBlockPositions.get(key);
        if (intTracker != null) {
            return this.tracker$getValidatedUUID(key, intTracker.notifierIndex);
        } else {
            final short shortKey = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackerImpl$trackedShortBlockPositions.get(shortKey);
            if (shortTracker != null) {
                return this.tracker$getValidatedUUID(shortKey, shortTracker.notifierIndex);
            }
        }

        return Optional.empty();
    }

    private Optional<User> tracker$getValidatedUser(final int key, final int ownerIndex) {
        final Optional<UUID> uuid = this.tracker$getValidatedUUID(key, ownerIndex);
        if (uuid.isPresent()) {
            final UUID userUniqueId = uuid.get();
            // get player if online
            final PlayerEntity player = this.world.func_152378_a(userUniqueId);
            if (player != null) {
                return Optional.of((User) player);
            }
            // player is not online, get or create user from storage
            return this.tracker$getUserFromId(userUniqueId);
        }
        return Optional.empty();
    }

    private Optional<UUID> tracker$getValidatedUUID(final int key, final int ownerIndex) {
        final UUID uuid = (((WorldInfoBridge) this.world.getWorldInfo()).bridge$getUniqueIdForIndex(ownerIndex)).orElse(null);
        if (uuid != null) {
            // Verify id is valid and not invalid
            if (SpongeImpl.getGlobalConfigAdapter().getConfig().getWorld().getInvalidLookupUuids().contains(uuid)) {
                this.trackerImpl$trackedIntBlockPositions.remove(key);
                return Optional.empty();
            }
            // player is not online, get or create user from storage
            return Optional.of(uuid);
        }
        return Optional.empty();
    }

    private Optional<User> tracker$getUserFromId(final UUID uuid) {
        // check username cache
        final String username = SpongeUsernameCache.getLastKnownUsername(uuid);
        if (username != null && this.trackerImpl$userStorageService != null) {
            return this.trackerImpl$userStorageService.get(GameProfile.of(uuid, username));
        }

        // check mojang cache
        final GameProfile profile = Sponge.getServer().getGameProfileManager().getCache().getById(uuid).orElse(null);
        if (profile != null && this.trackerImpl$userStorageService != null) {
            return this.trackerImpl$userStorageService.get(profile);
        }

        // If we reach this point, queue UUID for async lookup and return empty
        ((SpongeProfileManager) Sponge.getServer().getGameProfileManager()).lookupUserAsync(uuid);
        return Optional.empty();
    }

    // Special setter used by API
    @Override
    public void bridge$setBlockNotifier(final BlockPos pos, @Nullable final UUID uuid) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return;
        }
        if (pos.getY() <= 255) {
            final short blockPos = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackerImpl$trackedShortBlockPositions.get(blockPos);
            if (shortTracker != null) {
                shortTracker.notifierIndex = uuid == null ? -1 : ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getIndexForUniqueId(uuid);
            } else {
                this.trackerImpl$trackedShortBlockPositions.put(blockPos,
                        new PlayerTracker(uuid == null ? -1 : ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getIndexForUniqueId(uuid),
                                PlayerTracker.Type.NOTIFIER));
            }
        } else {
            final int blockPos = Constants.Sponge.blockPosToInt(pos);
            final PlayerTracker intTracker = this.trackerImpl$trackedIntBlockPositions.get(blockPos);
            if (intTracker != null) {
                intTracker.notifierIndex = uuid == null ? -1 : ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getIndexForUniqueId(uuid);
            } else {
                this.trackerImpl$trackedIntBlockPositions.put(blockPos,
                        new PlayerTracker(uuid == null ? -1 : ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getIndexForUniqueId(uuid),
                                PlayerTracker.Type.NOTIFIER));
            }
        }
    }

    // Special setter used by API
    @Override
    public void bridge$setBlockCreator(final BlockPos pos, @Nullable final UUID uuid) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return;
        }
        if (pos.getY() <= 255) {
            final short blockPos = Constants.Sponge.blockPosToShort(pos);
            final PlayerTracker shortTracker = this.trackerImpl$trackedShortBlockPositions.get(blockPos);
            if (shortTracker != null) {
                shortTracker.ownerIndex = uuid == null ? -1 : ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getIndexForUniqueId(uuid);
            } else {
                this.trackerImpl$trackedShortBlockPositions.put(blockPos, new PlayerTracker(uuid == null ? -1 : ((WorldInfoBridge) this.world.getWorldInfo())
                        .bridge$getIndexForUniqueId(uuid), PlayerTracker.Type.OWNER));
            }
        } else {
            final int blockPos = Constants.Sponge.blockPosToInt(pos);
            final PlayerTracker intTracker = this.trackerImpl$trackedIntBlockPositions.get(blockPos);
            if (intTracker != null) {
                intTracker.ownerIndex = uuid == null ? -1 : ((WorldInfoBridge) this.world.getWorldInfo()).bridge$getIndexForUniqueId(uuid);
            } else {
                this.trackerImpl$trackedIntBlockPositions.put(blockPos, new PlayerTracker(uuid == null ? -1 : ((WorldInfoBridge) this.world.getWorldInfo())
                        .bridge$getIndexForUniqueId(uuid), PlayerTracker.Type.OWNER));
            }
        }
    }

    @Override
    public void bridge$setTrackedIntPlayerPositions(final Map<Integer, PlayerTracker> trackedPositions) {
        this.trackerImpl$trackedIntBlockPositions = trackedPositions;
    }

    @Override
    public void bridge$setTrackedShortPlayerPositions(final Map<Short, PlayerTracker> trackedPositions) {
        this.trackerImpl$trackedShortBlockPositions = trackedPositions;
    }

    @Inject(method = "onLoad", at = @At("HEAD"))
    private void trackerImpl$startLoad(final CallbackInfo callbackInfo) {
        final boolean isFake = ((WorldBridge) this.world).bridge$isFake();
        if (!isFake) {
            if (!SpongeImplHooks.isMainThread()) {
                final PrettyPrinter printer = new PrettyPrinter(60).add("Illegal Async Chunk Load").centre().hr()
                    .addWrapped("Sponge relies on knowing when chunks are being loaded as chunks add entities"
                                + " to the parented world for management. These operations are generally not"
                                + " threadsafe and shouldn't be considered a \"Sponge bug \". Adding/removing"
                                + " entities from another thread to the world is never ok.")
                    .add()
                    .add(" %s : %d, %d", "Chunk Pos", this.x, this.z)
                    .add()
                    .add(new Exception("Async Chunk Load Detected"))
                    .log(SpongeImpl.getLogger(), Level.ERROR)
                    ;
                return;
            }
            if (PhaseTracker.getInstance().getCurrentState() == GenerationPhase.State.CHUNK_REGENERATING_LOAD_EXISTING) {
                return;
            }
            GenerationPhase.State.CHUNK_LOADING.createPhaseContext()
                    .source(this)
                    .world(this.world)
                    .chunk((net.minecraft.world.chunk.Chunk) (Object) this)
                    .buildAndSwitch();
        }
    }

    @Inject(method = "onLoad", at = @At("RETURN"))
    private void trackerImpl$endLoad(final CallbackInfo callbackInfo) {
        if (!((WorldBridge) this.world).bridge$isFake() && SpongeImplHooks.isMainThread()) {
            if (PhaseTracker.getInstance().getCurrentState() == GenerationPhase.State.CHUNK_REGENERATING_LOAD_EXISTING) {
                return;
            }
            // IF we're not on the main thread,
            PhaseTracker.getInstance().getCurrentContext().close();
        }
    }

}

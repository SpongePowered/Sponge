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
package org.spongepowered.common.mixin.tracker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = {Entity.class, TileEntity.class}, priority = 1100)
public abstract class OwnershipTrackedMixin_Tracker implements OwnershipTrackedBridge {

    @Nullable private UUID tracker$owner;
    @Nullable private UUID tracker$notifier;
    @Nullable private WeakReference<User> tracker$ownerUser;
    @Nullable private WeakReference<User> tracker$notifierUser;
    @Nullable private SpongeProfileManager tracker$profileManager;
    @Nullable private UserStorageService tracker$userService;

    @Override
    public Optional<UUID> tracked$getOwnerUUID() {
        return Optional.ofNullable(this.tracker$owner);
    }

    @Override
    public Optional<UUID> tracked$getNotifierUUID() {
        return Optional.ofNullable(this.tracker$notifier);
    }

    @Override
    public void tracked$setOwnerReference(@Nullable User owner) {
        this.tracker$ownerUser = new WeakReference<>(owner);
        this.tracker$owner = owner == null ? null : owner.getUniqueId();
    }

    @Override
    public Optional<User> tracked$getOwnerReference() {
        final User value = this.tracker$ownerUser == null ? null : this.tracker$ownerUser.get();
        if (value == null) {
            if (this.tracker$owner != null) {
                final Optional<User> user = this.tracked$getTrackedUser(PlayerTracker.Type.OWNER);
                user.ifPresent(owner -> this.tracker$ownerUser = new WeakReference<>(owner));
                return user;
            }
        }
        return Optional.ofNullable(value);
    }

    @Override
    public void tracked$setNotifier(@Nullable User notifier) {
        this.tracker$notifierUser = new WeakReference<>(notifier);
        this.tracker$notifier = notifier == null ? null : notifier.getUniqueId();
    }

    @Override
    public Optional<User> tracked$getNotifierReference() {
        final User value = this.tracker$notifierUser == null ? null : this.tracker$notifierUser.get();
        if (value == null) {
            if (this.tracker$owner != null) {
                final Optional<User> user = this.tracked$getTrackedUser(PlayerTracker.Type.NOTIFIER);
                user.ifPresent(owner -> this.tracker$notifierUser = new WeakReference<>(owner));
                return user;
            }
        }
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<User> tracked$getTrackedUser(PlayerTracker.Type nbtKey) {
        final UUID uuid = this.getTrackedUniqueId(nbtKey);

        if (uuid == null) {
            return Optional.empty();
        }
        // get player if online
        final Player player = Sponge.getServer().getPlayer(uuid).orElse(null);
        if (player != null) {
            return Optional.of(player);
        }
        // player is not online, get user from storage if one exists
        if (this.tracker$profileManager == null) {
            this.tracker$profileManager = ((SpongeProfileManager) Sponge.getServer().getGameProfileManager());
        }
        if (this.tracker$userService == null) {
            this.tracker$userService = SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get();
        }

        // check username cache
        final String username = SpongeUsernameCache.getLastKnownUsername(uuid);
        if (username != null) {
            return this.tracker$userService.get(GameProfile.of(uuid, username));
        }

        // check mojang cache
        final GameProfile profile = this.tracker$profileManager.getCache().getById(uuid).orElse(null);
        if (profile != null) {
            return this.tracker$userService.get(profile);
        }

        // If we reach this point, queue UUID for async lookup and return empty
        this.tracker$profileManager.lookupUserAsync(uuid);
        return Optional.empty();
    }

    @Override
    public void tracked$setTrackedUUID(PlayerTracker.Type type, @Nullable UUID uuid) {
        if (PlayerTracker.Type.OWNER == type) {
            this.tracker$owner = uuid;
        } else if (PlayerTracker.Type.NOTIFIER == type) {
            this.tracker$notifier = uuid;
        }
        if (((DataCompoundHolder) this).data$hasSpongeCompound()) {
            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeDataCompound();
            if (uuid == null) {
                if (spongeData.contains(type.compoundKey)) {
                    spongeData.remove(type.compoundKey);
                }
                return;
            }
            if (!spongeData.contains(type.compoundKey)) {
                final CompoundNBT sourceNbt = new CompoundNBT();
                sourceNbt.putUniqueId(Constants.UUID, uuid);
                spongeData.put(type.compoundKey, sourceNbt);
            } else {
                final CompoundNBT compoundTag = spongeData.getCompound(type.compoundKey);
                compoundTag.putUniqueId(Constants.UUID, uuid);
            }
        }
    }

    @Nullable
    private UUID getTrackedUniqueId(PlayerTracker.Type type) {
        if (this.tracker$owner != null && PlayerTracker.Type.OWNER == type) {
            return this.tracker$owner;
        }
        if (this instanceof IEntityOwnable) {
            final IEntityOwnable ownable = (IEntityOwnable) this;
            final Entity owner = ownable.getOwner();
            if (owner instanceof PlayerEntity) {
                this.tracked$setTrackedUUID(PlayerTracker.Type.OWNER, owner.getUniqueID());
                return owner.getUniqueID();
            }
        } else if (this.tracker$notifier != null && PlayerTracker.Type.NOTIFIER == type) {
            return this.tracker$notifier;
        }
        final CompoundNBT compound = ((DataCompoundHolder) this).data$getSpongeDataCompound();
        if (!compound.contains(type.compoundKey)) {
            return null;
        }
        final CompoundNBT creatorNbt = compound.getCompound(type.compoundKey);


        if (!creatorNbt.contains(Constants.UUID_MOST) && !creatorNbt.contains(Constants.UUID_LEAST)) {
            return null;
        }

        final UUID uniqueId = creatorNbt.getUniqueId(Constants.UUID);
        if (PlayerTracker.Type.OWNER == type) {
            this.tracker$owner = uniqueId;
        } else if (PlayerTracker.Type.NOTIFIER == type) {
            this.tracker$notifier = uniqueId;
        }
        return uniqueId;
    }
}

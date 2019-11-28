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
package org.spongepowered.common.mixin.tracking.bridge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
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

import javax.annotation.Nullable;

@Mixin(value = {Entity.class, TileEntity.class}, priority = 1100)
public class OwnershipTrackedBridgeMixin implements OwnershipTrackedBridge {

    @Nullable private UUID tracked$owner;
    @Nullable private UUID tracked$notifier;
    @Nullable private WeakReference<User> tracked$ownerUser;
    @Nullable private WeakReference<User> tracked$notifierUser;
    @Nullable private SpongeProfileManager tracked$profileManager;
    @Nullable private UserStorageService tracked$userService;

    @Override
    public Optional<UUID> tracked$getOwnerUUID() {
        return Optional.ofNullable(this.tracked$owner);
    }

    @Override
    public Optional<UUID> tracked$getNotifierUUID() {
        return Optional.ofNullable(this.tracked$notifier);
    }

    @Override
    public void tracked$setOwnerReference(@Nullable final User owner) {
        this.tracked$ownerUser = new WeakReference<>(owner);
        this.tracked$owner = owner == null ? null : owner.getUniqueId();
    }

    @Override
    public Optional<User> tracked$getOwnerReference() {
        final User value = this.tracked$ownerUser == null ? null : this.tracked$ownerUser.get();
        if (value == null) {
            if (this.tracked$owner != null) {
                final Optional<User> user = tracked$getTrackedUser(PlayerTracker.Type.OWNER);
                user.ifPresent(owner -> this.tracked$ownerUser = new WeakReference<>(owner));
                return user;
            }
        }
        return Optional.ofNullable(value);
    }

    @Override
    public void tracked$setNotifier(@Nullable final User notifier) {
        this.tracked$notifierUser = new WeakReference<>(notifier);
        this.tracked$notifier = notifier == null ? null : notifier.getUniqueId();
    }

    @Override
    public Optional<User> tracked$getNotifierReference() {
        final User value = this.tracked$notifierUser == null ? null : this.tracked$notifierUser.get();
        if (value == null) {
            if (this.tracked$owner != null) {
                final Optional<User> user = tracked$getTrackedUser(PlayerTracker.Type.NOTIFIER);
                user.ifPresent(owner -> this.tracked$notifierUser = new WeakReference<>(owner));
                return user;
            }
        }
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<User> tracked$getTrackedUser(final PlayerTracker.Type nbtKey) {
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
        if (this.tracked$profileManager == null) {
            this.tracked$profileManager = ((SpongeProfileManager) Sponge.getServer().getGameProfileManager());
        }
        if (this.tracked$userService == null) {
            this.tracked$userService = SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get();
        }

        // check username cache
        final String username = SpongeUsernameCache.getLastKnownUsername(uuid);
        if (username != null) {
            return this.tracked$userService.get(GameProfile.of(uuid, username));
        }

        // check mojang cache
        final GameProfile profile = this.tracked$profileManager.getCache().getById(uuid).orElse(null);
        if (profile != null) {
            return this.tracked$userService.get(profile);
        }

        // If we reach this point, queue UUID for async lookup and return empty
        this.tracked$profileManager.lookupUserAsync(uuid);
        return Optional.empty();
    }

    @Override
    public void tracked$setTrackedUUID(final PlayerTracker.Type type, @Nullable final UUID uuid) {
        if (PlayerTracker.Type.OWNER == type) {
            this.tracked$owner = uuid;
        } else if (PlayerTracker.Type.NOTIFIER == type) {
            this.tracked$notifier = uuid;
        }
        if (((DataCompoundHolder) this).data$hasRootCompound()) {
            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeCompound();
            if (uuid == null) {
                if (spongeData.contains(type.compoundKey)) {
                    spongeData.remove(type.compoundKey);
                }
                return;
            }
            if (!spongeData.contains(type.compoundKey)) {
                final CompoundNBT sourceNbt = new CompoundNBT();
                sourceNbt.putUniqueId(Constants.UUID, uuid);
                spongeData.func_74782_a(type.compoundKey, sourceNbt);
            } else {
                final CompoundNBT compoundTag = spongeData.getCompound(type.compoundKey);
                compoundTag.putUniqueId(Constants.UUID, uuid);
            }
        }
    }

    @Nullable
    private UUID getTrackedUniqueId(final PlayerTracker.Type nbtKey) {
        if (this.tracked$owner != null && PlayerTracker.Type.OWNER == nbtKey) {
            return this.tracked$owner;
        }
        if (this instanceof IEntityOwnable) {
            final IEntityOwnable ownable = (IEntityOwnable) this;
            final Entity owner = ownable.getOwner();
            if (owner instanceof PlayerEntity) {
                this.tracked$setTrackedUUID(PlayerTracker.Type.OWNER, owner.getUniqueID());
                return owner.getUniqueID();
            }
        } else if (this.tracked$notifier != null && PlayerTracker.Type.NOTIFIER == nbtKey) {
            return this.tracked$notifier;
        }
        final CompoundNBT nbt = ((DataCompoundHolder) this).data$getSpongeCompound();
        if (!nbt.contains(nbtKey.compoundKey)) {
            return null;
        }
        final CompoundNBT creatorNbt = nbt.getCompound(nbtKey.compoundKey);


        if (!creatorNbt.contains(Constants.UUID_MOST) && !creatorNbt.contains(Constants.UUID_LEAST)) {
            return null;
        }

        final UUID uniqueId = creatorNbt.getUniqueId(Constants.UUID);
        if (PlayerTracker.Type.OWNER == nbtKey) {
            this.tracked$owner = uniqueId;
        } else if (PlayerTracker.Type.NOTIFIER == nbtKey) {
            this.tracked$notifier = uniqueId;
        }
        return uniqueId;
    }


}

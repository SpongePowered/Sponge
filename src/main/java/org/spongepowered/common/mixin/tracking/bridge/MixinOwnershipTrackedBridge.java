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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(value = {Entity.class, TileEntity.class}, priority = 1100)
public class MixinOwnershipTrackedBridge implements OwnershipTrackedBridge {

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
    public Optional<User> tracked$getTrackedUser(PlayerTracker.Type nbtKey) {
        final UUID uuid = this.getTrackedUniqueId(nbtKey);

        if (uuid == null) {
            return Optional.empty();
        }
        // get player if online
        Player player = Sponge.getServer().getPlayer(uuid).orElse(null);
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
        String username = SpongeUsernameCache.getLastKnownUsername(uuid);
        if (username != null) {
            return this.tracked$userService.get(GameProfile.of(uuid, username));
        }

        // check mojang cache
        GameProfile profile = this.tracked$profileManager.getCache().getById(uuid).orElse(null);
        if (profile != null) {
            return this.tracked$userService.get(profile);
        }

        // If we reach this point, queue UUID for async lookup and return empty
        this.tracked$profileManager.lookupUserAsync(uuid);
        return Optional.empty();
    }

    @Override
    public void tracked$setTrackedUUID(PlayerTracker.Type type, @Nullable UUID uuid) {
        if (PlayerTracker.Type.OWNER == type) {
            this.tracked$owner = uuid;
        } else if (PlayerTracker.Type.NOTIFIER == type) {
            this.tracked$notifier = uuid;
        }
        if (((DataCompoundHolder) this).data$hasRootCompound()) {
            final NBTTagCompound spongeData = ((DataCompoundHolder) this).data$getSpongeCompound();
            if (uuid == null) {
                if (spongeData.hasKey(type.compoundKey)) {
                    spongeData.removeTag(type.compoundKey);
                }
                return;
            }
            if (!spongeData.hasKey(type.compoundKey)) {
                NBTTagCompound sourceNbt = new NBTTagCompound();
                sourceNbt.setUniqueId(NbtDataUtil.UUID, uuid);
                spongeData.setTag(type.compoundKey, sourceNbt);
            } else {
                final NBTTagCompound compoundTag = spongeData.getCompoundTag(type.compoundKey);
                compoundTag.setUniqueId(NbtDataUtil.UUID, uuid);
            }
        }
    }

    @Nullable
    private UUID getTrackedUniqueId(PlayerTracker.Type nbtKey) {
        if (this.tracked$owner != null && PlayerTracker.Type.OWNER == nbtKey) {
            return this.tracked$owner;
        }
        if (this instanceof IEntityOwnable) {
            IEntityOwnable ownable = (IEntityOwnable) this;
            final Entity owner = ownable.getOwner();
            if (owner instanceof EntityPlayer) {
                this.tracked$setTrackedUUID(PlayerTracker.Type.OWNER, owner.getUniqueID());
                return owner.getUniqueID();
            }
        } else if (this.tracked$notifier != null && PlayerTracker.Type.NOTIFIER == nbtKey) {
            return this.tracked$notifier;
        }
        NBTTagCompound nbt = ((DataCompoundHolder) this).data$getSpongeCompound();
        if (!nbt.hasKey(nbtKey.compoundKey)) {
            return null;
        }
        NBTTagCompound creatorNbt = nbt.getCompoundTag(nbtKey.compoundKey);


        if (!creatorNbt.hasKey(NbtDataUtil.UUID_MOST) && !creatorNbt.hasKey(NbtDataUtil.UUID_LEAST)) {
            return null;
        }

        UUID uniqueId = creatorNbt.getUniqueId(NbtDataUtil.UUID);
        if (PlayerTracker.Type.OWNER == nbtKey) {
            this.tracked$owner = uniqueId;
        } else if (PlayerTracker.Type.NOTIFIER == nbtKey) {
            this.tracked$notifier = uniqueId;
        }
        return uniqueId;
    }


}

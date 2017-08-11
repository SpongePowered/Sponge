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
package org.spongepowered.common.mixin.tracking.entity;

import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(value = net.minecraft.entity.Entity.class, priority = 1111)
public abstract class MixinEntity_Tracker implements Entity, IMixinEntity {

    private SpongeProfileManager spongeProfileManager;
    private UserStorageService userStorageService;
    @Shadow public net.minecraft.world.World world;

    @Override
    public void trackEntityUniqueId(String nbtKey, @Nullable UUID uuid) {
        if (NbtDataUtil.SPONGE_ENTITY_CREATOR.equals(nbtKey)) {
            this.creator = uuid;
        } else if (NbtDataUtil.SPONGE_ENTITY_NOTIFIER.equals(nbtKey)) {
            this.notifier = uuid;
        }
        final NBTTagCompound spongeData = getSpongeData();
        if (!spongeData.hasKey(nbtKey)) {
            if (uuid == null) {
                return;
            }

            NBTTagCompound sourceNbt = new NBTTagCompound();
            sourceNbt.setUniqueId(NbtDataUtil.UUID, uuid);
            spongeData.setTag(nbtKey, sourceNbt);
        } else {
            final NBTTagCompound compoundTag = spongeData.getCompoundTag(nbtKey);
            if (uuid == null) {
                compoundTag.removeTag(NbtDataUtil.UUID);
            } else {
                compoundTag.setUniqueId(NbtDataUtil.UUID, uuid);
            }
        }
    }

    @Override
    public Optional<UUID> getCreator() {
        if (this.creator != null) {
            return Optional.of(this.creator);
        }
        Optional<User> user = getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
        if (user.isPresent()) {
            return Optional.of(user.get().getUniqueId());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UUID> getNotifier() {
        if (this.notifier != null) {
            return Optional.of(this.notifier);
        }
        Optional<User> user = getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
        if (user.isPresent()) {
            return Optional.of(user.get().getUniqueId());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void setCreator(@Nullable UUID uuid) {
        trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, uuid);
    }

    @Override
    public void setNotifier(@Nullable UUID uuid) {
        trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_NOTIFIER, uuid);
    }

    @Nullable
    private UUID getTrackedUniqueId(String nbtKey) {
        if (this.creator != null && NbtDataUtil.SPONGE_ENTITY_CREATOR.equals(nbtKey)) {
            return this.creator;
        } else if (this instanceof IEntityOwnable) {
            IEntityOwnable ownable = (IEntityOwnable) this;
            final net.minecraft.entity.Entity owner = ownable.getOwner();
            if (owner != null && owner instanceof EntityPlayer) {
                this.setCreator(owner.getUniqueID());
                return owner.getUniqueID();
            }
        } else if (this.notifier != null && NbtDataUtil.SPONGE_ENTITY_NOTIFIER.equals(nbtKey)) {
            return this.notifier;
        }
        NBTTagCompound nbt = getSpongeData();
        if (!nbt.hasKey(nbtKey)) {
            return null;
        }
        NBTTagCompound creatorNbt = nbt.getCompoundTag(nbtKey);


        if (!creatorNbt.hasKey(NbtDataUtil.UUID + "Most") && !creatorNbt.hasKey(NbtDataUtil.UUID + "Least")) {
            return null;
        }

        UUID uniqueId = creatorNbt.getUniqueId(NbtDataUtil.UUID);
        if (NbtDataUtil.SPONGE_ENTITY_CREATOR.equals(nbtKey)) {
            this.creator = uniqueId;
        } else if (NbtDataUtil.SPONGE_ENTITY_NOTIFIER.equals(nbtKey)) {
            this.notifier = uniqueId;
        }
        return uniqueId;
    }

    @Nullable private UUID creator;
    @Nullable private UUID notifier;

    @Override
    public Optional<User> getCreatorUser() {
        if (this.creator != null) {
            return getUserForUuid(this.creator);
        }
        if (this instanceof IEntityOwnable) {
            IEntityOwnable ownable = (IEntityOwnable) this;
            final net.minecraft.entity.Entity owner = ownable.getOwner();
            if (owner != null && owner instanceof EntityPlayer) {
                this.setCreator(owner.getUniqueID());
                return Optional.of((User) owner);
            }
        }
        return getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
    }

    @Override
    public Optional<User> getNotifierUser() {
        if (this.notifier != null) {
            return getUserForUuid(this.notifier);
        }
        return getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<User> getTrackedPlayer(String nbtKey) {
        final UUID uuid = this.getTrackedUniqueId(nbtKey);
        if (uuid == null) {
            return Optional.empty();
        }
        return getUserForUuid(uuid);

    }

    private Optional<User> getUserForUuid(UUID uuid) {
        // get player if online
        Player player = Sponge.getServer().getPlayer(uuid).orElse(null);
        if (player != null) {
            return Optional.of(player);
        }
        // player is not online, get user from storage if one exists
        if (this.spongeProfileManager == null) {
            this.spongeProfileManager = ((SpongeProfileManager) Sponge.getServer().getGameProfileManager());
        }
        if (this.userStorageService == null) {
            this.userStorageService = SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get();
        }

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

}

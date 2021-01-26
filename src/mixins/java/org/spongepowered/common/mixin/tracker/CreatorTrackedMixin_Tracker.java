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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.profile.SpongeGameProfileManager;
import org.spongepowered.common.util.Constants;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin({Entity.class, BlockEntity.class})
public abstract class CreatorTrackedMixin_Tracker implements CreatorTrackedBridge {

    @Nullable private UUID tracker$creator;
    @Nullable private UUID tracker$notifier;
    @Nullable private WeakReference<User> tracker$creatorUser;
    @Nullable private WeakReference<User> tracker$notifierUser;

    @Override
    public Optional<UUID> tracked$getCreatorUUID() {
        return Optional.ofNullable(this.tracker$creator);
    }

    @Override
    public Optional<UUID> tracked$getNotifierUUID() {
        return Optional.ofNullable(this.tracker$notifier);
    }

    @Override
    public void tracked$setCreatorReference(@Nullable final User creator) {
        this.tracker$creator = creator == null ? null : creator.getUniqueId();
        this.tracker$creatorUser = new WeakReference<>(creator);
    }

    @Override
    public Optional<User> tracked$getCreatorReference() {
        final User value = this.tracker$creatorUser == null ? null : this.tracker$creatorUser.get();
        if (value == null) {
            if (this.tracker$creator != null) {
                final Optional<User> user = this.tracked$getTrackedUser(PlayerTracker.Type.CREATOR);
                user.ifPresent(creator -> this.tracker$creatorUser = new WeakReference<>(creator));
                return user;
            }
        }
        return Optional.ofNullable(value);
    }

    @Override
    public void tracked$setNotifier(@Nullable final User notifier) {
        this.tracker$notifierUser = new WeakReference<>(notifier);
        this.tracker$notifier = notifier == null ? null : notifier.getUniqueId();
    }

    @Override
    public Optional<User> tracked$getNotifierReference() {
        final User value = this.tracker$notifierUser == null ? null : this.tracker$notifierUser.get();
        if (value == null) {
            if (this.tracker$creator != null) {
                final Optional<User> user = this.tracked$getTrackedUser(PlayerTracker.Type.NOTIFIER);
                user.ifPresent(creator -> this.tracker$notifierUser = new WeakReference<>(creator));
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
        final ServerPlayer player = Sponge.getServer().getPlayer(uuid).orElse(null);
        final User user = player == null ? null : player.getUser();
        if (user != null) {
            return Optional.of(user);
        }

        // check username cache
        final String username = ((SpongeServer) Sponge.getServer()).getUsernameCache().getLastKnownUsername(uuid);
        if (username != null) {
            return Sponge.getServer().getUserManager().get(GameProfile.of(uuid, username));
        }

        // check mojang cache
        final GameProfile profile = Sponge.getServer().getGameProfileManager().getCache().getById(uuid).orElse(null);
        if (profile != null) {
            return Sponge.getServer().getUserManager().get(profile);
        }

        // If we reach this point, queue UUID for async lookup and return empty
        ((SpongeGameProfileManager) Sponge.getServer().getGameProfileManager()).lookupUserAsync(uuid);
        return Optional.empty();
    }

    @Override
    public void tracked$setTrackedUUID(final PlayerTracker.Type type, @Nullable final UUID uuid) {
        if (PlayerTracker.Type.CREATOR == type) {
            this.tracker$creator = uuid;
        } else if (PlayerTracker.Type.NOTIFIER == type) {
            this.tracker$notifier = uuid;
        }
//        if (((DataCompoundHolder) this).data$hasSpongeCompound()) {
//            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeDataCompound();
//            if (uuid == null) {
//                if (spongeData.contains(type.compoundKey)) {
//                    spongeData.remove(type.compoundKey);
//                }
//                return;
//            }
//            if (!spongeData.contains(type.compoundKey)) {
//                final CompoundNBT sourceNbt = new CompoundNBT();
//                sourceNbt.putUniqueId(Constants.UUID, uuid);
//                spongeData.put(type.compoundKey, sourceNbt);
//            } else {
//                final CompoundNBT compoundTag = spongeData.getCompound(type.compoundKey);
//                compoundTag.putUniqueId(Constants.UUID, uuid);
//            }
//        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private UUID getTrackedUniqueId(final PlayerTracker.Type type) {
        if (this.tracker$creator != null && PlayerTracker.Type.CREATOR == type) {
            return this.tracker$creator;
        }
        if ((Object)this instanceof TamableAnimal) {
            final TamableAnimal ownable = (TamableAnimal) (Object) this;
            final Entity owner = ownable.getOwner();
            if (owner instanceof Player) {
                this.tracked$setTrackedUUID(PlayerTracker.Type.CREATOR, owner.getUUID());
                return owner.getUUID();
            }
        } else if (this.tracker$notifier != null && PlayerTracker.Type.NOTIFIER == type) {
            return this.tracker$notifier;
        }
        final CompoundTag compound = ((DataCompoundHolder) this).data$getSpongeData();
        if (!compound.contains(type.compoundKey)) {
            return null;
        }
        final CompoundTag creatorNbt = compound.getCompound(type.compoundKey);


        if (!creatorNbt.contains(Constants.UUID_MOST) && !creatorNbt.contains(Constants.UUID_LEAST)) {
            return null;
        }

        final UUID uniqueId = creatorNbt.getUUID(Constants.UUID);
        if (PlayerTracker.Type.CREATOR == type) {
            this.tracker$creator = uniqueId;
        } else if (PlayerTracker.Type.NOTIFIER == type) {
            this.tracker$notifier = uniqueId;
        }
        return uniqueId;
    }
}

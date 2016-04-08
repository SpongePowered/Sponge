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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = net.minecraft.entity.Entity.class, priority = 1111)
public abstract class MixinEntity_Tracker implements Entity, IMixinEntity {

    @Shadow public net.minecraft.world.World worldObj;

    @Override
    public Optional<UUID> getCreator() {
        Optional<User> user = getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
        if (user.isPresent()) {
            return Optional.of(user.get().getUniqueId());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UUID> getNotifier() {
        Optional<User> user = getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
        if (user.isPresent()) {
            return Optional.of(user.get().getUniqueId());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> getTrackedPlayer(String nbtKey) {
        NBTTagCompound nbt = getSpongeData();
        if (!nbt.hasKey(nbtKey)) {
            return Optional.empty();
        } else {
            NBTTagCompound creatorNbt = nbt.getCompoundTag(nbtKey);

            if (!creatorNbt.hasKey(NbtDataUtil.WORLD_UUID_MOST) || !creatorNbt.hasKey(NbtDataUtil.WORLD_UUID_LEAST)) {
                return Optional.empty();
            }

            UUID uuid = new UUID(creatorNbt.getLong(NbtDataUtil.WORLD_UUID_MOST), creatorNbt.getLong(NbtDataUtil.WORLD_UUID_LEAST));
            // get player if online
            EntityPlayer player = this.worldObj.getPlayerEntityByUUID(uuid);
            if (player != null) {
                return Optional.of((User)player);
            }
            // player is not online, get user from storage if one exists
            return SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get().get(uuid);
        }
    }

}

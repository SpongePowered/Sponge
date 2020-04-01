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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

public class ItemStackGameProfileProvider extends ItemStackDataProvider<GameProfile> {

    public ItemStackGameProfileProvider() {
        super(Keys.GAME_PROFILE);
    }

    @Override
    protected Optional<GameProfile> getFrom(ItemStack dataHolder) {
        @Nullable CompoundNBT nbt = dataHolder.getChildTag(Constants.Item.Skull.ITEM_SKULL_OWNER);
        return Optional.ofNullable(nbt == null ? null : (GameProfile) NBTUtil.readGameProfile(nbt));
    }

    @Override
    protected boolean set(ItemStack dataHolder, @Nullable GameProfile value) {
        if (value == null) {
            dataHolder.getTag().remove(Constants.Item.Skull.ITEM_SKULL_OWNER);
        } else {
            final CompoundNBT nbt = NBTUtil.writeGameProfile(new CompoundNBT(), (com.mojang.authlib.GameProfile) resolveProfileIfNecessary(value));
            dataHolder.setTagInfo(Constants.Item.Skull.ITEM_SKULL_OWNER, nbt);
        }
        return true;
    }

    @Override
    protected boolean supports(Item item) {
        return item instanceof SkullItem;
    }

    public static @Nullable GameProfile resolveProfileIfNecessary(@Nullable final GameProfile profile) {
        if (profile == null) {
            return null;
        }
        if (profile.getPropertyMap().containsKey("textures")) {
            return profile;
        }
        // Skulls need a name in order to properly display -> resolve if no name is contained in the given profile
        final CompletableFuture<GameProfile> future = Sponge.getGame().getServer().getGameProfileManager().fill(profile);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            SpongeImpl.getLogger().debug("Exception while trying to fill skull GameProfile for '" + profile + "'", e);
            return profile;
        }
    }
}

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
package org.spongepowered.common.mixin.core.whitelist;

import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListEntry;
import net.minecraft.server.management.UserListWhitelist;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(UserListWhitelist.class)
public abstract class MixinUserListWhitelist extends UserList {

    public MixinUserListWhitelist(File saveFile) {
        super(saveFile);
    }

    @Override
    public boolean hasEntry(Object object) {
        return Sponge.getServiceManager().provideUnchecked(WhitelistService.class).isWhitelisted((GameProfile) object);
    }

    @Overwrite
    @Override
    public String[] getKeys() {
        List<String> names = new ArrayList<>();
        for (GameProfile profile: Sponge.getServiceManager().provideUnchecked(WhitelistService.class).getWhitelistedProfiles()) {
            names.add(profile.getName().get());
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public void addEntry(UserListEntry entry) {
        Sponge.getServiceManager().provideUnchecked(WhitelistService.class).addProfile((GameProfile) entry.getValue());
    }

    @Override
    public void removeEntry(Object object) {
        Sponge.getServiceManager().provideUnchecked(WhitelistService.class).removeProfile((GameProfile) object);
    }

    @Override
    public boolean isEmpty() {
        return Sponge.getServiceManager().provideUnchecked(WhitelistService.class).getWhitelistedProfiles().isEmpty();
    }

    @Overwrite
    public com.mojang.authlib.GameProfile getBannedProfile(String username) {
        for (GameProfile profile: Sponge.getServiceManager().provideUnchecked(WhitelistService.class).getWhitelistedProfiles()) {
            if (profile.getName().get().equals(username)) {
                return (com.mojang.authlib.GameProfile) profile;
            }
        }
        return null;
    }

}

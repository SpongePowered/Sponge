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
package org.spongepowered.common.mixin.core.ban;

import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListEntry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mixin(UserListBans.class)
public abstract class MixinUserListBans extends UserList<com.mojang.authlib.GameProfile, UserListBansEntry> {

    public MixinUserListBans(File saveFile) {
        super(saveFile);
    }

    @Override
    public boolean hasEntry(com.mojang.authlib.GameProfile object) {
        return Sponge.getServiceManager().provideUnchecked(BanService.class).isBanned((GameProfile) object);
    }

    @Override
    public UserListBansEntry getEntry(com.mojang.authlib.GameProfile object) {
        return (UserListBansEntry) Sponge.getServiceManager().provideUnchecked(BanService.class).getBanFor((GameProfile) object).orElse(null);
    }

    @Overwrite
    @Override
    public String[] getKeys() {
        List<String> names = new ArrayList<>();
        for (Ban.Profile ban: Sponge.getServiceManager().provideUnchecked(BanService.class).getProfileBans()) {
            if (ban.getProfile().getName().isPresent()) {
                names.add(ban.getProfile().getName().get());
            }
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public void addEntry(UserListBansEntry entry) {
        Sponge.getServiceManager().provideUnchecked(BanService.class).addBan((Ban) entry);
    }

    @Override
    public boolean isEmpty() {
        return Sponge.getServiceManager().provideUnchecked(BanService.class).getProfileBans().isEmpty();
    }

    @Override
    public void removeEntry(com.mojang.authlib.GameProfile object) {
        Sponge.getServiceManager().provideUnchecked(BanService.class).pardon((GameProfile) object);
    }

    @Overwrite
    public com.mojang.authlib.GameProfile isUsernameBanned(String username) {
        for (Ban.Profile ban: Sponge.getServiceManager().provideUnchecked(BanService.class).getProfileBans()) {
            if (ban.getProfile().getName().isPresent()) {
                if (ban.getProfile().getName().get().equals(username)) {
                    return (com.mojang.authlib.GameProfile) ban.getProfile();
                }
            }
        }
        return null;
    }

}

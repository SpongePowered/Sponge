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
package org.spongepowered.common.service.server.whitelist;

import com.google.inject.Singleton;
import net.minecraft.server.management.WhiteList;
import net.minecraft.server.management.WhitelistEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.management.UserListAccessor;
import org.spongepowered.common.accessor.server.management.UserListEntryAccessor;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.util.UserListUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public final class SpongeWhitelistService implements WhitelistService {

    @SuppressWarnings("unchecked")
    @Override
    public Collection<GameProfile> getWhitelistedProfiles() {
        final List<GameProfile> profiles = new ArrayList<>();

        final WhiteList list = SpongeCommon.getServer().getPlayerList().getWhiteList();
        for (final WhitelistEntry entry: ((UserListAccessor<com.mojang.authlib.GameProfile, WhitelistEntry>) list).accessor$map().values()) {
            profiles.add(SpongeGameProfile.of(((UserListEntryAccessor<com.mojang.authlib.GameProfile>) entry).accessor$user()));
        }

        return profiles;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isWhitelisted(final GameProfile profile) {
        final UserListAccessor<com.mojang.authlib.GameProfile, WhitelistEntry> whitelist = (UserListAccessor<com.mojang.authlib.GameProfile, WhitelistEntry>) SpongeWhitelistService
            .getWhitelist();

        whitelist.invoker$removeExpired();
        return whitelist.accessor$map().containsKey(whitelist.invoker$getKeyForUser(SpongeGameProfile.toMcProfile(profile)));
    }

    @Override
    public boolean addProfile(final GameProfile profile) {
        final boolean wasWhitelisted = this.isWhitelisted(profile);
        UserListUtil.addEntry(SpongeWhitelistService.getWhitelist(), new WhitelistEntry(SpongeGameProfile.toMcProfile(profile)));
        return wasWhitelisted;
    }

    @Override
    public boolean removeProfile(final GameProfile profile) {
        final boolean wasWhitelisted = this.isWhitelisted(profile);
        UserListUtil.removeEntry(SpongeWhitelistService.getWhitelist(), profile);
        return wasWhitelisted;
    }

    private static WhiteList getWhitelist() {
        return SpongeCommon.getServer().getPlayerList().getWhiteList();
    }
}

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
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.players.StoredUserEntryAccessor;
import org.spongepowered.common.accessor.server.players.StoredUserListAccessor;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.util.UserListUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;

@Singleton
public final class SpongeWhitelistService implements WhitelistService {

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Collection<GameProfile>> whitelistedProfiles() {
        final List<GameProfile> profiles = new ArrayList<>();

        final UserWhiteList list = SpongeCommon.server().getPlayerList().getWhiteList();
        for (final UserWhiteListEntry entry: ((StoredUserListAccessor<com.mojang.authlib.GameProfile, UserWhiteListEntry>) list).accessor$map().values()) {
            profiles.add(SpongeGameProfile.of(((StoredUserEntryAccessor<com.mojang.authlib.GameProfile>) entry).accessor$user()));
        }

        return CompletableFuture.completedFuture(profiles);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Boolean> isWhitelisted(final GameProfile profile) {
        final StoredUserListAccessor<com.mojang.authlib.GameProfile, UserWhiteListEntry> whitelist = (StoredUserListAccessor<com.mojang.authlib.GameProfile, UserWhiteListEntry>) SpongeWhitelistService
            .getWhitelist();

        whitelist.invoker$removeExpired();
        return CompletableFuture.completedFuture(whitelist.accessor$map().containsKey(whitelist.invoker$getKeyForUser(SpongeGameProfile.toMcProfile(profile))));
    }

    @Override
    public CompletableFuture<Boolean> addProfile(final GameProfile profile) {
        final boolean wasWhitelisted = UserListUtil.addEntry(SpongeWhitelistService.getWhitelist(), new UserWhiteListEntry(SpongeGameProfile.toMcProfile(profile))) != null;
        return CompletableFuture.completedFuture(wasWhitelisted);
    }

    @Override
    public CompletableFuture<Boolean> removeProfile(final GameProfile profile) {
        final boolean wasWhitelisted = UserListUtil.removeEntry(SpongeWhitelistService.getWhitelist(), SpongeGameProfile.toMcProfile(profile)) != null;
        return CompletableFuture.completedFuture(wasWhitelisted);
    }

    private static UserWhiteList getWhitelist() {
        return SpongeCommon.server().getPlayerList().getWhiteList();
    }
}

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

import net.minecraft.server.management.WhiteList;
import net.minecraft.server.management.WhitelistEntry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.common.accessor.server.management.UserListEntryAccessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Redirects all calls to whitelist to the {@link WhitelistService}.
 */
public class SpongeUserListWhitelist extends WhiteList {

    public SpongeUserListWhitelist(final File file) {
        super(file);
    }

    @Override
    protected boolean hasEntry(final com.mojang.authlib.GameProfile entry) {
        return Sponge.getServer().getServiceProvider().whitelistService().isWhitelisted((GameProfile) entry);
    }

    @Override
    public String[] getKeys() {
        final List<String> names = new ArrayList<>();
        for (final GameProfile profile : Sponge.getServer().getServiceProvider().whitelistService().getWhitelistedProfiles()) {
            profile.getName().ifPresent(names::add);
        }
        return names.toArray(new String[names.size()]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addEntry(final WhitelistEntry entry) {
        Sponge.getServer().getServiceProvider().whitelistService().addProfile(((UserListEntryAccessor<GameProfile>) entry).accessor$getValue());
    }

    @Override
    public void removeEntry(final com.mojang.authlib.GameProfile entry) {
        Sponge.getServer().getServiceProvider().whitelistService().removeProfile((GameProfile) entry);
    }

    @Override
    public boolean isEmpty() {
        return Sponge.getServer().getServiceProvider().whitelistService().getWhitelistedProfiles().isEmpty();
    }

    @Nullable
    public com.mojang.authlib.GameProfile getByName(final String profileName) {
        for (final GameProfile profile : Sponge.getServer().getServiceProvider().whitelistService().getWhitelistedProfiles()) {
            if (profile.getName().isPresent() && profile.getName().get().equals(profileName)) {
                return (com.mojang.authlib.GameProfile) profile;
            }
        }

        return null;
    }

}

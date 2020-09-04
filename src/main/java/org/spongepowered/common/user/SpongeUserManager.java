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
package org.spongepowered.common.user;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.user.UserManager;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SpongeUserManager implements UserManager {

    public static final UUID FAKEPLAYER_UUID = UUID.fromString("41C82C87-7AFB-4024-BA57-13D2C99CAE77");
    public static final GameProfile FAKEPLAYER_PROFILE = new SpongeGameProfile(FAKEPLAYER_UUID, null);

    private final ServerUserProvider serverUserProvider;

    public SpongeUserManager(final Server server) {
        this.serverUserProvider = new ServerUserProvider(server);
    }

    public void init() {
        this.serverUserProvider.refreshFilesystemProfiles();
        this.serverUserProvider.setupWatchers();
    }

    @Override
    public Optional<User> get(final UUID uniqueId) {
        return this.serverUserProvider.getUser(uniqueId);
    }

    @Override
    public Optional<User> get(final String lastKnownName) {
        checkNotNull(lastKnownName, "lastKnownName");
        checkArgument(!lastKnownName.isEmpty() && lastKnownName.length() <= 16, "Invalid username %s", lastKnownName);
        return this.serverUserProvider.getUser(lastKnownName);
    }

    @Override
    public Optional<User> get(final GameProfile profile) {
        return this.serverUserProvider.getUser(profile);
    }

    @Override
    public User getOrCreate(final GameProfile profile) {
        return this.serverUserProvider.getOrCreateUser(this.ensureNonEmptyUUID(profile), false);
    }

    public User forceRecreateUser(final GameProfile profile) {
        return this.serverUserProvider.getOrCreateUser(profile, true);
    }

    @Override
    public Collection<GameProfile> getAll() {
        return this.streamAll().collect(Collectors.toList());
    }

    @Override
    public Stream<GameProfile> streamAll() {
        return this.serverUserProvider.streamAll();
    }

    @Override
    public boolean delete(final GameProfile profile) {
        return this.serverUserProvider.deleteUser(checkNotNull(profile, "profile").getUniqueId());
    }

    @Override
    public boolean delete(final User user) {
        return this.delete(user.getProfile());
    }

    @Override
    public Stream<GameProfile> streamOfMatches(final String lastKnownName) {
        return this.serverUserProvider.matchKnownProfiles(checkNotNull(lastKnownName, "lastKnownName").toLowerCase(Locale.ROOT));
    }

    private GameProfile ensureNonEmptyUUID(final GameProfile profile) {
        if (profile.getUniqueId().equals(SpongeGameProfile.EMPTY_UUID)) {
            final String name = profile.getName().orElse(null);
            // Use Forge's FakePlayer UUID
            return new SpongeGameProfile(FAKEPLAYER_UUID, name);
        }
        return profile;
    }

}

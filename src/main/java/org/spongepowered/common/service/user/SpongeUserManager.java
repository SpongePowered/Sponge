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
package org.spongepowered.common.service.user;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.user.UserManager;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public final class SpongeUserManager implements UserManager {

    public static final UUID FAKEPLAYER_UUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");
    public static final GameProfile FAKEPLAYER_PROFILE = (GameProfile) new com.mojang.authlib.GameProfile(FAKEPLAYER_UUID, null);

    private final UserDiscoverer userDiscoverer;

    public SpongeUserManager(Server server) {
        this.userDiscoverer = new UserDiscoverer(server);
    }
    
    @Override
    public Optional<User> get(UUID uniqueId) {
        try {
            return Optional.ofNullable(this.userDiscoverer.findByProfile(Sponge.getServer().getGameProfileManager().get(checkNotNull(uniqueId, "uniqueId")).get()));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error looking up GameProfile!", e);
        }
    }

    @Override
    public Optional<User> get(String lastKnownName) {
        checkNotNull(lastKnownName, "lastKnownName");
        checkArgument(lastKnownName.length() > 0 && lastKnownName.length() <= 16, "Invalid username %s", lastKnownName);
        return Optional.ofNullable(this.userDiscoverer.findByUsername(lastKnownName));
    }

    @Override
    public Optional<User> get(GameProfile profile) {
        return Optional.ofNullable(this.userDiscoverer.findByProfile(profile));
    }

    @Override
    public User getOrCreate(GameProfile profile) {
        if (profile.getUniqueId() == null) {
            String name = profile.getName().orElse(null);
            // Use Forge's FakePlayer UUID
            profile = (GameProfile) new com.mojang.authlib.GameProfile(FAKEPLAYER_UUID, name);
        }

        Optional<User> user = this.get(profile);
        if (user.isPresent()) {
            return user.get();
        }
        return this.userDiscoverer.create((com.mojang.authlib.GameProfile) profile);
    }

    public User forceRecreateUser(GameProfile profile) {
        return this.userDiscoverer.forceRecreate((com.mojang.authlib.GameProfile) profile);
    }

    @Override
    public Collection<GameProfile> getAll() {
        return this.userDiscoverer.getAllProfiles();
    }

    @Override
    public Stream<GameProfile> streamAll() {
        return this.userDiscoverer.streamAllProfiles();
    }

    @Override
    public boolean delete(GameProfile profile) {
        return this.userDiscoverer.delete(checkNotNull(profile, "profile").getUniqueId());
    }

    @Override
    public boolean delete(User user) {
        return this.userDiscoverer.delete(checkNotNull(user, "user").getUniqueId());
    }

    @Override
    public Collection<GameProfile> match(String lastKnownName) {
        lastKnownName = checkNotNull(lastKnownName, "lastKnownName").toLowerCase(Locale.ROOT);
        Collection<GameProfile> allProfiles = this.userDiscoverer.getAllProfiles();
        Collection<GameProfile> matching = Sets.newHashSet();
        for (GameProfile profile : allProfiles) {
            if (profile.getName().isPresent() && profile.getName().get().startsWith(lastKnownName)) {
                matching.add(profile);
            }
        }
        return matching;
    }

}

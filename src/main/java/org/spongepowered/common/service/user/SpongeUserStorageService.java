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
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class SpongeUserStorageService implements UserStorageService {

    public static final UUID FAKEPLAYER_UUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");
    public static final GameProfile FAKEPLAYER_PROFILE = (GameProfile) new com.mojang.authlib.GameProfile(FAKEPLAYER_UUID, null);

    public void init() {
        UserDiscoverer.init();
    }

    @Override
    public Optional<User> get(UUID uniqueId) {
        try {
            return Optional.ofNullable(UserDiscoverer.findByProfile(Sponge.getServer().getGameProfileManager().get(checkNotNull(uniqueId, "uniqueId")).get()));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error looking up GameProfile!", e);
        }
    }

    @Override
    public Optional<User> get(String lastKnownName) {
        checkNotNull(lastKnownName, "lastKnownName");
        checkArgument(lastKnownName.length() > 0 && lastKnownName.length() <= 16, "Invalid username %s", lastKnownName);
        checkState(Sponge.isServerAvailable(), "Server is not available!");
        return Optional.ofNullable(UserDiscoverer.findByUsername(lastKnownName));
    }

    @Override
    public Optional<User> get(GameProfile profile) {
        return Optional.ofNullable(UserDiscoverer.findByProfile(profile));
    }

    @Override
    public User getOrCreate(GameProfile profile) {
        if (profile.getUniqueId() == null) {
            String name = profile.getName().orElse(null);
            // Use Forge's FakePlayer UUID
            profile = (GameProfile) new com.mojang.authlib.GameProfile(FAKEPLAYER_UUID, name);
        }

        Optional<User> user = get(profile);
        if (user.isPresent()) {
            return user.get();
        }
        return UserDiscoverer.create((com.mojang.authlib.GameProfile) profile);
    }

    public User forceRecreateUser(GameProfile profile) {
        return UserDiscoverer.forceRecreate((com.mojang.authlib.GameProfile) profile);
    }

    @Override
    public Collection<GameProfile> getAll() {
        return UserDiscoverer.getAllProfiles();
    }

    @Override
    public boolean delete(GameProfile profile) {
        checkState(Sponge.isServerAvailable(), "Server is not available!");
        return UserDiscoverer.delete(checkNotNull(profile, "profile").getUniqueId());
    }

    @Override
    public boolean delete(User user) {
        checkState(Sponge.isServerAvailable(), "Server is not available!");
        return UserDiscoverer.delete(checkNotNull(user, "user").getUniqueId());
    }

    @Override
    public Collection<GameProfile> match(String lastKnownName) {
        lastKnownName = checkNotNull(lastKnownName, "lastKnownName").toLowerCase(Locale.ROOT);
        Collection<GameProfile> allProfiles = UserDiscoverer.getAllProfiles();
        Collection<GameProfile> matching = Sets.newHashSet();
        for (GameProfile profile : allProfiles) {
            Optional<String> name = profile.getName();
            if (name.isPresent() && name.get().toLowerCase(Locale.ROOT).startsWith(lastKnownName)) {
                matching.add(profile);
            }
        }
        return matching;
    }

}

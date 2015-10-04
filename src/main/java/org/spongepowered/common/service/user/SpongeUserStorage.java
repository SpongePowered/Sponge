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
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorage;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class SpongeUserStorage implements UserStorage {

    @Override
    public Optional<User> get(UUID uniqueId) {
        return Optional.ofNullable(UserDiscoverer.findByUuid(checkNotNull(uniqueId, "uniqueId")));
    }

    @Override
    public Optional<User> get(String lastKnownName) {
        checkNotNull(lastKnownName, "lastKnownName");
        checkArgument(lastKnownName.length() >= 3 && lastKnownName.length() <= 16, "Invalid username %s", lastKnownName);
        return Optional.ofNullable(UserDiscoverer.findByUsername(lastKnownName));
    }

    @Override
    public Optional<User> get(GameProfile profile) {
        return Optional.ofNullable(UserDiscoverer.findByUuid(checkNotNull(profile, "profile").getUniqueId()));
    }

    @Override
    public User getOrCreate(GameProfile profile) {
        Optional<User> user = get(profile);
        if (user.isPresent()) {
            return user.get();
        }
        return UserDiscoverer.create((com.mojang.authlib.GameProfile) profile);
    }

    @Override
    public Collection<GameProfile> getAll() {
        return UserDiscoverer.getAllProfiles();
    }

    @Override
    public boolean delete(GameProfile profile) {
        return UserDiscoverer.delete(checkNotNull(profile, "profile").getUniqueId());
    }

    @Override
    public boolean delete(User user) {
        return UserDiscoverer.delete(checkNotNull(user, "user").getUniqueId());
    }

    @Override
    public Collection<GameProfile> match(String lastKnownName) {
        lastKnownName = checkNotNull(lastKnownName, "lastKnownName").toLowerCase(Locale.ROOT);
        Collection<GameProfile> allProfiles = UserDiscoverer.getAllProfiles();
        Collection<GameProfile> matching = Sets.newHashSet();
        for (GameProfile profile : allProfiles) {
            if (profile.getName().startsWith(lastKnownName)) {
                matching.add(profile);
            }
        }
        return matching;
    }

}

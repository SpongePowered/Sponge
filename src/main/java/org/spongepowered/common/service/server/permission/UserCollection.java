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
package org.spongepowered.common.service.server.permission;

import com.mojang.authlib.GameProfile;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.Collection;
import java.util.UUID;

/**
 * User collection keeping track of opped users.
 */
public class UserCollection extends SpongeSubjectCollection {

    public UserCollection(final SpongePermissionService service) {
        super(PermissionService.SUBJECTS_USER, service);
    }

    @Override
    public SpongeSubject get(final String identifier) {
        final UUID uuid = this.identityToUuid(identifier);
        if (uuid == null) {
            throw new IllegalArgumentException("Provided identifier must be a uuid, was " + identifier);
        }
        return this.get(this.uuidToGameProfile(uuid));
    }

    protected SpongeSubject get(final GameProfile profile) {
        return new UserSubject(profile, this);
    }

    private GameProfile uuidToGameProfile(final UUID uuid) {
        try {
            return SpongeGameProfile.toMcProfile(Sponge.server().gameProfileManager().basicProfile(uuid).get());
        } catch (final Exception e) {
            SpongeCommon.logger().warn("Failed to lookup game profile for {}", uuid, e);
            // TODO: I'm sure this is null for a reason, but it breaks subjects.
            // Temporary.
            return new GameProfile(uuid, null);
        }
    }

    @Override
    public boolean isRegistered(final String identifier) {
        final UUID uuid = this.identityToUuid(identifier);
        if (uuid == null) {
            return false;
        }
        // Name doesn't matter when getting entries
        final GameProfile profile = new GameProfile(uuid, null);
        return SpongePermissionService.getOps().get(profile) != null;
    }

    private UUID identityToUuid(final String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Subject> loadedSubjects() {
        return (Collection) SpongeCommon.game().server().onlinePlayers();
        /*return ImmutableSet.copyOf(Iterables.concat(
                Iterables.<Object, Subject>transform(SpongePermissionService.getOps().getValues().values(),
                        new Function<Object, Subject>() {
                        @Nullable
                        @Override
                        public Subject apply(Object input) {
                            GameProfile profile = (((UserListOpsEntry) input).value);
                            return get(profile);
                        }
                        // WARNING: This gives dupes
                    }), Sponge.game().getServer().getOnlinePlayers()));*/
    }

    public SpongePermissionService getService() {
        return this.service;
    }
}

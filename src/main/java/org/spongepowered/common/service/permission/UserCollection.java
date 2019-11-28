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
package org.spongepowered.common.service.permission;

import com.mojang.authlib.GameProfile;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.service.permission.base.SpongeSubject;
import org.spongepowered.common.service.permission.base.SpongeSubjectCollection;

import java.util.Collection;
import java.util.UUID;

/**
 * User collection keeping track of opped users.
 */
public class UserCollection extends SpongeSubjectCollection {

    public UserCollection(SpongePermissionService service) {
        super(PermissionService.SUBJECTS_USER, service);
    }

    @Override
    public SpongeSubject get(String identifier) {
        UUID uid = identToUuid(identifier);
        if (uid == null) {
            throw new IllegalArgumentException("Provided identifier must be a uuid, was " + identifier);
        }
        return get(uuidToGameProfile(uid));
    }

    protected SpongeSubject get(GameProfile profile) {
        return new UserSubject(profile, this);
    }

    private GameProfile uuidToGameProfile(UUID uniqueId) {
        try {
            return (GameProfile) Sponge.getServer().getGameProfileManager().get(uniqueId, true).get();
        } catch (Exception e) {
            SpongeImpl.getLogger().warn("Failed to lookup game profile for {}", uniqueId, e);
            return null;
        }
    }

    @Override
    public boolean isRegistered(String identifier) {
        UUID uid = identToUuid(identifier);
        if (uid == null) {
            return false;
        }
        GameProfile profile = uuidToGameProfile(uid);
        return SpongePermissionService.getOps().func_152683_b(profile) != null;
    }

    private UUID identToUuid(String identifier) {
        try {
            return UUID.fromString(identifier);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Subject> getLoadedSubjects() {
        return (Collection) SpongeImpl.getGame().getServer().getOnlinePlayers();
        /*return ImmutableSet.copyOf(Iterables.concat(
                Iterables.<Object, Subject>transform(SpongePermissionService.getOps().getValues().values(),
                        new Function<Object, Subject>() {
                        @Nullable
                        @Override
                        public Subject apply(Object input) {
                            GameProfile profile = ((GameProfile) ((UserListOpsEntry) input).value);
                            return get(profile);
                        }
                        // WARNING: This gives dupes
                    }), Sponge.getGame().getServer().getOnlinePlayers()));*/
    }

    public SpongePermissionService getService() {
        return this.service;
    }
}

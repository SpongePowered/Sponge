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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.common.bridge.authlib.GameProfileHolderBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;

import java.util.Objects;

/**
 * {@link SubjectBridge} helper class to apply the appropriate subject to the
 * mixin.
 */
public final class SubjectHelper {

    private SubjectHelper() {
    }

    public static void applySubject(final SubjectBridge ref, final String subjectCollectionIdentifier) {
        Objects.requireNonNull(ref);
        final PermissionService service = Sponge.server().serviceProvider().permissionService();
        final SubjectReference subject;

        // check if we're using the native Sponge impl
        // we can skip some unnecessary instance creation this way.
        if (service instanceof SpongePermissionService) {
            final SpongePermissionService serv = (SpongePermissionService) service;
            final SpongeSubjectCollection collection = serv.get(subjectCollectionIdentifier);

            if (ref instanceof GameProfileHolderBridge && collection instanceof UserCollection) {
                // GameProfile is already resolved, use it directly
                subject = ((UserCollection) collection).get(((GameProfileHolderBridge) ref).bridge$getGameProfile()).asSubjectReference();
            } else {
                subject = collection.get(((Subject) ref).identifier()).asSubjectReference();
            }
        } else {
            // build a new subject reference using the permission service
            // this doesn't actually load the subject, so it will be lazily init'd when needed.
            subject = service.newSubjectReference(subjectCollectionIdentifier, ((Subject) ref).identifier());
        }

        ref.bridge$setSubject(subject);
    }

}

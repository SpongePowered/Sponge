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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.mixin.core.service.permission.SubjectMixin;
import org.spongepowered.common.service.permission.base.SpongeSubjectCollection;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * {@link SubjectMixin} helper class to apply the appropriate subject to the
 * mixin.
 */
public class SubjectSettingCallback implements Predicate<PermissionService> {

    private final WeakReference<SubjectBridge> ref;

    public SubjectSettingCallback(SubjectBridge ref) {
        this.ref = new WeakReference<>(ref);
    }

    @Override
    public boolean test(@Nullable PermissionService input) {
        SubjectBridge ref = this.ref.get();
        if (ref == null) {
            // returning false from this predicate means this setting callback will be removed
            // as a listener, and will not be tested again.
            return false;
        }

        // if PS has just been unregistered, ignore the change.
        if (input == null) {
            return true;
        }

        SubjectReference subject;

        // check if we're using the native Sponge impl
        // we can skip some unnecessary instance creation this way.
        if (input instanceof SpongePermissionService) {
            SpongePermissionService serv = (SpongePermissionService) input;
            SpongeSubjectCollection collection = serv.get(ref.bridge$getSubjectCollectionIdentifier());

            if (ref instanceof User && collection instanceof UserCollection) {
                // GameProfile is already resolved, use it directly
                subject = ((UserCollection) collection).get((GameProfile) ((User) ref).getProfile()).asSubjectReference();
            } else {
                subject = collection.get(((Subject) ref).getIdentifier()).asSubjectReference();
            }
        } else {
            // build a new subject reference using the permission service
            // this doesn't actually load the subject, so it will be lazily init'd when needed.
            subject = input.newSubjectReference(ref.bridge$getSubjectCollectionIdentifier(), ((Subject) ref).getIdentifier());
        }

        ref.bridge$setSubject(subject);
        return true;
    }

}

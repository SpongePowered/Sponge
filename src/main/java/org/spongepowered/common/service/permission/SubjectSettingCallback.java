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
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.mixin.core.command.MixinSubject;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * {@link MixinSubject} helper class to apply the appropriate subject to the
 * mixin.
 */
public class SubjectSettingCallback implements Predicate<PermissionService> {

    private final WeakReference<IMixinSubject> ref;

    public SubjectSettingCallback(IMixinSubject ref) {
        this.ref = new WeakReference<>(ref);
    }

    @Override
    public boolean test(@Nullable PermissionService input) {
        IMixinSubject ref = this.ref.get();
        if (ref == null) {
            return false;
        }
        if (input == null) {
            return true;
        }
        SubjectCollection userSubjects = input.getSubjects(ref.getSubjectCollectionIdentifier());
        if (userSubjects != null) {
            Subject subject;
            if (ref instanceof User && userSubjects instanceof UserCollection) {
                // GameProfile is already resolved, use it directly
                subject = ((UserCollection) userSubjects).get((GameProfile) ((User) ref).getProfile());
            } else {
                subject = userSubjects.get(((Subject) ref).getIdentifier());
            }
            ref.setSubject(subject);
        }
        return true;
    }

}

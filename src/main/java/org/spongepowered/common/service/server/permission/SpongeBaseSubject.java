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

import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;

public abstract class SpongeBaseSubject implements Subject {

    public abstract PermissionService getService();

    @Override
    public abstract MemorySubjectData transientSubjectData();

    @Override
    public boolean isSubjectDataPersisted() {
        return false;
    }

    @Override
    public SubjectReference asSubjectReference() {
        return this.getService().newSubjectReference(this.containingCollection().identifier(), this.identifier());
    }

    @Override
    public boolean hasPermission(final String permission, final Cause cause) {
        return this.permissionValue(permission, cause) == Tristate.TRUE;
    }

    @Override
    public Tristate permissionValue(final String permission, final Cause cause) {
        return this.getDataPermissionValue(this.transientSubjectData(), permission);
    }

    protected Tristate getDataPermissionValue(MemorySubjectData subject, String permission) {
        Tristate res = subject.nodeTree(SubjectData.GLOBAL_CONTEXT).get(permission);

        if (res == Tristate.UNDEFINED) {
            for (final SubjectReference parent : subject.parents(SubjectData.GLOBAL_CONTEXT)) {
                res = parent.resolve().join().permissionValue(permission, null);
                if (res != Tristate.UNDEFINED) {
                    return res;
                }
            }
        }

        return res;
    }

    @Override
    public boolean isChildOf(final SubjectReference parent, final Cause cause) {
        return this.subjectData().parents(SubjectData.GLOBAL_CONTEXT).contains(parent);
    }

    @Override
    public List<? extends SubjectReference> parents(final Cause cause) {
        return this.subjectData().parents(SubjectData.GLOBAL_CONTEXT);
    }

    protected Optional<String> getDataOptionValue(MemorySubjectData subject, String option) {
        Optional<String> res = Optional.ofNullable(subject.options(SubjectData.GLOBAL_CONTEXT).get(option));

        if (!res.isPresent()) {
            for (SubjectReference parent : subject.parents(SubjectData.GLOBAL_CONTEXT)) {
                res = parent.resolve().join().option(option, null);
                if (res.isPresent()) {
                    return res;
                }
            }
        }

        return res;
    }

    @Override
    public Optional<String> option(String key, Cause cause) {
        return this.getDataOptionValue(this.transientSubjectData(), key);
    }

}

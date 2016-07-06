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
package org.spongepowered.common.service.permission.base;

import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class SpongeSubject implements Subject {

    @Override
    public MemorySubjectData getTransientSubjectData() {
        return getSubjectData();
    }

    @Override
    public abstract MemorySubjectData getSubjectData();

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return getPermissionValue(contexts, permission) == Tristate.TRUE;
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return getDataPermissionValue(getSubjectData(), permission);
    }

    protected Tristate getDataPermissionValue(MemorySubjectData subject, String permission) {
        Tristate res = subject.getNodeTree(SubjectData.GLOBAL_CONTEXT).get(permission);

        if (res == Tristate.UNDEFINED) {
            for (Subject parent : subject.getParents(SubjectData.GLOBAL_CONTEXT)) {
                Tristate tempRes = parent.getPermissionValue(SubjectData.GLOBAL_CONTEXT, permission);
                if (tempRes != Tristate.UNDEFINED) {
                    res = tempRes;
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return getSubjectData().getParents(contexts).contains(parent);
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return getSubjectData().getParents(contexts);
    }

    protected Optional<String> getDataOptionValue(MemorySubjectData subject, String option) {
        Optional<String> res = Optional.ofNullable(subject.getOptions(SubjectData.GLOBAL_CONTEXT).get(option));

        if (!res.isPresent()) {
            for (Subject parent : subject.getParents(SubjectData.GLOBAL_CONTEXT)) {
                Optional<String> tempRes = parent.getOption(SubjectData.GLOBAL_CONTEXT, option);
                if (tempRes.isPresent()) {
                    res = tempRes;
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        return getDataOptionValue(getSubjectData(), key);
    }

    @Override
    public Set<Context> getActiveContexts() {
        return SubjectData.GLOBAL_CONTEXT;
    }
}

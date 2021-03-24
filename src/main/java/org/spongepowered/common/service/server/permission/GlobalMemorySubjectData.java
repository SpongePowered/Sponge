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

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GlobalMemorySubjectData extends MemorySubjectData {

    /**
     * Creates a new subject data instance, using the provided service to request instances of permission subjects.
     */
    public GlobalMemorySubjectData(final Subject subject) {
        super(subject);
    }

    @Override
    public Map<Set<Context>, List<? extends SubjectReference>> allParents() {
        return ImmutableMap.of(SubjectData.GLOBAL_CONTEXT, this.parents(SubjectData.GLOBAL_CONTEXT));
    }

    @Override
    public CompletableFuture<Boolean> setPermission(final Set<Context> contexts, final String permission, final Tristate value) {
        if (!contexts.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        return super.setPermission(contexts, permission, value);
    }

    @Override
    public CompletableFuture<Boolean> clearPermissions(final Set<Context> contexts) {
        if (!contexts.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        return super.clearPermissions(contexts);
    }

    @Override
    public CompletableFuture<Boolean> addParent(final Set<Context> contexts, final SubjectReference parent) {
        if (!contexts.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        return super.addParent(contexts, parent);
    }

    @Override
    public CompletableFuture<Boolean> removeParent(final Set<Context> contexts, final SubjectReference parent) {
        if (!contexts.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        return super.removeParent(contexts, parent);
    }

    @Override
    public CompletableFuture<Boolean> clearParents(final Set<Context> contexts) {
        if (!contexts.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        return super.clearParents(contexts);
    }
}

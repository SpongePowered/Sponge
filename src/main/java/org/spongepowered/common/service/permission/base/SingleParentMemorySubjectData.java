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
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.common.service.permission.OpLevelCollection;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

public class SingleParentMemorySubjectData extends GlobalMemorySubjectData {
    private SubjectReference parent;

    /**
     * Creates a new subject data instance, using the provided service to request instances of permission subjects.
     *

     * @param service The service to request subject references from
     */
    public SingleParentMemorySubjectData(PermissionService service) {
        super(service);
    }

    @Override
    public List<SubjectReference> getParents(Set<Context> contexts) {
        final SubjectReference parent = getParent();
        return contexts.isEmpty() && parent != null ? Collections.singletonList(parent) : Collections.emptyList();
    }

    @Override
    public CompletableFuture<Boolean> addParent(Set<Context> contexts, SubjectReference parent) {
        if (!(parent instanceof OpLevelCollection.OpLevelSubject)) {
            return CompletableFuture.completedFuture(false);
        }
        if (!contexts.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        setParent(parent);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> removeParent(Set<Context> contexts, SubjectReference parent) {
        if (parent == this.parent) {
            setParent(null);
            return CompletableFuture.completedFuture(true);
        }

        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> clearParents() {
        return removeParent(GLOBAL_CONTEXT, this.parent);
    }

    @Override
    public CompletableFuture<Boolean> clearParents(Set<Context> contexts) {
        if (!contexts.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        return clearParents();
    }

    public void setParent(@Nullable SubjectReference parent) {
        this.parent = parent;
    }

    @Nullable
    public SubjectReference getParent() {
        return this.parent;
    }
}

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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation that forces a single parent to always be part of the parents.
 */
public class FixedParentMemorySubjectData extends GlobalMemorySubjectData {
    private final SubjectReference forcedParent;

    /**
     * Creates a new subject data instance, using the provided service to request instances of permission subjects.
     */
    public FixedParentMemorySubjectData(final Subject subject, final SubjectReference parent) {
        super(subject);
        this.forcedParent = parent;
    }

    @Override
    public List<SubjectReference> parents(final Set<Context> contexts) {
        return ImmutableList.<SubjectReference>builder().add(this.forcedParent).addAll(super.parents(contexts)).build();
    }

    @Override
    public CompletableFuture<Boolean> addParent(final Set<Context> contexts, final SubjectReference parent) {
        if (Objects.equal(this.forcedParent, parent) && contexts.isEmpty()) {
            return CompletableFuture.completedFuture(true);
        }
        return super.addParent(contexts, parent);
    }

    @Override
    public CompletableFuture<Boolean> removeParent(final Set<Context> contexts, final SubjectReference parent) {
        if (Objects.equal(this.forcedParent, parent)) {
            return CompletableFuture.completedFuture(false);
        }
        return super.removeParent(contexts, parent);
    }
}

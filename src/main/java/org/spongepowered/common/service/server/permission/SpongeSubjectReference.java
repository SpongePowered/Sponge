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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.concurrent.CompletableFuture;

public class SpongeSubjectReference implements SubjectReference {
    private final SpongePermissionService service;
    private final String collectionId;
    private final String subjectId;
    private @Nullable SpongeSubject cache;

    public SpongeSubjectReference(final SpongePermissionService service, final String collectionId, final String subjectId) {
        this.service = service;
        this.collectionId = collectionId;
        this.subjectId = subjectId;
    }

    @Override
    public String collectionIdentifier() {
        return this.collectionId;
    }

    @Override
    public String subjectIdentifier() {
        return this.subjectId;
    }

    @Override
    public synchronized CompletableFuture<Subject> resolve() {
        // lazily load
        if (this.cache == null) {
            this.cache = this.service.get(this.collectionId).get(this.subjectId);
        }

        return CompletableFuture.completedFuture(this.cache);
    }

}

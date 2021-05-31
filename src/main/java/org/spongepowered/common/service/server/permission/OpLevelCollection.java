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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpLevelCollection extends SpongeSubjectCollection {

    private final Map<String, OpLevelSubject> levels;

    public OpLevelCollection(final SpongePermissionService service) {
        super(PermissionService.SUBJECTS_GROUP, service);
        final ImmutableMap.Builder<String, OpLevelSubject> build = ImmutableMap.builder();
        for (int i = 0; i <= 4; ++i) {
            build.put("op_" + i, new OpLevelSubject(service, i)); // TODO: Add subject data
        }
        this.levels = build.build();
    }

    @Override
    public SpongeSubject get(final String identifier) {
        final SpongeSubject subject = this.levels.get(identifier);
        if (subject == null) {
            throw new IllegalArgumentException(identifier + " is not a valid op level group name (op_{0,4})");
        }
        return subject;
    }

    @Override
    public boolean isRegistered(final String identifier) {
        return this.levels.containsKey(identifier);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Subject> loadedSubjects() {
        return (Collection) this.levels.values();
    }

    public static class OpLevelSubject extends SpongeSubject {

        private final SpongePermissionService service;
        private final int level;
        private final MemorySubjectData data;

        public OpLevelSubject(final SpongePermissionService service, final int level) {
            this.service = service;
            this.level = level;
            this.data = new GlobalMemorySubjectData(this) {

                @Override
                public List<SubjectReference> parents(final Set<Context> contexts) {
                    if (!contexts.isEmpty()) {
                        return Collections.emptyList();
                    }
                    if (level == 0) {
                        return super.parents(contexts);
                    } else {
                        return ImmutableList.<SubjectReference>builder().add(service.getGroupForOpLevel(level - 1).asSubjectReference()).addAll(super.parents(contexts)).build();
                    }
                }
            };
            SpongePermissions.populateNonCommandPermissions(this.data, (permLevel, name) -> level == permLevel);
        }

        public int opLevel() {
            return this.level;
        }

        @Override
        public String identifier() {
            return "op_" + this.level;
        }

        @Override
        public SubjectCollection containingCollection() {
            return this.service.groupSubjects();
        }

        @Override
        public PermissionService service() {
            return this.service;
        }

        @Override
        public MemorySubjectData subjectData() {
            return this.data;
        }
    }
}

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

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.util.Tristate;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class DataFactoryCollection extends SpongeSubjectCollection {
    private final SpongePermissionService service;
    private final ConcurrentMap<String, SpongeSubject> subjects = new ConcurrentHashMap<>();
    private final Function<Subject, MemorySubjectData> dataFactory;

    protected DataFactoryCollection(
            final String identifier,
            final SpongePermissionService service,
            final Function<Subject, MemorySubjectData> dataFactory) {
        super(identifier, service);
        this.service = service;
        this.dataFactory = dataFactory;
    }

    @Override
    public SpongeSubject get(final String identifier) {
        checkNotNull(identifier, "identifier");
        if (!this.subjects.containsKey(identifier)) {
            this.subjects.putIfAbsent(identifier, new DataFactorySubject(identifier, this.dataFactory));
        }
        return this.subjects.get(identifier);
    }

    @Override
    public boolean isRegistered(final String identifier) {
        return this.subjects.containsKey(identifier);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Subject> loadedSubjects() {
        return (Collection) this.subjects.values();
    }

    private class DataFactorySubject extends SpongeSubject {
        private final String identifier;
        private final MemorySubjectData data;

        protected DataFactorySubject(final String identifier, final Function<Subject, MemorySubjectData> dataFactory) {
            this.identifier = identifier;
            this.data = dataFactory.apply(this);
        }

        @Override
        public String identifier() {
            return this.identifier;
        }

        @Override
        public Optional<String> friendlyIdentifier() {
            // TODO provide friendly identifier if possible
            return Optional.empty();
        }

        @Override
        public SubjectCollection containingCollection() {
            return DataFactoryCollection.this;
        }

        @Override
        public PermissionService service() {
            return DataFactoryCollection.this.service;
        }

        @Override
        public MemorySubjectData subjectData() {
            return this.data;
        }

        @Override
        public Tristate permissionValue(final String permission, final Cause cause) {
            Tristate ret = super.permissionValue(permission, cause);

            if (ret == Tristate.UNDEFINED) {
                ret = this.dataPermissionValue(DataFactoryCollection.this.defaults().transientSubjectData(), permission);
            }

            if (ret == Tristate.UNDEFINED) {
                ret = this.dataPermissionValue(DataFactoryCollection.this.service.defaults().transientSubjectData(), permission);
            }
            return ret;
        }

        @Override
        public Optional<String> option(final String option, final Cause cause) {
            Optional<String> ret = super.option(option, cause);
            if (!ret.isPresent()) {
                ret = this.dataOptionValue(DataFactoryCollection.this.defaults().subjectData(), option);
            }
            if (!ret.isPresent()) {
                ret = this.dataOptionValue(DataFactoryCollection.this.service.defaults().subjectData(), option);
            }
            return ret;
        }
    }
}

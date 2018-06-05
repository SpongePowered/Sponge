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

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.service.permission.base.SpongeSubject;
import org.spongepowered.common.service.permission.base.SpongeSubjectCollection;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class DataFactoryCollection extends SpongeSubjectCollection {
    private final SpongePermissionService service;
    private final ConcurrentMap<String, SpongeSubject> subjects = new ConcurrentHashMap<>();
    private final Function<String, MemorySubjectData> dataFactory;
    final Function<String, CommandSource> commandSourceFunction;

    protected DataFactoryCollection(String identifier, SpongePermissionService service, Function<String, MemorySubjectData> dataFactory,
                                    Function<String, CommandSource> commandSourceFunction) {
        super(identifier, service);
        this.service = service;
        this.dataFactory = dataFactory;
        this.commandSourceFunction = commandSourceFunction;
    }

    @Override
    public SpongeSubject get(String identifier) {
        checkNotNull(identifier, "identifier");
        if (!this.subjects.containsKey(identifier)) {
            this.subjects.putIfAbsent(identifier, new DataFactorySubject(identifier, this.dataFactory.apply(identifier)));
        }
        return this.subjects.get(identifier);
    }

    @Override
    public boolean isRegistered(String identifier) {
        return this.subjects.containsKey(identifier);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Subject> getLoadedSubjects() {
        return (Collection) this.subjects.values();
    }

    private class DataFactorySubject extends SpongeSubject {
        private final String identifier;
        private final MemorySubjectData data;

        protected DataFactorySubject(String identifier, MemorySubjectData data) {
            this.identifier = identifier;
            this.data = data;
        }

        @Override
        public String getIdentifier() {
            return this.identifier;
        }

        @Override
        public Optional<String> getFriendlyIdentifier() {
            return getCommandSource().map(CommandSource::getName);
        }

        @Override
        public Optional<CommandSource> getCommandSource() {
            return Optional.ofNullable(DataFactoryCollection.this.commandSourceFunction.apply(getIdentifier()));
        }

        @Override
        public SubjectCollection getContainingCollection() {
            return DataFactoryCollection.this;
        }

        @Override
        public PermissionService getService() {
            return DataFactoryCollection.this.service;
        }

        @Override
        public SubjectReference asSubjectReference() {
            return DataFactoryCollection.this.service.newSubjectReference(DataFactoryCollection.this.getIdentifier(), getIdentifier());
        }

        @Override
        public MemorySubjectData getSubjectData() {
            return this.data;
        }

        @Override
        public Tristate getPermissionValue(Set<Context> contexts, String permission) {
            Tristate ret = super.getPermissionValue(contexts, permission);

            if (ret == Tristate.UNDEFINED) {
                ret = getDataPermissionValue(DataFactoryCollection.this.getDefaults().getTransientSubjectData(), permission);
            }

            if (ret == Tristate.UNDEFINED) {
                ret = getDataPermissionValue(DataFactoryCollection.this.service.getDefaults().getTransientSubjectData(), permission);
            }
            return ret;
        }

        @Override
        public Optional<String> getOption(Set<Context> contexts, String option) {
            Optional<String> ret = super.getOption(contexts, option);
            if (!ret.isPresent()) {
                ret = getDataOptionValue(DataFactoryCollection.this.getDefaults().getSubjectData(), option);
            }
            if (!ret.isPresent()) {
                ret = getDataOptionValue(DataFactoryCollection.this.service.getDefaults().getSubjectData(), option);
            }
            return ret;
        }
    }
}

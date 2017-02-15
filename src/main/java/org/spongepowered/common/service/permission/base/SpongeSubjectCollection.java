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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.service.permission.SpongePermissionService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class SpongeSubjectCollection implements SubjectCollection {
    private final String identifier;
    protected final SpongePermissionService service;

    protected SpongeSubjectCollection(String identifier, SpongePermissionService service) {
        this.identifier = identifier;
        this.service = service;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public Predicate<String> getIdentifierValidityPredicate() {
        return s -> true;
    }

    @Override
    public SubjectReference newSubjectReference(String subjectIdentifier) {
        return this.service.newSubjectReference(getIdentifier(), subjectIdentifier);
    }

    public abstract SpongeSubject get(String identifier);

    public abstract boolean isRegistered(String identifier);

    @Override
    public CompletableFuture<Subject> loadSubject(String identifier) {
        return CompletableFuture.completedFuture(get(identifier));
    }

    @Override
    public Optional<Subject> getSubject(String identifier) {
        return Optional.of(get(identifier));
    }

    @Override
    public CompletableFuture<Boolean> hasSubject(String identifier) {
        return CompletableFuture.completedFuture(isRegistered(identifier));
    }

    @Override
    public CompletableFuture<Map<String, Subject>> loadSubjects(Set<String> identifiers) {
        Map<String, Subject> ret = new HashMap<>();
        for (String id : identifiers) {
            ret.put(id, get(id));
        }
        return CompletableFuture.completedFuture(ImmutableMap.copyOf(ret));
    }

    @Override
    public Map<Subject, Boolean> getLoadedWithPermission(String permission) {
        final Map<Subject, Boolean> ret = new HashMap<>();
        for (Subject subj : getLoadedSubjects()) {
            Tristate state = subj.getPermissionValue(subj.getActiveContexts(), permission);
            if (state != Tristate.UNDEFINED) {
                ret.put(subj, state.asBoolean());
            }
        }
        return Collections.unmodifiableMap(ret);
    }

    @Override
    public Map<Subject, Boolean> getLoadedWithPermission(Set<Context> contexts, String permission) {
        final Map<Subject, Boolean> ret = new HashMap<>();
        for (Subject subj : getLoadedSubjects()) {
            Tristate state = subj.getPermissionValue(contexts, permission);
            if (state != Tristate.UNDEFINED) {
                ret.put(subj, state.asBoolean());
            }
        }
        return Collections.unmodifiableMap(ret);
    }

    @Override
    public CompletableFuture<Map<SubjectReference, Boolean>> getAllWithPermission(String permission) {
        return CompletableFuture.completedFuture(getLoadedWithPermission(permission).entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().asSubjectReference(),
                        Map.Entry::getValue)
                )
        );
    }

    @Override
    public CompletableFuture<Map<SubjectReference, Boolean>> getAllWithPermission(Set<Context> contexts, String permission) {
        return CompletableFuture.completedFuture(getLoadedWithPermission(contexts, permission).entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().asSubjectReference(),
                        Map.Entry::getValue)
                )
        );
    }

    @Override
    public CompletableFuture<Set<String>> getAllIdentifiers() {
        return CompletableFuture.completedFuture(getLoadedSubjects().stream()
                .map(Subject::getIdentifier)
                .collect(ImmutableSet.toImmutableSet())
        );
    }

    @Override
    public SpongeSubject getDefaults() {
        return this.service.getDefaultCollection().get(getIdentifier());
    }

    @Override
    public void suggestUnload(String identifier) {
        // not needed since everything is stored in memory.
    }
}

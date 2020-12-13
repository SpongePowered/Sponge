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

import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.Contextual;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.bridge.permissions.SubjectBridge;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Common Subject Implementation via SubjectBridge
 */
public interface BridgeSubject extends Subject {

    @Override
    default SubjectReference asSubjectReference() {
        return ((SubjectBridge) this).bridge$resolveReferenceOptional()
                .orElseThrow(() -> new IllegalStateException("No subject reference present for user " + this));
    }

    @Override
    default boolean isSubjectDataPersisted() {
        return ((SubjectBridge) this).bridge$resolveOptional()
                .map(Subject::isSubjectDataPersisted)
                .orElse(false);
    }

    @Override
    default Set<Context> getActiveContexts() {
        return ((SubjectBridge) this).bridge$resolveOptional()
                .map(Subject::getActiveContexts)
                .orElseGet(Collections::emptySet);
    }

    @Override
    default Optional<String> getFriendlyIdentifier() {
        return ((SubjectBridge) this).bridge$resolveOptional()
                .flatMap(Contextual::getFriendlyIdentifier);
    }

    @Override
    default SubjectCollection getContainingCollection() {
        return ((SubjectBridge) this).bridge$resolve().getContainingCollection();
    }

    @Override
    default SubjectData getSubjectData() {
        return ((SubjectBridge) this).bridge$resolve().getSubjectData();
    }

    @Override
    default SubjectData getTransientSubjectData() {
        return ((SubjectBridge) this).bridge$resolve().getTransientSubjectData();
    }

    @Override
    default Tristate getPermissionValue(final Set<Context> contexts, final String permission) {
        return ((SubjectBridge) this).bridge$resolveOptional()
                .map(subject -> subject.getPermissionValue(contexts, permission))
                .orElseGet(() -> ((SubjectBridge) this).bridge$permDefault(permission));
    }

    @Override
    default boolean hasPermission(final Set<Context> contexts, final String permission) {
        return ((SubjectBridge) this).bridge$resolveOptional()
                .map(subject -> {
                    final Tristate ret = subject.getPermissionValue(contexts, permission);
                    if (ret == Tristate.UNDEFINED) {
                        return ((SubjectBridge) this).bridge$permDefault(permission).asBoolean();
                    }
                    return ret.asBoolean();
                })
                .orElseGet(() -> ((SubjectBridge) this).bridge$permDefault(permission).asBoolean());
    }

    @Override
    default boolean hasPermission(final String permission) {
        // forwarded to the implementation in this class, and not the default
        // in the Subject interface so permission defaults can be applied
        return this.hasPermission(this.getActiveContexts(), permission);
    }

    @Override
    default boolean isChildOf(final Set<Context> contexts, final SubjectReference parent) {
        return ((SubjectBridge) this).bridge$resolve().isChildOf(contexts, parent);
    }

    @Override
    default boolean isChildOf(final SubjectReference parent) {
        return ((SubjectBridge) this).bridge$resolve().isChildOf(parent);
    }

    @Override
    default List<SubjectReference> getParents(final Set<Context> contexts) {
        return ((SubjectBridge) this).bridge$resolve().getParents(contexts);
    }

    @Override
    default List<SubjectReference> getParents() {
        return ((SubjectBridge) this).bridge$resolve().getParents();
    }

    @Override
    default Optional<String> getOption(final Set<Context> contexts, final String key) {
        return ((SubjectBridge) this).bridge$resolve().getOption(contexts, key);
    }

    @Override
    default Optional<String> getOption(final String key) {
        return ((SubjectBridge) this).bridge$resolve().getOption(key);
    }
}

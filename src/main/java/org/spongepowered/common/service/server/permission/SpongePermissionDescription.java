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

import com.google.common.collect.ImmutableSet;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.plugin.PluginContainer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Basic implementation of {@link PermissionDescription}. Can only be used in
 * conjunction with {@link SpongePermissionService}.
 */
class SpongePermissionDescription implements PermissionDescription {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\.<[a-zA-Z0-9_-]+>");
    private static final Pattern PERMISSION_FORMAT = Pattern.compile("^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*(\\.<[a-zA-Z0-9_-]+>)?$");

    private final SpongePermissionService permissionService;
    private final String id;
    /* The ID but without any trailing placeholders */
    private final String strippedId;
    private final @Nullable Component description;
    private final PluginContainer owner;

    SpongePermissionDescription(final SpongePermissionService permissionService, final String id, final String strippedId, final @Nullable Component description, final PluginContainer owner) {
        this.permissionService = permissionService;
        this.id = id;
        this.strippedId = strippedId;
        this.description = description;
        this.owner = owner;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Optional<Component> description() {
        return Optional.ofNullable(this.description);
    }

    @Override
    public Map<? extends Subject, Boolean> assignedSubjects(String identifier) {
        SubjectCollection subjects = this.permissionService.get(identifier);
        return subjects.loadedWithPermission(this.strippedId);
    }

    @Override
    public boolean query(final Subject subj) {
        return subj.hasPermission(this.strippedId);
    }

    @Override
    public boolean query(final Subject subj, final ResourceKey key) {
        return subj.hasPermission(this.strippedId + '.' + key.getNamespace() + '.' + key.getValue());
    }

    @Override
    public boolean query(final Subject subj, final String... parameters) {
        if (parameters.length == 0) {
            return this.query(subj);
        } else if (parameters.length == 1) {
            return this.query(subj, parameters[0]);
        }
        final StringBuilder build = new StringBuilder(this.strippedId);
        for (final String parameter : parameters) {
            build.append('.').append(parameter);
        }
        return subj.hasPermission(build.toString());
    }

    @Override
    public boolean query(final Subject subj, final String parameter) {
        final String extendedPermission = this.strippedId + '.' + Objects.requireNonNull(parameter, "parameter");
        return subj.hasPermission(extendedPermission);
    }

    @Override
    public CompletableFuture<Map<? extends SubjectReference, Boolean>> findAssignedSubjects(String type) {
        SubjectCollection subjects = this.permissionService.get(type);
        return subjects.allWithPermission(this.strippedId);
    }

    @Override
    public Optional<PluginContainer> owner() {
        return Optional.of(this.owner);
    }

    @Override
    public Tristate getDefaultValue() {
        return this.permissionService.getDefaults().getPermissionValue(this.strippedId);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        SpongePermissionDescription other = (SpongePermissionDescription) obj;
        return this.id.equals(other.id) && this.owner.equals(other.owner) && this.description.equals(other.description);
    }

    @Override
    public String toString() {
        return "SpongePermissionDescription{"
          + "owner=" + this.owner
          + ", id=" + this.id
          + ", description=" + this.description
          + '}';
    }

    static class Builder implements PermissionDescription.Builder {

        private final SpongePermissionService permissionService;
        private final PluginContainer owner;
        private String id;
        private @Nullable Component description;
        private final Map<String, Tristate> roleAssignments = new LinkedHashMap<>();
        private Tristate defaultValue = Tristate.UNDEFINED;

        Builder(final SpongePermissionService permissionService, final PluginContainer owner) {
            this.permissionService = Objects.requireNonNull(permissionService, "permissionService");
            this.owner = Objects.requireNonNull(owner, "owner");
        }

        @Override
        public SpongePermissionDescription.Builder id(final String id) {
            if (!SpongePermissionDescription.PERMISSION_FORMAT.matcher(Objects.requireNonNull(id, "permissionId")).matches()) {
                throw new IllegalArgumentException("Input permission '" + this.id + "' is not in the allowed format '" + PERMISSION_FORMAT.pattern() + "'");
            }
            this.id = id;
            return this;
        }

        @Override
        public SpongePermissionDescription.Builder description(final @Nullable Component description) {
            this.description = description;
            return this;
        }

        @Override
        public SpongePermissionDescription.Builder assign(final String role, final boolean value) {
            Objects.requireNonNull(role, "role");
            this.roleAssignments.put(role, Tristate.fromBoolean(value));
            return this;
        }

        @Override
        public SpongePermissionDescription.Builder defaultValue(final Tristate defaultValue) {
            this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
            return this;
        }

        @Override
        public SpongePermissionDescription register() throws IllegalStateException {
            if (this.id == null) {
                throw new IllegalStateException("No id set");
            }
            final String strippedId = PLACEHOLDER.matcher(this.id).replaceAll("");

            SpongePermissionDescription description =
                    new SpongePermissionDescription(this.permissionService, this.id, strippedId, this.description, this.owner);
            this.permissionService.addDescription(description);

            // Set default value
            if (this.defaultValue != Tristate.UNDEFINED) {
                this.permissionService.getDefaults().getTransientSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, strippedId, this.defaultValue);
            }

            // Set role-templates
            SpongeSubjectCollection subjects = this.permissionService.get(PermissionService.SUBJECTS_ROLE_TEMPLATE);
            for (Entry<String, Tristate> assignment : this.roleAssignments.entrySet()) {
                Subject subject = subjects.get(assignment.getKey());
                subject.transientSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, strippedId, assignment.getValue());
            }
            return description;
        }
    }

}

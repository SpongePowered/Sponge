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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.service.permission.base.SpongeSubjectCollection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Basic implementation of {@link PermissionDescription}. Can only be used in
 * conjunction with {@link SpongePermissionService}.
 */
class SpongePermissionDescription implements PermissionDescription {

    private final SpongePermissionService permissionService;
    private final String id;
    @Nullable private final Text description;
    private final PluginContainer owner;

    SpongePermissionDescription(SpongePermissionService permissionService, String id, @Nullable Text description, PluginContainer owner) {
        super();
        this.permissionService = checkNotNull(permissionService, "permissionService");
        this.id = checkNotNull(id, "id");
        this.description = description;
        this.owner = checkNotNull(owner, "owner");
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Optional<Text> getDescription() {
        return Optional.ofNullable(this.description);
    }

    @Override
    public Map<Subject, Boolean> getAssignedSubjects(String identifier) {
        SubjectCollection subjects = this.permissionService.get(identifier);
        return subjects.getLoadedWithPermission(this.id);
    }

    @Override
    public CompletableFuture<Map<SubjectReference, Boolean>> findAssignedSubjects(String type) {
        SubjectCollection subjects = this.permissionService.get(type);
        return subjects.getAllWithPermission(this.id);
    }

    @Override
    public Optional<PluginContainer> getOwner() {
        return Optional.of(this.owner);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        SpongePermissionDescription other = (SpongePermissionDescription) obj;
        return this.id.equals(other.id) && this.owner.equals(other.owner) && this.description.equals(other.description);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("owner", this.owner)
                .add("id", this.id)
                .toString();
    }

    static class Builder implements PermissionDescription.Builder {

        private final SpongePermissionService permissionService;
        private final PluginContainer owner;
        private String id;
        @Nullable private Text description;
        private final Map<String, Tristate> roleAssignments = new LinkedHashMap<>();

        Builder(SpongePermissionService permissionService, PluginContainer owner) {
            super();
            this.permissionService = checkNotNull(permissionService, "permissionService");
            this.owner = checkNotNull(owner, "owner");
        }

        @Override
        public org.spongepowered.common.service.permission.SpongePermissionDescription.Builder id(String id) {
            this.id = checkNotNull(id, "permissionId");
            return this;
        }

        @Override
        public org.spongepowered.common.service.permission.SpongePermissionDescription.Builder description(@Nullable Text description) {
            this.description = description;
            return this;
        }

        @Override
        public org.spongepowered.common.service.permission.SpongePermissionDescription.Builder assign(String role, boolean value) {
            Preconditions.checkNotNull(role, "role");
            this.roleAssignments.put(role, Tristate.fromBoolean(value));
            return this;
        }

        @Override
        public SpongePermissionDescription register() throws IllegalStateException {
            checkState(this.id != null, "No id set");
            SpongePermissionDescription description =
                    new SpongePermissionDescription(this.permissionService, this.id, this.description, this.owner);
            this.permissionService.addDescription(description);

            // Set role-templates
            SpongeSubjectCollection subjects = this.permissionService.get(PermissionService.SUBJECTS_ROLE_TEMPLATE);
            for (Entry<String, Tristate> assignment : this.roleAssignments.entrySet()) {
                Subject subject = subjects.get(assignment.getKey());
                subject.getTransientSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, this.id, assignment.getValue());
            }
            return description;
        }
    }

}

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.minecraft.server.players.ServerOpList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * Permission service representing the vanilla operator permission structure.
 *
 * <p>Really doesn't do much else. Don't use this guys.
 */
@Singleton
public final class SpongePermissionService implements PermissionService {
    private static final String SUBJECTS_DEFAULT = "default";

    private final Game game;
    private final Map<String, PermissionDescription> descriptionMap = new LinkedHashMap<>();
    private @Nullable Collection<PermissionDescription> descriptions;
    private final ConcurrentMap<String, SpongeSubjectCollection> subjects = new ConcurrentHashMap<>();
    private final SpongeSubjectCollection defaultCollection;
    private final SpongeSubject defaultData;

    @Inject
    public SpongePermissionService(final Game game) {
        this.game = game;
        this.subjects.put(SpongePermissionService.SUBJECTS_DEFAULT, (this.defaultCollection = this.newCollection(SpongePermissionService.SUBJECTS_DEFAULT)));
        this.subjects.put(PermissionService.SUBJECTS_USER, new UserCollection(this));
        this.subjects.put(PermissionService.SUBJECTS_GROUP, new OpLevelCollection(this));

        this.subjects.put(PermissionService.SUBJECTS_COMMAND_BLOCK, new DataFactoryCollection(
            PermissionService.SUBJECTS_COMMAND_BLOCK, this,
                s -> new FixedParentMemorySubjectData(s, this.getGroupForOpLevel(2).asSubjectReference())));

        this.subjects.put(PermissionService.SUBJECTS_SYSTEM, new DataFactoryCollection(
            PermissionService.SUBJECTS_SYSTEM, this,
                s -> new FixedParentMemorySubjectData(s, this.getGroupForOpLevel(4).asSubjectReference())
//                , s -> {
//                    if (s.equals("Server")) {
//                        return SpongeImpl.game().getServer().getConsole();
//                    } /*else if (s.equals("RCON")) {
//                        TODO: Implement RCON API?
//                    }*/
//                    return null;
//                }
                ));

        this.defaultData = this.getDefaultCollection().get(SpongePermissionService.SUBJECTS_DEFAULT);
    }

    static ServerOpList getOps() {
        return SpongeCommon.getServer().getPlayerList().getOps();
    }

    static int getServerOpLevel() {
        return SpongeCommon.getServer().getOperatorUserPermissionLevel();
    }

    public Subject getGroupForOpLevel(final int level) {
        return this.groupSubjects().get("op_" + level);
    }

    @Override
    public SpongeSubjectCollection userSubjects() {
        return this.get(PermissionService.SUBJECTS_USER);
    }

    @Override
    public SpongeSubjectCollection groupSubjects() {
        return this.get(PermissionService.SUBJECTS_GROUP);
    }

    private SpongeSubjectCollection newCollection(final String identifier) {
        return new DataFactoryCollection(identifier, this, GlobalMemorySubjectData::new);
    }

    public SpongeSubjectCollection get(final String identifier) {
        SpongeSubjectCollection ret = this.subjects.get(identifier);
        if (ret == null) {
            final SpongeSubjectCollection existingRet = this.subjects.putIfAbsent(identifier, (ret = this.newCollection(identifier)));
            if (existingRet != null) {
                ret = existingRet;
            }
        }
        return ret;
    }

    @Override
    public SpongeSubject defaults() {
        return this.defaultData;
    }

    @Override
    public Predicate<String> identifierValidityPredicate() {
        return s -> true;
    }

    @Override
    public SubjectReference newSubjectReference(final String collectionIdentifier, final String subjectIdentifier) {
        checkNotNull(collectionIdentifier, "collectionIdentifier");
        checkNotNull(subjectIdentifier, "subjectIdentifier");
        return new SpongeSubjectReference(this, collectionIdentifier, subjectIdentifier);
    }

    @Override
    public CompletableFuture<SubjectCollection> loadCollection(final String identifier) {
        return CompletableFuture.completedFuture(this.get(identifier));
    }

    @Override
    public Optional<SubjectCollection> collection(final String identifier) {
        return Optional.of(this.get(identifier));
    }

    @Override
    public CompletableFuture<Boolean> hasCollection(final String identifier) {
        return CompletableFuture.completedFuture(this.subjects.containsKey(identifier));
    }

    @Override
    public Map<String, SubjectCollection> loadedCollections() {
        return ImmutableMap.copyOf(this.subjects);
    }

    @Override
    public CompletableFuture<Set<String>> allIdentifiers() {
        return CompletableFuture.completedFuture(this.loadedCollections().keySet());
    }

    @Override
    public Builder newDescriptionBuilder(final PluginContainer container) {
        return new SpongePermissionDescription.Builder(this, container);
    }

    public void addDescription(final PermissionDescription permissionDescription) {
        checkNotNull(permissionDescription, "permissionDescription");
        checkNotNull(permissionDescription.id(), "permissionId");
        this.descriptionMap.put(permissionDescription.id().toLowerCase(), permissionDescription);
        this.descriptions = null;
    }

    @Override
    public Optional<PermissionDescription> description(final String permissionId) {
        return Optional.ofNullable(this.descriptionMap.get(checkNotNull(permissionId, "permissionId").toLowerCase()));
    }

    @Override
    public Collection<PermissionDescription> descriptions() {
        Collection<PermissionDescription> descriptions = this.descriptions;
        if (descriptions == null) {
            descriptions = ImmutableList.copyOf(this.descriptionMap.values());
            this.descriptions = descriptions;
        }
        return descriptions;
    }

    public SpongeSubjectCollection getDefaultCollection() {
        return this.defaultCollection;
    }
}

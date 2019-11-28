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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.management.UserListOps;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.service.permission.base.FixedParentMemorySubjectData;
import org.spongepowered.common.service.permission.base.GlobalMemorySubjectData;
import org.spongepowered.common.service.permission.base.SpongeSubject;
import org.spongepowered.common.service.permission.base.SpongeSubjectCollection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Permission service representing the vanilla operator permission structure.
 *
 * <p>Really doesn't do much else. Don't use this guys.
 */
public class SpongePermissionService implements PermissionService {
    private static final String SUBJECTS_DEFAULT = "default";
    private static final Function<String, CommandSource> NO_COMMAND_SOURCE = s -> null;

    private final Game game;
    private final Map<String, PermissionDescription> descriptionMap = new LinkedHashMap<>();
    @Nullable private Collection<PermissionDescription> descriptions;
    private final ConcurrentMap<String, SpongeSubjectCollection> subjects = new ConcurrentHashMap<>();
    private final SpongeSubjectCollection defaultCollection;
    private final SpongeSubject defaultData;

    public SpongePermissionService(Game game) {
        this.game = game;
        this.subjects.put(SUBJECTS_DEFAULT, (this.defaultCollection = newCollection(SUBJECTS_DEFAULT)));
        this.subjects.put(SUBJECTS_USER, new UserCollection(this));
        this.subjects.put(SUBJECTS_GROUP, new OpLevelCollection(this));

        this.subjects.put(SUBJECTS_COMMAND_BLOCK, new DataFactoryCollection(SUBJECTS_COMMAND_BLOCK, this,
                s -> new FixedParentMemorySubjectData(this, getGroupForOpLevel(2).asSubjectReference()), NO_COMMAND_SOURCE));

        this.subjects.put(SUBJECTS_SYSTEM, new DataFactoryCollection(SUBJECTS_SYSTEM, this,
                s -> new FixedParentMemorySubjectData(this, getGroupForOpLevel(4).asSubjectReference()),
                s -> {
                    if (s.equals("Server")) {
                        return SpongeImpl.getGame().getServer().getConsole();
                    } /*else if (s.equals("RCON")) {
                        TODO: Implement RCON API?
                    }*/
                    return null;
                }));

        this.defaultData = getDefaultCollection().get(SUBJECTS_DEFAULT);
    }

    static UserListOps getOps() {
        return SpongeImpl.getServer().func_184103_al().func_152603_m();
    }

    static int getServerOpLevel() {
        return SpongeImpl.getServer().func_110455_j();
    }

    public Subject getGroupForOpLevel(int level) {
        return getGroupSubjects().get("op_" + level);
    }

    @Override
    public SpongeSubjectCollection getUserSubjects() {
        return get(PermissionService.SUBJECTS_USER);
    }

    @Override
    public SpongeSubjectCollection getGroupSubjects() {
        return get(PermissionService.SUBJECTS_GROUP);
    }

    private SpongeSubjectCollection newCollection(String identifier) {
        return new DataFactoryCollection(identifier, this, s -> new GlobalMemorySubjectData(SpongePermissionService.this), NO_COMMAND_SOURCE);
    }

    public SpongeSubjectCollection get(String identifier) {
        SpongeSubjectCollection ret = this.subjects.get(identifier);
        if (ret == null) {
            SpongeSubjectCollection existingRet = this.subjects.putIfAbsent(identifier, (ret = newCollection(identifier)));
            if (existingRet != null) {
                ret = existingRet;
            }
        }
        return ret;
    }

    @Override
    public SpongeSubject getDefaults() {
        return this.defaultData;
    }

    @Override
    public Predicate<String> getIdentifierValidityPredicate() {
        return s -> true;
    }

    @Override
    public SubjectReference newSubjectReference(String collectionIdentifier, String subjectIdentifier) {
        checkNotNull(collectionIdentifier, "collectionIdentifier");
        checkNotNull(subjectIdentifier, "subjectIdentifier");
        return new SpongeSubjectReference(this, collectionIdentifier, subjectIdentifier);
    }

    @Override
    public CompletableFuture<SubjectCollection> loadCollection(String identifier) {
        return CompletableFuture.completedFuture(get(identifier));
    }

    @Override
    public Optional<SubjectCollection> getCollection(String identifier) {
        return Optional.of(get(identifier));
    }

    @Override
    public CompletableFuture<Boolean> hasCollection(String identifier) {
        return CompletableFuture.completedFuture(this.subjects.containsKey(identifier));
    }

    @Override
    public Map<String, SubjectCollection> getLoadedCollections() {
        return ImmutableMap.copyOf(this.subjects);
    }

    @Override
    public CompletableFuture<Set<String>> getAllIdentifiers() {
        return CompletableFuture.completedFuture(getLoadedCollections().keySet());
    }

    @Override
    public void registerContextCalculator(ContextCalculator<Subject> calculator) {

    }

    @Override
    public Builder newDescriptionBuilder(Object instance) {
        Optional<PluginContainer> container = this.game.getPluginManager().fromInstance(checkNotNull(instance, "instance"));
        if (!container.isPresent()) {
            throw new IllegalArgumentException("The provided plugin object does not have an associated plugin container "
                    + "(in other words, is 'plugin' actually your plugin object?)");
        }

        return new SpongePermissionDescription.Builder(this, container.get());
    }

    public void addDescription(PermissionDescription permissionDescription) {
        checkNotNull(permissionDescription, "permissionDescription");
        checkNotNull(permissionDescription.getId(), "permissionId");
        this.descriptionMap.put(permissionDescription.getId().toLowerCase(), permissionDescription);
        this.descriptions = null;
    }

    @Override
    public Optional<PermissionDescription> getDescription(String permissionId) {
        return Optional.ofNullable(this.descriptionMap.get(checkNotNull(permissionId, "permissionId").toLowerCase()));
    }

    @Override
    public Collection<PermissionDescription> getDescriptions() {
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

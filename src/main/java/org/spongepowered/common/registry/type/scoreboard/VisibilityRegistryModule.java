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
package org.spongepowered.common.registry.type.scoreboard;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class VisibilityRegistryModule implements CatalogRegistryModule<Visibility> {

    @RegisterCatalog(Visibilities.class)
    public static final Map<String, Visibility> visibilityMappings = Maps.newHashMap();

    @Override
    public Optional<Visibility> getById(String id) {
        return Optional.ofNullable(visibilityMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Visibility> getAll() {
        return ImmutableList.copyOf(visibilityMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.visibilityMappings.put("all", (Visibility) (Object) Team.EnumVisible.ALWAYS);
        this.visibilityMappings.put("own_team", (Visibility) (Object) Team.EnumVisible.HIDE_FOR_OTHER_TEAMS);
        this.visibilityMappings.put("other_teams", (Visibility) (Object) Team.EnumVisible.HIDE_FOR_OWN_TEAM);
        this.visibilityMappings.put("none", (Visibility) (Object) Team.EnumVisible.NEVER);
    }
}

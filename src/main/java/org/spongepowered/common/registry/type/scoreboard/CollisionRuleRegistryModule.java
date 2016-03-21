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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.scoreboard.Team;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.scoreboard.CollisionRule;
import org.spongepowered.api.scoreboard.CollisionRules;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CollisionRuleRegistryModule implements CatalogRegistryModule<CollisionRule> {

    @RegisterCatalog(CollisionRules.class)
    public final Map<String, CollisionRule> collisionRules = Maps.newHashMap();

    @Override
    public void registerDefaults() {
        this.collisionRules.put("always", (CollisionRule) (Object) Team.CollisionRule.ALWAYS);
        this.collisionRules.put("never", (CollisionRule) (Object) Team.CollisionRule.NEVER);
        this.collisionRules.put("push_other_teams", (CollisionRule) (Object) Team.CollisionRule.HIDE_FOR_OTHER_TEAMS);
        this.collisionRules.put("push_own_team", (CollisionRule) (Object) Team.CollisionRule.HIDE_FOR_OWN_TEAM);
    }

    @Override
    public Optional<CollisionRule> getById(String id) {
        return Optional.ofNullable(this.collisionRules.get(checkNotNull(id, "id").toLowerCase()));
    }

    @Override
    public Collection<CollisionRule> getAll() {
        return ImmutableSet.copyOf(this.collisionRules.values());
    }

}

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
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class CriteriaRegistryModule implements CatalogRegistryModule<Criterion> {

    @RegisterCatalog(Criteria.class)
    public final Map<String, Criterion> criteriaMap = Maps.newHashMap();

    @Override
    public Optional<Criterion> getById(String id) {
        return Optional.ofNullable(this.criteriaMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Criterion> getAll() {
        return ImmutableList.copyOf(this.criteriaMap.values());
    }

    @Override
    public void registerDefaults() {
        this.criteriaMap.put("dummy", (Criterion) IScoreObjectiveCriteria.DUMMY);
        this.criteriaMap.put("trigger", (Criterion) IScoreObjectiveCriteria.TRIGGER);
        this.criteriaMap.put("health", (Criterion) IScoreObjectiveCriteria.health);
        this.criteriaMap.put("player_kills", (Criterion) IScoreObjectiveCriteria.playerKillCount);
        this.criteriaMap.put("total_kills", (Criterion) IScoreObjectiveCriteria.totalKillCount);
        this.criteriaMap.put("deaths", (Criterion) IScoreObjectiveCriteria.deathCount);
    }
}

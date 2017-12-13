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
package org.spongepowered.common.registry.type.statistic;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.statistic.StatisticType;
import org.spongepowered.api.statistic.StatisticTypes;
import org.spongepowered.common.statistic.SpongeStatisticType;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class StatisticTypeRegistryModule implements CatalogRegistryModule<StatisticType> {

    @RegisterCatalog(StatisticTypes.class)
    private final Map<String, StatisticType> statisticTypeMappings = Maps.newHashMap();

    @Override
    public Optional<StatisticType> getById(String id) {
        return Optional.ofNullable(this.statisticTypeMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<StatisticType> getAll() {
        return ImmutableSet.copyOf(this.statisticTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.registerDefault("basic");
        this.registerDefault("blocks_broken", "mine_block");
        this.registerDefault("entities_killed", "kill_entity");
        this.registerDefault("items_broken", "break_item");
        this.registerDefault("items_crafted", "craft_item");
        this.registerDefault("items_dropped", "drop");
        this.registerDefault("items_picked_up", "pickup");
        this.registerDefault("items_used", "use_item");
        this.registerDefault("killed_by_entity", "entity_killed_by");
    }

    private void registerDefault(String id) {
        this.statisticTypeMappings.put(id, new SpongeStatisticType(id));
    }

    private void registerDefault(String id, String alias) {
        SpongeStatisticType type = new SpongeStatisticType(id);
        this.statisticTypeMappings.put(id, type);
        this.statisticTypeMappings.put(alias, type);
    }

}

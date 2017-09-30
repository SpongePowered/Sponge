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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.Statistics;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class StatisticRegistryModule implements SpongeAdditionalCatalogRegistryModule<Statistic>, AlternateCatalogRegistryModule<Statistic> {

    @RegisterCatalog(Statistics.class)
    private final Map<String, Statistic> statisticMappings = Maps.newHashMap();

    public static StatisticRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private StatisticRegistryModule() {
    }

    @Override
    public Optional<Statistic> getById(String id) {
        return Optional.ofNullable(this.statisticMappings.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<Statistic> getAll() {
        return ImmutableList.copyOf(this.statisticMappings.values());
    }

    @Override
    public void registerAdditionalCatalog(Statistic stat) {
        checkNotNull(stat, "null statistic");
        this.statisticMappings.put(stat.getId().toLowerCase(Locale.ENGLISH), stat);
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public Map<String, Statistic> provideCatalogMap() {
        final HashMap<String, Statistic> map = new HashMap<>();
        for (Map.Entry<String, Statistic> entry : this.statisticMappings.entrySet()) {
            final String key = entry.getKey();
            final String alternateKey = MINECRAFT_SPONGE_ID_MAPPINGS.get(key);
            if (alternateKey != null) {
                map.put(alternateKey, entry.getValue());
            } else {
                map.put(key, entry.getValue());
            }
        }
        return map;
    }

    private static final ImmutableMap<String, String> MINECRAFT_SPONGE_ID_MAPPINGS = ImmutableMap.<String, String>builder()
        .put("shulker_box_opened", "open_shulker_box")
        .put("play_one_minute", "time_played")
        .build();

    private static final class Holder {

        static final StatisticRegistryModule INSTANCE = new StatisticRegistryModule();

    }

}

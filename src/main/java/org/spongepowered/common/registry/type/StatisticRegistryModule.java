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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.stats.StatList;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.Statistics;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class StatisticRegistryModule implements AdditionalCatalogRegistryModule<Statistic> {

    @RegisterCatalog(Statistics.class)
    private final Map<String, Statistic> statisticMappings = Maps.newHashMap();

    @Override
    public Optional<Statistic> getById(String id) {
        return Optional.ofNullable(this.statisticMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<Statistic> getAll() {
        return ImmutableSet.copyOf(this.statisticMappings.values());
    }

    @Override
    public void registerDefaults() {
        StatList.ALL_STATS.stream()
                .map(statistic -> (Statistic) statistic)
                .forEach(this::registerAdditionalCatalog);
        remap("boat_one_cm", "boat_distance");
        remap("climb_one_cm", "climb_distance");
        remap("crouch_one_cm", "crouch_distance");
        remap("dive_one_cm", "dive_distance");
        remap("fall_one_cm", "fall_distance");
        remap("fly_one_cm", "fly_distance");
        remap("horse_one_cm", "horse_distance");
        remap("drop", "items_dropped");
        remap("item_enchanted", "items_enchanted");
        remap("minecart_one_cm", "minecart_distance");
        remap("pig_one_cm", "pig_distance");
        remap("sprint_one_cm", "sprint_distance");
        remap("swim_one_cm", "swim_distance");
        remap("play_one_minute", "time_played");
        remap("walk_one_cm", "walk_distance");
    }

    private void remap(String official, String catalog) {
        this.statisticMappings.putIfAbsent(catalog, this.statisticMappings.get(official));
    }

    @AdditionalRegistration
    public void registerAdditionals() {
        registerDefaults();
    }

    @Override
    public void registerAdditionalCatalog(Statistic statistic) {
        String lowerId = statistic.getId().toLowerCase(Locale.ENGLISH);
        this.statisticMappings.putIfAbsent(lowerId, statistic);
        this.statisticMappings.putIfAbsent(lowerId.replaceFirst("^stat.", ""), statistic);
        String underscoreId = statistic.getId().replaceAll("[A-Z]", "_$0").toLowerCase(Locale.ENGLISH);
        this.statisticMappings.putIfAbsent(underscoreId, statistic);
        this.statisticMappings.putIfAbsent(underscoreId.replaceFirst("^stat.", ""), statistic);
    }

}

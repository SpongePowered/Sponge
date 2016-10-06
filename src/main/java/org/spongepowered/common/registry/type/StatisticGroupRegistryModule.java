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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.stats.StatList;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.statistic.Statistic;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.common.statistic.SpongeStatisticGroup;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class StatisticGroupRegistryModule implements AdditionalCatalogRegistryModule<StatisticGroup> {

    private final Map<String, StatisticGroup> groupMappings = Maps.newHashMap();

    @Override
    public Optional<StatisticGroup> getById(String id) {
        return Optional.ofNullable(this.groupMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<StatisticGroup> getAll() {
        return ImmutableList.copyOf(this.groupMappings.values());
    }

    @Override
    public void registerDefaults() {
        SpongeStatisticGroup.init();
        StatList.ALL_STATS.stream()
                .map(statistic -> (Statistic) statistic)
                .map(Statistic::getGroup)
                .distinct()
                .forEach(this::registerAdditionalCatalog);
    }

    @AdditionalRegistration
    public void registerAdditionals() {
        registerDefaults();
    }

    @Override
    public void registerAdditionalCatalog(StatisticGroup group) {
        this.groupMappings.putIfAbsent(group.getId().toLowerCase(Locale.ENGLISH), group);
    }

}

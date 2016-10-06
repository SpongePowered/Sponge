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
import net.minecraft.stats.AchievementList;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.statistic.achievement.Achievements;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public final class AchievementRegistryModule implements AdditionalCatalogRegistryModule<Achievement> {

    @RegisterCatalog(Achievements.class)
    private final Map<String, Achievement> achievementMappings = Maps.newHashMap();
    private @Nullable Set<Achievement> achievements;

    @Override
    public Optional<Achievement> getById(String id) {
        return Optional.ofNullable(this.achievementMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<Achievement> getAll() {
        if (this.achievements == null) {
            this.achievements = ImmutableSet.copyOf(this.achievementMappings.values());
        }
        return this.achievements;
    }

    @Override
    public void registerDefaults() {
        AchievementList.ACHIEVEMENTS.stream()
                .map(achievement -> (Achievement) achievement)
                .forEach(this::registerAdditionalCatalog);
        remap("potion", "brew_potion");
        remap("build_work_bench", "build_workbench");
        remap("theend", "end_portal");
        remap("blaze_rod", "get_blaze_rod");
        remap("diamonds", "get_diamonds");
        remap("ghast", "ghast_return");
        remap("portal", "nether_portal");
    }

    private void remap(String official, String catalog) {
        this.achievementMappings.putIfAbsent(catalog, this.achievementMappings.get(official));
    }

    @AdditionalRegistration
    public void registerAdditionals() {
        registerDefaults();
    }

    @Override
    public void registerAdditionalCatalog(Achievement achievement) {
        String lowerId = achievement.getId().toLowerCase(Locale.ENGLISH);
        this.achievementMappings.putIfAbsent(lowerId, achievement);
        this.achievementMappings.putIfAbsent(lowerId.replaceFirst("^achievement.", ""), achievement);
        String underscoreId = achievement.getId().replaceAll("[A-Z]", "_$0").toLowerCase(Locale.ENGLISH);
        this.achievementMappings.putIfAbsent(underscoreId, achievement);
        this.achievementMappings.putIfAbsent(underscoreId.replaceFirst("^achievement.", ""), achievement);
        this.achievements = null;
    }

}

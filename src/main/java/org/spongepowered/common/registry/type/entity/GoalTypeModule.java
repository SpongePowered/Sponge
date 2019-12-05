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
package org.spongepowered.common.registry.type.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.entity.ai.GoalExecutor;
import org.spongepowered.api.entity.ai.GoalExecutorType;
import org.spongepowered.api.entity.ai.GoalExecutorTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.ai.SpongeGoalExecutorType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.ai.goal.GoalSelector;

public class GoalTypeModule implements AlternateCatalogRegistryModule<GoalExecutorType> {

    public static GoalTypeModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(GoalExecutorTypes.class)
    private final Map<String, GoalExecutorType> goalTypes = new HashMap<>();

    @Override
    public Map<String, GoalExecutorType> provideCatalogMap() {
        Map<String, GoalExecutorType> goalMap = new HashMap<>();
        for (Map.Entry<String, GoalExecutorType> entry : this.goalTypes.entrySet()) {
            goalMap.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return goalMap;
    }

    @Override
    public Optional<GoalExecutorType> getById(String id) {
        checkNotNull(id);
        if (!id.contains(":")) {
            id = "minecraft:" + id; // assume vanilla
        }
        return Optional.ofNullable(this.goalTypes.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<GoalExecutorType> getAll() {
        return ImmutableList.copyOf(this.goalTypes.values());
    }

    @Override
    public void registerDefaults() {
        this.createGoalType("minecraft:normal", "Normal");
        this.createGoalType("minecraft:target", "Target");
    }

    private GoalExecutorType createGoalType(String combinedId, String name) {
        @SuppressWarnings("unchecked")
        final SpongeGoalExecutorType newType = new SpongeGoalExecutorType(combinedId, name, (Class<GoalExecutor<?>>) (Class<?>) GoalSelector.class);
        this.goalTypes.put(combinedId, newType);
        return newType;
    }

    public GoalExecutorType createGoalType(Object plugin, String id, String name) {
        final Optional<PluginContainer> optPluginContainer = SpongeImpl.getGame().getPluginManager().fromInstance(plugin);
        Preconditions.checkArgument(optPluginContainer.isPresent());
        final PluginContainer pluginContainer = optPluginContainer.get();
        final String combinedId = pluginContainer.getId().toLowerCase(Locale.ENGLISH) + ":" + id;

        return this.createGoalType(combinedId, name);
    }

    GoalTypeModule() {
    }

    private static final class Holder {

        static final GoalTypeModule INSTANCE = new GoalTypeModule();
    }
}

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
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.ai.task.AITaskTypes;
import org.spongepowered.api.entity.ai.task.builtin.SwimmingAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.AttackLivingAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.AvoidEntityAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.WanderAITask;
import org.spongepowered.api.entity.ai.task.builtin.WatchClosestAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.horse.RunAroundLikeCrazyAITask;
import org.spongepowered.api.entity.ai.task.builtin.creature.target.FindNearestAttackableTargetAITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.ai.SpongeAITaskType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AITaskTypeModule implements AlternateCatalogRegistryModule<AITaskType> {

    public static AITaskTypeModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(AITaskTypes.class)
    private final Map<String, AITaskType> aiTaskTypes = new ConcurrentHashMap<>();

    @Override
    public Map<String, AITaskType> provideCatalogMap() {
        Map<String, AITaskType> aiMap = new HashMap<>();
        for (Map.Entry<String, AITaskType> entry : this.aiTaskTypes.entrySet()) {
            aiMap.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return aiMap;
    }

    @Override
    public Optional<AITaskType> getById(String id) {
        checkNotNull(id);
        if (!id.contains(":")) {
            id = "minecraft:" + id; // assume vanilla
        }
        return Optional.ofNullable(this.aiTaskTypes.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<AITaskType> getAll() {
        return ImmutableList.copyOf(this.aiTaskTypes.values());
    }

    public Optional<AITaskType> getByAIClass(Class<?> clazz) {
        for (AITaskType type : this.aiTaskTypes.values()) {
            if (type.getAIClass().isAssignableFrom(clazz)) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }

    @Override
    public void registerDefaults() {
        createAITaskType("minecraft:wander", "Wander", WanderAITask.class);
        createAITaskType("minecraft:avoid_entity", "Avoid Entity", AvoidEntityAITask.class);
        createAITaskType("minecraft:run_around_like_crazy", "Run Around Like Crazy", RunAroundLikeCrazyAITask.class);
        createAITaskType("minecraft:swimming", "Swimming", SwimmingAITask.class);
        createAITaskType("minecraft:watch_closest", "Watch Closest", WatchClosestAITask.class);
        createAITaskType("minecraft:find_nearest_attackable_target", "Find Nearest Attackable Target", FindNearestAttackableTargetAITask.class);
        createAITaskType("minecraft:attack_living", "Attack Living", AttackLivingAITask.class);
    }

    private AITaskType createAITaskType(String combinedId, String name, Class<? extends AITask<? extends Agent>> aiClass) {
        final SpongeAITaskType newType = new SpongeAITaskType(combinedId, name, aiClass);
        this.aiTaskTypes.put(combinedId, newType);
        return newType;
    }

    public AITaskType createAITaskType(Object plugin, String id, String name, Class<? extends AITask<? extends Agent>> aiClass) {
        final Optional<PluginContainer> optPluginContainer = SpongeImpl.getGame().getPluginManager().fromInstance(plugin);
        Preconditions.checkArgument(optPluginContainer.isPresent());
        final PluginContainer pluginContainer = optPluginContainer.get();
        final String combinedId = pluginContainer.getId().toLowerCase(Locale.ENGLISH) + ':' + id;

        return createAITaskType(combinedId, name, aiClass);
    }

    AITaskTypeModule() {}

    private static final class Holder {
        static final AITaskTypeModule INSTANCE = new AITaskTypeModule();
    }
}

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
package org.spongepowered.common.registry.provider;

import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.ai.goal.GoalTypes;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public final class GoalTypeProvider {

    public static GoalTypeProvider INSTANCE;

    private final Map<Class<? extends Goal>, GoalType> mappings;

    GoalTypeProvider() {
        GoalTypeProvider.INSTANCE = this;

        this.mappings = new IdentityHashMap<>();

        this.mappings.put(AvoidEntityGoal.class, GoalTypes.AVOID_LIVING.get());
        this.mappings.put(MeleeAttackGoal.class, GoalTypes.ATTACK_LIVING.get());
        this.mappings.put(NearestAttackableTargetGoal.class, GoalTypes.FIND_NEAREST_ATTACKABLE.get());
        this.mappings.put(LookAtGoal.class, GoalTypes.LOOK_AT.get());
        this.mappings.put(LookRandomlyGoal.class, GoalTypes.LOOK_RANDOMLY.get());
        this.mappings.put(RandomWalkingGoal.class, GoalTypes.RANDOM_WALKING.get());
        this.mappings.put(RangedAttackGoal.class, GoalTypes.RANGED_ATTACK_AGAINST_AGENT.get());
        this.mappings.put(RunAroundLikeCrazyGoal.class, GoalTypes.RUN_AROUND_LIKE_CRAZY.get());
        this.mappings.put(SwimGoal.class, GoalTypes.SWIM.get());

    }

    public Optional<GoalType> get(Class<? extends Goal> type) {
        return Optional.ofNullable(this.mappings.get(type));
    }
}

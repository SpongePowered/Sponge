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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.entity.ai.goal.GoalType;
import org.spongepowered.api.entity.ai.goal.builtin.LookAtGoal;
import org.spongepowered.api.entity.ai.goal.builtin.LookRandomlyGoal;
import org.spongepowered.api.entity.ai.goal.builtin.SwimGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AttackLivingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.AvoidLivingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.RandomWalkingGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.RangedAttackAgainstAgentGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.horse.RunAroundLikeCrazyGoal;
import org.spongepowered.api.entity.ai.goal.builtin.creature.target.FindNearestAttackableTargetGoal;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.entity.ai.goal.SpongeGoalType;

import java.util.stream.Stream;

public final class GoalTypeStreamGenerator {

    private GoalTypeStreamGenerator() {
    }

    public static Stream<Tuple<GoalType, Class<? extends Goal>>> stream() {
        return Stream.of(
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("attack_living"), AttackLivingGoal.class), MeleeAttackGoal.class),
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("avoid_living"), AvoidLivingGoal.class), AvoidEntityGoal.class),
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("find_nearest_attackable"), FindNearestAttackableTargetGoal.class), NearestAttackableTargetGoal.class),
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("look_at"), LookAtGoal.class), net.minecraft.entity.ai.goal.LookAtGoal.class),
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("look_randomly"), LookRandomlyGoal.class), net.minecraft.entity.ai.goal.LookRandomlyGoal.class),
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("random_walking"), RandomWalkingGoal.class), net.minecraft.entity.ai.goal.RandomWalkingGoal.class),
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("ranged_attack_against_agent"), RangedAttackAgainstAgentGoal.class), RangedAttackGoal.class),
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("run_around_like_crazy"), RunAroundLikeCrazyGoal.class), net.minecraft.entity.ai.goal.RunAroundLikeCrazyGoal.class),
            Tuple.of(new SpongeGoalType(CatalogKey.minecraft("swim"), SwimGoal.class), net.minecraft.entity.ai.goal.SwimGoal.class)
        );
    }
}

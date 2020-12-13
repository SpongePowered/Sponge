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
package org.spongepowered.common.registry.builtin.vanilla;

import net.minecraft.scoreboard.ScoreCriteria;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.common.bridge.ResourceKeyBridge;

import java.util.stream.Stream;

public final class CriterionStreamGenerator {

    private CriterionStreamGenerator() {
    }

    public static Stream<Criterion> stream() {
        return Stream.of(
                CriterionStreamGenerator.newCriterion(ScoreCriteria.AIR, ResourceKey.sponge("air")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.ARMOR, ResourceKey.sponge("armor")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.DEATH_COUNT, ResourceKey.sponge("death_count")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.DUMMY, ResourceKey.sponge("dummy")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.FOOD, ResourceKey.sponge("food")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.HEALTH, ResourceKey.sponge("health")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.LEVEL, ResourceKey.sponge("level")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.KILL_COUNT_PLAYERS, ResourceKey.sponge("player_kill_count")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.KILL_COUNT_ALL, ResourceKey.sponge("total_kill_count")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.TRIGGER, ResourceKey.sponge("trigger")),
                CriterionStreamGenerator.newCriterion(ScoreCriteria.EXPERIENCE, ResourceKey.sponge("experience"))
        );
    }

    static Criterion newCriterion(final ScoreCriteria criteria, final ResourceKey key) {
        ((ResourceKeyBridge) criteria).bridge$setKey(key);
        return (Criterion) criteria;
    }
}

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
package org.spongepowered.common.scoreboard.criteria;

import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.api.scoreboard.criteria.Criterion;

public final class SpongeCriterionFactory implements Criterion.Factory {

    @Override
    public Criterion air() {
        return (Criterion) ObjectiveCriteria.AIR;
    }

    @Override
    public Criterion armor() {
        return (Criterion) ObjectiveCriteria.ARMOR;
    }

    @Override
    public Criterion deathCount() {
        return (Criterion) ObjectiveCriteria.DEATH_COUNT;
    }

    @Override
    public Criterion dummy() {
        return (Criterion) ObjectiveCriteria.DUMMY;
    }

    @Override
    public Criterion food() {
        return (Criterion) ObjectiveCriteria.FOOD;
    }

    @Override
    public Criterion health() {
        return (Criterion) ObjectiveCriteria.HEALTH;
    }

    @Override
    public Criterion level() {
        return (Criterion) ObjectiveCriteria.LEVEL;
    }

    @Override
    public Criterion playerKillCount() {
        return (Criterion) ObjectiveCriteria.KILL_COUNT_PLAYERS;
    }

    @Override
    public Criterion totalKillCount() {
        return (Criterion) ObjectiveCriteria.KILL_COUNT_ALL;
    }

    @Override
    public Criterion trigger() {
        return (Criterion) ObjectiveCriteria.TRIGGER;
    }

    @Override
    public Criterion experience() {
        return (Criterion) ObjectiveCriteria.EXPERIENCE;
    }
}

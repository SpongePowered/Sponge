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
package org.spongepowered.common.mixin.api.minecraft.advancements;

import com.google.common.base.Preconditions;
import net.minecraft.advancements.AdvancementProgress;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.CriterionProgress;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreCriterionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.advancement.criterion.ImplementationBackedCriterionProgress;
import org.spongepowered.common.bridge.advancements.AdvancementProgressBridge;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Mixin(AdvancementProgress.class)
public abstract class AdvancementProgressMixin_API implements org.spongepowered.api.advancement.AdvancementProgress {

    @Override
    public Optional<Instant> get() {
        return this.get(this.advancement().criterion()).get().get();
    }

    @Override
    public Instant grant() {
        return this.get(this.advancement().criterion()).get().grant();
    }

    @Override
    public Optional<Instant> revoke() {
        return this.get(this.advancement().criterion()).get().revoke();
    }

    @Override
    public Advancement advancement() {
        return ((AdvancementProgressBridge) this).bridge$getAdvancement();
    }

    @Override
    public Optional<CriterionProgress> get(AdvancementCriterion criterion) {
        Preconditions.checkNotNull(criterion, "criterion");
        final Map<String, ImplementationBackedCriterionProgress> map = ((AdvancementProgressBridge) this).bridge$getProgressMap();
        Preconditions.checkState(map != null, "progressMap isn't initialized");
        return Optional.ofNullable((CriterionProgress) map.get(criterion.name()));
    }

    @Override
    public Optional<ScoreCriterionProgress> get(ScoreAdvancementCriterion criterion) {
        Preconditions.checkNotNull(criterion);
        final Map<String, ImplementationBackedCriterionProgress> map = ((AdvancementProgressBridge) this).bridge$getProgressMap();
        Preconditions.checkState(map != null, "progressMap isn't initialized");
        return Optional.ofNullable((ScoreCriterionProgress) map.get(criterion.name()));
    }

    @Override
    public CriterionProgress require(final AdvancementCriterion criterion) {
        Objects.requireNonNull(criterion, "criterion");

        return this.get(criterion).orElseThrow(() -> new IllegalStateException("The criterion " + criterion.name()
                + " isn't present on the advancement '" + this.advancement() + "'."));
    }

    @Override
    public ScoreCriterionProgress require(final ScoreAdvancementCriterion criterion) {
        Objects.requireNonNull(criterion, "criterion");

        return this.get(criterion).orElseThrow(() -> new IllegalStateException("The score criterion " + criterion.name()
                + " isn't present on the advancement '" + this.advancement().key().toString() + "'."));
    }
}

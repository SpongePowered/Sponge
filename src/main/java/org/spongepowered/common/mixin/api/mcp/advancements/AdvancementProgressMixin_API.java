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
package org.spongepowered.common.mixin.api.mcp.advancements;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.advancements.AdvancementProgress;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.CriterionProgress;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreCriterionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.advancement.ImplementationBackedCriterionProgress;
import org.spongepowered.common.bridge.advancements.AdvancementProgressBridge;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Mixin(AdvancementProgress.class)
public class AdvancementProgressMixin_API implements org.spongepowered.api.advancement.AdvancementProgress {

    @Override
    public Optional<Instant> get() {
        return get(getAdvancement().getCriterion()).get().get();
    }

    @Override
    public Advancement getAdvancement() {
        return ((AdvancementProgressBridge) this).bridge$getAdvancement();
    }

    @Override
    public Optional<CriterionProgress> get(final AdvancementCriterion criterion) {
        checkState(SpongeImplHooks.isMainThread());
        checkNotNull(criterion, "criterion");
        final Map<AdvancementCriterion, ImplementationBackedCriterionProgress> map = ((AdvancementProgressBridge) this).bridge$getProgressMap();
        checkState(map != null, "progressMap isn't initialized");
        return Optional.ofNullable((CriterionProgress) map.get(criterion));
    }

    @Override
    public Optional<ScoreCriterionProgress> get(final ScoreAdvancementCriterion criterion) {
        checkState(SpongeImplHooks.isMainThread());
        final Map<AdvancementCriterion, ImplementationBackedCriterionProgress> map = ((AdvancementProgressBridge) this).bridge$getProgressMap();
        checkState(map != null, "progressMap isn't initialized");
        return Optional.ofNullable((ScoreCriterionProgress) map.get(criterion));
    }

    @Override
    public Instant grant() {
        return get(getAdvancement().getCriterion()).get().grant();
    }

    @Override
    public Optional<Instant> revoke() {
        return get(getAdvancement().getCriterion()).get().revoke();
    }
}

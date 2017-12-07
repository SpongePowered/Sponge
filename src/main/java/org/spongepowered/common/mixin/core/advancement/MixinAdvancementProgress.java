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
package org.spongepowered.common.mixin.core.advancement;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.PlayerAdvancements;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.CriterionProgress;
import org.spongepowered.api.advancement.criteria.OperatorCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.ScoreCriterionProgress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.common.advancement.ICriterionProgress;
import org.spongepowered.common.advancement.SpongeAndCriterion;
import org.spongepowered.common.advancement.SpongeAndCriterionProgress;
import org.spongepowered.common.advancement.SpongeOperatorCriterion;
import org.spongepowered.common.advancement.SpongeOrCriterion;
import org.spongepowered.common.advancement.SpongeOrCriterionProgress;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.advancement.SpongeScoreCriterionProgress;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementProgress;
import org.spongepowered.common.interfaces.advancement.IMixinCriterion;
import org.spongepowered.common.interfaces.advancement.IMixinCriterionProgress;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(AdvancementProgress.class)
public class MixinAdvancementProgress implements org.spongepowered.api.advancement.AdvancementProgress, IMixinAdvancementProgress {

    @Shadow @Final private Map<String, net.minecraft.advancements.CriterionProgress> criteria;

    private final Map<AdvancementCriterion, ICriterionProgress> progressMap = new HashMap<>();
    @Nullable private Advancement advancement;
    @Nullable private PlayerAdvancements playerAdvancements;

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(Map<String, Criterion> criteriaIn, String[][] requirements) {
        for (Map.Entry<String, Criterion> entry : criteriaIn.entrySet()) {
            final IMixinCriterionProgress criterionProgress = (IMixinCriterionProgress) this.criteria.get(entry.getKey());
            criterionProgress.setCriterion((AdvancementCriterion) entry.getValue());
            final IMixinCriterion criterion = (IMixinCriterion) entry.getValue();
            criterion.setName(entry.getKey());
            this.progressMap.put((AdvancementCriterion) entry.getValue(), (ICriterionProgress) criterionProgress);
        }
    }

    @Override
    public PlayerAdvancements getPlayerAdvancements() {
        checkState(this.playerAdvancements != null, "The playerAdvancements is not yet initialized");
        return this.playerAdvancements;
    }

    @Override
    public void setPlayerAdvancements(PlayerAdvancements playerAdvancements) {
        this.playerAdvancements = playerAdvancements;
    }

    @Override
    public void setAdvancement(Advancement advancement) {
        this.advancement = advancement;
    }

    @Override
    public void invalidateAchievedState() {
        for (ICriterionProgress progress : this.progressMap.values()) {
            progress.invalidateAchievedState();
        }
    }

    @Override
    public Advancement getAdvancement() {
        checkState(this.advancement != null, "The advancement is not yet initialized");
        return this.advancement;
    }

    @Override
    public Optional<ScoreCriterionProgress> get(ScoreAdvancementCriterion criterion) {
        return Optional.ofNullable((ScoreCriterionProgress) this.criteria.get(criterion.getName()));
    }

    @Override
    public Optional<CriterionProgress> get(AdvancementCriterion criterion) {
        checkNotNull(criterion, "criterion");
        ICriterionProgress progress = this.progressMap.get(criterion);
        if (progress != null) {
            return Optional.of(progress);
        }
        final AdvancementCriterion advCriterion = getAdvancement().getCriterion();
        if (criterion == AdvancementCriterion.EMPTY || (advCriterion instanceof OperatorCriterion &&
                !((SpongeOperatorCriterion) criterion).getRecursiveChildren().contains(criterion))) {
            return Optional.empty();
        } else if (criterion instanceof SpongeOrCriterion) {
            progress = new SpongeOrCriterionProgress(this, (SpongeOrCriterion) criterion);
        } else if (criterion instanceof SpongeAndCriterion) {
            progress = new SpongeAndCriterionProgress(this, (SpongeAndCriterion) criterion);
        } else if (criterion instanceof SpongeScoreCriterion) {
            progress = new SpongeScoreCriterionProgress(this, (SpongeScoreCriterion) criterion);
        } else {
            throw new IllegalStateException();
        }
        this.progressMap.put(criterion, progress);
        return Optional.of(progress);
    }

    @Override
    public Optional<Instant> get() {
        return get(getAdvancement().getCriterion()).get().get();
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

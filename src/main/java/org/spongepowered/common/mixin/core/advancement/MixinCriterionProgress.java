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

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.advancement.ICriterionProgress;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementProgress;
import org.spongepowered.common.interfaces.advancement.IMixinCriterion;
import org.spongepowered.common.interfaces.advancement.IMixinCriterionProgress;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("RedundantCast")
@Mixin(CriterionProgress.class)
public abstract class MixinCriterionProgress implements ICriterionProgress, IMixinCriterionProgress {

    @Shadow public abstract void obtain();
    @Shadow public abstract void reset();
    @Shadow @Final private AdvancementProgress advancementProgress;
    @Shadow @Nullable private Date obtained;

    @Nullable private AdvancementCriterion criterion;

    @Inject(method = "obtain", at = @At("RETURN"))
    private void onObtain(CallbackInfo ci) {
        ((IMixinAdvancementProgress) this.advancementProgress).invalidateAchievedState();
    }

    @Inject(method = "reset", at = @At("RETURN"))
    private void onReset(CallbackInfo ci) {
        ((IMixinAdvancementProgress) this.advancementProgress).invalidateAchievedState();
    }

    @Override
    public void setCriterion(AdvancementCriterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public AdvancementCriterion getCriterion() {
        checkState(this.criterion != null, "The criterion is not yet initialized");
        return this.criterion;
    }

    @Override
    public boolean achieved() {
        return this.obtained != null;
    }

    @Override
    public Optional<Instant> get() {
        return this.obtained == null ? Optional.empty() : Optional.of(this.obtained.toInstant());
    }

    @Override
    public Instant grant() {
        if (this.obtained != null) {
            return this.obtained.toInstant();
        }
        final SpongeScoreCriterion scoreCriterion = ((IMixinCriterion) getCriterion()).getScoreCriterion();
        final Advancement advancement = (Advancement) ((org.spongepowered.api.advancement.AdvancementProgress)
                this.advancementProgress).getAdvancement();
        ((IMixinAdvancementProgress) this.advancementProgress).getPlayerAdvancements()
                .grantCriterion(advancement, this.criterion.getName());
        return this.obtained.toInstant();
    }

    @Override
    public Optional<Instant> revoke() {
        if (this.obtained == null) {
            return Optional.empty();
        }
        final Instant instant = this.obtained.toInstant();
        final Advancement advancement = (Advancement) ((org.spongepowered.api.advancement.AdvancementProgress)
                this.advancementProgress).getAdvancement();
        ((IMixinAdvancementProgress) this.advancementProgress).getPlayerAdvancements()
                .revokeCriterion(advancement, this.criterion.getName());
        return Optional.of(instant);
    }

    @Override
    public void invalidateAchievedState() {
    }
}

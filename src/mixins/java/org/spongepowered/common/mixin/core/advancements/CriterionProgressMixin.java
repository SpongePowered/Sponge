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
package org.spongepowered.common.mixin.core.advancements;

import net.minecraft.advancements.CriterionProgress;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.advancement.criterion.ImplementationBackedCriterionProgress;
import org.spongepowered.common.bridge.advancements.AdvancementProgressBridge;
import org.spongepowered.common.bridge.advancements.CriterionProgressBridge;

@Mixin(CriterionProgress.class)
public abstract class CriterionProgressMixin implements CriterionProgressBridge, ImplementationBackedCriterionProgress {


    @Nullable private AdvancementCriterion impl$criterion;
    private org.spongepowered.api.advancement.AdvancementProgress impl$advancementProgress;

    @Inject(method = "grant", at = @At("RETURN"))
    private void onObtain(CallbackInfo ci) {
        ((AdvancementProgressBridge) this.impl$advancementProgress).bridge$invalidateAchievedState();
    }

    @Inject(method = "revoke", at = @At("RETURN"))
    private void onReset(CallbackInfo ci) {
        ((AdvancementProgressBridge) this.impl$advancementProgress).bridge$invalidateAchievedState();
    }

    @Override
    public AdvancementCriterion bridge$getCriterion() {
        return this.impl$criterion;
    }

    @Override
    public void bridge$setCriterion(AdvancementCriterion criterion) {
        this.impl$criterion = criterion;
    }

    @Override
    public org.spongepowered.api.advancement.AdvancementProgress bridge$getAdvancementProgress() {
        return this.impl$advancementProgress;
    }

    @Override
    public void bridge$setAdvancementProgress(org.spongepowered.api.advancement.AdvancementProgress advancementProgress) {
        this.impl$advancementProgress = advancementProgress;
    }


    @Override
    public boolean bridge$isCriterionAvailable() {
        return this.impl$criterion != null;
    }

    @Override
    public void invalidateAchievedState() {
    }
}

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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.ICriterionInstance;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.advancement.ICriterion;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.interfaces.advancement.IMixinCriterion;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(Criterion.class)
public class MixinCriterion implements ICriterion, IMixinCriterion {

    @Shadow @Final private ICriterionInstance criterionInstance;

    @Nullable private String name;
    @Nullable private SpongeScoreCriterion scoreCriterion;
    @Nullable private Integer scoreGoal;

    @Override
    public String getName() {
        if (this.name == null) {
            this.name = UUID.randomUUID().toString().replace("-", "");
        }
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public SpongeScoreCriterion getScoreCriterion() {
        return this.scoreCriterion;
    }

    @Override
    public void setScoreCriterion(@Nullable SpongeScoreCriterion criterion) {
        this.scoreCriterion = criterion;
    }

    @Nullable
    @Override
    public Integer getScoreGoal() {
        return this.scoreGoal;
    }

    @Override
    public void setScoreGoal(@Nullable Integer goal) {
        this.scoreGoal = goal;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<FilteredTrigger<?>> getTriggers() {
        return Collections.singleton((FilteredTrigger) this.criterionInstance);
    }

    @Inject(method = "criterionFromJson", at = @At("RETURN"))
    private static void onCriterionFromJson(JsonObject json, JsonDeserializationContext context, CallbackInfoReturnable<Criterion> ci) {
        final Criterion criterion = ci.getReturnValue();
        if (json.has("trigger_times")) {
            ((IMixinCriterion) criterion).setScoreGoal(json.get("trigger_times").getAsInt());
        }
    }
}

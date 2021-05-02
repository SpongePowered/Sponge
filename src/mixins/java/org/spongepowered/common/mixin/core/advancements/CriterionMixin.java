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

import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.advancement.criterion.SpongeScoreCriterion;
import org.spongepowered.common.advancement.criterion.SpongeScoreTrigger;
import org.spongepowered.common.bridge.advancements.CriterionBridge;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;

import java.util.UUID;

@Mixin(Criterion.class)
public abstract class CriterionMixin implements CriterionBridge {

    // @formatter:off
    @Shadow @Final private CriterionTriggerInstance trigger;
    // @formatter:on

    @Nullable private String impl$name;
    @Nullable private SpongeScoreCriterion impl$scoreCriterion;
    @Nullable private Integer impl$scoreGoal;
    @Nullable private String impl$scoreCriterionName;

    @Inject(method = "criterionFromJson", at = @At("RETURN"))
    private static void impl$fixTriggerTimeDeserializer(final JsonObject json, final DeserializationContext p_232633_1_,
            final CallbackInfoReturnable<Criterion> ci) {
        final Criterion criterion = ci.getReturnValue();
        if (json.has("trigger_times")) {
            ((CriterionBridge) criterion).bridge$setScoreGoal(json.get("trigger_times").getAsInt());
        }
        if (json.has("criterion")) {
            ((CriterionBridge) criterion).bridge$setScoreCriterionName(json.get("criterion").getAsString());
        }
    }

    @Inject(method = "serializeToJson", at = @At("RETURN"))
    private void impl$serializeTriggerTimes(CallbackInfoReturnable<JsonObject> cir) {
        if (this.trigger instanceof SpongeScoreTrigger.Instance) {
            cir.getReturnValue().addProperty("trigger_times", ((SpongeScoreTrigger.Instance) this.trigger).getTriggerTimes());
        }
        if (this.impl$scoreCriterion != null) {
            cir.getReturnValue().addProperty("criterion", this.impl$scoreCriterion.name());
        }
    }

    @Override
    public String bridge$getName() {
        if (this.impl$name == null) {
            this.impl$name = UUID.randomUUID().toString().replace("-", "");
        }
        return this.impl$name;
    }

    @Override
    public void bridge$setName(String name) {
        this.impl$name = name;
    }

    @Nullable
    @Override
    public SpongeScoreCriterion bridge$getScoreCriterion() {
        return this.impl$scoreCriterion;
    }

    @Override
    public void bridge$setScoreCriterion(@Nullable final SpongeScoreCriterion criterion) {
        this.impl$scoreCriterion = criterion;
    }

    @Nullable
    @Override
    public Integer bridge$getScoreGoal() {
        return this.impl$scoreGoal;
    }

    @Override
    public void bridge$setScoreGoal(@Nullable final Integer goal) {
        this.impl$scoreGoal = goal;
    }

    @Override
    public void bridge$setScoreCriterionName(String name) {
        this.impl$scoreCriterionName = name;
    }

    @Override
    @Nullable
    public String bridge$getScoreCriterionName() {
        return this.impl$scoreCriterionName;
    }
}

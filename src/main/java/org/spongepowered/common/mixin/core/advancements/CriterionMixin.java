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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.ICriterionInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.advancement.SpongeScoreCriterion;
import org.spongepowered.common.bridge.advancements.CriterionBridge;

import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(Criterion.class)
public class CriterionMixin implements CriterionBridge {

    @Shadow @Final @Nullable private ICriterionInstance criterionInstance;

    @Nullable private String impl$name;
    @Nullable private SpongeScoreCriterion impl$scoreCriterion;
    @Nullable private Integer impl$scoreGoal;

    @Override
    public String bridge$getName() {
        if (this.impl$name == null) {
            this.impl$name = UUID.randomUUID().toString().replace("-", "");
        }
        return this.impl$name;
    }

    @Override
    public void bridge$setName(final String name) {
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

    @Inject(method = "criterionFromJson", at = @At("RETURN"))
    private static void impl$fixTriggerTimeDeserializer(final JsonObject json, final JsonDeserializationContext context, final CallbackInfoReturnable<Criterion> ci) {
        final Criterion criterion = ci.getReturnValue();
        if (json.has("trigger_times")) {
            ((CriterionBridge) criterion).bridge$setScoreGoal(json.get("trigger_times").getAsInt());
        }
    }
}

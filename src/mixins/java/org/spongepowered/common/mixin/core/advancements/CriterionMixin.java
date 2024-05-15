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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.util.ExtraCodecs;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.advancement.criterion.SpongeScoreCriterion;
import org.spongepowered.common.advancement.criterion.SpongeScoreTrigger;
import org.spongepowered.common.bridge.advancements.CriterionBridge;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(Criterion.class)
public abstract class CriterionMixin<T extends CriterionTriggerInstance> implements CriterionBridge {

    // @formatter:off
    @Shadow @Final private T triggerInstance;
    // @formatter:on

    @Nullable private String impl$name;
    @Nullable private SpongeScoreCriterion impl$scoreCriterion;
    @Nullable private Integer impl$scoreGoal;
    @Nullable private String impl$scoreCriterionName;

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ExtraCodecs;dispatchOptionalValue(Ljava/lang/String;Ljava/lang/String;Lcom/mojang/serialization/Codec;Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"))
    private static MapCodec<Criterion<?>> impl$onCodecCreation(final String $$0,
            final String $$1,
            final Codec<CriterionTrigger<?>> $$2,
            final Function<? super Criterion<?>, ? extends CriterionTrigger<?>> $$3,
            final Function<? super CriterionTrigger<?>, ? extends Codec<? extends Criterion<?>>> $$4)
    {
        @SuppressWarnings("deprecation")
        var mcCodec = ExtraCodecs.dispatchOptionalValue($$0, $$1, $$2, $$3, $$4);

        final var triggerTimesCodec = Codec.INT.optionalFieldOf("trigger_times");
        var tmpCodec = CriterionMixin.impl$dependent(mcCodec, triggerTimesCodec, CriterionBridge::bridge$getScoreGoal, CriterionBridge::bridge$setScoreGoal);

        final var criterionCodec = Codec.STRING.optionalFieldOf("criterion");
        tmpCodec = CriterionMixin.impl$dependent(tmpCodec, criterionCodec,
                bridge -> bridge.bridge$getScoreCriterion() == null ? null : bridge.bridge$getScoreCriterion().name(),
                CriterionBridge::bridge$setScoreCriterionName);

        return tmpCodec;
    }

    private static <T> MapCodec<Criterion<?>> impl$dependent(
            final MapCodec<Criterion<?>> original,
            final MapCodec<Optional<T>> codec,
            final Function<CriterionBridge, T> getter,
            final BiConsumer<CriterionBridge, T> setter) {
        return original.dependent(codec,
                criterion -> Pair.of(Optional.ofNullable(getter.apply((CriterionBridge) (Object) criterion)), codec),
                (criterion, optValue) -> {
                    optValue.ifPresent(value -> setter.accept((CriterionBridge) (Object) criterion, value));
                    return criterion;
                });
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
        return this.triggerInstance instanceof SpongeScoreTrigger.TriggerInstance instance ? instance.triggerTimes() : null;
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


    @Inject(method = "hashCode", at = @At("RETURN"), cancellable = true)
    public void impl$onHashCode(CallbackInfoReturnable<Integer> cir) {
        int result = cir.getReturnValue();
        result += 31 * result + this.bridge$getName().hashCode();
        cir.setReturnValue(result);
    }

    @Inject(method = "equals", at = @At("RETURN"), cancellable = true)
    public void impl$Equals(Object obj, CallbackInfoReturnable<Boolean> cir) {
        boolean result = cir.getReturnValue();
        result = result && ((CriterionBridge) obj).bridge$getName().equals(this.bridge$getName());
        cir.setReturnValue(result);
    }

}

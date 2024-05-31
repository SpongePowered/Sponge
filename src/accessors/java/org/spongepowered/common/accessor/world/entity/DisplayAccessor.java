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
package org.spongepowered.common.accessor.world.entity;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedInvokerError;

@Mixin(Display.class)
public interface DisplayAccessor {

    @Invoker("setBillboardConstraints") void invoker$setBillboardConstraints(Display.BillboardConstraints $$0);
    @Invoker("getBillboardConstraints") Display.BillboardConstraints invoker$getBillboardConstraints();

    @Invoker("setBrightnessOverride") void invoker$setBrightnessOverride(@Nullable Brightness $$0);
    @Invoker("getPackedBrightnessOverride") int invoker$getPackedBrightnessOverride();

    @Invoker("setTransformation") void invoker$setTransformation(Transformation $$0);

    @Invoker("createTransformation") static Transformation invoker$createTransformation(SynchedEntityData $$0) {
        throw new UntransformedInvokerError();
    }

    @Invoker("setTransformationInterpolationDuration") void invoker$setInterpolationDuration(int $$0);

    @Invoker("getTransformationInterpolationDuration") int invoker$getInterpolationDuration();


    @Invoker("setTransformationInterpolationDelay") void invoker$setInterpolationDelay(int $$0);

    @Invoker("getTransformationInterpolationDelay") int invoker$getInterpolationDelay();

    @Invoker("setPosRotInterpolationDuration") void invoker$setPosRotInterpolationDuration(int $$0);

    @Invoker("getPosRotInterpolationDuration") int invoker$getPosRotInterpolationDuration();

    @Invoker("setShadowRadius") void invoker$setShadowRadius(float $$0);

    @Invoker("getShadowRadius") float invoker$getShadowRadius();


    @Invoker("setShadowStrength") void invoker$setShadowStrength(float $$0);

    @Invoker("getShadowStrength") float invoker$getShadowStrength();

    @Invoker("setViewRange") void invoker$setViewRange(float $$0);

    @Invoker("getViewRange") float invoker$getViewRange();

}

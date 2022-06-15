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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseRouter.class)
public abstract class NoiseRouterMixin_API implements org.spongepowered.api.world.generation.config.noise.NoiseRouter {

    // @formatter:off
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction barrierNoise;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction fluidLevelFloodednessNoise;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction fluidLevelSpreadNoise;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction lavaNoise;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction temperature;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction vegetation;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction continents;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction erosion;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction depth;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction ridges;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction initialDensityWithoutJaggedness;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction finalDensity;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction veinToggle;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction veinRidged;
    @Shadow @Final private net.minecraft.world.level.levelgen.DensityFunction veinGap;
    // @formatter:on

}

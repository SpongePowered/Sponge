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
package org.spongepowered.common.mixin.core.world.dimension;

import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin implements DimensionTypeBridge, ResourceKeyBridge {

    @Mutable @Shadow @Final private BiFunction<World, DimensionType, ? extends Dimension> factory;
    @Mutable @Shadow @Final private boolean hasSkyLight;
    @Nullable private SpongeDimensionType impl$spongeDimensionType;

    @Override
    public SpongeDimensionType bridge$getSpongeDimensionType() {
        return this.impl$spongeDimensionType;
    }

    @Override
    public void bridge$setSpongeDimensionType(final SpongeDimensionType dimensionType) {
        this.impl$spongeDimensionType = dimensionType;
        this.factory = dimensionType.getDimensionFactory();
        this.hasSkyLight = dimensionType.hasSkylight();
    }

    @Override
    public ResourceKey bridge$getKey() {
        return this.bridge$getSpongeDimensionType().getKey();
    }
}

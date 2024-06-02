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
package org.spongepowered.common.accessor.world.entity.animal;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;

@Mixin(Wolf.class)
public interface WolfAccessor {

    @Accessor("DATA_COLLAR_COLOR") static EntityDataAccessor<Integer> accessor$DATA_COLLAR_COLOR() {
        throw new UntransformedAccessorError();
    }

    @Accessor("isWet") boolean accessor$isWet();

    @Accessor("isWet") void accessor$isWet(final boolean isWet);

    @Accessor("isShaking") boolean accessor$isShaking();

    @Accessor("isShaking") void accessor$isShaking(final boolean isShaking);

    @Accessor("shakeAnim") void accessor$shakeAnim(final float shakeAnim);

    @Accessor("shakeAnimO") void accessor$shakeAnimO(final float shakeAnim0);
    @Invoker("setCollarColor") void invoker$setCollarColor(final DyeColor $$0);

}

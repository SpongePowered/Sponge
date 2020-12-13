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
package org.spongepowered.common.accessor.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AreaEffectCloudEntity.class)
public interface AreaEffectCloudEntityAccessor {

    @Accessor("effects") List<EffectInstance> accessor$effects();

    @Accessor("effects") @Mutable void accessor$effects(final List<EffectInstance> effects);

    @Accessor("waitTime") int accessor$waitTime();

    @Accessor("reapplicationDelay") int accessor$reapplicationDelay();

    @Accessor("reapplicationDelay") void accessor$reapplicationDelay(final int reapplicationDelay);

    @Accessor("durationOnUse") void accessor$durationOnUse(final int radiusOnUse);

    @Accessor("durationOnUse") int accessor$durationOnUse();

    @Accessor("radiusOnUse") float accessor$radiusOnUse();

    @Accessor("radiusPerTick") float accessor$radiusPerTick();

}

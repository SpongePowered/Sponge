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
package org.spongepowered.common.mixin.core.entity.monster;

import net.minecraft.entity.monster.EntitySkeleton;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.monster.WhiteSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSkeletonData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

@SuppressWarnings("deprecation")
@Mixin(EntitySkeleton.class)
public abstract class MixinEntitySkeleton extends MixinAbstractSkeleton implements WhiteSkeleton {

    @Deprecated
    @Override
    public org.spongepowered.api.data.manipulator.mutable.entity.SkeletonData getSkeletonData() {
        return new SpongeSkeletonData(org.spongepowered.api.data.type.SkeletonTypes.NORMAL);
    }

    @Deprecated
    @Override
    public Value<org.spongepowered.api.data.type.SkeletonType> variant() {
        return new SpongeValue<>(Keys.SKELETON_TYPE, org.spongepowered.api.data.type.SkeletonTypes.NORMAL, org.spongepowered.api.data.type.SkeletonTypes.NORMAL);
    }


}

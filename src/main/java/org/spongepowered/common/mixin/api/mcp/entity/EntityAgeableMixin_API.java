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
package org.spongepowered.common.mixin.api.mcp.entity;

import net.minecraft.entity.AgeableEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.AgeableData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgeableData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

@Mixin(AgeableEntity.class)
public abstract class EntityAgeableMixin_API extends EntityCreatureMixin_API implements Ageable {

    @Shadow public abstract void setScaleForAge(boolean child);
    @Shadow public abstract boolean shadow$isChild();

    @Shadow public abstract int getGrowingAge();

    @Override
    public void setScaleForAge() {
        this.setScaleForAge(this.shadow$isChild());
    }

    @Override
    public AgeableData getAgeData() {
        return new SpongeAgeableData(this.getGrowingAge(), this.shadow$isChild());
    }

    @Override
    public MutableBoundedValue<Integer> age() {
        return SpongeValueFactory.boundedBuilder(Keys.AGE)
            .minimum(Constants.Entity.Ageable.CHILD)
            .maximum(Integer.MAX_VALUE)
            .defaultValue(Constants.Entity.Ageable.ADULT)
            .actualValue(this.getGrowingAge())
            .build()
            ;
    }

    @Override
    public Value<Boolean> adult() {
        return new SpongeValue<>(Keys.IS_ADULT, true,  this.shadow$isChild());
    }
}

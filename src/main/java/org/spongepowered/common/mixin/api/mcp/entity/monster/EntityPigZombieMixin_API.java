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
package org.spongepowered.common.mixin.api.mcp.entity.monster;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.AngerableData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.monster.ZombiePigman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAggressiveData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAngerableData;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Collection;
import net.minecraft.entity.monster.ZombiePigmanEntity;

@Mixin(ZombiePigmanEntity.class)
public abstract class EntityPigZombieMixin_API extends EntityZombieMixin_API implements ZombiePigman {

    @Shadow private int angerLevel;

    @Shadow public abstract boolean isAngry();

    @Override
    public AngerableData getAngerData() {
        return new SpongeAngerableData(this.angerLevel);
    }

    @Override
    public MutableBoundedValue<Integer> angerLevel() {
        return SpongeValueFactory.boundedBuilder(Keys.ANGER)
                .actualValue(this.angerLevel)
                .defaultValue(0)
                .minimum(Integer.MIN_VALUE)
                .maximum(Integer.MAX_VALUE)
                .build();
    }

    @Override
    public void spongeApi$supplyVanillaManipulators(Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(getAngerData());
        manipulators.add(new SpongeAggressiveData(isAngry()));
    }
}

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
package org.spongepowered.common.mixin.api.minecraft.world.entity;

import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.animal.TameableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.api.minecraft.world.entity.animal.AnimalMixin_API;

import java.util.Set;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin_API extends AnimalMixin_API implements TameableAnimal {

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.requireValue(Keys.IS_SITTING).asImmutable());
        values.add(this.requireValue(Keys.IS_TAMED).asImmutable());

        this.getValue(Keys.OWNER).map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}

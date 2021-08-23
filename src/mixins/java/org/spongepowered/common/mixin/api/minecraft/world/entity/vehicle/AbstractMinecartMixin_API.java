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
package org.spongepowered.common.mixin.api.minecraft.world.entity.vehicle;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.vehicle.minecart.MinecartLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.api.minecraft.world.entity.EntityMixin_API;

import java.util.Set;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin_API extends EntityMixin_API implements MinecartLike {

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.requireValue(Keys.AIRBORNE_VELOCITY_MODIFIER).asImmutable());
        values.add(this.requireValue(Keys.DERAILED_VELOCITY_MODIFIER).asImmutable());
        values.add(this.requireValue(Keys.IS_ON_RAIL).asImmutable());
        values.add(this.requireValue(Keys.MINECART_BLOCK_OFFSET).asImmutable());
        values.add(this.requireValue(Keys.POTENTIAL_MAX_SPEED).asImmutable());
        values.add(this.requireValue(Keys.SLOWS_UNOCCUPIED).asImmutable());

        this.getValue(Keys.BLOCK_STATE).map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}

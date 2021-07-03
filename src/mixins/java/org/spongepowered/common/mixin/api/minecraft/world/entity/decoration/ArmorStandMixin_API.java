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
package org.spongepowered.common.mixin.api.minecraft.world.entity.decoration;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.api.minecraft.world.entity.LivingEntityMixin_API;

import java.util.Set;

@Mixin(net.minecraft.world.entity.decoration.ArmorStand.class)
@Implements(@Interface(iface = ArmorStand.class, prefix = "armor$"))
public abstract class ArmorStandMixin_API extends LivingEntityMixin_API implements ArmorStand {

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.requireValue(Keys.BODY_ROTATIONS).asImmutable());
        values.add(this.requireValue(Keys.CHEST_ROTATION).asImmutable());
        values.add(this.requireValue(Keys.HAS_ARMS).asImmutable());
        values.add(this.requireValue(Keys.HAS_BASE_PLATE).asImmutable());
        values.add(this.requireValue(Keys.HAS_MARKER).asImmutable());
        values.add(this.requireValue(Keys.HEAD_ROTATION).asImmutable());
        values.add(this.requireValue(Keys.IS_PLACING_DISABLED).asImmutable());
        values.add(this.requireValue(Keys.IS_SMALL).asImmutable());
        values.add(this.requireValue(Keys.IS_TAKING_DISABLED).asImmutable());
        values.add(this.requireValue(Keys.LEFT_ARM_ROTATION).asImmutable());
        values.add(this.requireValue(Keys.LEFT_LEG_ROTATION).asImmutable());
        values.add(this.requireValue(Keys.RIGHT_ARM_ROTATION).asImmutable());
        values.add(this.requireValue(Keys.RIGHT_LEG_ROTATION).asImmutable());

        return values;
    }

}

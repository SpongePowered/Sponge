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
package org.spongepowered.common.event.damage;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.EntityDamageSource;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallingBlockData;

public class MinecraftFallingBlockDamageSource extends EntityDamageSource {

    @SuppressWarnings("unused") private final ImmutableFallingBlockData fallingBlockData; // Used in the mixin

    public MinecraftFallingBlockDamageSource(final String p_i1567_1_, final FallingBlockEntity damageSourceEntityIn) {
        super(p_i1567_1_, damageSourceEntityIn);
        this.fallingBlockData = ((DataHolder) damageSourceEntityIn).get(FallingBlockData.class).get().asImmutable();
    }

    public MinecraftFallingBlockDamageSource(final String type, final FallingBlockEntity damageSourceEntityIn, final ImmutableFallingBlockData data) {
        super(type, damageSourceEntityIn);
        this.fallingBlockData = checkNotNull(data);
    }

    @Override
    public FallingBlockEntity getTrueSource() {
        return (FallingBlockEntity) super.func_76346_g();
    }

}

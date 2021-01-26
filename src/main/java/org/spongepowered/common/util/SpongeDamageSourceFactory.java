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
package org.spongepowered.common.util;

import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;

public final class SpongeDamageSourceFactory implements DamageSource.Factory {

    @Override
    public DamageSource drowning() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.DROWN;
    }

    @Override
    public DamageSource dryout() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.DRY_OUT;
    }

    @Override
    public DamageSource falling() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.FALL;
    }

    @Override
    public DamageSource fireTick() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.ON_FIRE;
    }

    @Override
    public DamageSource generic() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.GENERIC;
    }

    @Override
    public DamageSource magic() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.MAGIC;
    }

    @Override
    public DamageSource starvation() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.STARVE;
    }

    @Override
    public DamageSource voidSource() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.OUT_OF_WORLD;
    }

    @Override
    public DamageSource wither() {
        return (DamageSource) net.minecraft.world.damagesource.DamageSource.WITHER;
    }
}

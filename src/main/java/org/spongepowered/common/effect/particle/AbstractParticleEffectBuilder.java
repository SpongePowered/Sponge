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
package org.spongepowered.common.effect.particle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;

@SuppressWarnings("unchecked")
public abstract class AbstractParticleEffectBuilder<T extends ParticleEffect, B extends ParticleEffect.ParticleBuilder<T, B>>
    implements ParticleEffect.ParticleBuilder<T, B> {

    protected SpongeParticleType type;

    protected Vector3d motion = Vector3d.ZERO;
    protected Vector3d offset = Vector3d.ZERO;

    protected int count = 1;
    @Override
    public B type(ParticleType particleType) {
        checkNotNull(particleType, "ParticleType");
        checkArgument(particleType instanceof SpongeParticleType, "Must use a supported implementation of ParticleType!");
        this.type = (SpongeParticleType) particleType;
        return (B) this;
    }

    @Override
    public B motion(Vector3d motion) {
        checkNotNull(motion, "The motion vector cannot be null! Use Vector3d.ZERO instead!");
        this.motion = motion;
        return (B) this;
    }

    @Override
    public B offset(Vector3d offset) {
        checkNotNull(offset, "The offset vector cannot be null! Use Vector3d.ZERO instead!");
        this.offset = offset;
        return (B) this;
    }

    @Override
    public B count(int count) throws IllegalArgumentException {
        checkArgument(count > 0, "The count has to be greater then zero!");
        this.count = count;
        return (B) this;
    }

    @Override
    public B from(T value) {
        type(value.getType());
        this.motion = value.getMotion();
        this.offset = value.getOffset();
        this.count = value.getCount();
        return (B) this;
    }

    @Override
    public B reset() {
        return (B) this;
    }
}

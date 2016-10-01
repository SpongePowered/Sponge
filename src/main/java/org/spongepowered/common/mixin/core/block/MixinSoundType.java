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
package org.spongepowered.common.mixin.core.block;

import org.spongepowered.api.block.BlockSounds;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.util.SoundEvent;

@Mixin(net.minecraft.block.SoundType.class)
public abstract class MixinSoundType implements BlockSounds {
    @Shadow private SoundEvent breakSound;
    @Shadow private SoundEvent stepSound;
    @Shadow private SoundEvent placeSound;
    @Shadow private SoundEvent hitSound;
    @Shadow private SoundEvent fallSound;
    @Shadow public abstract float getVolume();
    @Shadow public abstract float getPitch();

    @Override
    public SoundType getBreakSound() {
        return (SoundType) this.breakSound;
    }

    @Override
    public SoundType getStepSound() {
        return (SoundType) this.stepSound;
    }

    @Override
    public SoundType getPlaceSound() {
        return (SoundType) this.placeSound;
    }

    @Override
    public SoundType getHitSound() {
        return (SoundType) this.hitSound;
    }

    @Override
    public SoundType getFallSound() {
        return (SoundType) this.fallSound;
    }
}

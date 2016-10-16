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

import net.minecraft.util.SoundEvent;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.block.SoundType.class)
@Implements(@Interface(iface = BlockSoundGroup.class, prefix = "group$"))
public abstract class MixinSoundType {

    @Shadow private SoundEvent breakSound;
    @Shadow private SoundEvent hitSound;

    @Shadow public abstract float getVolume();
    @Shadow public abstract float getPitch();
    @Shadow public abstract SoundEvent getStepSound();
    @Shadow public abstract SoundEvent getPlaceSound();
    @Shadow public abstract SoundEvent getFallSound();

    @Intrinsic
    public SoundEvent getBreakSound() {
        return this.breakSound;
    }

    @Intrinsic
    public SoundEvent getHitSound() {
        return this.hitSound;
    }

    public double group$getVolume() {
        return this.getVolume();
    }

    public double group$getPitch() {
        return this.getPitch();
    }

    public SoundType group$getBreakSound() {
        return (SoundType) this.getBreakSound();
    }

    public SoundType group$getHitSound() {
        return (SoundType) this.getHitSound();
    }

    public SoundType group$getStepSound() {
        return (SoundType) this.getStepSound();
    }

    public SoundType group$getPlaceSound() {
        return (SoundType) this.getPlaceSound();
    }

    public SoundType group$getFallSound() {
        return (SoundType) this.getFallSound();
    }
}

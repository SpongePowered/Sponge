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
package org.spongepowered.common.mixin.api.minecraft.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SoundType.class)
public abstract class BlockSoundGroupMixin_API implements BlockSoundGroup {

    // @formatter:off
    @Shadow @Final public float volume;
    @Shadow @Final public float pitch;
    @Shadow @Final private SoundEvent breakSound;
    @Shadow @Final private SoundEvent stepSound;
    @Shadow @Final private SoundEvent placeSound;
    @Shadow @Final private SoundEvent hitSound;
    @Shadow @Final private SoundEvent fallSound;
    // @formatter:on

    @Override
    public double volume() {
        return this.volume;
    }

    @Override
    public double pitch() {
        return this.pitch;
    }

    @Override
    public org.spongepowered.api.effect.sound.SoundType breakSound() {
        return (org.spongepowered.api.effect.sound.SoundType) (Object) this.breakSound;
    }

    @Override
    public org.spongepowered.api.effect.sound.SoundType stepSound() {
        return (org.spongepowered.api.effect.sound.SoundType) (Object) this.stepSound;
    }

    @Override
    public org.spongepowered.api.effect.sound.SoundType placeSound() {
        return (org.spongepowered.api.effect.sound.SoundType) (Object) this.placeSound;
    }

    @Override
    public org.spongepowered.api.effect.sound.SoundType hitSound() {
        return (org.spongepowered.api.effect.sound.SoundType) (Object) this.hitSound;
    }

    @Override
    public org.spongepowered.api.effect.sound.SoundType fallSound() {
        return (org.spongepowered.api.effect.sound.SoundType) (Object) this.fallSound;
    }
}

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

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.sounds.BlockSoundSet;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(BlockSoundSet.class)
public abstract class BlockSoundGroupMixin_API implements BlockSoundGroup {

    // @formatter:off
    @Shadow @Final private float volume;
    @Shadow @Final private float pitch;
    @Shadow @Final private Optional<Holder<SoundEvent>> breakSound;
    @Shadow @Final private Optional<Holder<SoundEvent>> stepSound;
    @Shadow @Final private Optional<Holder<SoundEvent>> placeSound;
    @Shadow @Final private Optional<Holder<SoundEvent>> hitSound;
    @Shadow @Final private Optional<Holder<SoundEvent>> fallSound;
    // @formatter:on

    private static SoundType api$sound(final Optional<Holder<SoundEvent>> sound) {
        return (SoundType) (Object) sound.orElse(SoundEvents.EMPTY).value();
    }

    @Override
    public double volume() {
        return this.volume;
    }

    @Override
    public double pitch() {
        return this.pitch;
    }

    @Override
    public SoundType breakSound() {
        return BlockSoundGroupMixin_API.api$sound(this.breakSound);
    }

    @Override
    public SoundType stepSound() {
        return BlockSoundGroupMixin_API.api$sound(this.stepSound);
    }

    @Override
    public SoundType placeSound() {
        return BlockSoundGroupMixin_API.api$sound(this.placeSound);
    }

    @Override
    public SoundType hitSound() {
        return BlockSoundGroupMixin_API.api$sound(this.hitSound);
    }

    @Override
    public SoundType fallSound() {
        return BlockSoundGroupMixin_API.api$sound(this.fallSound);
    }
}

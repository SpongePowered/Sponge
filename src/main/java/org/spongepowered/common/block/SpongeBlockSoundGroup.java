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
package org.spongepowered.common.block;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.sounds.BlockSoundSet;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.effect.sound.SoundType;

import java.util.Optional;

/**
 * Wraps a {@link BlockSoundSet} rather than being mixed into it: the record's
 * accessors share their names and erased descriptors with the API methods, so
 * a mixin would replace them and break the registry codec.
 */
public record SpongeBlockSoundGroup(BlockSoundSet set) implements BlockSoundGroup {

    private static Optional<SoundType> sound(final Optional<Holder<SoundEvent>> sound) {
        return sound.map(holder -> (SoundType) (Object) holder.value());
    }

    @Override
    public double volume() {
        return this.set.volume();
    }

    @Override
    public double pitch() {
        return this.set.pitch();
    }

    @Override
    public Optional<SoundType> breakSound() {
        return SpongeBlockSoundGroup.sound(this.set.breakSound());
    }

    @Override
    public Optional<SoundType> stepSound() {
        return SpongeBlockSoundGroup.sound(this.set.stepSound());
    }

    @Override
    public Optional<SoundType> placeSound() {
        return SpongeBlockSoundGroup.sound(this.set.placeSound());
    }

    @Override
    public Optional<SoundType> hitSound() {
        return SpongeBlockSoundGroup.sound(this.set.hitSound());
    }

    @Override
    public Optional<SoundType> fallSound() {
        return SpongeBlockSoundGroup.sound(this.set.fallSound());
    }
}

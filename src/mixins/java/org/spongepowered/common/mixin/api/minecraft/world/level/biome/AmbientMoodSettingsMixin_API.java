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
package org.spongepowered.common.mixin.api.minecraft.world.level.biome;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.world.biome.ambient.SoundConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AmbientMoodSettings.class)
public abstract class AmbientMoodSettingsMixin_API implements SoundConfig.Mood {

    // @formatter:off
    @Shadow @Final private Holder<SoundEvent> soundEvent;
    @Shadow @Final private int tickDelay;
    @Shadow @Final private int blockSearchExtent;
    @Shadow @Final private double soundPositionOffset;
    // @formatter:on

    @Override
    public SoundType sound() {
        return (SoundType) (Object) this.soundEvent.value();
    }

    @Override
    public int tickDelay() {
        return this.tickDelay;
    }

    @Override
    public int searchRadius() {
        return this.blockSearchExtent;
    }

    @Override
    public double distanceModifier() {
        return this.soundPositionOffset;
    }

}

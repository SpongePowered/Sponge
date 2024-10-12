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
package org.spongepowered.common.world.biome.ambient;

import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.world.biome.ambient.SoundConfig;

public class SpongeSoundConfigFactory implements SoundConfig.Factory {

    @Override
    public SoundConfig.Mood ofAmbientMood(final SoundType sound, final int tickDelay, final int searchRadius, final double distanceModifier) {
        return (SoundConfig.Mood) new AmbientMoodSettings(Holder.direct((SoundEvent) (Object) sound), tickDelay, searchRadius, distanceModifier);
    }

    @Override
    public SoundConfig.Additional ofAdditional(final SoundType sound, final double tickChance) {
        return (SoundConfig.Additional) new AmbientAdditionsSettings(Holder.direct((SoundEvent) (Object) sound), tickChance);
    }

    @Override
    public SoundConfig.BackgroundMusic ofBackroundMusic(final SoundType sound, final int minDelay, final int maxDelay, final boolean replacesCurrent) {
        return (SoundConfig.BackgroundMusic) new Music(Holder.direct((SoundEvent) (Object) sound), minDelay, maxDelay, replacesCurrent);
    }
}

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
package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;
import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayingData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayingData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePlayingData;

import java.util.Optional;

public class PlayingDataBuilder implements DataManipulatorBuilder<PlayingData, ImmutablePlayingData> {

    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityVillager;
    }

    @Override
    public PlayingData create() {
        return new SpongePlayingData();
    }

    @Override
    public Optional<PlayingData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final boolean isPlaying = ((EntityVillager) dataHolder).isPlaying();
            return Optional.of(new SpongePlayingData(isPlaying));
        }
        return Optional.empty();
    }

    @Override
    public Optional<PlayingData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.IS_PLAYING.getQuery())) {
            PlayingData playingData = create();
            playingData.set(Keys.IS_PLAYING, getData(container, Keys.IS_PLAYING));
            return Optional.of(playingData);
        }
        return Optional.empty();
    }
}

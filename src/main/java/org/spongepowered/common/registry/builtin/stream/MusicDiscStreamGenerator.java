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
package org.spongepowered.common.registry.builtin.stream;

import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.common.effect.record.SpongeRecordType;

import java.util.stream.Stream;

public final class MusicDiscStreamGenerator {

    private MusicDiscStreamGenerator() {
    }

    public static Stream<MusicDisc> stream() {
        return Stream.of(
            new SpongeRecordType(CatalogKey.minecraft("blocks"), (MusicDiscItem) Items.MUSIC_DISC_BLOCKS),
            new SpongeRecordType(CatalogKey.minecraft("cat"), (MusicDiscItem) Items.MUSIC_DISC_CAT),
            new SpongeRecordType(CatalogKey.minecraft("chirp"), (MusicDiscItem) Items.MUSIC_DISC_CHIRP),
            new SpongeRecordType(CatalogKey.minecraft("eleven"), (MusicDiscItem) Items.MUSIC_DISC_11),
            new SpongeRecordType(CatalogKey.minecraft("far"), (MusicDiscItem) Items.MUSIC_DISC_FAR),
            new SpongeRecordType(CatalogKey.minecraft("mall"), (MusicDiscItem) Items.MUSIC_DISC_MALL),
            new SpongeRecordType(CatalogKey.minecraft("mellohi"), (MusicDiscItem) Items.MUSIC_DISC_MELLOHI),
            new SpongeRecordType(CatalogKey.minecraft("stal"), (MusicDiscItem) Items.MUSIC_DISC_STAL),
            new SpongeRecordType(CatalogKey.minecraft("strad"), (MusicDiscItem) Items.MUSIC_DISC_STRAD),
            new SpongeRecordType(CatalogKey.minecraft("thirteen"), (MusicDiscItem) Items.MUSIC_DISC_13),
            new SpongeRecordType(CatalogKey.minecraft("wait"), (MusicDiscItem) Items.MUSIC_DISC_WAIT),
            new SpongeRecordType(CatalogKey.minecraft("ward"), (MusicDiscItem) Items.MUSIC_DISC_WARD)
        );
    }
}

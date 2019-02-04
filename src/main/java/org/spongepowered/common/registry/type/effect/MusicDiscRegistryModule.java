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
package org.spongepowered.common.registry.type.effect;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.effect.sound.music.MusicDiscs;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.effect.record.SpongeMusicDisc;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Optional;

@RegisterCatalog(MusicDiscs.class)
@RegistrationDependency(SoundRegistryModule.class)
public final class MusicDiscRegistryModule extends AbstractCatalogRegistryModule<MusicDisc> implements CatalogRegistryModule<MusicDisc> {

    public static MusicDiscRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private final Int2ObjectMap<MusicDisc> byInternalId = new Int2ObjectOpenHashMap<>();

    MusicDiscRegistryModule() {
    }

    public Optional<MusicDisc> getByInternalId(int internalId) {
        return Optional.ofNullable(this.byInternalId.get(internalId));
    }

    private void add(SpongeMusicDisc recordType) {
        register(recordType);
        this.byInternalId.put(recordType.getInternalId(), recordType);
    }

    @Override
    public void registerDefaults() {
        this.add(new SpongeMusicDisc("minecraft:thirteen", "item.record.13.desc", 2256, SoundTypes.MUSIC_DISC_13));
        this.add(new SpongeMusicDisc("minecraft:cat", "item.record.cat.desc", 2257, SoundTypes.MUSIC_DISC_CAT));
        this.add(new SpongeMusicDisc("minecraft:blocks", "item.record.blocks.desc", 2258, SoundTypes.MUSIC_DISC_BLOCKS));
        this.add(new SpongeMusicDisc("minecraft:chirp", "item.record.chirp.desc", 2259, SoundTypes.MUSIC_DISC_CHIRP));
        this.add(new SpongeMusicDisc("minecraft:far", "item.record.far.desc", 2260, SoundTypes.MUSIC_DISC_FAR));
        this.add(new SpongeMusicDisc("minecraft:mall", "item.record.mall.desc", 2261, SoundTypes.MUSIC_DISC_MALL));
        this.add(new SpongeMusicDisc("minecraft:mellohi", "item.record.mellohi.desc", 2262, SoundTypes.MUSIC_DISC_MELLOHI));
        this.add(new SpongeMusicDisc("minecraft:stal", "item.record.stal.desc", 2263, SoundTypes.MUSIC_DISC_STAL));
        this.add(new SpongeMusicDisc("minecraft:strad", "item.record.strad.desc", 2264, SoundTypes.MUSIC_DISC_STRAD));
        this.add(new SpongeMusicDisc("minecraft:ward", "item.record.ward.desc", 2265, SoundTypes.MUSIC_DISC_WARD));
        this.add(new SpongeMusicDisc("minecraft:eleven", "item.record.11.desc", 2266, SoundTypes.MUSIC_DISC_11));
        this.add(new SpongeMusicDisc("minecraft:wait", "item.record.wait.desc", 2267, SoundTypes.MUSIC_DISC_WAIT));
    }

    private static final class Holder {
        final static MusicDiscRegistryModule INSTANCE = new MusicDiscRegistryModule();
    }
}

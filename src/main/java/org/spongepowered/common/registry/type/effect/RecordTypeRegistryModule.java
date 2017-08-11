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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.effect.sound.record.RecordTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.effect.record.SpongeRecordType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency(SoundRegistryModule.class)
public final class RecordTypeRegistryModule implements CatalogRegistryModule<RecordType> {

    public static RecordTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(RecordTypes.class)
    private final Map<String, RecordType> mappings = new HashMap<>();
    private final Int2ObjectMap<RecordType> byInternalId = new Int2ObjectOpenHashMap<>();

    private RecordTypeRegistryModule() {
    }

    public Optional<RecordType> getByInternalId(int internalId) {
        return Optional.ofNullable(this.byInternalId.get(internalId));
    }

    @Override
    public Optional<RecordType> getById(String id) {
        checkNotNull(id);
        if (!id.contains(":")) {
            id = "minecraft:" + id; // assume vanilla
        }
        return Optional.ofNullable(this.mappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<RecordType> getAll() {
        return ImmutableList.copyOf(this.mappings.values());
    }

    private void add(SpongeRecordType recordType) {
        final String id = recordType.getId();
        this.mappings.put(id, recordType);
        this.byInternalId.put(recordType.getInternalId(), recordType);
    }

    @Override
    public void registerDefaults() {
        this.add(new SpongeRecordType("minecraft:thirteen", "item.record.13.desc", 2256, SoundTypes.RECORD_13));
        this.add(new SpongeRecordType("minecraft:cat", "item.record.cat.desc", 2257, SoundTypes.RECORD_CAT));
        this.add(new SpongeRecordType("minecraft:blocks", "item.record.blocks.desc", 2258, SoundTypes.RECORD_BLOCKS));
        this.add(new SpongeRecordType("minecraft:chirp", "item.record.chirp.desc", 2259, SoundTypes.RECORD_CHIRP));
        this.add(new SpongeRecordType("minecraft:far", "item.record.far.desc", 2260, SoundTypes.RECORD_FAR));
        this.add(new SpongeRecordType("minecraft:mall", "item.record.mall.desc", 2261, SoundTypes.RECORD_MALL));
        this.add(new SpongeRecordType("minecraft:mellohi", "item.record.mellohi.desc", 2262, SoundTypes.RECORD_MELLOHI));
        this.add(new SpongeRecordType("minecraft:stal", "item.record.stal.desc", 2263, SoundTypes.RECORD_STAL));
        this.add(new SpongeRecordType("minecraft:strad", "item.record.strad.desc", 2264, SoundTypes.RECORD_STRAD));
        this.add(new SpongeRecordType("minecraft:ward", "item.record.ward.desc", 2265, SoundTypes.RECORD_WARD));
        this.add(new SpongeRecordType("minecraft:eleven", "item.record.11.desc", 2266, SoundTypes.RECORD_11));
        this.add(new SpongeRecordType("minecraft:wait", "item.record.wait.desc", 2267, SoundTypes.RECORD_WAIT));
    }

    private static final class Holder {
        final static RecordTypeRegistryModule INSTANCE = new RecordTypeRegistryModule();
    }
}

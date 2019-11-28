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
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.effect.sound.record.RecordTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.effect.record.SpongeRecordType;
import org.spongepowered.common.mixin.core.item.ItemRecordAccessor;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegistrationDependency({SoundRegistryModule.class, ItemTypeRegistryModule.class})
public final class RecordTypeRegistryModule implements CatalogRegistryModule<RecordType> {

    public static RecordTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(RecordTypes.class)
    private final Map<String, RecordType> mappings = new HashMap<>();

    private RecordTypeRegistryModule() {
    }

    public Optional<RecordType> getByItem(Item itemType) {
        final ResourceLocation resourceLocation = Item.field_150901_e.getKey(itemType);
        if (resourceLocation == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mappings.get(resourceLocation.toString()));
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
    }

    private static final class Holder {
        final static RecordTypeRegistryModule INSTANCE = new RecordTypeRegistryModule();
    }

    @CustomCatalogRegistration
    public void customRegistration() {
        for (Map.Entry<SoundEvent, MusicDiscItem> recordEntry : ((ItemRecordAccessor) Items.field_151093_ce).accessor$getRecords().entrySet()) {
            final MusicDiscItem recordItem = recordEntry.getValue();
            final String key = Item.field_150901_e.getKey(recordItem).toString();
            if(!mappings.containsKey(key)) {
                this.add(new SpongeRecordType(key, recordItem.getTranslationKey(), (ItemType) recordItem, (SoundType) ((ItemRecordAccessor) recordItem).accessor$getSoundEvent()));
            }
        }

        RegistryHelper.mapFields(RecordTypes.class, fieldName -> {
            final String name = fieldName.toLowerCase(Locale.ENGLISH);
            if (name.equals("thirteen")) {
                return this.mappings.get("minecraft:record_13");
            } else if (name.equals("eleven")) {
                return this.mappings.get("minecraft:record_11");
            }
            final RecordType recordType = this.mappings.get("minecraft:record_" + name);
            return recordType;
        });
    }

}

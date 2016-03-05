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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SoundCategoryRegistryModule implements AlternateCatalogRegistryModule<SoundCategory> {

    @RegisterCatalog(SoundCategories.class)
    private final Map<String, SoundCategory> soundCategoryMap = new HashMap<>();

    @Override
    public Map<String, SoundCategory> provideCatalogMap() {
        Map<String, SoundCategory> soundCategoryMap = new HashMap<>();
        for (Map.Entry<String, SoundCategory> entry : this.soundCategoryMap.entrySet()) {
            soundCategoryMap.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return soundCategoryMap;
    }

    @Override
    public Optional<SoundCategory> getById(String id) {
        return Optional.ofNullable(this.soundCategoryMap.get(Preconditions.checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<SoundCategory> getAll() {
        return ImmutableList.copyOf(this.soundCategoryMap.values());
    }

    @Override
    public void registerDefaults() {
        this.soundCategoryMap.put("minecraft:master", (SoundCategory) (Object) net.minecraft.util.SoundCategory.MASTER);
        this.soundCategoryMap.put("minecraft:music", (SoundCategory) (Object) net.minecraft.util.SoundCategory.MUSIC);
        this.soundCategoryMap.put("minecraft:record", (SoundCategory) (Object) net.minecraft.util.SoundCategory.RECORDS);
        this.soundCategoryMap.put("minecraft:weather", (SoundCategory) (Object) net.minecraft.util.SoundCategory.WEATHER);
        this.soundCategoryMap.put("minecraft:block", (SoundCategory) (Object) net.minecraft.util.SoundCategory.BLOCKS);
        this.soundCategoryMap.put("minecraft:hostile", (SoundCategory) (Object) net.minecraft.util.SoundCategory.HOSTILE);
        this.soundCategoryMap.put("minecraft:neutral", (SoundCategory) (Object) net.minecraft.util.SoundCategory.NEUTRAL);
        this.soundCategoryMap.put("minecraft:player",  (SoundCategory) (Object) net.minecraft.util.SoundCategory.PLAYERS);
        this.soundCategoryMap.put("minecraft:ambient", (SoundCategory) (Object) net.minecraft.util.SoundCategory.AMBIENT);
        this.soundCategoryMap.put("minecraft:voice", (SoundCategory) (Object) net.minecraft.util.SoundCategory.VOICE);
    }
}

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
package org.spongepowered.common.registry.type.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.type.SkinParts;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeSkinPart;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class SkinPartRegistryModule implements AlternateCatalogRegistryModule<SkinPart> {

    private static final SkinPartRegistryModule INSTANCE = new SkinPartRegistryModule();

    public static SkinPartRegistryModule getInstance() {
        return INSTANCE;
    }

    private SkinPartRegistryModule(){ }

    @RegisterCatalog(SkinParts.class)
    private final ImmutableMap<String, SkinPart> skinPartMap = ImmutableMap.<String, SkinPart>builder()
        .put("minecraft:hat", this.createSkinPart(6, "hat"))
        .put("minecraft:cape", this.createSkinPart(0, "cape"))
        .put("minecraft:jacket", this.createSkinPart(1, "jacket"))
        .put("minecraft:left_sleeve", this.createSkinPart(2, "left_sleeve"))
        .put("minecraft:right_sleeve", this.createSkinPart(3, "right_sleeve"))
        .put("minecraft:left_pants_leg", this.createSkinPart(4, "left_pants_leg"))
        .put("minecraft:right_pants_leg", this.createSkinPart(5, "right_pants_leg"))
        .build();

    private SkinPart createSkinPart(final int ordinal, final String id) {
        return new SpongeSkinPart(ordinal, id);
    }

    @Override
    public Optional<SkinPart> getById(final String id) {
        return Optional.ofNullable(this.skinPartMap.get(checkNotNull(id, "id").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<SkinPart> getAll() {
        return this.skinPartMap.values();
    }

    @Override
    public Map<String, SkinPart> provideCatalogMap() {
        final HashMap<String, SkinPart> map = new HashMap<>();
        for (final Map.Entry<String, SkinPart> entry : this.skinPartMap.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return map;
    }
}

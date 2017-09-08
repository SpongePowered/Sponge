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
package org.spongepowered.common.registry.type.text;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.selector.SelectorTypes;
import org.spongepowered.common.text.selector.SpongeSelectorType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class SelectorTypeRegistryModule implements AlternateCatalogRegistryModule<SelectorType> {

    @RegisterCatalog(SelectorTypes.class)
    private final Map<String, SelectorType> selectorMappings = Maps.newHashMap();

    @Override
    public Optional<SelectorType> getById(String id) {
        return Optional.ofNullable(this.selectorMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<SelectorType> getAll() {
        return ImmutableList.copyOf(this.selectorMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.selectorMappings.put("minecraft:all_players", new SpongeSelectorType("minecraft:all_players", "a"));
        this.selectorMappings.put("minecraft:all_entities", new SpongeSelectorType("minecraft:all_entities", "e"));
        this.selectorMappings.put("minecraft:nearest_player", new SpongeSelectorType("minecraft:nearest_player", "p"));
        this.selectorMappings.put("minecraft:source", new SpongeSelectorType("minecraft:source", "s"));
        this.selectorMappings.put("minecraft:random", new SpongeSelectorType("minecraft:random", "r"));
    }

    @Override
    public Map<String, SelectorType> provideCatalogMap() {
        final HashMap<String, SelectorType> map = new HashMap<>();
        for (Map.Entry<String, SelectorType> entry : this.selectorMappings.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return map;
    }
}

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
package org.spongepowered.common.registry.type.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.item.SpongeFireworkShape;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FireworkShapeRegistryModule implements CatalogRegistryModule<FireworkShape> {

    @RegisterCatalog(FireworkShapes.class)
    private Map<String, FireworkShape> fireworkShapeMap = new HashMap<>();

    @Override
    public Optional<FireworkShape> getById(String id) {
        id = checkNotNull(id).toLowerCase(Locale.ENGLISH);
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return Optional.ofNullable(this.fireworkShapeMap.get(id));
    }

    @Override
    public Collection<FireworkShape> getAll() {
        return ImmutableList.copyOf(this.fireworkShapeMap.values());
    }

    @Override
    public void registerDefaults() {
        this.fireworkShapeMap.putAll(FireworkUtils.shapeMapping.values().stream()
                .collect(Collectors.toMap(SpongeFireworkShape::getId, Function.identity())));
    }
}

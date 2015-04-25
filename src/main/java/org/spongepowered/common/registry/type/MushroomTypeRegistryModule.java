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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.gen.type.MushroomTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;
import org.spongepowered.common.world.gen.type.SpongeMushroomType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class MushroomTypeRegistryModule implements CatalogRegistryModule<MushroomType> {

    @RegisterCatalog(MushroomTypes.class)
    private final Map<String, MushroomType> mushroomTypeMap = ImmutableMap.<String, MushroomType>builder()
        .put("brown", new SpongeMushroomType("brown", (PopulatorObject) new WorldGenBigMushroom(0)))
        .put("red", new SpongeMushroomType("red", (PopulatorObject) new WorldGenBigMushroom(1)))
        .build();

    @Override
    public Optional<MushroomType> getById(String id) {
        return Optional.ofNullable(this.mushroomTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<MushroomType> getAll() {
        return ImmutableList.copyOf(this.mushroomTypeMap.values());
    }
}

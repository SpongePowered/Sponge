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
package org.spongepowered.common.registry.type.block;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;
import org.spongepowered.common.rotation.SpongeRotation;

import java.util.Collection;
import java.util.Optional;

public final class RotationRegistryModule implements CatalogRegistryModule<Rotation> {

    @RegisterCatalog(Rotations.class)
    public static final BiMap<String, Rotation> rotationMap = ImmutableBiMap.<String, Rotation>builder()
        .put("top", new SpongeRotation(0, "Top"))
        .put("top_right", new SpongeRotation(45, "Top Right"))
        .put("right", new SpongeRotation(90, "Right"))
        .put("bottom_right", new SpongeRotation(135, "Bottom Right"))
        .put("bottom", new SpongeRotation(180, "Bottom"))
        .put("bottom_left", new SpongeRotation(225, "Bottom Left"))
        .put("left", new SpongeRotation(270, "Left"))
        .put("top_left", new SpongeRotation(315, "Top Left"))
        .build();

    @Override
    public Optional<Rotation> getById(String id) {
        return Optional.ofNullable(rotationMap.get(id.toLowerCase()));
    }

    @Override
    public Collection<Rotation> getAll() {
        return ImmutableList.copyOf(rotationMap.values());
    }

}

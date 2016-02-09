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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.common.registry.type.ImmutableCatalogRegistryModule;
import org.spongepowered.common.rotation.SpongeRotation;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class RotationRegistryModule extends ImmutableCatalogRegistryModule<Rotation> {

    public static RotationRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<Integer, Rotation> rotation;

    private RotationRegistryModule() {
        this.rotation = getAll().stream()
                .map((rotation) -> Maps.immutableEntry(rotation.getAngle(), rotation)).<ImmutableMap
                .Builder<Integer, Rotation>>collect(ImmutableMap::builder, ImmutableMap.Builder::put, (left, right) -> left.putAll(right.build()))
                .build();
    }

    @Override
    protected void collect(BiConsumer<String, Rotation> consumer) {
        add(consumer, new SpongeRotation(0, "top", "Top"));
        add(consumer, new SpongeRotation(45, "top_right", "Top Right"));
        add(consumer, new SpongeRotation(90, "right", "Right"));
        add(consumer, new SpongeRotation(135, "bottom_right", "Bottom Right"));
        add(consumer, new SpongeRotation(180, "bottom", "Bottom"));
        add(consumer, new SpongeRotation(225, "bottom_left", "Bottom Left"));
        add(consumer, new SpongeRotation(270, "left", "Left"));
        add(consumer, new SpongeRotation(315, "top_left", "Top Left"));
    };

    public Optional<Rotation> getByAngle(int degrees) {
        return Optional.ofNullable(this.rotation.get((degrees % 360 + 360) % 360));
    }

    private static final class Holder {

        private static final RotationRegistryModule INSTANCE = new RotationRegistryModule();

    }

}

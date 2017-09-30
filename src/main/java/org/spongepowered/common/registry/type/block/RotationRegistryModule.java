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

import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;
import org.spongepowered.common.rotation.SpongeRotation;

import java.util.Map;
import java.util.Optional;

@RegisterCatalog(Rotations.class)
public final class RotationRegistryModule
        extends AbstractPrefixAlternateCatalogTypeRegistryModule<Rotation>
        implements CatalogRegistryModule<Rotation> {

    public static RotationRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        register(new SpongeRotation(0, "minecraft:top", "Top"));
        register(new SpongeRotation(45, "minecraft:top_right", "Top Right"));
        register(new SpongeRotation(90, "minecraft:right", "Right"));
        register(new SpongeRotation(135, "minecraft:bottom_right", "Bottom Right"));
        register(new SpongeRotation(180, "minecraft:bottom", "Bottom"));
        register(new SpongeRotation(225, "minecraft:bottom_left", "Bottom Left"));
        register(new SpongeRotation(270, "minecraft:left", "Left"));
        register(new SpongeRotation(315, "minecraft:top_left", "Top Left"));
    }

    RotationRegistryModule() {
        super("minecraft");
    }

    public Optional<Rotation> getRotationFromDegree(int degrees) {
        for (Map.Entry<String, Rotation> entry : this.catalogTypeMap.entrySet()) {
            if (entry.getValue().getAngle() == degrees) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    private static final class Holder {
        static final RotationRegistryModule INSTANCE = new RotationRegistryModule();
    }
}

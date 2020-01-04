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
package org.spongepowered.common.registry.builtin;

import net.minecraft.entity.item.PaintingType;
import org.spongepowered.api.data.type.ArtType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class PaintingTypeSupplier {

    private PaintingTypeSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(ArtType.class, "kebab", () -> (ArtType) PaintingType.KEBAB)
            .registerSupplier(ArtType.class, "aztec", () -> (ArtType) PaintingType.AZTEC)
            .registerSupplier(ArtType.class, "alban", () -> (ArtType) PaintingType.ALBAN)
            .registerSupplier(ArtType.class, "aztec2", () -> (ArtType) PaintingType.AZTEC2)
            .registerSupplier(ArtType.class, "bomb", () -> (ArtType) PaintingType.BOMB)
            .registerSupplier(ArtType.class, "plant", () -> (ArtType) PaintingType.PLANT)
            .registerSupplier(ArtType.class, "wasteland", () -> (ArtType) PaintingType.WASTELAND)
            .registerSupplier(ArtType.class, "pool", () -> (ArtType) PaintingType.POOL)
            .registerSupplier(ArtType.class, "courbet", () -> (ArtType) PaintingType.COURBET)
            .registerSupplier(ArtType.class, "sea", () -> (ArtType) PaintingType.SEA)
            .registerSupplier(ArtType.class, "sunset", () -> (ArtType) PaintingType.SUNSET)
            .registerSupplier(ArtType.class, "creebet", () -> (ArtType) PaintingType.CREEBET)
            .registerSupplier(ArtType.class, "wanderer", () -> (ArtType) PaintingType.WANDERER)
            .registerSupplier(ArtType.class, "graham", () -> (ArtType) PaintingType.GRAHAM)
            .registerSupplier(ArtType.class, "match", () -> (ArtType) PaintingType.MATCH)
            .registerSupplier(ArtType.class, "bust", () -> (ArtType) PaintingType.BUST)
            .registerSupplier(ArtType.class, "stage", () -> (ArtType) PaintingType.STAGE)
            .registerSupplier(ArtType.class, "void", () -> (ArtType) PaintingType.VOID)
            .registerSupplier(ArtType.class, "skull_and_roses", () -> (ArtType) PaintingType.SKULL_AND_ROSES)
            .registerSupplier(ArtType.class, "wither", () -> (ArtType) PaintingType.WITHER)
            .registerSupplier(ArtType.class, "fighters", () -> (ArtType) PaintingType.FIGHTERS)
            .registerSupplier(ArtType.class, "pointer", () -> (ArtType) PaintingType.POINTER)
            .registerSupplier(ArtType.class, "pigscene", () -> (ArtType) PaintingType.PIGSCENE)
            .registerSupplier(ArtType.class, "burning_skull", () -> (ArtType) PaintingType.BURNING_SKULL)
            .registerSupplier(ArtType.class, "skeleton", () -> (ArtType) PaintingType.SKELETON)
            .registerSupplier(ArtType.class, "donkey_kong", () -> (ArtType) PaintingType.DONKEY_KONG)
        ;
    }
}

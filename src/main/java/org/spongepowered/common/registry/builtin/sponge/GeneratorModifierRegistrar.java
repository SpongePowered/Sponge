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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.world.WorldType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.gen.GeneratorModifierType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

import java.util.stream.Stream;

public final class GeneratorModifierRegistrar {

    private GeneratorModifierRegistrar() {
    }

    public static void registerRegistry(final SpongeCatalogRegistry registry) {
        registry.generateRegistry(GeneratorModifierType.class, ResourceKey.minecraft("generator_modifier"), Stream.empty(), false, false);
    }

    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry
                .registerCatalogAndSupplier(GeneratorModifierType.class, "amplified", () -> (GeneratorModifierType) WorldType.AMPLIFIED)
                .registerCatalogAndSupplier(GeneratorModifierType.class, "buffet", () -> (GeneratorModifierType) WorldType.BUFFET)
                .registerCatalogAndSupplier(GeneratorModifierType.class, "customized", () -> (GeneratorModifierType) WorldType.CUSTOMIZED)
                .registerCatalogAndSupplier(GeneratorModifierType.class, "debug", () -> (GeneratorModifierType) WorldType.DEBUG_ALL_BLOCK_STATES)
                .registerCatalogAndSupplier(GeneratorModifierType.class, "default_1_1", () -> (GeneratorModifierType) WorldType.DEFAULT_1_1)
                .registerCatalogAndSupplier(GeneratorModifierType.class, "none", () -> (GeneratorModifierType) WorldType.DEFAULT)
                .registerCatalogAndSupplier(GeneratorModifierType.class, "flat", () -> (GeneratorModifierType) WorldType.FLAT)
                .registerCatalogAndSupplier(GeneratorModifierType.class, "large_biomes", () -> (GeneratorModifierType) WorldType.LARGE_BIOMES)
        ;
    }
}

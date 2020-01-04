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
package org.spongepowered.common.registry.builtin.registry;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.common.data.type.SpongeWoodType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Arrays;
import java.util.HashSet;

public final class WoodTypeRegistry {

    private WoodTypeRegistry() {
    }

    public static void generateRegistry(SpongeCatalogRegistry registry) {

        // TODO 1.14 - Tag system is fucking stupid Mojang, why can we not know if something is made of Acacia Wood???????
        // TODO 1.14 - May need to remove tags altogether

        registry
            .registerRegistry(WoodType.class, CatalogKey.minecraft("wood_type"), () -> new HashSet<>(Arrays.asList(
                new SpongeWoodType(CatalogKey.minecraft("acacia"), new SpongeTranslation("block.minecraft.acacia_wood"), null),
                new SpongeWoodType(CatalogKey.minecraft("birch"), new SpongeTranslation("block.minecraft.birch_wood"), null),
                new SpongeWoodType(CatalogKey.minecraft("dark_oak"), new SpongeTranslation("block.minecraft.dark_oak_wood"), null),
                new SpongeWoodType(CatalogKey.minecraft("jungle"), new SpongeTranslation("block.minecraft.jungle_wood"), null),
                new SpongeWoodType(CatalogKey.minecraft("oak"), new SpongeTranslation("block.minecraft.oak_wood"), null),
                new SpongeWoodType(CatalogKey.minecraft("spruce"), new SpongeTranslation("block.minecraft.spruce_wood"), null)))
            , true);
    }
}

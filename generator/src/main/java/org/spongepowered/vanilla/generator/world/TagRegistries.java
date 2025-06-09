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
package org.spongepowered.vanilla.generator.world;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.minecraft.core.registries.Registries;
import org.spongepowered.vanilla.generator.Context;
import org.spongepowered.vanilla.generator.Generator;
import org.spongepowered.vanilla.generator.TagGenerator;

import java.util.List;

public class TagRegistries {

    public static List<Generator> tagRegistries(final Context context) {
        return List.<Generator>of(
            new TagGenerator(
                "BLOCK_TYPE",
                Registries.BLOCK,
                context.relativeClass("block", "BlockType"),
                "tag",
                "BlockTypeTags"
            ),
            new TagGenerator(
                "BIOME",
                Registries.BIOME,
                context.relativeClass("world.biome", "Biome"),
                "tag",
                "BiomeTags"
            ),
            new TagGenerator(
                "ITEM_TYPE",
                Registries.ITEM,
                context.relativeClass("item", "ItemType"),
                "tag",
                "ItemTypeTags"
            ),
            new TagGenerator(
                "ENTITY_TYPE",
                Registries.ENTITY_TYPE,
                ParameterizedTypeName.get(context.relativeClass("entity", "EntityType"), WildcardTypeName.subtypeOf(Object.class)),
                "tag",
                "EntityTypeTags"
            ),
            new TagGenerator(
                "FLUID_TYPE",
                Registries.FLUID,
                context.relativeClass("fluid", "FluidType"),
                "tag",
                "FluidTypeTags"
            ),
            new TagGenerator(
                "ENCHANTMENT_TYPE",
                Registries.ENCHANTMENT,
                context.relativeClass("item.enchantment", "EnchantmentType"),
                "tag",
                "EnchantmentTypeTags"
            ),

            new TagGenerator(
                "DAMAGE_TYPE",
                Registries.DAMAGE_TYPE,
                context.relativeClass("event.cause.entity.damage", "DamageType"),
                "tag",
                "DamageTypeTags"
            )
        );
    }
}

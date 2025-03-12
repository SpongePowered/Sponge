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
package org.spongepowered.vanilla.generator.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import org.spongepowered.vanilla.generator.ClassFieldsValidator;
import org.spongepowered.vanilla.generator.Context;
import org.spongepowered.vanilla.generator.EnumEntriesValidator;
import org.spongepowered.vanilla.generator.Generator;
import org.spongepowered.vanilla.generator.RegistryEntriesGenerator;
import org.spongepowered.vanilla.generator.RegistryEntriesValidator;
import org.spongepowered.vanilla.generator.RegistryScope;

import java.util.List;
import java.util.Locale;

public class ItemRegistries {

    public static List<Generator> itemRegistries(final Context context) {
        return List.<Generator>of(
            new EnumEntriesValidator<>(
                "data.type",
                "DyeColors",
                DyeColor.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "item",
                "ItemRarities",
                Rarity.class,
                "getSerializedName",
                "sponge"
            ),
            new RegistryEntriesValidator<>(
                "item.enchantment",
                "EnchantmentTypes",
                Registries.ENCHANTMENT
            ),
            new RegistryEntriesGenerator<>(
                "item",
                "ItemTypes",
                "ITEM_TYPE",
                context.relativeClass("item", "ItemType"),
                Registries.ITEM
            ),
            new RegistryEntriesGenerator<>(
                "item.potion",
                "PotionTypes",
                "POTION_TYPE",
                context.relativeClass("item.potion", "PotionType"),
                Registries.POTION
            ),
            new RegistryEntriesGenerator<>(
                "effect.sound.music",
                "MusicDiscs",
                "MUSIC_DISC",
                context.relativeClass("effect.sound.music", "MusicDisc"),
                Registries.JUKEBOX_SONG,
                $ -> true,
                RegistryScope.SERVER
            ),
            new RegistryEntriesValidator<>(
                "item.recipe",
                "RecipeTypes",
                Registries.RECIPE_TYPE
            ),
            new RegistryEntriesGenerator<>(
                "item.recipe.smithing",
                "TrimMaterials",
                "TRIM_MATERIAL",
                context.relativeClass("item.recipe.smithing", "TrimMaterial"),
                Registries.TRIM_MATERIAL,
                $ ->true,
                RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "item.recipe.smithing",
                "TrimPatterns",
                "TRIM_PATTERN",
                context.relativeClass("item.recipe.smithing", "TrimPattern"),
                Registries.TRIM_PATTERN,
                $ -> true,
                RegistryScope.SERVER
            ),
            new EnumEntriesValidator<>(
                "entity.display",
                "ItemDisplayTypes",
                ItemDisplayContext.class,
                "getSerializedName",
                "sponge"
            ),
            new ClassFieldsValidator<>(
                "data.type",
                "ItemTiers",
                ToolMaterial.class
            ),
            new ClassFieldsValidator<>(
                "data.type",
                "ArmorMaterials",
                ArmorMaterials.class,
                (s) -> ResourceLocation.withDefaultNamespace(s.toLowerCase(Locale.ROOT).replace("_scute", ""))
            )
        );
    }
}

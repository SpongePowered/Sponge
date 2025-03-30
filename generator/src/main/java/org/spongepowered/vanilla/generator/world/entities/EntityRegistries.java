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
package org.spongepowered.vanilla.generator.world.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import org.spongepowered.vanilla.generator.Context;
import org.spongepowered.vanilla.generator.EnumEntriesValidator;
import org.spongepowered.vanilla.generator.Generator;
import org.spongepowered.vanilla.generator.RegistryEntriesGenerator;
import org.spongepowered.vanilla.generator.RegistryEntriesValidator;
import org.spongepowered.vanilla.generator.RegistryScope;

import java.util.List;
import java.util.Set;

public class EntityRegistries {
    public static List<Generator> enumEntries(final Context context) {
        return List.<Generator>of(
            new EnumEntriesValidator<>(
                "entity",
                "EntityCategories",
                MobCategory.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "PandaGenes",
                Panda.Gene.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "SpellTypes",
                SpellcasterIllager.IllagerSpell.class,
                "name",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "RabbitTypes",
                Rabbit.Variant.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "ParrotTypes",
                Parrot.Variant.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "MooshroomTypes",
                MushroomCow.Variant.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "FoxTypes",
                Fox.Variant.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "LlamaTypes",
                Llama.Variant.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "HorseColors",
                Variant.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "HorseStyles",
                Markings.class,
                "name",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "TropicalFishShapes",
                TropicalFish.Pattern.class,
                "getSerializedName",
                "sponge"
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "CatTypes",
                "CAT_TYPE",
                context.relativeClass("data.type", "CatType"),
                Registries.CAT_VARIANT
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "FrogTypes",
                "FROG_TYPE",
                context.relativeClass("data.type", "FrogType"),
                Registries.FROG_VARIANT
            ),
            new EnumEntriesValidator<>(
                "entity.display",
                "BillboardTypes",
                Display.BillboardConstraints.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "entity.display",
                "TextAlignments",
                Display.TextDisplay.Align.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "item.inventory.equipment",
                "EquipmentGroups",
                EquipmentSlot.Type.class,
                "name",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "item.inventory.equipment",
                "EquipmentTypes",
                EquipmentSlot.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "SalmonSizes",
                Salmon.Variant.class,
                "getSerializedName",
                "sponge"
            ),
            new EnumEntriesValidator<>(
                "data.type",
                "AxolotlVariants",
                Axolotl.Variant.class,
                "getSerializedName",
                "sponge"
            )
        );
    }

    public static List<Generator> registryEntries(final Context context) {
        return List.<Generator>of(
            new RegistryEntriesValidator<>(
                "entity",
                "EntityTypes",
                Registries.ENTITY_TYPE,
                $ -> true,
                Set.of(ResourceLocation.fromNamespaceAndPath("sponge", "human")) // Sponge's Human type is an extra addition
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "ArtTypes",
                "ART_TYPE",
                context.relativeClass("data.type", "ArtType"),
                Registries.PAINTING_VARIANT,
                a -> true, RegistryScope.SERVER
            ),
            new RegistryEntriesGenerator<>(
                "entity.attribute.type",
                "AttributeTypes",
                "ATTRIBUTE_TYPE",
                context.relativeClass("entity.attribute.type", "RangedAttributeType"),
                Registries.ATTRIBUTE,
                a -> true, null,
                context.relativeClass("entity.attribute.type", "AttributeType")
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "VillagerTypes",
                "VILLAGER_TYPE",
                context.relativeClass("data.type", "VillagerType"),
                Registries.VILLAGER_TYPE
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "ProfessionTypes",
                "PROFESSION_TYPE",
                context.relativeClass("data.type", "ProfessionType"),
                Registries.VILLAGER_PROFESSION
            ),
            new RegistryEntriesGenerator<>(
                "data.type",
                "WolfVariants",
                "WOLF_VARIANT",
                context.relativeClass("data.type", "WolfVariant"),
                Registries.WOLF_VARIANT
            )
        );
    }
}

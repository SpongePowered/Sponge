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
package org.spongepowered.common.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ArmorMaterials;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.ItemTiers;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.api.effect.sound.music.MusicDiscs;
import org.spongepowered.api.fluid.FluidStackSnapshot;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.api.item.ItemRarities;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.potion.PotionTypes;
import org.spongepowered.api.item.recipe.smithing.ArmorTrim;
import org.spongepowered.api.item.recipe.smithing.TrimMaterials;
import org.spongepowered.api.item.recipe.smithing.TrimPatterns;
import org.spongepowered.api.util.Color;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ItemStackDataTest {

    @Test
    public void testEnchantments() {
        final ItemStack goldenApple = ItemStack.of(ItemTypes.ENCHANTED_GOLDEN_APPLE);
        DataTest.checkOfferData(goldenApple, Keys.APPLIED_ENCHANTMENTS, List.of(Enchantment.of(EnchantmentTypes.SHARPNESS, 5)));
        DataTest.checkOfferData(goldenApple, Keys.APPLIED_ENCHANTMENTS, List.of(Enchantment.of(EnchantmentTypes.PROTECTION, 4)));
    }

    @Test
    public void testFood() {
        final ItemStack goldenApple = ItemStack.of(ItemTypes.ENCHANTED_GOLDEN_APPLE);
        DataTest.checkGetData(goldenApple, Keys.REPLENISHED_FOOD, 4);
        DataTest.checkGetData(goldenApple, Keys.REPLENISHED_SATURATION, (double) 9.6f);
    }

    @Test
    public void testArmor() {
        DataTest.checkGetData(ItemStack.of(ItemTypes.DIAMOND_LEGGINGS), Keys.ARMOR_MATERIAL, ArmorMaterials.DIAMOND.get());
        DataTest.checkGetData(ItemStack.of(ItemTypes.LEATHER_BOOTS), Keys.ARMOR_MATERIAL, ArmorMaterials.LEATHER.get());
        DataTest.checkGetData(ItemStack.of(ItemTypes.TURTLE_HELMET), Keys.ARMOR_MATERIAL, ArmorMaterials.TURTLE_SCUTE.get());

        DataTest.checkGetData(ItemStack.of(ItemTypes.LEATHER_BOOTS), Keys.DAMAGE_ABSORPTION, 1.0);
        DataTest.checkGetData(ItemStack.of(ItemTypes.DIAMOND_CHESTPLATE), Keys.DAMAGE_ABSORPTION, 8.0);
    }

    @Test
    public void testWrittenBook() {
        final ItemStack writtenBook = ItemStack.of(ItemTypes.WRITTEN_BOOK);
        DataTest.checkOfferData(writtenBook, Keys.AUTHOR, Component.text("You"));
        DataTest.checkGetData(writtenBook, Keys.GENERATION, 0);
        DataTest.checkOfferData(writtenBook, Keys.GENERATION, 2);
        DataTest.checkOfferData(writtenBook, Keys.PAGES, Arrays.asList(Component.text("Page 1"), Component.text("Page 2")));
    }

    @Test
    public void testWritableBook() {
        final ItemStack writableBook = ItemStack.of(ItemTypes.WRITABLE_BOOK);
        DataTest.checkOfferData(writableBook, Keys.PLAIN_PAGES, Arrays.asList("Page 1", "Page 2"));
    }

    @Disabled
    @Test
    public void testAxe() {
        final ItemStack axe = ItemStack.of(ItemTypes.WOODEN_AXE);
        DataTest.checkGetData(axe, Keys.BREAKABLE_BLOCK_TYPES, null);
        DataTest.checkOfferData(axe, Keys.BREAKABLE_BLOCK_TYPES, new HashSet<>(Arrays.asList(BlockTypes.COCOA.get(), BlockTypes.JUNGLE_LEAVES.get())));

        DataTest.checkGetData(axe, Keys.CUSTOM_NAME, null);
        DataTest.checkOfferData(axe, Keys.CUSTOM_NAME, Component.text("Jungle Axe"));

        DataTest.checkGetData(axe, Keys.DISPLAY_NAME,
            Component.translatable("chat.square_brackets").args(Component.empty().append(Component.text("Jungle Axe")).decorate(TextDecoration.ITALIC))
                .color(NamedTextColor.WHITE).hoverEvent(axe.asImmutable().asHoverEvent()));

        DataTest.checkOfferData(axe, Keys.HIDE_ATTRIBUTES, true);
        DataTest.checkOfferData(axe, Keys.HIDE_CAN_DESTROY, true);
        DataTest.checkOfferData(axe, Keys.HIDE_ENCHANTMENTS, true);
        DataTest.checkOfferData(axe, Keys.HIDE_UNBREAKABLE, true);

        DataTest.checkOfferData(axe, Keys.IS_UNBREAKABLE, true);
        DataTest.checkOfferData(axe, Keys.ITEM_DURABILITY, 5);

        DataTest.checkOfferData(axe, Keys.LOCK_TOKEN, "Key");
        DataTest.checkOfferData(axe, Keys.LORE, Arrays.asList(Component.text("1"), Component.text("2")));

        DataTest.checkGetData(axe, Keys.MAX_DURABILITY, 59);
    }

    @Test
    public void testBurnTime() {
        DataTest.checkGetData(ItemStack.of(ItemTypes.WOODEN_AXE), Keys.BURN_TIME, 200);
        DataTest.checkGetData(ItemStack.of(ItemTypes.COAL), Keys.BURN_TIME, 1600);
    }

    @Test
    public void testColor() {
        DataTest.checkOfferData(ItemStack.of(ItemTypes.LEATHER_BOOTS), Keys.COLOR, Color.BLACK);
        DataTest.checkOfferData(ItemStack.of(ItemTypes.POTION), Keys.COLOR, Color.WHITE);
        DataTest.checkOfferData(ItemStack.of(ItemTypes.SPLASH_POTION), Keys.COLOR, Color.RED);
    }

    @Disabled
    @Test
    public void testWaterBucket() {
        final ItemStack waterBucket = ItemStack.of(ItemTypes.WATER_BUCKET);
        DataTest.checkGetData(waterBucket, Keys.CONTAINER_ITEM, ItemTypes.BUCKET.get());
        DataTest.checkGetData(waterBucket, Keys.FLUID_ITEM_STACK, FluidStackSnapshot.builder().fluid(FluidTypes.WATER).build());
    }

    @Test
    public void testDyeColor() {
        DataTest.checkGetData(ItemStack.of(ItemTypes.RED_BANNER), Keys.DYE_COLOR, DyeColors.RED.get());
        DataTest.checkGetData(ItemStack.of(ItemTypes.RED_WOOL), Keys.DYE_COLOR, DyeColors.RED.get());
    }

    @Disabled
    @Test
    public void testEfficiency() {
        DataTest.checkGetData(ItemStack.of(ItemTypes.WOODEN_AXE), Keys.EFFICIENCY, 2.0);
        DataTest.checkGetData(ItemStack.of(ItemTypes.DIAMOND_SHOVEL), Keys.EFFICIENCY, 8.0);
    }

    @Test
    public void testRarity() {
        DataTest.checkGetData(ItemStack.of(ItemTypes.WOODEN_AXE), Keys.ITEM_RARITY, ItemRarities.COMMON.get());
        DataTest.checkGetData(ItemStack.of(ItemTypes.PLAYER_HEAD), Keys.ITEM_RARITY, ItemRarities.UNCOMMON.get());
    }

    @Disabled
    @Test
    public void testMusicDisc() {
        final ItemStack musicDisc = ItemStack.of(ItemTypes.MUSIC_DISC_11);
        DataTest.checkGetData(musicDisc, Keys.MUSIC_DISC, MusicDiscs.ELEVEN.get());
        DataTest.checkOfferData(musicDisc, Keys.NOTE_PITCH, NotePitches.E1.get());
    }

    @Test
    public void testPlaceableBlockTypes() {
        DataTest.checkOfferData(ItemStack.of(ItemTypes.STONE), Keys.PLACEABLE_BLOCK_TYPES, Set.of(BlockTypes.OBSIDIAN.get()));
    }

    @Test
    public void testPotionType() {
        DataTest.checkOfferData(ItemStack.of(ItemTypes.POTION), Keys.POTION_TYPE, PotionTypes.AWKWARD.get());
        DataTest.checkOfferData(ItemStack.of(ItemTypes.SPLASH_POTION), Keys.POTION_TYPE, PotionTypes.MUNDANE.get());
    }

    @Disabled
    @Test
    public void testEnchantedBook() {
        final ItemStack enchantedBook = ItemStack.of(ItemTypes.ENCHANTED_BOOK);
        DataTest.checkOfferData(enchantedBook, Keys.STORED_ENCHANTMENTS, List.of(Enchantment.of(EnchantmentTypes.SHARPNESS, 5), Enchantment.of(EnchantmentTypes.PROTECTION, 4)));
    }

    @Disabled
    @Test
    public void testToolType() {
        DataTest.checkGetData(ItemStack.of(ItemTypes.WOODEN_AXE), Keys.TOOL_TYPE, ItemTiers.WOOD.get());
        DataTest.checkGetData(ItemStack.of(ItemTypes.DIAMOND_PICKAXE), Keys.TOOL_TYPE, ItemTiers.DIAMOND.get());
    }

    @Test
    public void testArmorTrim() {
        final ItemStack netheriteChestplate = ItemStack.builder()
            .itemType(ItemTypes.NETHERITE_CHESTPLATE)
            .add(Keys.ARMOR_TRIM, ArmorTrim.of(TrimMaterials.DIAMOND.get(), TrimPatterns.SPIRE.get()))
            .build();
        DataTest.checkGetData(netheriteChestplate, Keys.ARMOR_TRIM, ArmorTrim.of(TrimMaterials.DIAMOND.get(), TrimPatterns.SPIRE.get()));
        DataTest.checkOfferData(netheriteChestplate, Keys.ARMOR_TRIM, ArmorTrim.of(TrimMaterials.EMERALD.get(), TrimPatterns.WAYFINDER.get()));
    }
}

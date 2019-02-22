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
package org.spongepowered.test;

import static org.spongepowered.api.item.ItemTypes.BED;
import static org.spongepowered.api.item.ItemTypes.BEDROCK;
import static org.spongepowered.api.item.ItemTypes.COAL;
import static org.spongepowered.api.item.ItemTypes.IRON_BLOCK;
import static org.spongepowered.api.item.ItemTypes.IRON_INGOT;
import static org.spongepowered.api.item.ItemTypes.STONE;
import static org.spongepowered.api.item.ItemTypes.WOOL;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingResult;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

/**
 * Adds BedRock. Literally.
 *
 * TODO There is a Forge client bug with recipes where a Forge client cannot connect to a Sponge-based server that only
 *      adds a new recipe via a plugin (or any server where you simply add a new recipe). In the interest of not breaking testing altogether,
 *      disabling this plugin for now.
 */
// @Plugin(id = "recipe_test", name = "Recipe Test", description = "A plugin to test recipes", version = "0.0.0")
public class RecipeTest {

    private final Logger logger;

    @Inject
    public RecipeTest(Logger logger) {
        this.logger = logger;
    }

    @SuppressWarnings(value = "deprecation")
    @Listener
    public void onInit(GameInitializationEvent event) {
        final Ingredient s = Ingredient.of(IRON_INGOT);
        final Ingredient b = Ingredient.of(IRON_BLOCK);
        final ItemStack item = ItemStack.of(BEDROCK, 1);
        final ShapedCraftingRecipe recipe = CraftingRecipe.shapedBuilder().rows()
                .row(s, s, s)
                .row(s, b, s)
                .row(s, s, s)
                .result(item)
                .id("iron_to_bedrock")
                .build();

        Sponge.getGame().getRegistry().getCraftingRecipeRegistry().register(recipe);
        this.logger.info("Registering custom crafting recipes using the deprecated method!");
    }

    @Listener
    public void onRegisterCraftingRecipes(GameRegistryEvent.Register<CraftingRecipe> event) {
        final Ingredient s = Ingredient.of(STONE);
        final Ingredient b = Ingredient.of(BED, WOOL);
        final ItemStack item = ItemStack.of(BEDROCK, 1);
        final DataTransactionResult trans = item.offer(Keys.ITEM_ENCHANTMENTS, Collections.singletonList(Enchantment.of(EnchantmentTypes.UNBREAKING, 1)));
        if (trans.getType() != DataTransactionResult.Type.SUCCESS) {
            this.logger.error("Could not build recipe output!");
        }
        final ShapedCraftingRecipe recipe = CraftingRecipe.shapedBuilder().rows()
                .row(s, s, s)
                .row(s, b, s)
                .row(s, s, s)
                .result(item)
                .id("bedrock")
                .build();
        event.register(recipe);

        final ShapedCraftingRecipe arrowRecipe = (ShapedCraftingRecipe) event.getRegistryModule().getById("minecraft:arrow").get();
        event.register(new ArrowRecipe(arrowRecipe, 5));
        this.logger.info("Registering custom crafting recipes!");
    }

    @Listener
    public void onRegisterSmeltingRecipes(GameRegistryEvent.Register<SmeltingRecipe> event) {
        final ItemStack in = ItemStack.of(COAL, 1);
        final ItemStack out = ItemStack.of(COAL, 1);
        out.offer(Keys.DISPLAY_NAME, Text.of("Hot Coal"));

        event.register(SmeltingRecipe.builder()
                .ingredient(in)
                .result(out)
                .id("hot_coal")
                .experience(5)
                .build());
        this.logger.info("Registering custom smelting recipes!");

        final SmeltIronRecipe smeltIronRecipe = new SmeltIronRecipe();
        event.register(smeltIronRecipe);
        this.logger.info("SmeltIronRecipe ID: " + smeltIronRecipe.getId());

        final SmeltGoldRecipe smeltGoldRecipe = new SmeltGoldRecipe();
        event.register(smeltGoldRecipe);
        this.logger.info("SmeltGoldRecipe ID: " + smeltGoldRecipe.getId());

        this.logger.info("## Smelting recipes:");
        event.getRegistryModule().getAll().forEach(recipe -> this.logger.info(" - " + recipe.getId()));
    }

    @Listener
    public void onCraftPreview(CraftItemEvent.Preview event) {
        if (event.getRecipe().isPresent()) {
            if (event.getRecipe().get().getExemplaryResult().getType() == BEDROCK) {
                ItemStackSnapshot item = event.getPreview().getFinal();
                List<Text> lore = Arrays.asList(Text.of("Uncraftable"));
                item = item.with(Keys.ITEM_LORE, lore).get();
                event.getPreview().setCustom(item);
            }
        }
    }

    @Listener
    public void onCraft(CraftItemEvent.Craft event, @First Player player) {
        if (event.getRecipe().isPresent()) {
            if (event.getRecipe().get().getExemplaryResult().getType() == BEDROCK) {
                player.sendMessage(Text.of("You tried to craft Bedrock!"));
                event.setCancelled(true);
            }
        }
    }

    public static class ArrowRecipe implements ShapedCraftingRecipe {

        private final ShapedCraftingRecipe baseRecipe;
        private final double multiplier;

        ArrowRecipe(ShapedCraftingRecipe baseRecipe, double multiplier) {
            this.baseRecipe = baseRecipe;
            this.multiplier = multiplier;
        }

        @Override
        public Ingredient getIngredient(int x, int y) {
            return this.baseRecipe.getIngredient(x, y);
        }

        @Override
        public int getWidth() {
            return this.baseRecipe.getWidth();
        }

        @Override
        public int getHeight() {
            return this.baseRecipe.getHeight();
        }

        @Override
        public boolean isValid(CraftingGridInventory grid, World world) {
            return this.baseRecipe.isValid(grid, world);
        }

        @Override
        public ItemStackSnapshot getResult(CraftingGridInventory grid) {
            final ItemStack stack = this.baseRecipe.getResult(grid).createStack();
            stack.setQuantity((int) Math.round(stack.getQuantity() * this.multiplier));
            return stack.createSnapshot();
        }

        @Override
        public List<ItemStackSnapshot> getRemainingItems(CraftingGridInventory grid) {
            return this.baseRecipe.getRemainingItems(grid);
        }

        @Override
        public Optional<String> getGroup() {
            return this.baseRecipe.getGroup();
        }

        @Override
        public String getId() {
            return "recipe_test:arrow";
        }

        @Override
        public String getName() {
            return this.baseRecipe.getName();
        }

        @Override
        public ItemStackSnapshot getExemplaryResult() {
            return this.baseRecipe.getExemplaryResult();
        }
    }

    public static class SmeltIronRecipe implements SmeltingRecipe {

        @Override
        public ItemStackSnapshot getExemplaryIngredient() {
            return ItemStack.of(ItemTypes.IRON_BLOCK).createSnapshot();
        }

        @Override
        public boolean isValid(ItemStackSnapshot ingredient) {
            return ingredient.getType() == ItemTypes.IRON_BLOCK;
        }

        @Override
        public Optional<SmeltingResult> getResult(ItemStackSnapshot ingredient) {
            if (!isValid(ingredient)) {
                return Optional.empty();
            }
            return Optional.of(new SmeltingResult(ItemStack.of(ItemTypes.IRON_INGOT, 9).createSnapshot(), 0));
        }

        @Override
        public ItemStackSnapshot getExemplaryResult() {
            return ItemStack.of(ItemTypes.IRON_INGOT, 9).createSnapshot();
        }
    }

    public static class SmeltGoldRecipe implements SmeltingRecipe {

        @Override
        public String getId() {
            return "recipe_test:smelt_gold_block_to_ingots";
        }

        @Override
        public String getName() {
            return "Smelt Gold Block To Ingot";
        }

        @Override
        public ItemStackSnapshot getExemplaryIngredient() {
            return ItemStack.of(ItemTypes.GOLD_BLOCK).createSnapshot();
        }

        @Override
        public boolean isValid(ItemStackSnapshot ingredient) {
            return ingredient.getType() == ItemTypes.GOLD_BLOCK;
        }

        @Override
        public Optional<SmeltingResult> getResult(ItemStackSnapshot ingredient) {
            if (!isValid(ingredient)) {
                return Optional.empty();
            }
            return Optional.of(new SmeltingResult(ItemStack.of(ItemTypes.GOLD_INGOT, 9).createSnapshot(), 0));
        }

        @Override
        public ItemStackSnapshot getExemplaryResult() {
            return ItemStack.of(ItemTypes.GOLD_INGOT, 9).createSnapshot();
        }
    }
}

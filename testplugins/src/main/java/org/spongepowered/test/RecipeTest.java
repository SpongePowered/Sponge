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

import static org.spongepowered.api.item.ItemTypes.BEDROCK;
import static org.spongepowered.api.item.ItemTypes.CHARCOAL;
import static org.spongepowered.api.item.ItemTypes.STONE;
import static org.spongepowered.api.item.ItemTypes.WHITE_BED;
import static org.spongepowered.api.item.ItemTypes.WHITE_WOOL;
import static org.spongepowered.test.RecipeTest.PLUGIN_ID;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.registry.RegistryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.crafting.CraftingInventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.recipe.Recipe;
import org.spongepowered.api.item.recipe.RecipeTypes;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.SpecialCraftingRecipe;
import org.spongepowered.api.item.recipe.single.StoneCutterRecipe;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Recipe Tests
 *
 * TODO still bugged?
 * There is a Forge client bug with recipes where a Forge client cannot connect to a Sponge-based server that only
 *      adds a new recipe via a plugin (or any server where you simply add a new recipe). In the interest of not breaking testing altogether,
 *      disabling this plugin for now.
 */
@Plugin(id = PLUGIN_ID, name = "Recipe Test", description = "A plugin to test recipes", version = "0.0.0")
public class RecipeTest implements LoadableModule {

    @Inject private PluginContainer container;
    private final Logger logger;
    private final RecipeTest.CraftListener listener = new RecipeTest.CraftListener ();

    public static final String PLUGIN_ID = "recipe_test";
    private ShapedCraftingRecipe emeraldPaper;

    @Inject
    public RecipeTest(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void enable(MessageReceiver src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    @Listener
    public void onRegisterCraftingRecipes(RegistryEvent.Catalog<Recipe> event) {
        this.craftingRecipes(event);
        this.smeltingRecipes(event);
        this.stoneCutterRecipes(event);
    }

    private void craftingRecipes(RegistryEvent.Catalog<Recipe> event) {
        final Ingredient s = Ingredient.of(STONE.get());
        final Ingredient b = Ingredient.of(WHITE_BED.get(), WHITE_WOOL.get());
        final ItemStack item = ItemStack.of(BEDROCK.get(), 1);
        final DataTransactionResult
                trans = item.offer(Keys.APPLIED_ENCHANTMENTS, Collections.singletonList(Enchantment.of(EnchantmentTypes.UNBREAKING.get(), 1)));
        if (trans.getType() != DataTransactionResult.Type.SUCCESS) {
            this.logger.error("Could not build recipe output!");
        }
        final ShapedCraftingRecipe recipe = CraftingRecipe.shapedBuilder().rows()
                .row(s, s, s)
                .row(s, b, s)
                .row(s, s, s)
                .result(item)
                .key(CatalogKey.of(PLUGIN_ID, "bedrock"))
                .build();
        event.register(recipe);

        // override vanilla arrow recipe
        ShapedCraftingRecipe arrowRecipe = (ShapedCraftingRecipe) Sponge.getRegistry().getRecipeRegistry().getById(CatalogKey.minecraft("arrow")).get();
        ShapedCraftingRecipe newArrowRecipe = CraftingRecipe.shapedBuilder().shapedLike(arrowRecipe).result(ItemStack.of(ItemTypes.ARROW.get(), 5).createSnapshot()).build();
        event.register(newArrowRecipe);

        // mixing 1xcharcoal and 4xcoal makes 5xcoal
        Ingredient coalIngredient = Ingredient.of(ItemTypes.COAL.get());
        Ingredient charCoalIngredient = Ingredient.of(CHARCOAL.get());
        ShapelessCraftingRecipe coalRecipe = ShapelessCraftingRecipe.builder()
                .addIngredients(coalIngredient, coalIngredient, coalIngredient, coalIngredient, charCoalIngredient)
                .result(ItemStack.of(ItemTypes.COAL.get(), 5))
                .key(CatalogKey.of(PLUGIN_ID, "craft_mix_charcoal_to_coal"))
                .build();
        event.register(coalRecipe);

        // special recipe coal + charcoal makes coal with coal remaining
        // First build helper recipe for the shape
        ShapelessCraftingRecipe specialShape = ShapelessCraftingRecipe.builder()
                .addIngredients(coalIngredient, charCoalIngredient)
                .result(ItemStack.of(ItemTypes.COAL.get()))
                .key(CatalogKey.of(PLUGIN_ID, "unregistered_mix_charcoal_to_coal"))
                .build();
        // Then build special recipe with that shape
        SpecialCraftingRecipe specialCoalRecipe = SpecialCraftingRecipe.builder()
                .matching(specialShape)
                .remainingItems(this::retainCoal)
                .result(ItemStack.of(ItemTypes.COAL.get()))
                .key(CatalogKey.of(PLUGIN_ID, "special_craft_mix_charcoal_to_coal"))
                .build();
        event.register(specialCoalRecipe);

        this.emeraldPaper = ShapedCraftingRecipe.builder().aisle("ppp", "pep", "ppp")
                .where('p', Ingredient.of(ItemTypes.PAPER.get()))
                .where('e', Ingredient.of(ItemTypes.EMERALD.get()))
                .result(ItemStackSnapshot.empty())
                .key(CatalogKey.of(PLUGIN_ID, "unregistered_emerald_paper"))
                .build();

        SpecialCraftingRecipe specialBiomePaper = SpecialCraftingRecipe.builder()
                .matching(this::specialMatcher)
                .result(this::specialResult)
                .key(CatalogKey.of(PLUGIN_ID, "special_biome_paper"))
                .build();
        event.register(specialBiomePaper);

    }

    private List<ItemStack> retainCoal(CraftingInventory craftingInventory) {
        List<ItemStack> remaining = new ArrayList<>();
        for (Slot slot : craftingInventory.slots()) {
            if (slot.peek().getType() == ItemTypes.COAL.get()) {
                remaining.add(slot.peek());
            } else {
                remaining.add(ItemStack.empty());
            }
        }
        return remaining;
    }

    private ItemStack specialResult(CraftingInventory craftingInventory) {
        Inventory parent = craftingInventory.parent();
        if (parent instanceof CarriedInventory) {
            Optional optCarrier = ((CarriedInventory) parent).getCarrier();
            if (optCarrier.isPresent()) {
                if (optCarrier.get() instanceof BlockCarrier) {
                    Location location = ((BlockCarrier) optCarrier.get()).getLocation();
                    BiomeType biome = location.getBiome();
                    ItemStack result = ItemStack.of(ItemTypes.PAPER.get());
                    result.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Paper crafted in ", biome.getKey()));
                    return result;
                }
            }
        }
        return ItemStack.empty();
    }

    private boolean specialMatcher(CraftingInventory craftingInventory, World world) {
        return this.emeraldPaper.isValid(craftingInventory, world);
    }

    private void smeltingRecipes(RegistryEvent.Catalog<Recipe> event) {
        final ItemStack in = ItemStack.of(ItemTypes.COAL.get(), 1);
        final ItemStack out = ItemStack.of(ItemTypes.COAL.get(), 1);
        out.offer(Keys.DISPLAY_NAME, Text.of("Hot Coal"));

        event.register(SmeltingRecipe.builder()
                .ingredient(in)
                .result(out)
                .key(CatalogKey.of(PLUGIN_ID, "hot_coal"))
                .experience(5)
                .build());
        this.logger.info("Registering custom smelting recipes!");

        SmeltingRecipe smeltGoldRecipe = SmeltingRecipe.builder()
                        .ingredient(ItemTypes.GOLD_BLOCK.get())
                        .result(ItemStack.of(ItemTypes.GOLD_INGOT.get(), 9))
                        .key(CatalogKey.of(PLUGIN_ID, "smelt_gold_block_to_ingot"))
                        .experience(0d)
                        .type(RecipeTypes.BLASTING.get())
                        .build();
        SmeltingRecipe smeltIronRecipe = SmeltingRecipe.builder()
                        .ingredient(ItemTypes.IRON_BLOCK.get())
                        .result(ItemStack.of(ItemTypes.IRON_INGOT.get(), 9))
                        .key(CatalogKey.of(PLUGIN_ID, "smelt_iron_block_to_ingot"))
                        .experience(0d)
                        .type(RecipeTypes.BLASTING.get())
                        .build();

        event.register(smeltGoldRecipe);
        event.register(smeltIronRecipe);

        this.logger.info("## Blast Furnace recipes:");
        Sponge.getRegistry().getRecipeRegistry().getAllOfType(RecipeTypes.BLASTING.get()).forEach(recipe -> this.logger.info(" - " + recipe.getKey()));
    }

    private void stoneCutterRecipes(RegistryEvent.Catalog<Recipe> event) {
        StoneCutterRecipe goldIngot = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.GOLD_BLOCK.get())
                .result(ItemStack.of(ItemTypes.GOLD_INGOT.get(), 9))
                .key(CatalogKey.of(PLUGIN_ID, "cut_gold_block_into_ingot")).build();
        StoneCutterRecipe goldNugget = StoneCutterRecipe.builder()
                        .ingredient(ItemTypes.GOLD_BLOCK.get())
                        .result(ItemStack.of(ItemTypes.GOLD_NUGGET.get(), 9*9))
                        .key(CatalogKey.of(PLUGIN_ID, "cut_gold_block_into_nugget")).build();
        event.register(goldIngot);
        event.register(goldNugget);
    }

    public static class CraftListener {

        @Listener
        public void onCraftPreview(CraftItemEvent.Preview event) {
            if (event.getRecipe().isPresent()) {
                if (event.getRecipe().get().getExemplaryResult().getType() == BEDROCK.get()) {
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
                if (event.getRecipe().get().getExemplaryResult().getType() == BEDROCK.get()) {
                    player.sendMessage(Text.of("You tried to craft Bedrock!"));
                    event.setCancelled(true);
                }
            }
        }

    }
}

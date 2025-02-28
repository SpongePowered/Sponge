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
package org.spongepowered.test.recipe;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.BannerPatternShapes;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackLike;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.recipe.RecipeTypes;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.ShapelessCraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.SpecialCraftingRecipe;
import org.spongepowered.api.item.recipe.single.StoneCutterRecipe;
import org.spongepowered.api.item.recipe.smithing.SmithingRecipe;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Plugin("recipetest")
public final class RecipeTest implements LoadableModule {

    private final PluginContainer plugin;
    private boolean enabled = false;

    @Inject
    public RecipeTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        this.enabled = true;
        Sponge.server().dataPackManager().reload();
    }

    @Override
    public void disable(final CommandContext ctx) {
        this.enabled = false;
        Sponge.server().dataPackManager().reload();
    }

    @SuppressWarnings("unchecked")
    @Listener
    private void onRecipeRegistry(final RegisterRegistryValueEvent event) {

        if (!this.enabled) {
            return;
        }
        // Standard recipes and ItemStack(with nbt) ingredient and results
        event.registry(RegistryTypes.RECIPE, registry -> {
            final Ingredient whiteRock = Ingredient.of(ItemTypes.POLISHED_DIORITE.get());
            final Ingredient whiteBed = Ingredient.of(ItemTypes.WHITE_BED.get());
            final ItemStack bedrock = ItemStack.of(ItemTypes.BEDROCK);

            final CraftingRecipe whiteBedrockRecipe = CraftingRecipe.shapedBuilder().rows()
                .row(whiteRock, whiteRock, whiteRock)
                .row(whiteRock, whiteBed, whiteRock)
                .row(whiteRock, whiteRock, whiteRock)
                .result(bedrock.copy())
                .build();

            registry.register(ResourceKey.of(this.plugin, "white_bedrock"), whiteBedrockRecipe);

            final Ingredient redRock = Ingredient.of(ItemTypes.POLISHED_GRANITE);
            final Ingredient redBed = Ingredient.of(ItemTypes.RED_BED);
            final ItemStack redBedRock = bedrock.copy();
            redBedRock.offer(Keys.CUSTOM_NAME, Component.text("Bedrock", NamedTextColor.RED));

            final CraftingRecipe redBedrockRecipe = CraftingRecipe.shapedBuilder().rows()
                .aisle("ggg", "gbg", "ggg")
                .where('g', redRock)
                .where('b', redBed)
                .result(redBedRock)
                .build();

            registry.register(ResourceKey.of(this.plugin, "red_bedrock"), redBedrockRecipe);

            final ItemStack moreBedrock = bedrock.copy();
            moreBedrock.setQuantity(9);
            final CraftingRecipe moreBedrockRecipe = CraftingRecipe.shapedBuilder().rows()
                .aisle("ggg", "gbg", "ggg")
                .where('g', redRock)
                .where('b', Ingredient.of(bedrock.copy()))
                .result(moreBedrock)
                .build();

            registry.register(ResourceKey.of(this.plugin, "more_red_bedrock"), moreBedrockRecipe);

            final CraftingRecipe cheapGoldenAppleRecipe = CraftingRecipe.shapelessBuilder()
                .addIngredients(ItemTypes.YELLOW_WOOL, ItemTypes.APPLE)
                .result(ItemStack.of(ItemTypes.GOLDEN_APPLE))
                .build();

            registry.register(ResourceKey.of(this.plugin, "cheap_golden_apple"), cheapGoldenAppleRecipe);

            final CraftingRecipe expensiveGoldenAppleRecipe = CraftingRecipe.shapelessBuilder()
                .addIngredients(ItemTypes.YELLOW_WOOL, ItemTypes.ENCHANTED_GOLDEN_APPLE)
                .result(ItemStack.of(ItemTypes.GOLDEN_APPLE))
                .build();

            registry.register(ResourceKey.of(this.plugin, "expensive_golden_apple"), expensiveGoldenAppleRecipe);

            final Ingredient bedrocks = Ingredient.of(bedrock, redBedRock);
            final CraftingRecipe bedrocksToGranite = CraftingRecipe.shapelessBuilder()
                .addIngredients(bedrocks, bedrocks)
                .result(ItemStack.of(ItemTypes.GRANITE, 13))
                .build();

            registry.register(ResourceKey.of(this.plugin, "bedrocks_to_granite"), bedrocksToGranite);

            final CookingRecipe diamondToCoalRecipe = CookingRecipe.builder().type(RecipeTypes.SMELTING)
                .ingredient(Ingredient.of(ItemTypes.DIAMOND))
                .result(ItemTypes.COAL)
                .experience(0)
                .build();

            registry.register(ResourceKey.of(this.plugin, "diamond_to_coal"), diamondToCoalRecipe);

            final CookingRecipe burnPaperAndSticksRecipe = CookingRecipe.builder().type(RecipeTypes.SMELTING)
                .ingredient(Ingredient.of(ItemTypes.PAPER, ItemTypes.STICK))
                .result(ItemTypes.GUNPOWDER)
                .experience(1)
                .cookingTime(Ticks.of(1))
                .build();

            registry.register(ResourceKey.of(this.plugin, "burn_paper_and_sticks"), burnPaperAndSticksRecipe);

            final CookingRecipe charcoalToCoalRecipe = CookingRecipe.builder().type(RecipeTypes.BLASTING)
                .ingredient(Ingredient.of(ItemTypes.CHARCOAL))
                .result(ItemTypes.COAL)
                .build();

            registry.register(ResourceKey.of(this.plugin, "charcoal_to_coal"), charcoalToCoalRecipe);

            final ItemStack redderBedrock = bedrock.copy();
            redderBedrock.offer(Keys.CUSTOM_NAME, Component.text("Bedrock", NamedTextColor.DARK_RED));

            final CookingRecipe removeRedOnBedrock = CookingRecipe.builder().type(RecipeTypes.BLASTING)
                .ingredient(Ingredient.of(redBedRock))
                .result(redderBedrock)
                .cookingTime(Ticks.of(20))
                .experience(100)
                .build();

            registry.register(ResourceKey.of(this.plugin, "redder_bedrock"), removeRedOnBedrock);

            final CookingRecipe overcookedPorkchopRecipe = CookingRecipe.builder().type(RecipeTypes.SMOKING)
                .ingredient(Ingredient.of(ItemTypes.COOKED_PORKCHOP))
                .result(ItemTypes.COAL)
                .build();

            registry.register(ResourceKey.of(this.plugin, "overcooked_porkchop"), overcookedPorkchopRecipe);

            final CookingRecipe sticksToTorches = CookingRecipe.builder().type(RecipeTypes.CAMPFIRE_COOKING)
                .ingredient(Ingredient.of(ItemTypes.STICK))
                .result(ItemTypes.TORCH)
                .cookingTime(Ticks.of(20))
                .build();

            registry.register(ResourceKey.of(this.plugin, "stick_to_torch"), sticksToTorches);

            final StoneCutterRecipe stonecutter1 = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.BEDROCK)
                .result(ItemStack.of(ItemTypes.BLACK_CONCRETE, 32))
                .build();

            final StoneCutterRecipe stonecutter2 = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.BEDROCK)
                .result(ItemStack.of(ItemTypes.BLACK_GLAZED_TERRACOTTA, 16))
                .build();

            final StoneCutterRecipe stonecutter3 = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.BEDROCK)
                .result(ItemStack.of(ItemTypes.BLACK_WOOL, 64))
                .build();

            registry.register(ResourceKey.of(this.plugin, "cut_bedrock_to_concrete"), stonecutter1);
            registry.register(ResourceKey.of(this.plugin, "cut_bedrock_to_terracotta"), stonecutter2);
            registry.register(ResourceKey.of(this.plugin, "cut_bedrock_wool"), stonecutter3);

            // Predicate Ingredients

            final Predicate<? super ItemStackLike> hardnessPredicate = stack -> stack.type().block().map(b -> b.defaultState().get(Keys.DESTROY_SPEED).orElse(0d) > 20).orElse(false); // e.g. obsidian
            final Ingredient hardBlock = Ingredient.of(ResourceKey.of(this.plugin, "hardblock"), hardnessPredicate, ItemStack.of(ItemTypes.OBSIDIAN));
            final CraftingRecipe hardblockToWool =
                ShapelessCraftingRecipe.builder().addIngredients(hardBlock).result(ItemStack.of(ItemTypes.WHITE_WOOL))
                    .build();

            registry.register(ResourceKey.of(this.plugin, "hardblock_to_wool"), hardblockToWool);

            // Function Results

            final ItemStack villagerEgg = ItemStack.of(ItemTypes.VILLAGER_SPAWN_EGG);
            final CraftingRecipe villagerSpawnEggRecipe = ShapedCraftingRecipe.builder()
                .aisle(" e ", "eve", " e ")
                .where('v', Ingredient.of(ItemTypes.BOOK))
                .where('e', Ingredient.of(ItemTypes.EMERALD_BLOCK))
                .result(grid -> {
                    final Optional<ServerPlayer> player = Sponge.server().causeStackManager().currentCause().first(ServerPlayer.class);
                    final String name = player.map(ServerPlayer::name).orElse("Steve");
                    villagerEgg.offer(Keys.CUSTOM_NAME, Component.text(name));
                    return villagerEgg.copy();
                }, villagerEgg.copy())
                .build();

            registry.register(ResourceKey.of(this.plugin, "villager_spawn_egg"), villagerSpawnEggRecipe);

            final ItemStack writtenBook = ItemStack.of(ItemTypes.WRITTEN_BOOK);
            writtenBook.offer(Keys.CUSTOM_NAME, Component.text("Biome Data"));
            writtenBook.offer(Keys.AUTHOR, Component.text("Herobrine"));
            final CraftingRecipe biomeDetectorRecipe = ShapedCraftingRecipe.builder()
                .aisle("d", "b")
                .where('d', Ingredient.of(ItemTypes.DAYLIGHT_DETECTOR))
                .where('b', Ingredient.of(ItemTypes.BOOK))
                .result(grid -> {
                    final Optional<ServerPlayer> player = Sponge.server().causeStackManager().currentCause().first(ServerPlayer.class);
                    final Optional<Biome> biome = player.map(p -> p.world().biome(p.blockPosition()));
                    final String name = biome.map(present -> RegistryTypes.BIOME.keyFor(player.get().world(), present)).map(ResourceKey::toString).orElse("Unknown");
                    final Integer biomeTemperature = biome.map(Biome::temperature).map(d -> (int) (d * 10)).orElse(0);
                    final Integer biomeHumidity = biome.map(Biome::humidity).map(d -> (int) (d * 10)).orElse(0);
                    final TextComponent temperature = Component.text("Temperature: ").append(Component.text(biomeTemperature));
                    final TextComponent humidity = Component.text("Humidity: ").append(Component.text(biomeHumidity));
                    writtenBook.offer(Keys.CUSTOM_NAME, Component.text("Biome Data: " + name));
                    writtenBook.offer(Keys.PAGES, Arrays.asList(temperature, humidity));
                    writtenBook.offer(Keys.AUTHOR, Component.text(player.map(ServerPlayer::name).orElse("Herobrine")));
                    return writtenBook.copy();
                }, writtenBook.copy())
                .build();

            registry.register(ResourceKey.of(this.plugin, "biome_detector"), biomeDetectorRecipe);
            final Ingredient blackOrWhite = Ingredient.of(ItemTypes.BLACK_WOOL, ItemTypes.WHITE_WOOL);
            final CraftingRecipe blackOrWhiteRecipe = ShapelessCraftingRecipe.builder()
                .addIngredients(blackOrWhite, blackOrWhite, blackOrWhite)
                .result(grid -> {
                    final int blacks = grid.query(QueryTypes.ITEM_TYPE, ItemTypes.BLACK_WOOL).capacity();
                    final int whites = grid.query(QueryTypes.ITEM_TYPE, ItemTypes.WHITE_WOOL).capacity();
                    return blacks > whites ? ItemStack.of(ItemTypes.BLACK_WOOL, 3) : ItemStack.of(ItemTypes.WHITE_WOOL, 3);
                }, ItemStack.of(ItemTypes.GRAY_WOOL))
                .build();

            registry.register(ResourceKey.of(this.plugin, "black_or_white"), blackOrWhiteRecipe);

            // Custom results dont work well in cooking recipes
            final ItemStack anvil = ItemStack.of(ItemTypes.DAMAGED_ANVIL);
            final CookingRecipe cookedAnvilRecipe = CookingRecipe.builder().type(RecipeTypes.BLASTING)
                .ingredient(ItemTypes.IRON_BLOCK)
                .result(inv -> anvil.copy(), anvil.copy())
                .build();
            registry.register(ResourceKey.of(this.plugin, "cooked_anvil"), cookedAnvilRecipe);

            final StoneCutterRecipe cutPlanksRecipe = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.OAK_PLANKS)
                .result(input -> {
                    if (new Random().nextBoolean()) {
                        return ItemStack.of(ItemTypes.OAK_SLAB, 4);
                    }
                    return ItemStack.of(ItemTypes.OAK_SLAB, 3);
                }, ItemStack.of(ItemTypes.OAK_SLAB, 2))
                .build();
            registry.register(ResourceKey.of(this.plugin, "cut_planks"), cutPlanksRecipe);

            final CraftingRecipe stripedBannerRecipe = SpecialCraftingRecipe.builder()
                .matching((inv, world) -> {
                    if (inv.capacity() != 9) {
                        return false;
                    }
                    final var grid = inv.asGrid();
                    final ItemType stick = grid.peek(1, 2).get().type();
                    if (!stick.isAnyOf(ItemTypes.STICK)) {
                        return false;
                    }

                    final ItemStack middleItem = grid.peekAt(1).get();

                    final ItemType type00 = grid.peek(0, 0).get().type();
                    final ItemType type10 = grid.peek(1, 0).get().type();
                    final ItemType type20 = grid.peek(2, 0).get().type();

                    final ItemType type01 = grid.peek(0, 1).get().type();
                    final ItemType type11 = grid.peek(1, 1).get().type();
                    final ItemType type21 = grid.peek(2, 1).get().type();

                    if (type00 == type01 && type01 == type20 && type20 == type21 && type10 == type11) {
                        if (type00.isAnyOf(ItemTypes.WHITE_WOOL)) {
                            if (middleItem.get(Keys.DYE_COLOR).isPresent()) {
                                return true;
                            }
                        }
                    }

                    return false;
                })
                .result((inv -> {
                    final DyeColor dyeColor = inv.peekAt(1).get().get(Keys.DYE_COLOR).get();
                    final ItemStack banner = ItemStack.of(ItemTypes.WHITE_BANNER);
                    final BannerPatternLayer pattern = BannerPatternLayer.of(BannerPatternShapes.STRIPE_CENTER, dyeColor);
                    banner.offer(Keys.BANNER_PATTERN_LAYERS, Arrays.asList(pattern));
                    return banner;
                }))
                .build();
            registry.register(ResourceKey.of(this.plugin, "special"), stripedBannerRecipe);

            final CraftingRecipe squeezeSpongeRecipe = ShapelessCraftingRecipe.builder()
                .addIngredients(ItemTypes.WET_SPONGE, ItemTypes.BUCKET)
                .remainingItems(inv -> inv.slots().stream().map(Slot::peek)
                    .map(item -> (item.type().isAnyOf(ItemTypes.WET_SPONGE) ? ItemTypes.SPONGE : ItemTypes.AIR).get())
                    .map(ItemStack::of)
                    .collect(Collectors.toList()))
                .result(ItemStack.of(ItemTypes.WATER_BUCKET))
                .build();

            registry.register(ResourceKey.of(this.plugin, "squeeze_sponge_recipe"), squeezeSpongeRecipe);

            // Smithing recipes

            final SmithingRecipe ironToGoldIngot = SmithingRecipe.builder()
                .template(ItemTypes.PAPER)
                .base(ItemTypes.IRON_INGOT)
                .addition(ItemTypes.NETHERITE_INGOT)
                .result(ItemStack.of(ItemTypes.GOLD_INGOT))
                .build();

            registry.register(ResourceKey.of(this.plugin, "iron_to_gold_ingot"), ironToGoldIngot);

            final ItemStack chainMail = ItemStack.of(ItemTypes.CHAINMAIL_CHESTPLATE);
            chainMail.offer(Keys.CUSTOM_NAME, Component.text("Heavy Chainmail", NamedTextColor.DARK_GRAY));
            chainMail.offer(Keys.LORE, Arrays.asList(Component.text("Smithing occured", NamedTextColor.DARK_GRAY)));
            final SmithingRecipe smithChainmail = SmithingRecipe.builder()
                .template(ItemTypes.PAPER)
                .base(ItemTypes.IRON_CHESTPLATE)
                .addition(ItemTypes.FIRE_CHARGE)
                .result(chainMail)
                .build();

            registry.register(ResourceKey.of(this.plugin, "smith_chainmail"), smithChainmail);
        });

    }
}

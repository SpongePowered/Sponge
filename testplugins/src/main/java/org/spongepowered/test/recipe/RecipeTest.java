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
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.BannerPatternShapes;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.recipe.RecipeRegistration;
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
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
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
    public void enable(CommandContext ctx) {
        this.enabled = true;
        try {
            Sponge.server().commandManager().process("reload");
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disable(CommandContext ctx) {
        this.enabled = false;
        try {
            Sponge.server().commandManager().process("reload");
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    @Listener
    @SuppressWarnings("unchecked")
    public void onRecipeRegistry(RegisterDataPackValueEvent<RecipeRegistration> event) {

        if (!this.enabled) {
            return;
        }
        // Standard recipes and ItemStack(with nbt) ingredient and results

        final Ingredient whiteRock = Ingredient.of(ItemTypes.POLISHED_DIORITE.get());
        final Ingredient whiteBed = Ingredient.of(ItemTypes.WHITE_BED.get());
        final ItemStack bedrock = ItemStack.of(ItemTypes.BEDROCK);

        final RecipeRegistration whiteBedrockRecipe = CraftingRecipe.shapedBuilder().rows()
                .row(whiteRock, whiteRock, whiteRock)
                .row(whiteRock, whiteBed, whiteRock)
                .row(whiteRock, whiteRock, whiteRock)
                .result(bedrock.copy())
                .key(ResourceKey.of(this.plugin, "white_bedrock"))
                .build();

        event.register(whiteBedrockRecipe);

        final Ingredient redRock = Ingredient.of(ItemTypes.POLISHED_GRANITE);
        final Ingredient redBed = Ingredient.of(ItemTypes.RED_BED);
        final ItemStack redBedRock = bedrock.copy();
        redBedRock.offer(Keys.CUSTOM_NAME, Component.text("Bedrock", NamedTextColor.RED));

        final RecipeRegistration redBedrockRecipe = CraftingRecipe.shapedBuilder().rows()
                .aisle("ggg", "gbg", "ggg")
                .where('g', redRock)
                .where('b', redBed)
                .result(redBedRock)
                .key(ResourceKey.of(this.plugin, "red_bedrock"))
                .build();

        event.register(redBedrockRecipe);

        final ItemStack moreBedrock = bedrock.copy();
        moreBedrock.setQuantity(9);
        final RecipeRegistration moreBedrockRecipe = CraftingRecipe.shapedBuilder().rows()
                .aisle("ggg", "gbg", "ggg")
                .where('g', redRock)
                .where('b', Ingredient.of(bedrock.copy()))
                .result(moreBedrock)
                .key(ResourceKey.of(this.plugin, "more_red_bedrock"))
                .build();

        event.register(moreBedrockRecipe);

        final RecipeRegistration cheapGoldenAppleRecipe = CraftingRecipe.shapelessBuilder()
                .addIngredients(ItemTypes.YELLOW_WOOL, ItemTypes.APPLE)
                .result(ItemStack.of(ItemTypes.GOLDEN_APPLE))
                .key(ResourceKey.of(this.plugin, "cheap_golden_apple"))
                .build();

        event.register(cheapGoldenAppleRecipe);

        final RecipeRegistration expensiveGoldenAppleRecipe = CraftingRecipe.shapelessBuilder()
                .addIngredients(ItemTypes.YELLOW_WOOL, ItemTypes.ENCHANTED_GOLDEN_APPLE)
                .result(ItemStack.of(ItemTypes.GOLDEN_APPLE))
                .key(ResourceKey.of(this.plugin, "expensive_golden_apple"))
                .build();

        event.register(expensiveGoldenAppleRecipe);

        final Ingredient bedrocks = Ingredient.of(bedrock, redBedRock);
        final RecipeRegistration bedrocksToGranite = CraftingRecipe.shapelessBuilder()
                .addIngredients(bedrocks, bedrocks)
                .result(ItemStack.of(ItemTypes.GRANITE, 13))
                .key(ResourceKey.of(this.plugin, "bedrocks_to_granite"))
                .build();

        event.register(bedrocksToGranite);

        final RecipeRegistration diamondToCoalRecipe = CookingRecipe.builder().type(RecipeTypes.SMELTING)
                .ingredient(Ingredient.of(ItemTypes.DIAMOND))
                .result(ItemTypes.COAL)
                .experience(0)
                .key(ResourceKey.of(this.plugin, "diamond_to_coal"))
                .build();

        event.register(diamondToCoalRecipe);

        final RecipeRegistration burnPaperAndSticksRecipe = CookingRecipe.builder().type(RecipeTypes.SMELTING)
                .ingredient(Ingredient.of(ItemTypes.PAPER, ItemTypes.STICK))
                .result(ItemTypes.GUNPOWDER)
                .experience(1)
                .cookingTime(1)
                .key(ResourceKey.of(this.plugin, "burn_paper_and_sticks"))
                .build();

        event.register(burnPaperAndSticksRecipe);

        final RecipeRegistration charcoalToCoalRecipe = CookingRecipe.builder().type(RecipeTypes.BLASTING)
                .ingredient(Ingredient.of(ItemTypes.CHARCOAL))
                .result(ItemTypes.COAL)
                .key(ResourceKey.of(this.plugin, "charcoal_to_coal"))
                .build();

        event.register(charcoalToCoalRecipe);

        final ItemStack redderBedrock = bedrock.copy();
        redderBedrock.offer(Keys.CUSTOM_NAME, Component.text("Bedrock", NamedTextColor.DARK_RED));

        final RecipeRegistration removeRedOnBedrock = CookingRecipe.builder().type(RecipeTypes.BLASTING)
                .ingredient(Ingredient.of(redBedRock))
                .result(redderBedrock)
                .cookingTime(20)
                .experience(100)
                .key(ResourceKey.of(this.plugin, "redder_bedrock"))
                .build();

        event.register(removeRedOnBedrock);

        final RecipeRegistration overcookedPorkchopRecipe = CookingRecipe.builder().type(RecipeTypes.SMOKING)
                .ingredient(Ingredient.of(ItemTypes.COOKED_PORKCHOP))
                .result(ItemTypes.COAL)
                .key(ResourceKey.of(this.plugin, "overcooked_porkchop"))
                .build();

        event.register(overcookedPorkchopRecipe);

        final RecipeRegistration sticksToTorches = CookingRecipe.builder().type(RecipeTypes.CAMPFIRE_COOKING)
                .ingredient(Ingredient.of(ItemTypes.STICK))
                .result(ItemTypes.TORCH)
                .cookingTime(20)
                .key(ResourceKey.of(this.plugin, "stick_to_torch"))
                .build();

        event.register(sticksToTorches);

        final RecipeRegistration stonecutter1 = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.BEDROCK)
                .result(ItemStack.of(ItemTypes.BLACK_CONCRETE, 64))
                .key(ResourceKey.of(this.plugin, "cut_bedrock_to_concrete"))
                .build();

        final RecipeRegistration stonecutter2 = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.BEDROCK)
                .result(ItemStack.of(ItemTypes.BLACK_GLAZED_TERRACOTTA, 64))
                .key(ResourceKey.of(this.plugin, "cut_bedrock_to_terracotta"))
                .build();

        final RecipeRegistration stonecutter3 = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.BEDROCK)
                .result(ItemStack.of(ItemTypes.BLACK_WOOL, 64))
                .key(ResourceKey.of(this.plugin, "cut_bedrock_wool"))
                .build();

        event.register(stonecutter1);
        event.register(stonecutter2);
        event.register(stonecutter3);

        // Predicate Ingredients

        final Predicate<ItemStack> hardnessPredicate = stack -> stack.type().block().map(b -> b.defaultState().get(Keys.DESTROY_SPEED).orElse(0d) > 20).orElse(false); // e.g. obsidian
        final Ingredient hardBlock = Ingredient.of(ResourceKey.of(this.plugin, "hardblock"), hardnessPredicate, ItemStack.of(ItemTypes.BEDROCK));
        final RecipeRegistration hardblockToWool =
                ShapelessCraftingRecipe.builder().addIngredients(hardBlock).result(ItemStack.of(ItemTypes.WHITE_WOOL))
                        .key(ResourceKey.of(this.plugin, "hardblock_to_wool"))
                        .build();

        event.register(hardblockToWool);

        // Function Results

        final ItemStack villagerEgg = ItemStack.of(ItemTypes.VILLAGER_SPAWN_EGG);
        final RecipeRegistration villagerSpawnEggRecipe = ShapedCraftingRecipe.builder()
                .aisle(" e ", "eve", " e ")
                .where('v', Ingredient.of(ItemTypes.BOOK))
                .where('e', Ingredient.of(ItemTypes.EMERALD_BLOCK))
                .result(grid -> {
                    final Optional<ServerPlayer> player = Sponge.server().causeStackManager().currentCause().first(ServerPlayer.class);
                    final String name = player.map(ServerPlayer::name).orElse("Steve");
                    villagerEgg.offer(Keys.CUSTOM_NAME, Component.text(name));
                    return villagerEgg.copy();
                }, villagerEgg.copy())
                .key(ResourceKey.of(this.plugin, "villager_spawn_egg"))
                .build();

        event.register(villagerSpawnEggRecipe);

        final ItemStack writtenBook = ItemStack.of(ItemTypes.WRITTEN_BOOK);
        writtenBook.offer(Keys.CUSTOM_NAME, Component.text("Biome Data"));
        writtenBook.offer(Keys.AUTHOR, Component.text("Herobrine"));
        final RecipeRegistration biomeDetectorRecipe = ShapedCraftingRecipe.builder()
                .aisle("d", "b")
                .where('d', Ingredient.of(ItemTypes.DAYLIGHT_DETECTOR))
                .where('b', Ingredient.of(ItemTypes.BOOK))
                .result(grid -> {
                    final Optional<ServerPlayer> player = Sponge.server().causeStackManager().currentCause().first(ServerPlayer.class);
                    final Optional<Biome> biome = player.map(p -> p.world().biome(p.blockPosition()));
                    final String name = biome.map(present -> RegistryTypes.BIOME.keyFor(player.get().world().registries(), present)).map(ResourceKey::toString).orElse("Unknown");
                    final Integer biomeTemperature = biome.map(Biome::temperature).map(d -> (int) (d*10)).orElse(0);
                    final Integer biomeHumidity = biome.map(Biome::humidity).map(d -> (int) (d*10)).orElse(0);
                    final TextComponent temperature = Component.text("Temperature: ").append(Component.text(biomeTemperature));
                    final TextComponent humidity = Component.text("Humidity: ").append(Component.text(biomeHumidity));
                    writtenBook.offer(Keys.CUSTOM_NAME, Component.text("Biome Data: " + name));
                    writtenBook.offer(Keys.PAGES, Arrays.asList(temperature, humidity));
                    writtenBook.offer(Keys.AUTHOR, Component.text(player.map(ServerPlayer::name).orElse("Herobrine")));
                    return writtenBook.copy();
                }, writtenBook.copy())
                .key(ResourceKey.of(this.plugin, "biome_detector"))
                .build();

        event.register(biomeDetectorRecipe);
        final Ingredient blackOrWhite = Ingredient.of(ItemTypes.BLACK_WOOL, ItemTypes.WHITE_WOOL);
        final RecipeRegistration blackOrWhiteRecipe = ShapelessCraftingRecipe.builder()
                .addIngredients(blackOrWhite, blackOrWhite, blackOrWhite)
                .result(grid -> {
                    final int blacks = grid.query(QueryTypes.ITEM_TYPE, ItemTypes.BLACK_WOOL).capacity();
                    final int whites = grid.query(QueryTypes.ITEM_TYPE, ItemTypes.WHITE_WOOL).capacity();
                    return blacks > whites ? ItemStack.of(ItemTypes.BLACK_WOOL, 3) : ItemStack.of(ItemTypes.WHITE_WOOL, 3);
                }, ItemStack.empty())
                .key(ResourceKey.of(this.plugin, "black_or_white"))
                .build();

        event.register(blackOrWhiteRecipe);

// Custom results dont work well in cooking recipes
//        final ItemStack anvil = ItemStack.of(ItemTypes.DAMAGED_ANVIL);
//        final RecipeRegistration<SmeltingRecipe> cookedAnvilRecipe = SmeltingRecipe.builder().type(RecipeTypes.BLASTING)
//                .ingredient(ItemTypes.IRON_BLOCK)
//                .result(inv -> {
//                    return anvil.copy();
//                }, anvil.copy())
//                .key(ResourceKey.of(this.plugin, "cooked_anvil"))
//                .build();
//        event.register(cookedAnvilRecipe);

        final RecipeRegistration cutPlanksRecipe = StoneCutterRecipe.builder()
                .ingredient(ItemTypes.OAK_PLANKS)
                .result(input -> {
                    if (new Random().nextBoolean()) {
                        return ItemStack.of(ItemTypes.OAK_SLAB, 4);
                    }
                    return ItemStack.of(ItemTypes.OAK_SLAB, 3);
                }, ItemStack.of(ItemTypes.OAK_SLAB, 2))
                .key(ResourceKey.of(this.plugin, "cut_planks"))
                .build();
        event.register(cutPlanksRecipe);


        final RecipeRegistration stripedBannerRecipe = SpecialCraftingRecipe.builder()
                .matching((inv, world) -> {
                    if (inv.capacity() != 9) {
                        return false;
                    }
                    final ItemType stick = inv.asGrid().peek(2,1).get().type();
                    if (!stick.isAnyOf(ItemTypes.STICK)) {
                        return false;
                    }

                    final ItemStack middleItem = inv.peekAt(1).get();

                    final ItemType type00 = inv.asGrid().peek(0,0).get().type();
                    final ItemType type10 = inv.asGrid().peek(0,1).get().type();
                    final ItemType type20 = inv.asGrid().peek(0,2).get().type();

                    final ItemType type01 = inv.asGrid().peek(1,0).get().type();
                    final ItemType type11 = inv.asGrid().peek(1,1).get().type();
                    final ItemType type21 = inv.asGrid().peek(1,2).get().type();

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
                .key(ResourceKey.of(this.plugin, "special"))
                .build();
        event.register(stripedBannerRecipe);

        final RecipeRegistration squeezeSpongeRecipe = ShapelessCraftingRecipe.builder()
                .addIngredients(ItemTypes.WET_SPONGE, ItemTypes.BUCKET)
                .remainingItems(inv -> inv.slots().stream().map(Slot::peek)
                        .map(item -> (item.type().isAnyOf(ItemTypes.WET_SPONGE) ? ItemTypes.SPONGE : ItemTypes.AIR).get())
                        .map(ItemStack::of)
                        .collect(Collectors.toList()))
                .result(ItemStack.of(ItemTypes.WATER_BUCKET))
                .key(ResourceKey.of(this.plugin, "squeeze_sponge_recipe"))
                .build();

        event.register(squeezeSpongeRecipe);

        // Smithing recipes

        final RecipeRegistration ironToGoldIngot = SmithingRecipe.builder()
                .base(ItemTypes.IRON_INGOT)
                .addition(ItemTypes.NETHERITE_INGOT)
                .result(ItemStack.of(ItemTypes.GOLD_INGOT))
                .key(ResourceKey.of(this.plugin, "iron_to_gold_ingot"))
                .build();

        event.register(ironToGoldIngot);

        final ItemStack chainMail = ItemStack.of(ItemTypes.CHAINMAIL_CHESTPLATE);
        chainMail.offer(Keys.CUSTOM_NAME, Component.text("Heavy Chainmail", NamedTextColor.DARK_GRAY));
        chainMail.offer(Keys.LORE, Arrays.asList(Component.text("Smithing occured", NamedTextColor.DARK_GRAY)));
        final RecipeRegistration smithChainmail = SmithingRecipe.builder()
                .base(ItemTypes.IRON_CHESTPLATE)
                .addition(ItemTypes.FIRE_CHARGE)
                .result(chainMail)
                .key(ResourceKey.of(this.plugin, "smith_chainmail"))
                .build();

        event.register(smithChainmail);


    }
}

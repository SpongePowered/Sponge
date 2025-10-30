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
package org.spongepowered.common.recipe;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.recipe.RecipeTypes;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Builder;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.inventory.AbstractCraftingMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.AbstractFurnaceMenuAccessor;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class RecipePlaceTest {

    // Utilities

    private static int CONTAINER_COUNTER = 0;

    private static RecipeBookMenu createMenu(final MenuType<? extends RecipeBookMenu> menuType, final ServerPlayer player) {
        return menuType.create(RecipePlaceTest.CONTAINER_COUNTER++, player.getInventory());
    }

    private static String stackToString(final ItemStack stack) {
        return stack.isEmpty()
            ? "(empty)"
            : String.format("(%s: %s, max: %s)",
                stack.type().key(RegistryTypes.ITEM_TYPE).value(), stack.quantity(), stack.maxStackQuantity());
    }

    private static String inventoryToString(final Inventory inventory, final boolean removeEmpty) {
        return inventory.slots().stream()
            .map(Slot::peek)
            .filter(stack -> !removeEmpty || !stack.isEmpty())
            .map(RecipePlaceTest::stackToString)
            .collect(Collectors.joining(", "));
    }

    private static ItemStack withQuantity(final ItemStack stack, final int quantity) {
        final ItemStack copy = stack.copy();
        copy.setQuantity(quantity);
        return copy;
    }

    // Tests

    private static <T, I extends RecipeInput> void testRecipe(
        final RecipeBookMenu menu, final ServerPlayer player, final TestContext context,
        final Function<RecipeBookMenu, T> inputInventoryProvider, final Function<T, I> inputProvider
    ) {
        final Inventory playerInventory = (Inventory) player.getInventory();
        final List<ItemStack> initialInventory = context.inventory();
        playerInventory.clear();
        for (int i = 0; i < initialInventory.size(); ++i) {
            playerInventory.set(i, initialInventory.get(i));
        }

        final List<ItemStack> initialInput = context.input();
        final Inventory input = (Inventory) inputInventoryProvider.apply(menu);
        assertTrue(initialInput.size() <= input.capacity(),
            () -> String.format("Initial input size (%s) is greater than actual input size (%s)",
                initialInput.size(), input.capacity()));

        input.clear();
        for (int i = 0; i < initialInput.size(); ++i) {
            input.set(i, initialInput.get(i));
        }

        final List<String> inputs = new ArrayList<>();
        final List<String> inventories = new ArrayList<>();
        inputs.add(RecipePlaceTest.inventoryToString(input, false));
        inventories.add(RecipePlaceTest.inventoryToString(playerInventory, true));
        for (int i = 0; i < context.clickAmount(); ++i) {
            menu.handlePlacement(context.shiftClick(), true, context.recipe(), player.serverLevel(), player.getInventory());
            inputs.add(RecipePlaceTest.inventoryToString(input, false));
            inventories.add(RecipePlaceTest.inventoryToString(playerInventory, true));
        }

        final Supplier<String> history = () -> "Placement history:\n" +
            IntStream.range(0, inputs.size())
                .map(click -> inputs.size() - click - 1)
                .mapToObj(click -> String.format("""
                        - After click %s:
                          - Input:     %s
                          - Inventory: %s""",
                    click, inputs.get(click), inventories.get(click)))
                .collect(Collectors.joining("\n"));

        if (context.expectedCrafts() != 0) {
            assertTrue(((Recipe<I>) context.recipe().value()).matches(inputProvider.apply((T) input), player.level()),
                () -> "Recipe does not match\n" + history.get());
        }

        final List<Integer> quantities = input.slots().stream()
            .map(Slot::peek)
            .map(ItemStack::quantity)
            .distinct()
            .toList();
        final int max = quantities.stream().max(Integer::compare).get();
        final int min = quantities.stream().filter(quantity -> quantity != 0).min(Integer::compare).orElse(context.expectedCrafts());
        assertTrue(context.expectedCrafts() == max && min == max,
            () -> String.format("Expected %s items in each slot but found %s\n%s",
                context.expectedCrafts(), context.expectedCrafts() == max ? min : max, history.get()));
    }

    private static <T, I extends RecipeInput> void testRecipe(
        final MenuType<? extends RecipeBookMenu> menuType, final ServerPlayer player, final TestContext context,
        final Function<RecipeBookMenu, T> inputInventoryProvider, final Function<T, I> inputProvider
    ) {
        RecipePlaceTest.testRecipe(RecipePlaceTest.createMenu(menuType, player), player, context, inputInventoryProvider, inputProvider);
    }

    private static void testCraftingRecipe(final ServerPlayer player, final TestContext context) {
        RecipePlaceTest.testRecipe(MenuType.CRAFTING, player, context,
            menu -> ((AbstractCraftingMenuAccessor) menu).accessor$craftSlots(),
            CraftingContainer::asCraftInput);
    }

    private static void testSmeltingRecipe(final ServerPlayer player, final TestContext context) {
        RecipePlaceTest.testRecipe(MenuType.FURNACE, player, context,
            menu -> ((Inventory) ((AbstractFurnaceMenuAccessor) menu).accessor$container()).slot(0).get(),
            slot -> new SingleRecipeInput(ItemStackUtil.toNative(slot.peek())));
    }

    // TestContexts

    private static TestContext context(final String key, final Builder<? extends org.spongepowered.api.item.recipe.Recipe<?>, ?> spongeRecipe) {
        return new TestContext(new RecipeHolder<>(
            net.minecraft.resources.ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath("sponge", key)),
            (Recipe<?>) spongeRecipe.build()));
    }

    private static Stream<TestContext> populateTests(
        final TestContext baseTest,
        final int regularExpectedCrafts,
        // This exists due to vanilla having a bug with shift-placing that
        // pulls item to crafting grid up to exactly ItemType's max stack size
        // even if ItemStack's max stack size is less or more than that.
        final int shiftExpectedCrafts,
        final List<ItemStack> partialInventory, final List<ItemStack> partialInput,
        final List<ItemStack> badInventory, final List<ItemStack> badInput
    ) {
        final List<ItemStack> totalInitialInventory = Stream.concat(partialInventory.stream(), partialInput.stream()).toList();
        final List<TestContext> baseInputs = List.of(
            baseTest.name("Empty input").input(List.of()),
            baseTest.name("Bad input").input(badInput)
        );

        final Stream<TestContext> toFail = baseInputs.stream()
            .flatMap(context -> Stream.of(
                context.name("Empty inventory").inventory(List.of()),
                context.name("Bad inventory").inventory(badInventory)
            ))
            .flatMap(context -> Stream.of(context, context.shift()))
            // 2 clicks is enough to ensure we always fail
            .flatMap(context -> Stream.of(context, context.clicks(2)))
            .map(context -> context.crafts(0));

        final TestContext regularTest = baseTest.name("Partial input").inventory(partialInventory).input(partialInput);
        final Stream<TestContext> toMatchSingleClick = Stream.concat(
            baseInputs.stream().map(context -> context.inventory(totalInitialInventory)),
            Stream.of(regularTest)
        ).flatMap(context -> Stream.of(
            context.crafts(1),
            context.shift().crafts(shiftExpectedCrafts)
        ));

        // If tests above pass, after first click we end up with the same layout no matter the initial input.
        // So we can perform multiple-click tests on a single input.
        final Stream<TestContext> toMatchMultipleClicks = Stream.of(regularTest)
            .flatMap(context -> Stream.concat(
                IntStream.rangeClosed(2, regularExpectedCrafts)
                    .mapToObj(clicks -> context.clicks(clicks).crafts(clicks)),
                Stream.of(
                    context.clicks(regularExpectedCrafts + 1).crafts(regularExpectedCrafts),
                    context.shift().clicks(2).crafts(shiftExpectedCrafts))
            ));

        return Stream.concat(toFail, Stream.concat(toMatchSingleClick, toMatchMultipleClicks));
    }

    private static Stream<TestContext> streamCraftingRecipes() {
        final ItemStack empty = ItemStack.empty();
        final ItemStack bedrock = ItemStack.of(ItemTypes.BEDROCK);
        final ItemStack stone64 = ItemStack.of(ItemTypes.STONE, 64);
        final ItemStack pearl = ItemStack.of(ItemTypes.ENDER_PEARL);
        final ItemStack pearl16 = RecipePlaceTest.withQuantity(pearl, 16);
        final ItemStack smallPearl = pearl.copy();
        smallPearl.offer(Keys.MAX_STACK_SIZE, 32);
        final ItemStack smallPearl4 = RecipePlaceTest.withQuantity(smallPearl, 4);
        final ItemStack smallPearl32 = RecipePlaceTest.withQuantity(smallPearl, 32);
        final ItemStack bigPearl = pearl.copy();
        bigPearl.offer(Keys.MAX_STACK_SIZE, 8);
        final ItemStack bigPearl4 = RecipePlaceTest.withQuantity(bigPearl, 4);
        final ItemStack bigPearl8 = RecipePlaceTest.withQuantity(bigPearl, 8);
        final ItemStack result = ItemStack.of(ItemTypes.BARRIER);

        final Ingredient stoneIngredient = Ingredient.of(stone64.type());
        final Ingredient anyPearlIngredient = Ingredient.of(pearl.type());
        final Ingredient smallPearlIngredient = Ingredient.of(ResourceKey.sponge("small_pearl"),
            stack -> stack.type() == smallPearl.type()
                && stack.maxStackQuantity() == smallPearl.maxStackQuantity(),
            pearl);
        final Ingredient bigPearlIngredient = Ingredient.of(ResourceKey.sponge("big_pearl"),
            stack -> stack.type() == bigPearl.type()
                && stack.maxStackQuantity() == bigPearl.maxStackQuantity(),
            pearl);

        return Stream.of(
            RecipePlaceTest.populateTests(
                RecipePlaceTest.context("regular_shaped_crafting", CraftingRecipe.shapedBuilder()
                    .aisle("S S", " P ")
                    .where('S', stoneIngredient)
                    .where('P', anyPearlIngredient)
                    .result(result)),
                8, 16,
                List.of(stone64, bigPearl8, bigPearl8),
                List.of(
                    stone64, empty,     empty,
                    empty,   bigPearl4, empty),
                Collections.nCopies(9, bedrock), Collections.nCopies(9, bedrock)
            ),

            RecipePlaceTest.populateTests(
                RecipePlaceTest.context("custom_shaped_crafting", CraftingRecipe.shapedBuilder()
                    .aisle("SSS", "BBB", "SSS")
                    .where('S', smallPearlIngredient)
                    .where('B', bigPearlIngredient)
                    .result(result)),
                4, 4,
                List.of(smallPearl32, bigPearl, bigPearl, smallPearl32, bigPearl, bigPearl, smallPearl32),
                List.of(
                    smallPearl4, empty, smallPearl4,
                    bigPearl4,   empty, bigPearl4,
                    smallPearl,  empty, empty
                    ),
                List.of(pearl16), Collections.nCopies(9, pearl)
            ),

            RecipePlaceTest.populateTests(
                RecipePlaceTest.context("regular_shapeless_crafting", CraftingRecipe.shapelessBuilder()
                    .addIngredients(anyPearlIngredient, stoneIngredient, anyPearlIngredient)),
                16, 16,
                List.of(smallPearl32),
                List.of(
                    smallPearl4, stone64),
                Collections.nCopies(10, bedrock), Collections.nCopies(3, bedrock)
            )/*,

            //TODO uncomment after shapeless recipe fix
            RecipePlaceTest.populateTests(
                RecipePlaceTest.context("custom_shapeless_crafting", CraftingRecipe.shapelessBuilder()
                    .addIngredients(
                        smallPearlIngredient, smallPearlIngredient, smallPearlIngredient,
                        bigPearlIngredient, bigPearlIngredient, bigPearlIngredient)),
                8, 16,
                Collections.nCopies(5, bigPearl8),
                List.of(
                    smallPearl32, smallPearl32, smallPearl,
                    bigPearl4,    bigPearl4,   empty),
                List.of(pearl16), Collections.nCopies(6, pearl)
            )*/
        ).flatMap(Function.identity());
    }

    private static Stream<TestContext> streamSmeltingRecipes() {
        final ItemStack bedrock = ItemStack.of(ItemTypes.BEDROCK, 64);
        final ItemStack snowball = ItemStack.of(ItemTypes.SNOWBALL);
        final ItemStack snowball4 = RecipePlaceTest.withQuantity(snowball, 4);
        final ItemStack bigSnowball = snowball.copy();
        bigSnowball.offer(Keys.MAX_STACK_SIZE, 4);
        final ItemStack result = ItemStack.of(ItemTypes.BARRIER);

        return Stream.of(
            RecipePlaceTest.populateTests(
                RecipePlaceTest.context("regular_smelting", CookingRecipe.builder()
                    .type(RecipeTypes.SMELTING)
                    .ingredient(Ingredient.of(snowball.type()))
                    .result(result)),
                4, 8,
                Collections.nCopies(8, bigSnowball), List.of(),
                List.of(bedrock), List.of(bedrock)
            ),

            RecipePlaceTest.populateTests(
                RecipePlaceTest.context("custom_smelting", CookingRecipe.builder()
                    .type(RecipeTypes.SMELTING)
                    .ingredient(Ingredient.of(ResourceKey.sponge("big_snowball"),
                        stack -> stack.type() == bigSnowball.type()
                            && stack.maxStackQuantity() == bigSnowball.maxStackQuantity(),
                        snowball))
                    .result(result)),
                4, 8,
                Collections.nCopies(8, bigSnowball), List.of(),
                List.of(snowball4), List.of(snowball4)
            )
        ).flatMap(Function.identity());
    }

    @TestFactory
    public Stream<DynamicTest> testRecipes() {
        final ServerPlayer player = new FakePlayer(SpongeCommon.server().overworld(), new GameProfile(UUID.randomUUID(), "Player"));
        return Stream.of(
            RecipePlaceTest.streamCraftingRecipes().map(context ->
                dynamicTest(context.asTestName(), () -> RecipePlaceTest.testCraftingRecipe(player, context))),
            RecipePlaceTest.streamSmeltingRecipes().map(context ->
                dynamicTest(context.asTestName(), () -> RecipePlaceTest.testSmeltingRecipe(player, context)))
        ).flatMap(Function.identity());
    }

    private record TestContext(
        RecipeHolder<?> recipe, String testName, int expectedCrafts, boolean shiftClick, int clickAmount,
        List<ItemStack> inventory, List<ItemStack> input
    ) {
        public TestContext(final RecipeHolder<?> recipe) {
            this(recipe, "", 1, false, 1, List.of(), List.of());
        }

        public TestContext name(final String testName) {
            final String newTestName = this.testName.isEmpty() ? testName : (this.testName + ", " + testName);
            return new TestContext(this.recipe, newTestName, this.expectedCrafts, this.shiftClick, this.clickAmount, this.inventory, this.input);
        }

        public TestContext crafts(final int expectedCrafts) {
            return new TestContext(this.recipe, this.testName, expectedCrafts, this.shiftClick, this.clickAmount, this.inventory, this.input);
        }

        public TestContext shift() {
            return new TestContext(this.recipe, this.testName, this.expectedCrafts, true, this.clickAmount, this.inventory, this.input);
        }

        public TestContext clicks(final int clickAmount) {
            return new TestContext(this.recipe, this.testName, this.expectedCrafts, this.shiftClick, clickAmount, this.inventory, this.input);
        }

        public TestContext inventory(final List<ItemStack> items) {
            return new TestContext(this.recipe, this.testName, this.expectedCrafts, this.shiftClick, this.clickAmount, items, this.input);
        }

        public TestContext input(final List<ItemStack> items) {
            return new TestContext(this.recipe, this.testName, this.expectedCrafts, this.shiftClick, this.clickAmount, this.inventory, items);
        }

        public String asTestName() {
            return String.format("%s recipe (crafts: %s, shift: %s, clicks: %s, %s)",
                this.recipe.id().location().getPath(),
                this.expectedCrafts,
                this.shiftClick,
                this.clickAmount,
                this.testName.isEmpty() ? "Regular" : this.testName);
        }
    }

    private static final class FakePlayer extends ServerPlayer {

        public FakePlayer(final ServerLevel level, final GameProfile name) {
            super(level.getServer(), level, name, ClientInformation.createDefault());
            this.connection = new GamePacketListener(this);
        }

        private static final class GamePacketListener extends ServerGamePacketListenerImpl {
            private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.SERVERBOUND);

            public GamePacketListener(final ServerPlayer player) {
                super(player.server, GamePacketListener.DUMMY_CONNECTION, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
            }
        }
    }
}

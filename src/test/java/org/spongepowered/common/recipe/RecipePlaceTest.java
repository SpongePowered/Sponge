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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class RecipePlaceTest {

    private static RecipeBookMenu createMenu(final MenuType<? extends RecipeBookMenu> menuType, final ServerPlayer player) {
        return menuType.create(1, player.getInventory());
    }

    private static String stackToString(final ItemStack stack) {
        return stack.isEmpty()
            ? "(empty)"
            : String.format("(%s: %s, max: %s)",
                stack.type().key(RegistryTypes.ITEM_TYPE).value(), stack.quantity(), stack.maxStackQuantity());
    }

    private static String stacksToString(final boolean removeEmpty, final List<ItemStack> items) {
        return items.stream()
            .filter(stack -> !removeEmpty || !stack.isEmpty())
            .map(RecipePlaceTest::stackToString)
            .collect(Collectors.joining(", "));
    }

    private static List<ItemStack> createExpectedInput(final List<ItemStack> items, final int quantity) {
        return items.stream()
            .map(ItemStack::copy)
            .peek(stack -> stack.setQuantity(quantity))
            .toList();
    }

    private static ItemStack withQuantity(final ItemStack stack, final int quantity) {
        final ItemStack copy = stack.copy();
        copy.setQuantity(quantity);
        return copy;
    }

    private static Stream<TestContext> badLayoutTests(final TestContext base) {
        return Stream.of(base)
            .flatMap(context -> Stream.of(
                context.name("Empty input").input(List.of()),
                context.name("Bad input")
            ))
            .flatMap(context -> Stream.of(
                context.name("Empty inventory").inventory(List.of()),
                context.name("Bad inventory")
            ))
            .flatMap(context -> Stream.of(context, context.shift()))
            .flatMap(context -> Stream.of(context, context.creative()))
            // 2 clicks is enough to ensure we always get empty input
            .map(context -> context.expectInputs(List.of(List.of(), List.of())));
    }

    private static Stream<TestContext> streamCraftingRecipes() {
        final ItemStack empty = ItemStack.empty();
        final ItemStack bedrock = ItemStack.of(ItemTypes.BEDROCK);
        final ItemStack stone = ItemStack.of(ItemTypes.STONE);
        final ItemStack stone64 = RecipePlaceTest.withQuantity(stone, 64);
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
            Stream.of(new TestContext("regular_shaped_crafting", CraftingRecipe.shapedBuilder()
                .aisle("S S", " P ")
                .where('S', stoneIngredient)
                .where('P', anyPearlIngredient)
                .result(result)))
                .flatMap(test -> Stream.concat(
                    new DefaultedTestPopulator(test
                        .inventory(List.of(stone64, bigPearl8, bigPearl8))
                        .input(List.of(
                            stone64, empty,     empty,
                            empty,   bigPearl4, empty,
                            empty,   empty,     empty)))
                        .expectCrafts(8, 16)
                        .expectInput(List.of(
                            stone, empty,    stone,
                            empty, bigPearl, empty,
                            empty, empty,    empty))
                        .populate(),
                    RecipePlaceTest.badLayoutTests(test
                        .inventory(Collections.nCopies(9, bedrock))
                        .input(Collections.nCopies(9, bedrock)))
                )),

            Stream.of(new TestContext("custom_shaped_crafting", CraftingRecipe.shapedBuilder()
                .aisle("SSS", "BBB", "SSS")
                .where('S', smallPearlIngredient)
                .where('B', bigPearlIngredient)
                .result(result)))
                .flatMap(test -> Stream.concat(
                    new DefaultedTestPopulator(test
                        .inventory(List.of(smallPearl32, bigPearl, bigPearl, bigPearl, bigPearl, smallPearl32))
                        .input(List.of(
                            smallPearl4, empty, smallPearl4,
                            bigPearl4,   empty, bigPearl4,
                            smallPearl,  empty, empty)))
                        .expectCrafts(4, 4)
                        .expectInput(List.of(
                            smallPearl, smallPearl, smallPearl,
                            bigPearl,   bigPearl,   bigPearl,
                            smallPearl, smallPearl, smallPearl))
                        .populate(),
                    RecipePlaceTest.badLayoutTests(test
                        .inventory(List.of(pearl16))
                        .input(Collections.nCopies(9, pearl)))
                )),

            Stream.of(new TestContext("regular_shapeless_crafting", CraftingRecipe.shapelessBuilder()
                .addIngredients(
                    anyPearlIngredient, stoneIngredient, anyPearlIngredient)))
                .flatMap(test -> Stream.concat(
                    new DefaultedTestPopulator(test
                        .inventory(List.of(smallPearl32))
                        .input(List.of(
                            smallPearl4, stone64, empty,
                            empty,       empty,   empty,
                            empty,       empty,   empty)))

                        .expectCrafts(16, 16)
                        .expectInput(List.of(
                            smallPearl, stone, smallPearl,
                            empty,      empty, empty,
                            empty,      empty, empty))
                        .populate(),
                    RecipePlaceTest.badLayoutTests(test
                        .inventory(Collections.nCopies(10, bedrock))
                        .input(Collections.nCopies(3, bedrock)))
                )),

            Stream.of(new TestContext("custom_shapeless_crafting", CraftingRecipe.shapelessBuilder()
                .addIngredients(
                    smallPearlIngredient, smallPearlIngredient, smallPearlIngredient,
                    bigPearlIngredient,   bigPearlIngredient,   bigPearlIngredient)))
                .flatMap(test -> Stream.concat(
                    new DefaultedTestPopulator(test
                        .inventory(Collections.nCopies(5, bigPearl8))
                        .input(List.of(
                            smallPearl32, smallPearl32, smallPearl,
                            bigPearl4,    bigPearl4,    empty,
                            empty,        empty,        empty)))

                        .expectCrafts(8, 16)
                        .expectInput(List.of(
                            smallPearl, smallPearl, smallPearl,
                            bigPearl,   bigPearl,   bigPearl,
                            empty,      empty,      empty))
                        .populate(),
                    RecipePlaceTest.badLayoutTests(test
                        .inventory(List.of(pearl16))
                        .input(Collections.nCopies(6, pearl)))
                ))
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
            Stream.of(new TestContext("regular_smelting", CookingRecipe.builder()
                .type(RecipeTypes.SMELTING)
                .ingredient(Ingredient.of(snowball.type()))
                .result(result)))
                .flatMap(test -> Stream.concat(
                    new DefaultedTestPopulator(test
                        .inventory(Collections.nCopies(8, bigSnowball)))

                        .expectCrafts(4, 8)
                        .expectInput(List.of(bigSnowball))
                        .populate(),
                    RecipePlaceTest.badLayoutTests(test
                        .inventory(List.of(bedrock))
                        .input(List.of(bedrock)))
                )),

            Stream.of(new TestContext("custom_smelting", CookingRecipe.builder()
                .type(RecipeTypes.SMELTING)
                .ingredient(Ingredient.of(ResourceKey.sponge("big_snowball"),
                    stack -> stack.type() == bigSnowball.type()
                        && stack.maxStackQuantity() == bigSnowball.maxStackQuantity(),
                    snowball))
                .result(result)))
                .flatMap(test -> Stream.concat(
                    new DefaultedTestPopulator(test
                        .inventory(Collections.nCopies(8, bigSnowball)))

                        .expectCrafts(4, 8)
                        .expectInput(List.of(bigSnowball))
                        .populate(),
                    RecipePlaceTest.badLayoutTests(test
                        .inventory(List.of(snowball4))
                        .input(List.of(snowball4)))
                ))
        ).flatMap(Function.identity());
    }

    @TestFactory
    public Stream<DynamicTest> generateTests() {
        return Stream.of(
            RecipePlaceTest.streamCraftingRecipes().map(context ->
                dynamicTest(context.asTestName(), context::testCrafting)),
            RecipePlaceTest.streamSmeltingRecipes().map(context ->
                dynamicTest(context.asTestName(), context::testSmelting))
        ).flatMap(Function.identity());
    }

    @FunctionalInterface
    private interface TestPopulator {

        Stream<TestContext> populate();
    }

    private static final class SimpleTestPopulator implements TestPopulator {

        private final TestContext base;
        private final List<TestContext> tests = new ArrayList<>();

        public SimpleTestPopulator(final TestContext base) {
            this.base = base;
        }

        public SimpleTestPopulator add(final Function<TestContext, TestContext> testProvider) {
            this.tests.add(testProvider.apply(this.base));
            return this;
        }

        public SimpleTestPopulator addAll(final Function<TestContext, Stream<TestContext>> testProvider) {
            testProvider.apply(this.base).forEach(this.tests::add);
            return this;
        }

        @Override
        public Stream<TestContext> populate() {
            return this.tests.stream();
        }
    }

    private static final class DefaultedTestPopulator implements TestPopulator {

        private final TestContext base;

        private int expectedRegularCrafts;
        // This exists due to vanilla having a bug with shift-placing that
        // pulls item to crafting grid up to exactly ItemType's max stack size
        // even if ItemStack's max stack size is less or more than that.
        private int expectedShiftCrafts;
        private List<ItemStack> expectedInput = List.of();

        public DefaultedTestPopulator(final TestContext base) {
            this.base = base;
        }

        public DefaultedTestPopulator expectCrafts(final int regularCrafts, final int shiftCrafts) {
            this.expectedRegularCrafts = regularCrafts;
            this.expectedShiftCrafts = shiftCrafts;
            return this;
        }

        public DefaultedTestPopulator expectInput(final List<ItemStack> items) {
            this.expectedInput = items;
            return this;
        }

        @Override
        public Stream<TestContext> populate() {
            final List<ItemStack> totalInitialInventory = Stream.concat(this.base.inventory.stream(), this.base.input().stream()).toList();
            final List<ItemStack> expectedShiftInput = RecipePlaceTest.createExpectedInput(this.expectedInput, this.expectedShiftCrafts);

            final Stream<TestContext> testSingleClick = Stream.of(this.base)
                .map(context -> context
                    .name("Empty input").input(List.of())
                    .name("Total inventory").inventory(totalInitialInventory))
                .flatMap(context -> Stream.of(context, context.creative()))
                .flatMap(context -> Stream.of(
                    context.expectInput(RecipePlaceTest.createExpectedInput(this.expectedInput, 1)),
                    context.shift().expectInput(expectedShiftInput)
                ));

            // After first click we end up with the same layout no matter the initial input.
            // So we can perform multiple-click tests on a single input.
            final Stream<TestContext> testMultipleClicks = Stream.of(this.base)
                .map(context -> context
                    .name("Partial input")
                    .name("Partial inventory"))
                .flatMap(context -> Stream.of(context, context.creative()))
                .flatMap(context -> Stream.of(
                    context
                        .expectInputs(IntStream.rangeClosed(1, this.expectedRegularCrafts)
                            .mapToObj(clicks -> RecipePlaceTest.createExpectedInput(this.expectedInput, clicks))
                            .toList())
                        .expectInput(RecipePlaceTest.createExpectedInput(this.expectedInput, this.expectedRegularCrafts)),
                    context.shift().expectInputs(List.of(expectedShiftInput, expectedShiftInput))
                ));

            final Stream<TestContext> testFullInventory = Stream.of(this.base)
                .map(context -> context
                    .name("Partial input")
                    .name("Full inventory").inventory(Collections.nCopies(36, ItemStack.of(ItemTypes.BARRIER, 64))))
                .flatMap(context -> Stream.of(context, context.shift()))
                .flatMap(context -> Stream.of(
                    context.expectInput(this.base.input()),
                    context.creative().expectInput(List.of())
                ));

            return Stream.of(testSingleClick, testMultipleClicks, testFullInventory)
                .flatMap(Function.identity());
        }
    }

    private record TestContext(
        RecipeHolder<?> recipe, String testName,
        boolean shiftClick, boolean creativeMode,
        List<ItemStack> inventory, List<ItemStack> input,
        List<List<ItemStack>> expectedInputs
    ) {
        public TestContext(final String key, final Builder<? extends org.spongepowered.api.item.recipe.Recipe<?>, ?> recipe) {
            this(new RecipeHolder<>(
                net.minecraft.resources.ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath("sponge", key)),
                (Recipe<?>) recipe.build()));
        }

        public TestContext(final RecipeHolder<?> recipe) {
            this(recipe, "", false, false, List.of(), List.of(), List.of());
        }

        public TestContext name(final String testName) {
            final String newTestName = this.testName.isEmpty() ? testName : (this.testName + ", " + testName);
            return new TestContext(this.recipe, newTestName, this.shiftClick, this.creativeMode, this.inventory, this.input, this.expectedInputs);
        }

        public TestContext shift() {
            return new TestContext(this.recipe, this.testName, true, this.creativeMode, this.inventory, this.input, this.expectedInputs);
        }

        public TestContext creative() {
            return new TestContext(this.recipe, this.testName, this.shiftClick, true, this.inventory, this.input, this.expectedInputs);
        }

        public TestContext inventory(final List<ItemStack> items) {
            return new TestContext(this.recipe, this.testName, this.shiftClick, this.creativeMode, items, this.input, this.expectedInputs);
        }

        public TestContext input(final List<ItemStack> items) {
            return new TestContext(this.recipe, this.testName, this.shiftClick, this.creativeMode, this.inventory, items, this.expectedInputs);
        }

        public TestContext expectInputs(final List<List<ItemStack>> expectedInputs) {
            return new TestContext(this.recipe, this.testName, this.shiftClick, this.creativeMode, this.inventory, this.input, expectedInputs);
        }

        public TestContext expectInput(final List<ItemStack> expectedInput) {
            return this.expectInputs(Stream.concat(this.expectedInputs.stream(), Stream.of(expectedInput)).toList());
        }

        public String asTestName() {
            return String.format("Place recipe %s, %s (Shift click: %s, Creative mode: %s, Total clicks: %s)",
                this.recipe.id().location().getPath(),
                this.testName,
                this.shiftClick,
                this.creativeMode,
                this.expectedInputs.size());
        }

        // Tests

        public void testCrafting() {
            this.test(MenuType.CRAFTING, menu -> (Inventory) ((AbstractCraftingMenuAccessor) menu).accessor$craftSlots());
        }

        public void testSmelting() {
            this.test(MenuType.FURNACE, menu -> ((Inventory) ((AbstractFurnaceMenuAccessor) menu).accessor$container()).slot(0).orElseThrow());
        }

        public void test(
            final MenuType<? extends RecipeBookMenu> menuType,
            final Function<RecipeBookMenu, Inventory> inputProvider
        ) {
            this.test(player -> RecipePlaceTest.createMenu(menuType, player), inputProvider);
        }

        public void test(
            final Function<ServerPlayer, RecipeBookMenu> menuProvider,
            final Function<RecipeBookMenu, Inventory> inputProvider
        ) {
            final ServerPlayer player = new FakePlayer(SpongeCommon.server().overworld(), new GameProfile(UUID.randomUUID(), "Player"));
            final RecipeBookMenu menu = menuProvider.apply(player);

            final Inventory playerInventory = (Inventory) player.getInventory();
            final List<ItemStack> initialInventory = this.inventory();
            for (int i = 0; i < initialInventory.size(); ++i) {
                playerInventory.set(i, initialInventory.get(i));
            }

            final Inventory input = inputProvider.apply(menu);
            final List<ItemStack> initialInput = this.input();
            for (int i = 0; i < initialInput.size(); ++i) {
                input.set(i, initialInput.get(i));
            }

            for (int i = 0; i < this.expectedInputs().size(); ++i) {
                menu.handlePlacement(this.shiftClick(), this.creativeMode(), this.recipe(), player.serverLevel(), player.getInventory());

                final List<ItemStack> actualInput = input.slots().stream().map(Slot::peek).toList();
                final List<ItemStack> expectedInput = this.expectedInputs().get(i);
                final int click = i+1;
                for (int j = 0; j < actualInput.size(); ++j) {
                    final ItemStack actualStack = actualInput.get(j);
                    final ItemStack expectedStack = expectedInput.size() <= j ? ItemStack.empty() : expectedInput.get(j);
                    assertTrue(net.minecraft.world.item.ItemStack.matches(
                            ItemStackUtil.toNative(actualStack), ItemStackUtil.toNative(expectedStack)),
                        () -> String.format("""
                            Actual input doesn't match expected input after click %s
                            Test: %s,
                            Expected input:    %s
                            Actual input:      %s
                            Initial input:     %s
                            Initial inventory: %s""",
                            click,
                            this.asTestName(),
                            RecipePlaceTest.stacksToString(false, expectedInput),
                            RecipePlaceTest.stacksToString(false, actualInput),
                            RecipePlaceTest.stacksToString(false, initialInput),
                            RecipePlaceTest.stacksToString(true, initialInventory)
                        ));
                }
            }
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

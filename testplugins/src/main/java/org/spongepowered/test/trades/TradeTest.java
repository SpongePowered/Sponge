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
package org.spongepowered.test.trades;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ProfessionTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackGenerator;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.List;
import java.util.function.Supplier;

@Plugin("tradetest")
public class TradeTest implements LoadableModule {

    private final PluginContainer pluginContainer;

    @Inject
    public TradeTest(final PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    // This field refers to the display name of the villager that will sell our stuff
    static final Component FLARDIAN = Component.text("Flardian", Style.style(NamedTextColor.AQUA, TextDecoration.BOLD, TextDecoration.ITALIC));

    // This field refers to the display name of our ItemStack
    static final Component ITEM_DISPLAY = Component.text()
        .append(Component.text( "[", Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD)))
        .append(Component.text("FLARD", Style.style(NamedTextColor.GREEN, TextDecoration.ITALIC)))
        .append(Component.text("]", Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD)))
        .build();

    // Here we define the Lore we will be using for out items.
    private static final Component LORE_FIRST = TradeTest.blueText("This is indeed a glorious day!");
    private static final Component LORE_SECOND = TradeTest.blueText( "Shining sun makes the clouds flee");
    private static final Component LORE_THIRD = TradeTest.blueText("With State of ").append(TradeTest.sponge()).append(TradeTest.blueText(" again today"));
    private static final Component LORE_FOURTH = TradeTest.blueText( "Granting delights for you and me");
    private static final Component LORE_FIFTH = TradeTest.blueText("For ").append(TradeTest.sponge()).append(TradeTest.blueText(" is in a State of play"));
    private static final Component LORE_SIXTH = TradeTest.blueText("Today, be happy as can be!");
    static final ImmutableList<Component> LORE = ImmutableList.of(
        TradeTest.LORE_FIRST, TradeTest.LORE_SECOND,
        TradeTest.LORE_THIRD, TradeTest.LORE_FOURTH,
        TradeTest.LORE_FIFTH,
        TradeTest.LORE_SIXTH
    );

    private static Component blueText(final String message) {
        return TradeTest.buildComponentWithStuff(NamedTextColor.BLUE, TextDecoration.ITALIC, message);
    }
    private static Component sponge() {
        return TradeTest.buildComponentWithStuff(NamedTextColor.GOLD, TextDecoration.BOLD, "Sponge");
    }

    private static Component buildComponentWithStuff(final NamedTextColor color, final TextDecoration decoration, final String message) {
        return Component.text(message, Style.style(color, decoration));
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.pluginContainer, new SpawnListener());
    }

    public static final class SpawnListener {
        // Here are the items that we will sell and buy
        private static final List<Supplier<ItemType>> SELL_TYPES = ImmutableList.of(
            ItemTypes.SLIME_BALL, ItemTypes.BLAZE_ROD, ItemTypes.APPLE,
            ItemTypes.GHAST_TEAR, ItemTypes.COBBLESTONE, ItemTypes.STICK, ItemTypes.EMERALD);
        private static final List<Supplier<ItemType>> BUYING_TYPES = ImmutableList.of(
            ItemTypes.ACACIA_DOOR,
            ItemTypes.ACACIA_LEAVES,
            ItemTypes.BIRCH_LEAVES,
            ItemTypes.SPRUCE_LEAVES,
            ItemTypes.OAK_LEAVES,
            ItemTypes.JUNGLE_LEAVES,
            ItemTypes.DARK_OAK_LEAVES,
            ItemTypes.BOOKSHELF,
            ItemTypes.COAL,
            ItemTypes.COBBLESTONE,
            ItemTypes.ANVIL,
            ItemTypes.IRON_ORE,
            ItemTypes.APPLE,
            ItemTypes.WHEAT_SEEDS,
            ItemTypes.DIRT
        );

        private static TradeOfferGenerator generateTradeOffer() {
            final WeightedTable<ItemType> buyingItemTable = new WeightedTable<>();
            SpawnListener.BUYING_TYPES.forEach(type -> buyingItemTable.add(type.get(), 1));
            final WeightedTable<ItemType> sellingTable = new WeightedTable<>();
            SpawnListener.SELL_TYPES.forEach(type -> sellingTable.add(type.get(), 1));
            return TradeOfferGenerator.builder()
                .firstBuyingItemGenerator(ItemStackGenerator.builder()
                    .baseItem(buyingItemTable)
                    .build()
                )
                .sellingItemGenerator(ItemStackGenerator.builder()
                    .baseItem(sellingTable)
                    .add(Keys.CUSTOM_NAME, TradeTest.ITEM_DISPLAY)
                    .add(Keys.LORE, TradeTest.LORE)
                    .build()
                )
                .startingUses(VariableAmount.baseWithRandomAddition(0, 1))
                .maxUses(VariableAmount.baseWithRandomAddition(100, VariableAmount.range(5, 2400)))
                .build();

        }

        @Listener
        private void onSpawn(final SpawnEntityEvent event) {
            // Here we create the villager that will sell out stuff.
            // Sponge takes inspiration from Entity systems, where any object can have any data.
            // The data we're setting here is then represented as the key.
            // Once we have our data we then offer the data to the entity using the specified key.
            event.entities().stream()
                .filter(entity1 -> entity1.type().equals(EntityTypes.VILLAGER.get()) && Math.random() > 0.7)
                .forEach(villager -> {
                    final DataManipulator.Mutable container = DataManipulator.mutableOf()
                        .set(Keys.PROFESSION_TYPE, ProfessionTypes.CLERIC.get(villager.world()))
                        .set(Keys.CUSTOM_NAME, TradeTest.FLARDIAN)
                        .set(Keys.INVULNERABILITY_TICKS, Ticks.of(10000))
                        // We have to specify the experience and level because otherwise the villager's brains reset
                        // the job.... because they have a mind of their own...
                        .set(Keys.EXPERIENCE, 1)
                        .set(Keys.EXPERIENCE_LEVEL, 1)
                        .set(Keys.TRADE_OFFERS, ImmutableList.of(SpawnListener.generateTradeOffer().apply(villager, villager.random())));
                    villager.copyFrom(container);
                });
        }
    }

}

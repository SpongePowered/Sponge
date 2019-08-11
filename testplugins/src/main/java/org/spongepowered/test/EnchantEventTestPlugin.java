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

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.EnchantItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Plugin(id = "enchanteventtest", name = "EnchantEventTest", description = "Tests enchantment events.", version = "0.0.0")
public class EnchantEventTestPlugin implements LoadableModule {

    @Inject private PluginContainer container;

    private final EnchantmentListener listener = new EnchantmentListener();

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class EnchantmentListener {

        @Listener
        public void onLvlRequirementCalc(EnchantItemEvent.CalculateLevelRequirement event, @First Player player) {
            event.setLevelRequirement(33);
        }

        @Listener
        public void onEnchantmentCalc(EnchantItemEvent.CalculateEnchantment event, @First Player player) {
            List<Enchantment> pool = Arrays.asList(
                    Enchantment.of(EnchantmentTypes.FORTUNE, 5),
                    Enchantment.of(EnchantmentTypes.EFFICIENCY, 7),
                    Enchantment.of(EnchantmentTypes.UNBREAKING, 10));

            Enchantment.RandomListBuilder builder = Enchantment.randomListBuilder()
                    .seed(event.getSeed())
                    .option(event.getOption())
                    .level(event.getLevelRequirement())
                    .item(event.getItem().createStack());

            if (event.getItem().getType() == ItemTypes.GOLDEN_PICKAXE) {
                builder.fixedPool(pool);
            }

            event.setEnchantments(builder.build());
        }

        @Listener
        public void onEnchant(EnchantItemEvent.Post event, @First Player player) {
            for (SlotTransaction trans : event.getTransactions()) {
                if (event.getEnchantingSlot() == trans.getSlot()) {
                    ItemStackSnapshot withLore = trans.getDefault()
                            .with(Keys.ITEM_LORE, Collections.singletonList(
                                    Text.of("Enchanted by ", player.getName()))).get();
                    trans.setCustom(withLore);
                }
            }
        }
    }
}

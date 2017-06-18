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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

@Plugin(id = "enchantmenttest", name = "Enchantment Test", description = "Tests Sponge's simple enchantment API.")
public final class EnchantmentTest {

    @Listener
    public void onGameInitialization(final GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                Command.builder()
                        .parameter(Parameter.catalogedElement(EnchantmentType.class).onlyOne().setKey("enchantment").build())
                        .parameter(Parameter.integerNumber().onlyOne().setKey("level").build())
                        .setTargetedExecutorErrorMessage(Text.of(TextColors.RED, "You must be a player to use this command!"))
                        .targetedExecutor((cause, player, args) -> {
                            final ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty());
                            if (!itemStack.supports(Keys.ITEM_ENCHANTMENTS)) {
                                throw new CommandException(Text.of(TextColors.RED, "This item does not support item enchantments."));
                            }
                            final EnchantmentType type = args.<EnchantmentType>getOne("enchantment").orElse(EnchantmentTypes.BINDING_CURSE);
                            final int level = args.<Integer>getOne("level").orElse(1);
                            final Enchantment newEnchantment = Enchantment.builder()
                                    .type(type)
                                    .level(level)
                                    .build();
                            final List<Enchantment> enchantments = itemStack.get(Keys.ITEM_ENCHANTMENTS).orElse(new ArrayList<>());
                            enchantments.add(newEnchantment);
                            itemStack.offer(Keys.ITEM_ENCHANTMENTS, enchantments);
                            player.setItemInHand(HandTypes.MAIN_HAND, itemStack);
                            player.sendMessage(Text.of(TextColors.GOLD, "You have successfully added the enchantment " + type.getName() + " with a level of " + level + "."));
                            return CommandResult.success();
                        }, Player.class)
                        .build(),
                "spongeenchant");
        Sponge.getCommandManager().register(this,
                Command.builder()
                        .setTargetedExecutorErrorMessage(Text.of(TextColors.RED, "You must be a player to use this command!"))
                        .targetedExecutor((cause, player, args) -> {
                            final ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.empty());
                            if (!itemStack.supports(Keys.ITEM_ENCHANTMENTS)) {
                                throw new CommandException(Text.of(TextColors.RED, "This item does not support item enchantments."));
                            }
                            final List<Enchantment> enchantments = itemStack.get(Keys.ITEM_ENCHANTMENTS).orElse(ImmutableList.of());
                            if (enchantments.isEmpty()) {
                                player.sendMessage(Text.of(TextColors.RED, "This item has no enchantments!"));
                            }
                            enchantments.forEach(enchantment -> {
                                final EnchantmentType type = enchantment.getType();
                                player.sendMessage(Text.of(TextColors.GOLD, "============================="));
                                player.sendMessage(Text.of(TextColors.GOLD, "Type: ", TextColors.GRAY, type.getName()));
                                player.sendMessage(Text.of(TextColors.GOLD, "Type ID: ", TextColors.GRAY, type.getId()));
                                player.sendMessage(Text.of(TextColors.GOLD, "Translation: ", TextColors.GRAY, enchantment.getType().getTranslation()));
                                player.sendMessage(Text.of(TextColors.GOLD, "Level: ", TextColors.GRAY, enchantment.getLevel()));
                                player.sendMessage(Text.of(TextColors.GOLD, "Maximum level: ", TextColors.GRAY, type.getMaximumLevel()));
                                player.sendMessage(Text.of(TextColors.GOLD, "Minimum level: ", TextColors.GRAY, type.getMinimumLevel()));
                                player.sendMessage(Text.of(TextColors.GOLD, "Weight: ", TextColors.GRAY, type.getWeight()));
                                player.sendMessage(Text.of(TextColors.GOLD, "Curse: ", TextColors.GRAY, type.isCurse()));
                                player.sendMessage(Text.of(TextColors.GOLD, "Treasure: ", TextColors.GRAY, type.isTreasure()));
                            });
                            return CommandResult.success();
                        }, Player.class)
                        .build(),
                "spongeenchantinfo");
    }

}

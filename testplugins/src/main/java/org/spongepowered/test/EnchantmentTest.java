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
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

@Plugin(id = "enchantmenttest", name = "Enchantment Test", description = "Tests Sponge's simple enchantment API.", version = "0.0.0")
public final class EnchantmentTest {

    @Inject private PluginContainer container;

    @Listener
    public void onGameInitialization(final GameInitializationEvent event) {
        Parameter.Key<EnchantmentType> keyEnch = Parameter.key("enchantment", EnchantmentType.class);
        Parameter.Key<Integer> keyLevel = Parameter.key("level", Integer.class);

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .parameters(
                                Parameter.catalogedElement(EnchantmentType.class).setKey(keyEnch).build(), // TODO onlyone?
                                Parameter.integerNumber().setKey(keyLevel).build())                        // TODO onlyone?
                        .setExecutor((ctx) -> {
                            if (!(ctx.getSubject() instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command!"));
                            }
                            final Player player = (Player) ctx.getSubject();
                            final ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND.get());
                            if (!itemStack.supports(Keys.APPLIED_ENCHANTMENTS)) {
                                throw new CommandException(Text.of(TextColors.RED, "This item does not support item enchantments."));
                            }
                            final EnchantmentType type = ctx.getOne(keyEnch).orElse(EnchantmentTypes.BINDING_CURSE.get());
                            final int level = ctx.getOne(keyLevel).orElse(1);
                            final Enchantment newEnchantment = Enchantment.builder()
                                    .type(type)
                                    .level(level)
                                    .build();
                            final List<Enchantment> enchantments = itemStack.get(Keys.APPLIED_ENCHANTMENTS).orElse(new ArrayList<>());
                            enchantments.add(newEnchantment);
                            itemStack.offer(Keys.APPLIED_ENCHANTMENTS, enchantments);
                            player.setItemInHand(HandTypes.MAIN_HAND.get(), itemStack);
                            player.sendMessage(Text.of(TextColors.GOLD, "You have successfully added the enchantment " + type.getKey() + " with a level of " + level + "."));
                            return CommandResult.success();
                        })
                        .build(),
                "spongeenchant");
        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .setExecutor((ctx) -> {
                            if (!(ctx.getSubject() instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command!"));
                            }
                            final Player player = (Player) ctx.getSubject();
                            final ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND.get());
                            if (!itemStack.supports(Keys.APPLIED_ENCHANTMENTS)) {
                                throw new CommandException(Text.of(TextColors.RED, "This item does not support item enchantments."));
                            }
                            final List<Enchantment> enchantments = itemStack.get(Keys.APPLIED_ENCHANTMENTS).orElse(ImmutableList.of());
                            if (enchantments.isEmpty()) {
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.RED, "This item has no enchantments!"));
                            }
                            enchantments.forEach(enchantment -> {
                                final EnchantmentType type = enchantment.getType();
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "============================="));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Type: ", TextColors.GRAY, type.getTranslation()));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Type ID: ", TextColors.GRAY, type.getKey()));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Translation: ", TextColors.GRAY, enchantment.getType().getTranslation()));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Level: ", TextColors.GRAY, enchantment.getLevel()));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Maximum level: ", TextColors.GRAY, type.getMaximumLevel()));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Minimum level: ", TextColors.GRAY, type.getMinimumLevel()));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Weight: ", TextColors.GRAY, type.getWeight()));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Curse: ", TextColors.GRAY, type.isCurse()));
                                ctx.getMessageReceiver().sendMessage(Text.of(TextColors.GOLD, "Treasure: ", TextColors.GRAY, type.isTreasure()));
                            });
                            return CommandResult.success();
                        })
                        .build(),
                "spongeenchantinfo");
    }

}

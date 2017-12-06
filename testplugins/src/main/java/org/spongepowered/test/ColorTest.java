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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;

import java.util.Optional;

@Plugin(id = "color_test", name = "Color Test", description = "Use /color info to show color or dye color info. Use /color test to change an items color.")
public class ColorTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            Optional<ItemStack> optItem = getHandItem(src);
                            if (optItem.isPresent()) {
                                String id = optItem.get().getType().getId();
                                if (optItem.get().supports(Keys.COLOR)) {
                                    Color color = optItem.get().get(Keys.COLOR).get();
                                    src.sendMessage(Text.of(id, "'s color is ", String.format("0x%06X", color.getRgb()), " ", color.toVector3i()));
                                } else if (optItem.get().supports(Keys.DYE_COLOR)) {
                                    DyeColor dyeColor = optItem.get().get(Keys.DYE_COLOR).get();
                                    src.sendMessage(Text.of(id, "'s dye color is ", dyeColor.getId(), " (", String.format("0x%06X", dyeColor.getColor().getRgb()), "),", dyeColor.getColor().toVector3i()));
                                } else {
                                    src.sendMessage(Text.of(id, " does not have a color or dye color."));
                                }
                                return CommandResult.success();
                            }
                            return CommandResult.empty();
                        })
                        .build(), "info")
                .child(CommandSpec.builder()
                        .executor(((src, args) -> {
                            Optional<ItemStack> optItem = getHandItem(src);
                            if (optItem.isPresent()) {
                                String id = optItem.get().getType().getId();
                                if (optItem.get().supports(Keys.COLOR)) {
                                    Color color = optItem.get().get(Keys.COLOR).get();
                                    src.sendMessage(Text.of(id, "'s old color is ", String.format("0x%06X", color.getRgb()), " ", color.toVector3i()));
                                    color = Color.of((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                                    src.sendMessage(Text.of(id, "'s new color is ", String.format("0x%06X", color.getRgb()), " ", color.toVector3i()));
                                    optItem.get().offer(Keys.COLOR, color);
                                    ((Player) src).setItemInHand(HandTypes.MAIN_HAND, optItem.get());
                                    return CommandResult.success();
                                } else {
                                    src.sendMessage(Text.of(id, " does not have a color."));
                                }
                            }
                            return CommandResult.empty();
                        }))
                        .build(), "test")
                .build(), "color");
    }

    private Optional<ItemStack> getHandItem(CommandSource src) {
        Optional<ItemStack> optItem = Optional.empty();
        if (src instanceof Player) {
            optItem = ((Player) src).getItemInHand(HandTypes.MAIN_HAND);
            if (!optItem.isPresent()) {
                src.sendMessage(Text.of("You must be holding an item in your main hand."));
            }
        } else {
            src.sendMessage(Text.of("Only a player may use this command."));
        }
        return optItem;
    }

}

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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.InstrumentProperty;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
@Plugin(id = "playersimulatedtest", name = "PlayerSimulatedTest", description = "Tests Fake Player methods.", version = "0.0.0")
public class PlayerSimulatedTest {

    private static boolean enabled = false;

    @Listener
    public void onInitialization(final GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .arguments()
                        .executor((src, args) -> {
                            enabled = !enabled;

                            if (enabled) {
                                src.sendMessage(Text.of(TextColors.DARK_GREEN,
                                        "You have enabled fake-player digging test.\n" +
                                        "give yourself a diamond pickaxe, with or without silktouch, and right click" +
                                        "with a different item in your main hand. The pick should take damage, and " +
                                        "the blocks should break instantly, like the pick broke it, with appropriate" +
                                        "causes."));
                            } else {
                                src.sendMessage(Text.of(TextColors.DARK_GREEN, "You have disabled fake-player digging test."));
                            }

                            return CommandResult.success();
                        })
                        .build(),
                "playersimulatedtest");
    }

    @Listener
    public void onUseItem(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
        if (!enabled) {
            return;
        }
        final BlockSnapshot snapshot = event.getTargetBlock();

        Inventory firstSlot = player.getInventory().query(ItemTypes.DIAMOND_PICKAXE).first();
        Optional<ItemStack> first = firstSlot.peek();
        if(!first.isPresent()) return;
        player.getWorld().digBlockWith(snapshot.getPosition(), first.get(), player.getProfile());
        firstSlot.set(first.get());
    }
}

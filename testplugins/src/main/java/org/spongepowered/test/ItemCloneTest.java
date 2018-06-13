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
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

@Plugin(id = "itemtest", name = "ItemTest", version = "1.0", description = "tests item stack serialization and deserialization")
public class ItemCloneTest {
    private static String ITEM_STRING = null;

    @Listener
    public void init(GameInitializationEvent e) {
        registerCommands();
    }

    private void registerCommands() {
        CommandSpec save = CommandSpec.builder()
            .executor((commandSource, commandContext) -> {
                Player player = (Player) commandSource;

                if (!player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                    player.sendMessages(Text.of(TextColors.RED, "You must be holding an item in hand to perform the data clone test"));
                    return CommandResult.empty();
                }

                final ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND).get();
                final ItemStackSnapshot snapshot = item.createSnapshot();
                String json = null;

                final DataContainer data = snapshot.toContainer();
                try {
                    json = DataFormats.JSON.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ITEM_STRING = json;
                System.out.println("saved: " + ITEM_STRING);

                return CommandResult.success();
            }).build();

        CommandSpec check = CommandSpec.builder()
            .executor((commandSource, commandContext) -> {
                Player player = (Player) commandSource;
                final DataContainer container;

                try {
                    container = DataFormats.JSON.read(ITEM_STRING);
                } catch (IOException e) {
                    e.printStackTrace();
                    return CommandResult.empty();
                }

                final ItemStackSnapshot snapshot = container.getSerializable(DataQuery.of(), ItemStackSnapshot.class)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize an ItemStackSnapshot"));

                final ItemStack stack = snapshot.createStack();
                if (player.getInventory().contains(stack)) {
                    player.sendMessage(Text.of("Your inventory contains the stored item"));
                } else {
                    player.sendMessage(Text.of("Your inventory does not contain the stored item"));
                }

                return CommandResult.success();
            }).build();

        Sponge.getCommandManager().register(this, save, "cssave");
        Sponge.getCommandManager().register(this, check, "cscheck");
    }

}
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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(id = "book_pages_test", name = "Book pages test", description = "Tests reading and writing books.", version = "1.0")
public class BookPagesTest {

    @Inject PluginContainer container;
    private static Parameter.Value<String> paramText  = Parameter.remainingRawJoinedStrings().setKey("text").build();

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this.container,
                Command.builder()
                    .setExecutor(this::readBook)
                    .build(), "readbook");

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .parameters(paramText)
                        .setExecutor(this::writeBook)
                        .build(), "writebook");
    }

    private CommandResult readBook(CommandContext context) throws CommandException {
        if (!(context.getSubject() instanceof Player)) {
            throw new CommandException(Text.of("Must be a player!"));
        }

        Player player = (Player) context.getSubject();

        // get the item in hand if it is a book
        ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND.get());
        if (item.getType() != ItemTypes.WRITABLE_BOOK.get() && item.getType() != ItemTypes.WRITABLE_BOOK.get()) {
            throw new CommandException(Text.of("Must be holding a book!"));
        }

        List<Text> text;
        Text type = Text.of(item.getTranslation());
        if (item.getType() == ItemTypes.WRITTEN_BOOK.get()) {
            // get the standard text
            text = item.get(Keys.BOOK_PAGES).orElseGet(ArrayList::new);
        } else {
            // get the string as texts, no formatting
            text = item.get(Keys.PLAIN_BOOK_PAGES).map(x -> x.stream().<Text>map(Text::of).collect(Collectors.toList())).orElseGet(ArrayList::new);
        }

        Sponge.getServiceManager().provideUnchecked(PaginationService.class).builder().title(type).contents(text).sendTo(context.getMessageReceiver());
        return CommandResult.success();
    }

    private CommandResult writeBook(CommandContext context) throws CommandException {
        if (!(context.getSubject() instanceof Player)) {
            throw new CommandException(Text.of("Must be a player!"));
        }

        Player player = (Player) context.getSubject();

        // get the item in hand if it is a book
        ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND.get());
        if (item.getType() != ItemTypes.WRITABLE_BOOK.get() && item.getType() != ItemTypes.WRITABLE_BOOK.get()) {
            throw new CommandException(Text.of("Must be holding a book!"));
        }

        String toSet = context.requireOne(paramText);
        DataTransactionResult result;
        if (item.getType() == ItemTypes.WRITTEN_BOOK.get()) {
            // then we need the text representation. We'll assume that we have ampersand encoded for ease of testing.
            result = item.offer(Keys.BOOK_PAGES, Collections.singletonList(TextSerializers.FORMATTING_CODE.deserialize(toSet)));
        } else { // writable
            result = item.offer(Keys.PLAIN_BOOK_PAGES, Collections.singletonList(toSet));
        }

        if (result.isSuccessful()) {
            player.sendMessage(Text.of(TextColors.GREEN, "Successfully set the book contents."));
            return CommandResult.success();
        }

        throw new CommandException(Text.of("Unable to set the book contents."));
    }

}

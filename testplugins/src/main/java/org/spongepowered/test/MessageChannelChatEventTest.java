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
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "msg-channel-event-test", name = "Message Channel Chat Event Test", description = MessageChannelChatEventTest.DESCRIPTION, version = "0.0.0")
public class MessageChannelChatEventTest {

    public static final String DESCRIPTION = "Use /msgchatevent to toggle altering the chat of the sender";
    private static final Text PARAMETER = Text.of("state");

    private boolean enabled = false;

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                    .arguments(GenericArguments.bool(PARAMETER))
                    .executor((source, context) -> {
                        this.enabled = context.requireOne(PARAMETER);
                        source.sendMessage(Text.of("Altering chat: ", this.enabled));
                        return CommandResult.success();
                    }).build(),
                "spongemsgchatevent");
    }

    @Listener
    public void onMessageChat(MessageChannelEvent.Chat event, @Root CommandSource source) {
        if (this.enabled) {
            MessageEvent.MessageFormatter formatters = event.getFormatter();
            formatters.setHeader(Text.of("[", source.getName(), "]: "));
            formatters.setFooter(Text.of(" - Sponge"));
        }
    }

}

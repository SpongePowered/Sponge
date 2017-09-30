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
package org.spongepowered.common.text.chat;

import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

public final class ChatUtil {

    private ChatUtil() {
    }

    public static void sendMessage(ITextComponent component, MessageChannel channel, CommandSource source, boolean isChat) {
        Text raw = SpongeTexts.toText(component);
        MessageFormatter formatter = new MessageEvent.MessageFormatter(raw);
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        Cause cause = isMainThread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), source);
        MessageChannelEvent event;
        if (isChat) {
            event = SpongeEventFactory.createMessageChannelEventChat(cause, channel, Optional.of(channel), formatter, raw, false);
        } else {
            event = SpongeEventFactory.createMessageChannelEvent(cause, channel, Optional.of(channel), formatter, false);
        }
        if (!SpongeImpl.postEvent(event) && !event.isMessageCancelled() && event.getChannel().isPresent()) {
            event.getChannel().get().send(source, event.getMessage(), isChat ? ChatTypes.CHAT : ChatTypes.SYSTEM);
        }
    }

}

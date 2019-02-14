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
package org.spongepowered.common.relocate.co.aikar.timings;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.source.RemoteSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TimingsReportListener {
    private final List<MessageChannel> channels;
    private final Runnable onDone;
    private String timingsURL;
    private MessageChannel combinedChannel;

    public TimingsReportListener(CommandSource channels) {
        this(channels, null);
    }
    public TimingsReportListener(CommandSource sender, Runnable onDone) {
        this(Lists.newArrayList(MessageChannel.fixed(sender)), onDone);
    }
    public TimingsReportListener(List<MessageChannel> channels) {
        this(channels, null);
    }
    public TimingsReportListener(List<MessageChannel> channels, Runnable onDone) {
        Validate.notNull(channels);
        Validate.notEmpty(channels);

        this.channels = Lists.newArrayList(channels);
        this.addConsoleIfNeeded(this.channels);
        this.onDone = onDone;
        this.combinedChannel = MessageChannel.combined(this.channels);
    }

    public String getTimingsURL() {
        return timingsURL;
    }

    public void done() {
        done(null);
    }

    public void done(String url) {
        this.timingsURL = url;
        if (onDone != null) {
            onDone.run();
        }
    }

    public void addConsoleIfNeeded(List<MessageChannel> channels) {
        boolean hasConsole = false;
        for (MessageChannel channel: channels) {
            for (MessageReceiver receiver: channel.getMembers()) {
                if (receiver instanceof ConsoleSource) {
                    hasConsole = true;
                    break;
                }
            }
        }

        if (!hasConsole) {
            channels.add(MessageChannel.TO_CONSOLE);
        }
    }

    public void send(Text text) {
        this.combinedChannel.send(text);
    }

    public MessageChannel getChannel() {
        return this.combinedChannel;
    }
}

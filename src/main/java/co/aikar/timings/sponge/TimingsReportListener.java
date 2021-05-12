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
package co.aikar.timings.sponge;

import com.google.common.collect.Lists;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.Server;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.common.server.ServerConsoleSystemSubject;

import java.util.List;

public final class TimingsReportListener {
    private final List<Audience> channels;
    private final Runnable onDone;
    private String timingsURL;
    private ForwardingAudience audience;

    public TimingsReportListener(Audience sender, Runnable onDone) {
        this(Lists.newArrayList(sender), onDone);
    }
    public TimingsReportListener(List<Audience> channels) {
        this(channels, null);
    }
    public TimingsReportListener(List<Audience> channels, Runnable onDone) {
        Validate.notNull(channels);
        Validate.notEmpty(channels);

        this.channels = Lists.newArrayList(channels);
        this.addConsoleIfNeeded(this.channels);
        this.onDone = onDone;
        this.audience = Audience.audience(this.channels);
    }

    public String getTimingsURL() {
        return this.timingsURL;
    }

    public void done() {
        this.done(null);
    }

    public void done(String url) {
        this.timingsURL = url;
        if (this.onDone != null) {
            this.onDone.run();
        }
    }

    public void addConsoleIfNeeded(List<Audience> channels) {
        boolean hasConsole = false;
        for (Audience channel: channels) {
            if (channel instanceof ServerConsoleSystemSubject) {
                hasConsole = true;
                break;
            }
        }

        if (!hasConsole) {
            channels.add(Audiences.system());
        }
    }

    public void send(Component text) {
        this.audience.sendMessage(Identity.nil(), text);
    }

    public ForwardingAudience getChannel() {
        return this.audience;
    }
}

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
package org.spongepowered.vanilla.applaunch.util;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A simple appender that will capture output
 */
public class CaptureAppender extends AbstractAppender {
    private final Queue<String> messages = new ConcurrentLinkedQueue<>();

    public static CaptureAppender.Builder builder() {
        return new Builder();
    }

    protected CaptureAppender(
        final String name,
        final Filter filter,
        final Layout<? extends Serializable> layout,
        final boolean ignoreExceptions,
        final Property[] properties
    ) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(final LogEvent event) {
        this.messages.add(event.getMessage().getFormattedMessage());
    }

    public Queue<String> messages() {
        return this.messages;
    }

    public static class Builder extends AbstractAppender.Builder<CaptureAppender.Builder> {

        Builder() {
        }

        public CaptureAppender build() {
            final Layout<? extends Serializable> layout = this.getOrCreateLayout();
            return new CaptureAppender(this.getName(), this.getFilter(), layout, this.isIgnoreExceptions(), this.getPropertyArray());
        }

    }
}

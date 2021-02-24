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
package org.spongepowered.vanilla.chat;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.async.AsyncLoggerContext;
import org.apache.logging.log4j.message.MessageFactory;

import java.net.URI;

/**
 * A custom logger context that overrides the message factory for loggers
 * created within the ModLauncher class loader.
 */
public class VanillaLoggerContext extends AsyncLoggerContext {

    public VanillaLoggerContext(final String name, final Object externalContext, final URI configLocn) {
        super(name, externalContext, configLocn);
    }

    // Override these methods to inject our own message factory when none is provided.

    @Override
    public Logger getLogger(final String name, final MessageFactory messageFactory) {
        return super.getLogger(name, messageFactory == null ? ReusableComponentMessageFactory.INSTANCE : messageFactory);
    }

    @Override
    public boolean hasLogger(final String name) {
        return this.hasLogger(name, (MessageFactory) null);
    }

    @Override
    public boolean hasLogger(final String name, final MessageFactory messageFactory) {
        return super.hasLogger(name, messageFactory == null ? ReusableComponentMessageFactory.INSTANCE : messageFactory);
    }

    @Override
    public boolean hasLogger(final String name, final Class<? extends MessageFactory> messageFactoryClass) {
        return super.hasLogger(name, messageFactoryClass == null ? ReusableComponentMessageFactory.class : messageFactoryClass);
    }
}

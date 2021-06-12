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

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory2;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.apache.logging.log4j.message.ReusableObjectMessage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.adventure.NativeComponentRenderer;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Locale;

/**
 * Message factory for log4j that will process chat components.
 */
public class ReusableComponentMessageFactory implements MessageFactory2 {
    private static final ThreadLocal<ComponentMessage> componentMessageThreadLocal = new ThreadLocal<>();
    private final ReusableMessageFactory backing = new ReusableMessageFactory();

    public static final ReusableComponentMessageFactory INSTANCE = new ReusableComponentMessageFactory();

    private ReusableComponentMessageFactory() {
    }

    private static ComponentMessage getComponentMessage() {
        @Nullable ComponentMessage message = ReusableComponentMessageFactory.componentMessageThreadLocal.get();
        if (message == null) {
            ReusableComponentMessageFactory.componentMessageThreadLocal.set((message = new ComponentMessage()));
        }
        return message;
    }


    @Override
    public Message newMessage(Object message) {
        final ComponentMessage result = ReusableComponentMessageFactory.getComponentMessage();
        result.set(message);
        return result;
    }

    @Override
    public Message newMessage(String message) {
        return this.backing.newMessage(message);
    }

    @Override
    public Message newMessage(String message, Object... params) {
        return this.backing.newMessage(message, params);
    }

    @Override
    public Message newMessage(CharSequence charSequence) {
        return this.backing.newMessage(charSequence);
    }

    @Override
    public Message newMessage(String message, Object p0) {
        return this.backing.newMessage(message, p0);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1) {
        return this.backing.newMessage(message, p0, p1);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2) {
        return this.backing.newMessage(message, p0, p1, p2);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3) {
        return this.backing.newMessage(message, p0, p1, p2, p3);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return this.backing.newMessage(message, p0, p1, p2, p3, p4);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return this.backing.newMessage(message, p0, p1, p2, p3, p4, p5);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return this.backing.newMessage(message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return this.backing.newMessage(message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return this.backing.newMessage(message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return this.backing.newMessage(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    static class ComponentMessage extends ReusableObjectMessage {
        private static final long serialVersionUID = -3477272587468239708L;
        private static final ComponentRenderer<Locale> RENDERER = GlobalTranslator.renderer();

        @Override
        public String getFormattedMessage() {
            final Object param = this.getParameter();
            // TODO: ansi formatting
            if (param instanceof ComponentLike) {
                return PlainTextComponentSerializer.plainText().serialize(
                    ComponentMessage.RENDERER.render(((ComponentLike) param).asComponent(), Locale.getDefault())
                );
            } else if (param instanceof net.minecraft.network.chat.Component) {
                return NativeComponentRenderer.apply((Component) param, Locale.getDefault()).getString();
            }
            return super.getFormattedMessage();
        }

        @Override
        public void formatTo(final StringBuilder buffer) {
            final Object param = this.getParameter();
            // TODO: ansi formatting
            if (param instanceof ComponentLike) {
                SpongeAdventure.flattener().flatten(
                    ComponentMessage.RENDERER.render(((ComponentLike) param).asComponent(), Locale.getDefault()),
                    buffer::append
                );
            } else if (param instanceof net.minecraft.network.chat.Component) {
                buffer.append(NativeComponentRenderer.apply((Component) param, Locale.getDefault()).getString());
            } else {
                super.formatTo(buffer);
            }
        }
    }
}

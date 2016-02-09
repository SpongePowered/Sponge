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
package org.spongepowered.common.registry.type.text;

import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.common.registry.type.ImmutableCatalogRegistryModule;
import org.spongepowered.common.text.chat.SpongeChatType;

import java.util.function.BiConsumer;

public final class ChatTypeRegistryModule extends ImmutableCatalogRegistryModule<ChatType> {

    public static ChatTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private ChatTypeRegistryModule() {
    }

    @Override
    protected void collect(BiConsumer<String, ChatType> consumer) {
        add(consumer, new SpongeChatType((byte) 0, "chat"), "chat");
        add(consumer, new SpongeChatType((byte) 1, "system"), "system");
        add(consumer, new SpongeChatType((byte) 2, "action_bar"), "action_bar");
    }

    private static final class Holder {

        private static final ChatTypeRegistryModule INSTANCE = new ChatTypeRegistryModule();

    }

}

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
package org.spongepowered.common.adventure;

import net.kyori.adventure.text.format.Style;
import net.minecraft.network.chat.ChatTypeDecoration;
import org.spongepowered.api.adventure.ChatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SpongeChatTypeBuilder implements ChatType.Builder {

    private String translationKey;
    private net.minecraft.network.chat.Style style;
    private List<ChatTypeDecoration.Parameter> parameters;

    private ChatTypeDecoration narration;

    public SpongeChatTypeBuilder() {
        this.reset();
    }

    @Override
    public ChatType.Builder from(final ChatType value) {
        net.minecraft.network.chat.ChatType chatType = (net.minecraft.network.chat.ChatType) (Object) value;
        this.translationKey = chatType.chat().translationKey();
        this.parameters = chatType.chat().parameters();
        this.style = chatType.chat().style();
        this.narration = chatType.narration();
        return this;
    }

    @Override
    public ChatType.Builder translationKey(final String translationKey) {
        this.translationKey = translationKey;
        return this;
    }

    @Override
    public ChatType.Builder style(final Style style) {
        this.style = SpongeAdventure.asVanilla(style);
        return this;
    }

    private void addParameter(final ChatTypeDecoration.Parameter parameter) {
        if (this.parameters.contains(parameter)) {
            // TODO check
            //   throw new IllegalStateException("Parameter already exists");
        }
        this.parameters.add(parameter);
    }

    @Override
    public ChatType.Builder addSender() {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.addParameter(ChatTypeDecoration.Parameter.SENDER);
        return this;
    }

    @Override
    public ChatType.Builder addContent() {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.addParameter(ChatTypeDecoration.Parameter.CONTENT);
        return this;
    }

    @Override
    public ChatType.Builder addTarget() {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.addParameter(ChatTypeDecoration.Parameter.TARGET);
        return this;
    }

    @Override
    public ChatType.Builder reset() {
        this.translationKey = null;
        this.style = net.minecraft.network.chat.Style.EMPTY;
        this.parameters = null;
        this.narration = ChatTypeDecoration.withSender("chat.type.text.narrate");
        return this;
    }

    @Override
    public ChatType build() {
        Objects.requireNonNull(this.translationKey, "name");
        Objects.requireNonNull(this.parameters, "parameter");
        Objects.requireNonNull(this.style, "style");
        return (ChatType) (Object) new net.minecraft.network.chat.ChatType(new ChatTypeDecoration(this.translationKey, this.parameters, this.style), this.narration);
    }
}

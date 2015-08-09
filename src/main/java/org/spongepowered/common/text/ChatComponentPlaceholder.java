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

package org.spongepowered.common.text;

import net.minecraft.util.IChatComponent;

import java.util.List;

import net.minecraft.util.ChatComponentText;

public class ChatComponentPlaceholder extends ChatComponentText {

    private final String placeholderKey;

    public ChatComponentPlaceholder(String placeholderKey) {
        this(placeholderKey, "{" + placeholderKey + "}");
    }

    public ChatComponentPlaceholder(String placeholderKey, String msg) {
        super(msg);
        this.placeholderKey = placeholderKey;
    }

    public String getPlaceholderKey() {
        return this.placeholderKey;
    }

    public ChatComponentPlaceholder createCopy() {
        ChatComponentPlaceholder copy = new ChatComponentPlaceholder(placeholderKey, super.getChatComponentText_TextValue());
        copy.setChatStyle(this.getChatStyle().createShallowCopy());
        @SuppressWarnings("unchecked")
        List<IChatComponent> siblings = this.getSiblings();
        for (IChatComponent sibling : siblings) {
            copy.appendSibling(sibling.createCopy());
        }
        return copy;
    }

}

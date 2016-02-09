
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

import com.google.common.base.Objects;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.transformer.Transformer;
import org.spongepowered.api.text.transformer.Transformers;
import org.spongepowered.api.text.transformer.ValueForKeyTransformer;

import java.util.List;

public class ChatComponentPlaceholder extends ChatComponentStyle {

    private final Transformer<?> transformer;
    private IChatComponent fallback;

    public ChatComponentPlaceholder(String key) {
        this(Transformers.key(key));
    }

    public ChatComponentPlaceholder(Transformer<?> contextAwareTransformer) {
        this(contextAwareTransformer, null);
    }

    public ChatComponentPlaceholder(Transformer<?> contextAwareTransformer, IChatComponent fallback) {
        this.transformer = contextAwareTransformer;
        this.fallback = fallback;
    }

    public Transformer<?> getTransformer() {
        return this.transformer;
    }

    public String getTransformerKey() {
        if (this.transformer instanceof ValueForKeyTransformer) {
            return ((ValueForKeyTransformer<?>) this.transformer).getKey();
        } else {
            return this.transformer.toString();
        }
    }

    @Override
    public String getUnformattedTextForChat() {
        return this.fallback == null ? "{" + getTransformerKey() + "}" : this.fallback.getUnformattedTextForChat();
    }

    public IChatComponent getFallback() {
        return this.fallback;
    }

    public void setFallback(IChatComponent fallback) {
        this.fallback = fallback;
    }

    @Override
    public ChatComponentPlaceholder createCopy() {
        ChatComponentPlaceholder copy =
                new ChatComponentPlaceholder(this.transformer, this.fallback == null ? null : this.fallback.createCopy());
        copy.setChatStyle(this.getChatStyle().createShallowCopy());
        @SuppressWarnings("unchecked")
        List<IChatComponent> siblings = this.getSiblings();
        for (IChatComponent sibling : siblings) {
            copy.appendSibling(sibling.createCopy());
        }
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof ChatComponentPlaceholder)) {
            return false;
        } else {
            ChatComponentPlaceholder other = (ChatComponentPlaceholder) obj;
            return this.transformer.equals(other.transformer) && super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("transformer", this.transformer)
                .add("fallback", this.fallback)
                .add("siblings", this.siblings)
                .add("style", getChatStyle())
                .toString();
    }

}

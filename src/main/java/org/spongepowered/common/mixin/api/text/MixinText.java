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
package org.spongepowered.common.mixin.api.text;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.text.impl.LiteralTextImpl;

@Mixin(value = Text.class, remap = false)
public interface MixinText {

    /**
     * @author kashike
     */
    @Overwrite
    @SuppressWarnings("deprecation")
    static Text of() {
        return LiteralTextImpl.EMPTY;
    }

    /**
     * @author kashike
     */
    @Overwrite
    @SuppressWarnings("deprecation")
    static Text empty() {
        return LiteralTextImpl.EMPTY;
    }

    /**
     * @author kashike
     */
    @Overwrite
    @SuppressWarnings("deprecation")
    static Text newLine() {
        return LiteralTextImpl.NEW_LINE;
    }

    /**
     * @author kashike
     */
    @Overwrite
    @SuppressWarnings("deprecation")
    static LiteralText of(String content) {
        checkNotNull(content, "content");
        if (content.isEmpty()) {
            return LiteralTextImpl.EMPTY;
        } else if (content.equals(LiteralTextImpl.NEW_LINE_STRING)) {
            return LiteralTextImpl.NEW_LINE;
        }
        return Text.builder(content).build();
    }

    /**
     * @author kashike
     */
    @Overwrite
    @SuppressWarnings("deprecation")
    static LiteralText of(char content) {
        if (content == LiteralTextImpl.NEW_LINE_CHAR) {
            return LiteralTextImpl.NEW_LINE;
        }
        return Text.builder(String.valueOf(content)).build();
    }
}

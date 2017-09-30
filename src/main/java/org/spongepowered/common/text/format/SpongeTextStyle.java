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
package org.spongepowered.common.text.format;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CaseFormat;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Locale;

import javax.annotation.Nullable;

@NonnullByDefault
public class SpongeTextStyle extends TextStyle.Base {

    private final TextFormatting handle;

    private SpongeTextStyle(TextFormatting handle, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underline,
            @Nullable Boolean strikethrough, @Nullable Boolean obfuscated) {
        super(bold, italic, underline, strikethrough, obfuscated);
        this.handle = checkNotNull(handle, "handle");
    }

    @Override
    public String getName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.handle.name());
    }

    @Override
    public String getId() {
        return "minecraft:" + this.handle.name().toLowerCase(Locale.ENGLISH);
    }

    public static SpongeTextStyle of(TextFormatting handle) {
        if (handle == TextFormatting.RESET) {
            return new SpongeTextStyle(TextFormatting.RESET, false, false, false, false, false);
        }

        return new SpongeTextStyle(handle,
                equalsOrNull(handle, TextFormatting.BOLD),
                equalsOrNull(handle, TextFormatting.ITALIC),
                equalsOrNull(handle, TextFormatting.UNDERLINE),
                equalsOrNull(handle, TextFormatting.STRIKETHROUGH),
                equalsOrNull(handle, TextFormatting.OBFUSCATED)
        );
    }

    @Nullable
    private static Boolean equalsOrNull(TextFormatting handle, TextFormatting check) {
        return handle == check ? true : null;
    }

}

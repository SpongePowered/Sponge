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

import com.google.common.base.MoreObjects;
import net.minecraft.util.text.TextFormatting;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.util.OptBool;

import java.util.Objects;
import java.util.Optional;

public class SpongeTextStyle implements TextStyle {
    
    @Nullable private Boolean bold, italic, underline, strikethrough, obfuscated;

    protected SpongeTextStyle(TextFormatting handle) {
        this(handle == TextFormatting.BOLD ? true : null, handle == TextFormatting.ITALIC ? true : null, handle == TextFormatting.UNDERLINE ? true
                : null, handle == TextFormatting.STRIKETHROUGH ? true : null, handle == TextFormatting.OBFUSCATED ? true : null);
    }

    private SpongeTextStyle(@Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underline,
        @Nullable Boolean strikethrough, @Nullable Boolean obfuscated) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return this.bold == null && this.italic == null && this.underline == null && this.strikethrough == null && this.obfuscated == null;
    }

    @Override
    public TextStyle bold(@Nullable Boolean bold) {
        return new SpongeTextStyle(
            bold,
            this.italic,
            this.underline,
            this.strikethrough,
            this.obfuscated
        );
    }

    @Override
    public TextStyle italic(@Nullable Boolean italic) {
        return new SpongeTextStyle(
            this.bold,
            italic,
            this.underline,
            this.strikethrough,
            this.obfuscated
        );
    }

    @Override
    public TextStyle underline(@Nullable Boolean underline) {
        return new SpongeTextStyle(
            this.bold,
            this.italic,
            underline,
            this.strikethrough,
            this.obfuscated
        );
    }

    @Override
    public TextStyle strikethrough(@Nullable Boolean strikethrough) {
        return new SpongeTextStyle(
            this.bold,
            this.italic,
            this.underline,
            strikethrough,
            this.obfuscated
        );
    }

    @Override
    public TextStyle obfuscated(@Nullable Boolean obfuscated) {
        return new SpongeTextStyle(
            this.bold,
            this.italic,
            this.underline,
            this.strikethrough,
            obfuscated
        );
    }

    @Override
    public Optional<Boolean> hasBold() {
        return OptBool.of(this.bold);
    }

    @Override
    public Optional<Boolean> hasItalic() {
        return OptBool.of(this.italic);
    }

    @Override
    public Optional<Boolean> hasUnderline() {
        return OptBool.of(this.underline);
    }

    @Override
    public Optional<Boolean> hasStrikethrough() {
        return OptBool.of(this.strikethrough);
    }

    @Override
    public Optional<Boolean> hasObfuscated() {
        return OptBool.of(this.obfuscated);
    }

    @Override
    public boolean contains(TextStyle... styles) {
        for (TextStyle style : checkNotNull(styles, "styles")) {
            checkNotNull(style, "style");
            
            final SpongeTextStyle implStyle = (SpongeTextStyle) style;
            
            if (!containsOrOverrides(this.bold, implStyle.bold)
                || !containsOrOverrides(this.italic, implStyle.italic)
                || !containsOrOverrides(this.underline, implStyle.underline)
                || !containsOrOverrides(this.strikethrough, implStyle.strikethrough)
                || !containsOrOverrides(this.obfuscated, implStyle.obfuscated)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public TextStyle negate() {
        return new SpongeTextStyle(
            negate(this.bold),
            negate(this.italic),
            negate(this.underline),
            negate(this.strikethrough),
            negate(this.obfuscated)
        );
    }

    @Override
    public TextStyle and(TextStyle... styles) {
        return composeStyle(styles, false);
    }

    @Override
    public TextStyle andNot(TextStyle... styles) {
        return composeStyle(styles, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpongeTextStyle)) {
            return false;
        }

        final SpongeTextStyle that = (SpongeTextStyle) o;

        return (this.bold != null && this.bold.equals(that.bold))
            && (this.italic != null && this.italic.equals(that.italic))
            && (this.underline != null && this.underline.equals(that.underline))
            && (this.obfuscated != null && this.obfuscated.equals(that.obfuscated))
            && (this.strikethrough != null && this.strikethrough.equals(that.strikethrough));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bold, this.italic, this.underline, this.obfuscated, this.strikethrough);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SpongeTextStyle.class)
            .omitNullValues()
            .add("bold", this.bold)
            .add("italic", this.italic)
            .add("underline", this.underline)
            .add("strikethrough", this.strikethrough)
            .add("obfuscated", this.obfuscated)
            .toString();
    }

    private boolean containsOrOverrides(Boolean leftProperty, Boolean rightProperty) {
        return rightProperty == null || leftProperty == rightProperty;
    }

    @Nullable
    private Boolean negate(@Nullable Boolean property) {
        if (property != null) {
            return !property;
        }

        return null;
    }

    @Nullable
    private Boolean composeStyle(@Nullable Boolean leftProperty, @Nullable Boolean rightProperty) {
        if (leftProperty == null) {
            return rightProperty;
        } else if (rightProperty == null) {
            return leftProperty;
        } else if (leftProperty != rightProperty) {
            return null;
        } else {
            return leftProperty;
        }
    }

    private TextStyle composeStyle(TextStyle[] styles, boolean negate) {
        checkNotNull(styles, "styles");
        if (styles.length == 0) {
            return this;
        } else if (this.isEmpty() && styles.length == 1) {
            TextStyle style = checkNotNull(styles[0], "style");
            return negate ? style.negate() : style;
        }

        @Nullable Boolean boldAcc = this.bold;
        @Nullable Boolean italicAcc = this.italic;
        @Nullable Boolean underlineAcc = this.underline;
        @Nullable Boolean strikethroughAcc = this.strikethrough;
        @Nullable Boolean obfuscatedAcc = this.obfuscated;

        if (negate) {
            for (TextStyle style : styles) {
                checkNotNull(style, "style");
                final SpongeTextStyle implStyle = (SpongeTextStyle) style;
                boldAcc = composeStyle(boldAcc, negate(implStyle.bold));
                italicAcc = composeStyle(italicAcc, negate(implStyle.italic));
                underlineAcc = composeStyle(underlineAcc, negate(implStyle.underline));
                strikethroughAcc = composeStyle(strikethroughAcc, negate(implStyle.strikethrough));
                obfuscatedAcc = composeStyle(obfuscatedAcc, negate(implStyle.obfuscated));
            }
        } else {
            for (TextStyle style : styles) {
                checkNotNull(style, "style");
                final SpongeTextStyle implStyle = (SpongeTextStyle) style;
                boldAcc = composeStyle(boldAcc, implStyle.bold);
                italicAcc = composeStyle(italicAcc, implStyle.italic);
                underlineAcc = composeStyle(underlineAcc, implStyle.underline);
                strikethroughAcc = composeStyle(strikethroughAcc, implStyle.strikethrough);
                obfuscatedAcc = composeStyle(obfuscatedAcc, implStyle.obfuscated);
            }
        }

        return new SpongeTextStyle(
            boldAcc,
            italicAcc,
            underlineAcc,
            strikethroughAcc,
            obfuscatedAcc
        );
    }
}

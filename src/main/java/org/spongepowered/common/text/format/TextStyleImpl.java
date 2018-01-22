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
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.util.OptBool;

import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nullable;

public class TextStyleImpl implements TextStyle {

    /**
     * Whether text where this style is applied is bolded.
     */
    protected final Optional<Boolean> bold;
    /**
     * Whether text where this style is applied is italicized.
     */
    protected final Optional<Boolean> italic;
    /**
     * Whether text where this style is applied is underlined.
     */
    protected final Optional<Boolean> underline;
    /**
     * Whether text where this style is applied has a strikethrough.
     */
    protected final Optional<Boolean> strikethrough;
    /**
     * Whether text where this style is applied is obfuscated.
     */
    protected final Optional<Boolean> obfuscated;

    public TextStyleImpl(@Nullable Boolean bold,
            @Nullable Boolean italic,
            @Nullable Boolean underline,
            @Nullable Boolean strikethrough,
            @Nullable Boolean obfuscated) {
        this(
                OptBool.of(bold),
                OptBool.of(italic),
                OptBool.of(underline),
                OptBool.of(strikethrough),
                OptBool.of(obfuscated)
        );
    }

    private TextStyleImpl(Optional<Boolean> bold,
            Optional<Boolean> italic,
            Optional<Boolean> underline,
            Optional<Boolean> strikethrough,
            Optional<Boolean> obfuscated) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.obfuscated = obfuscated;
        this.strikethrough = strikethrough;
    }

    @Override
    public boolean isComposite() {
        // Return true by default as the TextStyle class is composite by default
        return true;
    }

    @Override
    public boolean isEmpty() {
        return !(this.bold.isPresent()
                || this.italic.isPresent()
                || this.underline.isPresent()
                || this.strikethrough.isPresent()
                || this.obfuscated.isPresent());
    }

    @Override
    public TextStyle bold(@Nullable Boolean bold) {
        return new TextStyleImpl(
                OptBool.of(bold),
                this.italic,
                this.underline,
                this.strikethrough,
                this.obfuscated
        );
    }

    @Override
    public TextStyle italic(@Nullable Boolean italic) {
        return new TextStyleImpl(
                this.bold,
                OptBool.of(italic),
                this.underline,
                this.strikethrough,
                this.obfuscated
        );
    }

    @Override
    public TextStyle underline(@Nullable Boolean underline) {
        return new TextStyleImpl(
                this.bold,
                this.italic,
                OptBool.of(underline),
                this.strikethrough,
                this.obfuscated
        );
    }

    @Override
    public TextStyle strikethrough(@Nullable Boolean strikethrough) {
        return new TextStyleImpl(
                this.bold,
                this.italic,
                this.underline,
                OptBool.of(strikethrough),
                this.obfuscated
        );
    }

    @Override
    public TextStyle obfuscated(@Nullable Boolean obfuscated) {
        return new TextStyleImpl(
                this.bold,
                this.italic,
                this.underline,
                this.strikethrough,
                OptBool.of(obfuscated)
        );
    }

    @Override
    public Optional<Boolean> isBold() {
        return this.bold;
    }

    @Override
    public Optional<Boolean> isItalic() {
        return this.italic;
    }

    @Override
    public Optional<Boolean> hasUnderline() {
        return this.underline;
    }

    @Override
    public Optional<Boolean> hasStrikethrough() {
        return this.strikethrough;
    }

    @Override
    public Optional<Boolean> isObfuscated() {
        return this.obfuscated;
    }

    @Override
    public boolean contains(TextStyle... styles) {
        for (TextStyle style : checkNotNull(styles, "styles")) {
            checkNotNull(style, "style");
            if (!propContains(this.bold, style.isBold())
                    || !propContains(this.italic, style.isItalic())
                    || !propContains(this.underline, style.hasUnderline())
                    || !propContains(this.strikethrough, style.hasStrikethrough())
                    || !propContains(this.obfuscated, style.isObfuscated())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public TextStyle negate() {
        // Do a negation of each property
        return new TextStyleImpl(
                propNegate(this.bold),
                propNegate(this.italic),
                propNegate(this.underline),
                propNegate(this.strikethrough),
                propNegate(this.obfuscated)
        );
    }

    @Override
    public TextStyle and(TextStyle... styles) {
        return compose(styles, false);
    }

    @Override
    public TextStyle andNot(TextStyle... styles) {
        return compose(styles, true);
    }

    private TextStyle compose(TextStyle[] styles, boolean negate) {
        checkNotNull(styles, "styles");
        if (styles.length == 0) {
            return this;
        } else if (this.isEmpty() && styles.length == 1) {
            TextStyle style = checkNotNull(styles[0], "style");
            return negate ? style.negate() : style;
        }

        Optional<Boolean> boldAcc = this.bold;
        Optional<Boolean> italicAcc = this.italic;
        Optional<Boolean> underlineAcc = this.underline;
        Optional<Boolean> strikethroughAcc = this.strikethrough;
        Optional<Boolean> obfuscatedAcc = this.obfuscated;

        if (negate) {
            for (TextStyle style : styles) {
                checkNotNull(style, "style");
                boldAcc = propCompose(boldAcc, propNegate(style.isBold()));
                italicAcc = propCompose(italicAcc, propNegate(style.isItalic()));
                underlineAcc = propCompose(underlineAcc, propNegate(style.hasUnderline()));
                strikethroughAcc = propCompose(strikethroughAcc, propNegate(style.hasStrikethrough()));
                obfuscatedAcc = propCompose(obfuscatedAcc, propNegate(style.isObfuscated()));
            }
        } else {
            for (TextStyle style : styles) {
                checkNotNull(style, "style");
                boldAcc = propCompose(boldAcc, style.isBold());
                italicAcc = propCompose(italicAcc, style.isItalic());
                underlineAcc = propCompose(underlineAcc, style.hasUnderline());
                strikethroughAcc = propCompose(strikethroughAcc, style.hasStrikethrough());
                obfuscatedAcc = propCompose(obfuscatedAcc, style.isObfuscated());
            }
        }

        return new TextStyleImpl(
                boldAcc,
                italicAcc,
                underlineAcc,
                strikethroughAcc,
                obfuscatedAcc
        );
    }

    @Override
    public void applyTo(Text.Builder builder) {
        builder.style(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TextStyleImpl)) {
            return false;
        }

        TextStyleImpl that = (TextStyleImpl) o;
        return this.bold.equals(that.bold)
                && this.italic.equals(that.italic)
                && this.underline.equals(that.underline)
                && this.obfuscated.equals(that.obfuscated)
                && this.strikethrough.equals(that.strikethrough);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.bold, this.italic, this.underline, this.obfuscated, this.strikethrough);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(TextStyle.class)
                .omitNullValues()
                .add("bold", this.bold.orElse(null))
                .add("italic", this.italic.orElse(null))
                .add("underline", this.underline.orElse(null))
                .add("strikethrough", this.strikethrough.orElse(null))
                .add("obfuscated", this.obfuscated.orElse(null))
                .toString();
    }

    /**
     * Utility method to check if the given "super-property" contains the given
     * "sub-property".
     *
     * @param superprop The super property
     * @param subprop The sub property
     * @return True if the property is contained, otherwise false
     */
    private static boolean propContains(Optional<Boolean> superprop, Optional<Boolean> subprop) {
        return !subprop.isPresent() || superprop.equals(subprop);
    }

    /**
     * Utility method to negate a property if it is not null.
     *
     * @param prop The property to negate
     * @return The negated property, or {@link Optional#empty()}
     */
    private static Optional<Boolean> propNegate(Optional<Boolean> prop) {
        if (prop.isPresent()) {
            return OptBool.of(!prop.get());
        }
        return OptBool.ABSENT;
    }

    /**
     * Utility method to perform a compose operation between two properties.
     *
     * @param prop1 The first property
     * @param prop2 The second property
     * @return The composition of the two properties
     */
    private static Optional<Boolean> propCompose(Optional<Boolean> prop1, Optional<Boolean> prop2) {
        if (!prop1.isPresent()) {
            return prop2;
        } else if (!prop2.isPresent()) {
            return prop1;
        } else if (!prop1.equals(prop2)) {
            return OptBool.ABSENT;
        } else {
            return prop1;
        }
    }

    public static final class Real extends TextStyleImpl implements TextStyle.Base {

        private final TextFormatting handle;

        protected Real(TextFormatting handle, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underline,
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

        public static Real of(final TextFormatting real) {
            if (real == TextFormatting.RESET) {
                return new Real(TextFormatting.RESET, false, false, false, false, false);
            }

            return new Real(real,
                    equalsOrNull(real, TextFormatting.BOLD),
                    equalsOrNull(real, TextFormatting.ITALIC),
                    equalsOrNull(real, TextFormatting.UNDERLINE),
                    equalsOrNull(real, TextFormatting.STRIKETHROUGH),
                    equalsOrNull(real, TextFormatting.OBFUSCATED)
            );
        }

        @Nullable
        private static Boolean equalsOrNull(TextFormatting handle, TextFormatting check) {
            return handle == check ? true : null;
        }
    }

    public static final class None extends TextStyleImpl implements TextStyle.Base {

        public None() {
            super((Boolean) null, null, null, null, null);
        }

        @Override
        public String getId() {
            return "NONE"; // TODO
        }

        @Override
        public String getName() {
            return "NONE"; // TODO
        }
    }
}

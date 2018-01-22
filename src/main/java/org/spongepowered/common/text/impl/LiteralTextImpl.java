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
package org.spongepowered.common.text.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import javax.annotation.Nullable;

public final class LiteralTextImpl extends TextImpl implements LiteralText {
    public static final char NEW_LINE_CHAR = '\n';
    public static final String NEW_LINE_STRING = "\n";

    /**
     * The empty, unformatted {@link Text} instance.
     */
    public static final LiteralText EMPTY = new LiteralTextImpl("");
    /**
     * An unformatted {@link Text} that will start a new line (if supported).
     */
    public static final LiteralText NEW_LINE = new LiteralTextImpl(NEW_LINE_STRING);

    final String content;

    LiteralTextImpl(final String content) {
        this.content = checkNotNull(content, "content");
    }

    LiteralTextImpl(final TextFormat format, final ImmutableList<Text> children, @Nullable final ClickAction<?> clickAction,
            @Nullable final HoverAction<?> hoverAction, @Nullable final ShiftClickAction<?> shiftClickAction, final String content) {
        super(format, children, clickAction, hoverAction, shiftClickAction);
        this.content = checkNotNull(content, "content");
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    protected ITextComponent createComponent() {
        return new TextComponentString(this.content);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SelectorTextImpl) || !super.equals(o)) {
            return false;
        }

        final LiteralTextImpl that = (LiteralTextImpl) o;
        return this.content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.content);
    }

    @Override
    MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .addValue(this.content);
    }

    public static final class Builder extends AbstractBuilder implements LiteralText.Builder {

        private String content;

        public Builder() {
            this("");
        }

        Builder(final String content) {
            this.content(content);
        }

        Builder(final Text text) {
            super(text);
        }

        Builder(final LiteralText text) {
            super(text);
            this.content = text.getContent();
        }

        @Override
        public final String getContent() {
            return this.content;
        }

        @Override
        public Builder content(final String content) {
            this.content = checkNotNull(content, "content");
            return this;
        }

        @Override
        public LiteralText build() {
            // Special case for empty builder
            if (this.format.isEmpty() && this.children.isEmpty() && this.clickAction == null && this.hoverAction == null
                    && this.shiftClickAction == null) {
                if (this.content.isEmpty()) {
                    return EMPTY;
                } else if (this.content.equals(NEW_LINE_STRING)) {
                    return NEW_LINE;
                }
            }

            return new LiteralTextImpl(
                    this.format,
                    ImmutableList.copyOf(this.children),
                    this.clickAction,
                    this.hoverAction,
                    this.shiftClickAction,
                    this.content);
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Builder) || !super.equals(o)) {
                return false;
            }

            final Builder that = (Builder) o;
            return Objects.equals(this.content, that.content);

        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.content);
        }

        @Override
        MoreObjects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .addValue(this.content);
        }

        @Override
        public Builder format(final TextFormat format) {
            return (Builder) super.format(format);
        }

        @Override
        public Builder color(final TextColor color) {
            return (Builder) super.color(color);
        }

        @Override
        public Builder style(final TextStyle... styles) {
            return (Builder) super.style(styles);
        }

        @Override
        public Builder onClick(@Nullable final ClickAction<?> clickAction) {
            return (Builder) super.onClick(clickAction);
        }

        @Override
        public Builder onHover(@Nullable final HoverAction<?> hoverAction) {
            return (Builder) super.onHover(hoverAction);
        }

        @Override
        public Builder onShiftClick(@Nullable final ShiftClickAction<?> shiftClickAction) {
            return (Builder) super.onShiftClick(shiftClickAction);
        }

        @Override
        public Builder append(final Text... children) {
            return (Builder) super.append(children);
        }

        @Override
        public Builder append(final Collection<? extends Text> children) {
            return (Builder) super.append(children);
        }

        @Override
        public Builder append(final Iterable<? extends Text> children) {
            return (Builder) super.append(children);
        }

        @Override
        public Builder append(final Iterator<? extends Text> children) {
            return (Builder) super.append(children);
        }

        @Override
        public Builder insert(final int pos, final Text... children) {
            return (Builder) super.insert(pos, children);
        }

        @Override
        public Builder insert(final int pos, final Collection<? extends Text> children) {
            return (Builder) super.insert(pos, children);
        }

        @Override
        public Builder insert(final int pos, final Iterable<? extends Text> children) {
            return (Builder) super.insert(pos, children);
        }

        @Override
        public Builder insert(final int pos, final Iterator<? extends Text> children) {
            return (Builder) super.insert(pos, children);
        }

        @Override
        public Builder remove(final Text... children) {
            return (Builder) super.remove(children);
        }

        @Override
        public Builder remove(final Collection<? extends Text> children) {
            return (Builder) super.remove(children);
        }

        @Override
        public Builder remove(final Iterable<? extends Text> children) {
            return (Builder) super.remove(children);
        }

        @Override
        public Builder remove(final Iterator<? extends Text> children) {
            return (Builder) super.remove(children);
        }

        @Override
        public Builder removeAll() {
            return (Builder) super.removeAll();
        }

        @Override
        public Builder from(final Text value) {
            return new Builder(value);
        }

        @Override
        public Text.Builder reset() {
            return new Builder();
        }
    }
}

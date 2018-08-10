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
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.translation.Translation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import javax.annotation.Nullable;

public final class TranslatableTextImpl extends TextImpl implements TranslatableText {

    final Translation translation;
    final ImmutableList<Object> arguments;

    public TranslatableTextImpl(final Translation translation, final ImmutableList<Object> arguments) {
        this.translation = checkNotNull(translation, "translation");
        this.arguments = checkNotNull(arguments, "arguments");
    }

    TranslatableTextImpl(final TextFormat format, final ImmutableList<Text> children, @Nullable final ClickAction<?> clickAction,
            @Nullable final HoverAction<?> hoverAction, @Nullable final ShiftClickAction<?> shiftClickAction, final Translation translation,
            final ImmutableList<Object> arguments) {
        super(format, children, clickAction, hoverAction, shiftClickAction);
        this.translation = checkNotNull(translation, "translation");
        this.arguments = checkNotNull(arguments, "arguments");
    }

    @Override
    public Translation getTranslation() {
        return this.translation;
    }

    @Override
    public ImmutableList<Object> getArguments() {
        return this.arguments;
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    protected ITextComponent createComponent() {
        return new TextComponentTranslation(this.translation.getId(), unwrapArguments(this.arguments));
    }

    private static Object[] unwrapArguments(final ImmutableList<Object> args) {
        final Object[] result = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            final Object arg = args.get(i);
            if (arg instanceof TextImpl) {
                result[i] = ((TextImpl) arg).asComponentCopy();
            } else {
                result[i] = arg;
            }
        }
        return result;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TranslatableTextImpl) || !super.equals(o)) {
            return false;
        }

        final TranslatableTextImpl that = (TranslatableTextImpl) o;
        return this.translation.equals(that.translation)
                && this.arguments.equals(that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.translation, this.arguments);
    }

    @Override
    MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .addValue(this.translation)
                .add("arguments", this.arguments);
    }

    public static final class Builder extends TextImpl.AbstractBuilder implements TranslatableText.Builder {

        private Translation translation;
        private ImmutableList<Object> arguments;

        public Builder() {
        }

        Builder(final Text text) {
            super(text);
        }

        Builder(final TranslatableText text) {
            super(text);
            this.translation = text.getTranslation();
            this.arguments = text.getArguments();
        }

        @Override
        public final Translation getTranslation() {
            return this.translation;
        }

        @Override
        public final ImmutableList<Object> getArguments() {
            return this.arguments;
        }

        @Override
        public Builder translation(final Translation translation, final Object... args) {
            this.translation = checkNotNull(translation, "translation");
            this.arguments = ImmutableList.copyOf(checkNotNull(args, "args"));
            return this;
        }

        @Override
        public Builder translation(final org.spongepowered.api.text.translation.Translatable translatable, final Object... args) {
            return this.translation(checkNotNull(translatable, "translatable").getTranslation(), args);
        }

        @Override
        public TranslatableText build() {
            return new TranslatableTextImpl(
                    this.format,
                    ImmutableList.copyOf(this.children),
                    this.clickAction,
                    this.hoverAction,
                    this.shiftClickAction,
                    this.translation,
                    this.arguments);
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
            return Objects.equals(this.translation, that.translation)
                    && Objects.equals(this.arguments, that.arguments);

        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.translation, this.arguments);
        }

        @Override
        MoreObjects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .addValue(this.translation)
                    .add("arguments", this.arguments);
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
        public TranslatableText.Builder from(final Text value) {
            return new Builder(value);
        }

        @Override
        public Builder reset() {
            return new Builder();
        }
    }
}

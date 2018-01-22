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
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.text.ScoreText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import javax.annotation.Nullable;

public final class ScoreTextImpl extends TextImpl implements ScoreText {

    final Score score;
    final Optional<String> override;

    ScoreTextImpl(final Score score) {
        this.score = checkNotNull(score, "score");
        this.override = Optional.empty();
    }

    ScoreTextImpl(final TextFormat format, final ImmutableList<Text> children, @Nullable final ClickAction<?> clickAction,
            @Nullable final HoverAction<?> hoverAction, @Nullable final ShiftClickAction<?> shiftClickAction,
            final Score score, @Nullable final String override) {
        super(format, children, clickAction, hoverAction, shiftClickAction);
        this.score = checkNotNull(score, "score");
        this.override = Optional.ofNullable(override);
    }

    @Override
    public Score getScore() {
        return this.score;
    }

    @Override
    public Optional<String> getOverride() {
        return this.override;
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    // TODO
    @Override
    protected ITextComponent createComponent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScoreTextImpl) || !super.equals(o)) {
            return false;
        }

        final ScoreTextImpl that = (ScoreTextImpl) o;
        return this.score.equals(that.score) && this.override.equals(that.override);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), this.score, this.override);
    }

    @Override
    MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .addValue(this.score)
                .add("override", this.override.orElse(null));
    }

    public static final class Builder extends TextImpl.AbstractBuilder implements ScoreText.Builder {

        private Score score;
        @Nullable private String override;

        public Builder() {
        }

        Builder(final Text text) {
            super(text);
        }

        Builder(final ScoreText text) {
            super(text);
            this.score = text.getScore();
            this.override = text.getOverride().orElse(null);
        }

        @Override
        public final Score getScore() {
            return this.score;
        }

        @Override
        public ScoreText.Builder score(final Score score) {
            this.score = checkNotNull(score, "score");
            return this;
        }

        @Override
        public final Optional<String> getOverride() {
            return Optional.ofNullable(this.override);
        }

        @Override
        public ScoreText.Builder override(@Nullable final String override) {
            this.override = override;
            return this;
        }

        @Override
        public ScoreText build() {
            return new ScoreTextImpl(
                    this.format,
                    ImmutableList.copyOf(this.children),
                    this.clickAction,
                    this.hoverAction,
                    this.shiftClickAction,
                    this.score,
                    this.override);
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
            return Objects.equal(this.score, that.score)
                    && Objects.equal(this.override, that.override);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), this.score, this.override);
        }

        @Override
        MoreObjects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .addValue(this.score)
                    .add("override", this.override);
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
        public Builder reset() {
            return new Builder();
        }
    }
}

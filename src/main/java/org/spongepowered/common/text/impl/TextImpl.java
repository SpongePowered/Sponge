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
import com.google.common.collect.Iterators;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.interfaces.text.IMixinTextComponent;
import org.spongepowered.common.text.action.ClickTextActionImpl;
import org.spongepowered.common.text.action.HoverTextActionImpl;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import javax.annotation.Nullable;

public abstract class TextImpl implements Text {

    /**
     * A {@link Comparator} for texts that compares the plain text of two text
     * instances.
     */
    public static Comparator<Text> PLAIN_COMPARATOR = Comparator.comparing(Text::toPlain);

    final TextFormat format;
    final ImmutableList<Text> children;
    final Optional<ClickAction<?>> clickAction;
    final Optional<HoverAction<?>> hoverAction;
    final Optional<ShiftClickAction<?>> shiftClickAction;
    /**
     * An {@link Iterable} providing an {@link Iterator} over this {@link Text}
     * as well as all children text and their children.
     */
    final Iterable<Text> childrenIterable;

    @Nullable private ITextComponent component;
    @Nullable private String json;

    TextImpl() {
        this.format = TextFormat.of(); // TODO
        this.children = ImmutableList.of();
        this.clickAction = Optional.empty();
        this.hoverAction = Optional.empty();
        this.shiftClickAction = Optional.empty();
        this.childrenIterable = () -> Iterators.singletonIterator(this);
    }

    TextImpl(final TextFormat format, final ImmutableList<Text> children, @Nullable final ClickAction<?> clickAction,
            @Nullable final HoverAction<?> hoverAction, @Nullable final ShiftClickAction<?> shiftClickAction) {
        this.format = checkNotNull(format, "format");
        this.children = checkNotNull(children, "children");
        this.clickAction = Optional.ofNullable(clickAction);
        this.hoverAction = Optional.ofNullable(hoverAction);
        this.shiftClickAction = Optional.ofNullable(shiftClickAction);
        this.childrenIterable = () -> new TextIterator(this);
    }

    @Override
    public final TextFormat getFormat() {
        return this.format;
    }

    @Override
    public final TextColor getColor() {
        return this.format.getColor();
    }

    @Override
    public final TextStyle getStyle() {
        return this.format.getStyle();
    }

    @Override
    public final ImmutableList<Text> getChildren() {
        return this.children;
    }

    @Override
    public final Iterable<Text> withChildren() {
        return this.childrenIterable;
    }

    @Override
    public final Optional<ClickAction<?>> getClickAction() {
        return this.clickAction;
    }

    @Override
    public final Optional<HoverAction<?>> getHoverAction() {
        return this.hoverAction;
    }

    @Override
    public final Optional<ShiftClickAction<?>> getShiftClickAction() {
        return this.shiftClickAction;
    }

    @Override
    public final boolean isEmpty() {
        return this == LiteralTextImpl.EMPTY;
    }

    @Override
    public abstract Builder toBuilder();

    @Override
    public final String toPlain() {
        return TextSerializers.PLAIN.serialize(this);
    }

    @Override
    public final String toPlainSingle() {
        return TextSerializers.PLAIN.serializeSingle(this);
    }

    @Override
    public final Text concat(final Text other) {
        return this.toBuilder().append(other).build();
    }

    @Override
    public final Text trim() {
        return this.toBuilder().trim().build();
    }

    protected abstract ITextComponent createComponent();

    private ITextComponent asComponent() {
        if (this.component == null) {
            this.component = this.createComponent();

            final Style style = this.component.getStyle();

            if (this.format.getColor() != TextColors.NONE) {
                style.setColor(((SpongeTextColor) this.format.getColor()).getHandle());
            }

            if (!this.format.getStyle().isEmpty()) {
                style.setBold(this.format.getStyle().isBold().orElse(null));
                style.setItalic(this.format.getStyle().isItalic().orElse(null));
                style.setUnderlined(this.format.getStyle().hasUnderline().orElse(null));
                style.setStrikethrough(this.format.getStyle().hasStrikethrough().orElse(null));
                style.setObfuscated(this.format.getStyle().isObfuscated().orElse(null));
            }

            this.clickAction.ifPresent(action -> style.setClickEvent(((ClickTextActionImpl) action).asEvent()));
            this.hoverAction.ifPresent(action -> style.setHoverEvent(((HoverTextActionImpl) action).asEvent()));
            this.shiftClickAction.ifPresent(action -> style.setInsertion(((ShiftClickAction.InsertText) action).getResult()));

            for (final Text child : this.children) {
                this.component.appendSibling(((TextImpl) child).asComponentCopy());
            }
        }

        return this.component;
    }

    // Mutable instances are not nice :(
    public ITextComponent asComponentCopy() {
        return this.asComponent().createCopy();
    }

    public String toJson() {
        if (this.json == null) {
            this.json = ITextComponent.Serializer.componentToJson(this.asComponent());
        }
        return this.json;
    }

    public String toLegacy(final char code) {
        return ((IMixinTextComponent) this.asComponent()).toLegacy(code);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.getContentVersion())
                .set(Queries.JSON, TextSerializers.JSON.serialize(this));
    }

    @Override
    public int compareTo(final Text o) {
        return PLAIN_COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TextImpl)) {
            return false;
        }

        final TextImpl that = (TextImpl) o;
        return this.format.equals(that.format)
                && this.children.equals(that.children)
                && this.clickAction.equals(that.clickAction)
                && this.hoverAction.equals(that.hoverAction)
                && this.shiftClickAction.equals(that.shiftClickAction);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.format, this.children, this.clickAction, this.hoverAction, this.shiftClickAction);
    }

    MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("format", this.format.isEmpty() ? null : this.format)
                .add("children", this.children.isEmpty() ? null : this.children)
                .add("clickAction", this.clickAction.orElse(null))
                .add("hoverAction", this.hoverAction.orElse(null))
                .add("shiftClickAction", this.shiftClickAction.orElse(null));
    }

    @Override
    public final String toString() {
        return this.toStringHelper().toString();
    }

    @Override
    public final Text toText() {
        return this;
    }

    public abstract static class AbstractBuilder implements Text.Builder {

        TextFormat format = TextFormat.of();
        List<Text> children = new ArrayList<>();
        @Nullable ClickAction<?> clickAction;
        @Nullable HoverAction<?> hoverAction;
        @Nullable ShiftClickAction<?> shiftClickAction;

        /**
         * Constructs a new empty {@link Text.Builder}.
         */
        AbstractBuilder() {
        }

        /**
         * Constructs a new {@link Text.Builder} with the properties of the given
         * {@link Text} as initial values.
         *
         * @param text The text to copy the values from
         */
        AbstractBuilder(final Text text) {
            this.format = text.getFormat();
            this.children = new ArrayList<>(text.getChildren());
            this.clickAction = text.getClickAction().orElse(null);
            this.hoverAction = text.getHoverAction().orElse(null);
            this.shiftClickAction = text.getShiftClickAction().orElse(null);
        }

        @Override
        public final TextFormat getFormat() {
            return this.format;
        }

        @Override
        public Text.Builder format(final TextFormat format) {
            this.format = checkNotNull(format, "format");
            return this;
        }

        @Override
        public final TextColor getColor() {
            return this.format.getColor();
        }

        @Override
        public Builder color(final TextColor color) {
            this.format = this.format.color(color);
            return this;
        }

        @Override
        public final TextStyle getStyle() {
            return this.format.getStyle();
        }

        @Override
        // TODO: Make sure this is the correct behaviour
        public Builder style(final TextStyle... styles) {
            this.format = this.format.style(this.format.getStyle().and(styles));
            return this;
        }

        @Override
        public final Optional<ClickAction<?>> getClickAction() {
            return Optional.ofNullable(this.clickAction);
        }

        @Override
        public Builder onClick(@Nullable final ClickAction<?> clickAction) {
            this.clickAction = clickAction;
            return this;
        }

        @Override
        public final Optional<HoverAction<?>> getHoverAction() {
            return Optional.ofNullable(this.hoverAction);
        }

        @Override
        public Builder onHover(@Nullable final HoverAction<?> hoverAction) {
            this.hoverAction = hoverAction;
            return this;
        }

        @Override
        public final Optional<ShiftClickAction<?>> getShiftClickAction() {
            return Optional.ofNullable(this.shiftClickAction);
        }

        @Override
        public Builder onShiftClick(@Nullable final ShiftClickAction<?> shiftClickAction) {
            this.shiftClickAction = shiftClickAction;
            return this;
        }

        @Override
        public final List<Text> getChildren() {
            return Collections.unmodifiableList(this.children);
        }

        @Override
        public Builder append(final Text... children) {
            Collections.addAll(this.children, children);
            return this;
        }

        @Override
        public Builder append(final Collection<? extends Text> children) {
            this.children.addAll(children);
            return this;
        }

        @Override
        public Builder append(final Iterable<? extends Text> children) {
            for (final Text child : children) {
                this.children.add(child);
            }
            return this;
        }

        @Override
        public Builder append(final Iterator<? extends Text> children) {
            while (children.hasNext()) {
                this.children.add(children.next());
            }
            return this;
        }

        @Override
        public Builder insert(final int pos, final Text... children) {
            this.children.addAll(pos, Arrays.asList(children));
            return this;
        }

        @Override
        public Builder insert(final int pos, final Collection<? extends Text> children) {
            this.children.addAll(pos, children);
            return this;
        }

        @Override
        public Builder insert(int pos, final Iterable<? extends Text> children) {
            for (final Text child : children) {
                this.children.add(pos++, child);
            }
            return this;
        }

        @Override
        public Builder insert(int pos, final Iterator<? extends Text> children) {
            while (children.hasNext()) {
                this.children.add(pos++, children.next());
            }
            return this;
        }

        @Override
        public Builder remove(final Text... children) {
            this.children.removeAll(Arrays.asList(children));
            return this;
        }

        @Override
        public Builder remove(final Collection<? extends Text> children) {
            this.children.removeAll(children);
            return this;
        }

        @Override
        public Builder remove(final Iterable<? extends Text> children) {
            for (final Text child : children) {
                this.children.remove(child);
            }
            return this;
        }

        @Override
        public Builder remove(final Iterator<? extends Text> children) {
            while (children.hasNext()) {
                this.children.remove(children.next());
            }
            return this;
        }

        @Override
        public Builder removeAll() {
            this.children.clear();
            return this;
        }

        @Override
        public Builder trim() {
            final Iterator<Text> front = this.children.iterator();
            while (front.hasNext()) {
                if (front.next().isEmpty()) {
                    front.remove();
                } else {
                    break;
                }
            }
            final ListIterator<Text> back = this.children.listIterator(this.children.size());
            while (back.hasPrevious()) {
                if (back.previous().isEmpty()) {
                    back.remove();
                } else {
                    break;
                }
            }
            return this;
        }

        @Override
        public abstract Text build();

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AbstractBuilder)) {
                return false;
            }

            final AbstractBuilder that = (AbstractBuilder) o;
            return Objects.equal(this.format, that.format)
                    && Objects.equal(this.clickAction, that.clickAction)
                    && Objects.equal(this.hoverAction, that.hoverAction)
                    && Objects.equal(this.shiftClickAction, that.shiftClickAction)
                    && Objects.equal(this.children, that.children);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.format, this.clickAction, this.hoverAction, this.shiftClickAction, this.children);
        }

        MoreObjects.ToStringHelper toStringHelper() {
            return MoreObjects.toStringHelper(this)
                    .omitNullValues()
                    .add("format", this.format.isEmpty() ? null : this.format)
                    .add("children", this.children.isEmpty() ? null : this.children)
                    .add("clickAction", this.clickAction)
                    .add("hoverAction", this.hoverAction)
                    .add("shiftClickAction", this.shiftClickAction);
        }

        @Override
        public final String toString() {
            return this.toStringHelper().toString();
        }

        @Override
        public final Text toText() {
            return this.build();
        }
    }
}

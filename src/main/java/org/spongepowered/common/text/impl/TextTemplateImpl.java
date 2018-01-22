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
import com.google.common.collect.ImmutableMap;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.TextTemplateArgumentException;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

public final class TextTemplateImpl implements TextTemplate {

    /**
     * Empty representation of a {@link TextTemplate}. This is returned if the
     * array supplied to {@link #of(Object...)} is empty.
     */
    public static final TextTemplate EMPTY = new TextTemplateImpl(DEFAULT_OPEN_ARG, DEFAULT_CLOSE_ARG, new Object[]{});

    final ImmutableList<Object> elements;
    final ImmutableMap<String, Arg> arguments;
    final Text text;
    final String openArg;
    final String closeArg;

    TextTemplateImpl(final String openArg, final String closeArg, final Object[] elements) {
        this.openArg = openArg;
        this.closeArg = closeArg;

        // collect elements
        final ImmutableList.Builder<Object> elementList = ImmutableList.builder();
        final Map<String, ArgImpl> argumentMap = new HashMap<>();
        for (Object element : elements) {
            if (element instanceof ArgImpl.BuilderImpl) {
                element = ((ArgImpl.BuilderImpl) element).build();
            }
            if (element instanceof ArgImpl) {
                // check for non-equal duplicate argument
                final ArgImpl newArg = new ArgImpl((ArgImpl) element, this.openArg, this.closeArg);
                final ArgImpl oldArg = argumentMap.get(newArg.name);
                if (oldArg != null && !oldArg.equals(newArg)) {
                    throw new TextTemplateArgumentException("Tried to supply an unequal argument with a duplicate name \""
                            + newArg.name + "\" to TextTemplate.");
                }
                argumentMap.put(newArg.name, newArg);
                element = newArg;
            }
            elementList.add(element);
        }
        this.elements = elementList.build();
        this.arguments = ImmutableMap.copyOf(argumentMap);

        // build text representation
        Text.Builder builder = null;
        for (final Object element : this.elements) {
            builder = this.apply(element, builder);
        }
        this.text = Optional.ofNullable(builder).orElse(Text.builder()).build();
    }

    @Override
    public List<Object> getElements() {
        return this.elements;
    }

    @Override
    public Map<String, Arg> getArguments() {
        return this.arguments;
    }

    @Override
    public String getOpenArgString() {
        return this.openArg;
    }

    @Override
    public String getCloseArgString() {
        return this.closeArg;
    }

    @Override
    public TextTemplate concat(final TextTemplate other) {
        final List<Object> elements = new ArrayList<>(this.elements);
        elements.addAll(other.getElements());
        return new TextTemplateImpl(this.openArg, this.closeArg, elements.toArray(new Object[elements.size()]));
    }

    @Override
    public Text.Builder apply() {
        return this.apply(Collections.emptyMap());
    }

    @Override
    public Text.Builder apply(final Map<String, ?> params) {
        return this.apply(null, params);
    }

    private Text.Builder apply(@Nullable Text.Builder result, final Map<String, ?> params) {
        checkNotNull(params, "params");
        for (final Object element : this.elements) {
            result = this.apply(element, result, params);
        }
        return Optional.ofNullable(result).orElse(Text.builder());
    }

    @Nullable
    private Text.Builder apply(final Object element, @Nullable Text.Builder builder, final Map<String, ?> params) {
        // Note: The builder is initialized as null to avoid unnecessary Text nesting
        if (element instanceof ArgImpl) {
            final ArgImpl arg = (ArgImpl) element;
            final Object param = params.get(arg.name);
            if (param == null) {
                arg.checkOptional();
                if (arg.defaultValue != null) {
                    builder = this.applyArg(arg.defaultValue, arg, builder);
                }
            } else {
                builder = this.applyArg(param, arg, builder);
            }
        } else {
            builder = this.apply(element, builder);
        }
        return builder;
    }

    private Text.Builder apply(final Object element, @Nullable Text.Builder builder) {
        if (element instanceof Text) {
            final Text text = (Text) element;
            if (builder == null) {
                builder = text.toBuilder();
            } else {
                builder.append(text);
            }
        } else if (element instanceof TextElement) {
            if (builder == null) {
                builder = Text.builder();
            }
            ((TextElement) element).applyTo(builder);
        } else {
            final String str = element.toString();
            if (builder == null) {
                builder = Text.builder(str);
            } else {
                builder.append(Text.of(str));
            }
        }
        return builder;
    }

    private Text.Builder applyArg(final Object param, final ArgImpl arg, @Nullable Text.Builder builder) {
        if (builder == null) {
            builder = Text.builder();
        }
        // wrap the parameter in the argument format
        final Text.Builder wrapper = Text.builder().format(arg.format);
        this.apply(param, wrapper);
        builder.append(wrapper.build());
        return builder;
    }

    @Override
    public Text toText() {
        return this.text;
    }

    @Override
    public Iterator<Object> iterator() {
        return this.elements.iterator();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("elements", this.elements)
                .add("arguments", this.arguments)
                .add("text", this.text)
                .add("openArg", this.openArg)
                .add("closeArg", this.closeArg)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.elements, this.openArg, this.closeArg);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof TextTemplateImpl)) {
            return false;
        }
        final TextTemplateImpl that = (TextTemplateImpl) obj;
        return that.elements.equals(this.elements)
                && that.openArg.equals(this.openArg)
                && that.closeArg.equals(this.closeArg);
    }

    @ConfigSerializable
    public static final class ArgImpl implements TextTemplate.Arg {

        @Setting final boolean optional;
        @Setting @Nullable final Text defaultValue;
        final String name; // defined by node name
        final TextFormat format; // defined in "content" node
        final String openArg;
        final String closeArg;

        ArgImpl(final String name, final boolean optional, @Nullable final Text defaultValue, final TextFormat format, final String openArg, final String closeArg) {
            this.name = name;
            this.optional = optional;
            this.defaultValue = defaultValue;
            this.format = format;
            this.openArg = openArg;
            this.closeArg = closeArg;
        }

        ArgImpl(final String name, final boolean optional, @Nullable final Text defaultValue, final TextFormat format) {
            this(name, optional, defaultValue, format, DEFAULT_OPEN_ARG, DEFAULT_CLOSE_ARG);
        }

        ArgImpl(final ArgImpl arg, final String openArg, final String closeArg) {
            this(arg.name, arg.optional, arg.defaultValue, arg.format, openArg, closeArg);
        }

        void checkOptional() {
            if (!this.optional) {
                throw new TextTemplateArgumentException("Missing required argument in TextTemplate \"" + this.name + "\".");
            }
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public boolean isOptional() {
            return this.optional;
        }

        @Override
        public Optional<Text> getDefaultValue() {
            return Optional.ofNullable(this.defaultValue);
        }

        @Override
        public TextFormat getFormat() {
            return this.format;
        }

        @Override
        public String getOpenArgString() {
            return this.openArg;
        }

        @Override
        public String getCloseArgString() {
            return this.closeArg;
        }

        @Override
        public Text toText() {
            return Text.builder(this.openArg + this.name + this.closeArg).format(this.format).build();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .omitNullValues()
                    .add("optional", this.optional)
                    .add("defaultValue", this.defaultValue)
                    .add("name", this.name)
                    .add("format", this.format.isEmpty() ? null : this.format)
                    .add("openArg", this.openArg)
                    .add("closeArg", this.closeArg)
                    .toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.optional, this.defaultValue, this.openArg, this.closeArg);
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof ArgImpl)) {
                return false;
            }
            final ArgImpl that = (ArgImpl) obj;
            return that.name.equals(this.name)
                    && that.optional == this.optional
                    && (that.defaultValue != null ? that.defaultValue.equals(this.defaultValue) : this.defaultValue == null)
                    && that.openArg.equals(this.openArg)
                    && that.closeArg.equals(this.closeArg);
        }

        public static final class BuilderImpl implements Arg.Builder {

            String name;
            boolean optional = false;
            @Nullable Text defaultValue;
            TextFormat format = TextFormat.of();

            @Override
            public BuilderImpl name(final String name) {
                this.name = name;
                return this;
            }

            @Override
            public BuilderImpl optional(final boolean optional) {
                this.optional = optional;
                return this;
            }

            @Override
            public BuilderImpl defaultValue(final Text defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }

            @Override
            public BuilderImpl format(final TextFormat format) {
                this.format = format;
                return this;
            }

            @Override
            public BuilderImpl color(final TextColor color) {
                this.format = this.format.color(color);
                return this;
            }

            @Override
            public BuilderImpl style(final TextStyle style) {
                this.format = this.format.style(style);
                return this;
            }

            @Override
            public ArgImpl build() {
                return new ArgImpl(this.name, this.optional, this.defaultValue, this.format);
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .omitNullValues()
                        .add("name", this.name)
                        .add("optional", this.optional)
                        .add("defaultValue", this.defaultValue)
                        .add("format", this.format.isEmpty() ? null : this.format)
                        .toString();
            }

            @Override
            public Arg.Builder from(final Arg value) {
                this.name = value.getName();
                this.optional = value.isOptional();
                this.defaultValue = value.getDefaultValue().orElse(null);
                this.format = value.getFormat();
                return this;
            }

            @Override
            public Arg.Builder reset() {
                return new BuilderImpl();
            }
        }
    }
}

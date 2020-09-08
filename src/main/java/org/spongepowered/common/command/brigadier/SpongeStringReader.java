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
package org.spongepowered.common.command.brigadier;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;

// ArgumentReader.Mutable specifies a non null getRead() method, StringReader suggests its
// nullable - but it isn't. So we just need to suppress the warning.
// With elements from JsonToNBT for allow this to parse Json for users.
@SuppressWarnings("NullableProblems")
public final class SpongeStringReader extends StringReader implements ArgumentReader.Mutable {

    private static final char SYNTAX_QUOTE = '"';

    public SpongeStringReader(final String string) {
        super(string);
    }

    public SpongeStringReader(final StringReader other) {
        super(other);
    }

    public void unskipWhitespace() {
        if (this.getRemainingLength() >= 0) {
            if (this.peek(-1) == CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
                this.setCursor(this.getCursor() - 1);
            }
        }
    }

    @Override
    @NonNull
    public String getInput() {
        return this.getString();
    }

    @Override
    public char peekCharacter() {
        if (this.canRead()) {
            final char read = this.read();
            this.setCursor(this.getCursor() - 1);
            return read;
        }
        throw new IllegalStateException("Cannot get character when at the end of the string.");
    }

    @Override
    public char parseChar() {
        return this.read();
    }

    @Override
    public int parseInt() throws ArgumentParseException {
        try {
            return this.readInt();
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(TextComponent.of("Could not parse an integer"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public double parseDouble() throws ArgumentParseException {
        try {
            return this.readDouble();
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(TextComponent.of("Could not parse a double"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public float parseFloat() throws ArgumentParseException {
        try {
            return this.readFloat();
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(TextComponent.of("Could not parse a float"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public ResourceKey parseResourceKey() throws ArgumentParseException {
        return this.readResourceLocation(null);
    }

    @Override
    public ResourceKey parseResourceKey(@NonNull final String defaultNamespace) throws ArgumentParseException {
        return this.readResourceLocation(defaultNamespace);
    }

    @Override
    @NonNull
    public String parseUnquotedString() {
        final int start = this.getCursor();
        while (this.canRead() && !Character.isWhitespace(this.peek())) {
            this.skip();
        }
        return this.getString().substring(start, this.getCursor());
    }

    @Override
    @NonNull
    public String parseString() throws ArgumentParseException {
        try {
            if (this.canRead() && this.peek() == SYNTAX_QUOTE) {
                return this.readQuotedString();
            } else {
                return this.readUnquotedString();
            }
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(TextComponent.of("Could not parse string"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public String peekString() throws ArgumentParseException {
        final int currentCursor = this.getCursor();
        final String peek = this.parseString();
        this.setCursor(currentCursor);
        return peek;
    }

    @Override
    public boolean parseBoolean() throws ArgumentParseException {
        try {
            return this.readBoolean();
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(TextComponent.of("Could not parse a boolean"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public String parseJson() throws ArgumentParseException {
        final int startCursor = this.getCursor();
        try {
            this.readStruct();
        } catch (final ArgumentParseException e) {
            this.setCursor(startCursor);
            throw e;
        }
        final int endCursor = this.getCursor(); // this will be just after a }
        return this.getInput().substring(startCursor, endCursor);
    }

    @Override
    public JsonObject parseJsonObject() throws ArgumentParseException {
        return new Gson().fromJson(this.parseJson(), JsonObject.class);
    }

    @Override
    @NonNull
    public SpongeImmutableArgumentReader getImmutable() {
        return new SpongeImmutableArgumentReader(this.getString(), this.getCursor());
    }

    @Override
    public void setState(@NonNull final ArgumentReader state) throws IllegalArgumentException {
        if (state.getInput().equals(this.getString())) {
            this.setCursor(state.getCursor());
        } else {
            throw new IllegalArgumentException("The provided ArgumentReader does not match this ArgumentReader");
        }
    }

    @Override
    @NonNull
    public ArgumentParseException createException(@NonNull final Component errorMessage) {
        return new ArgumentParseException(errorMessage, this.getInput(), this.getCursor());
    }

    @NonNull
    public ArgumentParseException createException(@NonNull final Component errorMessage, @NonNull final Throwable inner) {
        return new ArgumentParseException(errorMessage, inner, this.getInput(), this.getCursor());
    }

    // JSON parsing. Mostly taken from JsonToNBT
    protected String readKey() throws ArgumentParseException {
        this.skipWhitespace();
        if (!this.canRead()) {
            throw this.createException(TextComponent.of("Unable to read JSON key"));
        } else {
            return this.getString();
        }
    }

    public void readValue() throws ArgumentParseException {
        this.skipWhitespace();
        if (!this.canRead()) {
            throw this.createException(TextComponent.of("Unable to read JSON value"));
        } else {
            final char c0 = this.peek();
            if (c0 == '{') {
                this.readStruct();
            } else {
                if (c0 == '[') {
                    this.readList();
                } else {
                    this.readValue();
                }
            }
        }
    }

    protected void readList() throws ArgumentParseException {
        if (this.canRead(3) && !StringReader.isQuotedStringStart(this.peek(1)) && this.peek(2) == ';') {
            this.readArrayTag();
        } else {
            this.readListTag();
        }
    }

    public void readStruct() throws ArgumentParseException {
        this.expectAfterWhitespace('{');
        this.skipWhitespace();

        while(this.canRead() && this.peek() != '}') {
            final String s = this.readKey();
            if (s.isEmpty()) {
                throw this.createException(TextComponent.of("Unable to read JSON key"));
            }

            try {
                this.expect(':');
            } catch (final CommandSyntaxException e) {
                throw this.createException(TextComponent.of(e.getMessage()));
            }
            if (!this.hasElementSeparator()) {
                break;
            }

            if (!this.canRead()) {
                throw this.createException(TextComponent.of("Unable to read JSON key"));
            }
        }

        this.expectAfterWhitespace('}');
    }

    private void readListTag() throws ArgumentParseException {
        this.expectAfterWhitespace('[');
        this.skipWhitespace();
        if (!this.canRead()) {
            throw this.createException(TextComponent.of("Unable to read JSON list"));
        } else {
            while(this.peek() != ']') {
                this.readValue();
                if (!this.hasElementSeparator()) {
                    break;
                }

                if (!this.canRead()) {
                    throw this.createException(TextComponent.of("Unable to read JSON value"));
                }
            }

            this.expectAfterWhitespace(']');
        }
    }

    private void readArrayTag() throws ArgumentParseException {
        this.expectAfterWhitespace('[');
        this.read();
        this.read();
        this.skipWhitespace();
        if (!this.canRead()) {
            throw this.createException(TextComponent.of("Unable to read JSON array"));
        }
        this.readArray();
    }

    private void readArray() throws ArgumentParseException {
        while(true) {
            if (this.peek() != ']') {
                this.readValue();
                if (this.hasElementSeparator()) {
                    if (!this.canRead()) {
                        throw this.createException(TextComponent.of("Unable to read JSON value"));
                    }
                    continue;
                }
            }

            this.expectAfterWhitespace(']');
            return;
        }
    }

    private boolean hasElementSeparator() {
        this.skipWhitespace();
        if (this.canRead() && this.peek() == ',') {
            this.skip();
            this.skipWhitespace();
            return true;
        } else {
            return false;
        }
    }

    private void expectAfterWhitespace(final char expected) throws ArgumentParseException {
        this.skipWhitespace();
        try {
            this.expect(expected);
        } catch (final CommandSyntaxException e) {
            throw this.createException(TextComponent.of(e.getMessage()));
        }
    }

    private ResourceKey readResourceLocation(@Nullable final String defaultNamespace) throws ArgumentParseException {
        final int i = this.getCursor();

        while (this.canRead() && ResourceLocation.isValidPathCharacter(this.peek())) {
            this.skip();
        }

        final String s = this.getString().substring(i, this.getCursor());
        if (s.contains(":")) {
            return ResourceKey.resolve(s);
        }

        if (defaultNamespace == null) {
            this.setCursor(i);
            throw this.createException(TranslatableComponent.of("argument.id.invalid"));
        }
        return ResourceKey.of(defaultNamespace, s);
    }

}

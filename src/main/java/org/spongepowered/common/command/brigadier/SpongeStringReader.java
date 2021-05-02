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

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.data.persistence.NBTTranslator;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

// ArgumentReader.Mutable specifies a non null read() method, StringReader suggests its
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
    public @NonNull String input() {
        return this.getString();
    }

    @Override
    public int remainingLength() {
        return this.getRemainingLength();
    }

    @Override
    public int totalLength() {
        return this.getTotalLength();
    }

    @Override
    public int cursor() {
        return this.getCursor();
    }

    @Override
    public String parsed() {
        final int cursor = this.getCursor();
        if (cursor == 0) {
            return "";
        }
        return this.getString().substring(0, cursor);
    }

    @Override
    public String remaining() {
        return this.getRemaining();
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
            throw new ArgumentParseException(Component.text("Could not parse an integer"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public double parseDouble() throws ArgumentParseException {
        try {
            return this.readDouble();
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(Component.text("Could not parse a double"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public float parseFloat() throws ArgumentParseException {
        try {
            return this.readFloat();
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(Component.text("Could not parse a float"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public ResourceKey parseResourceKey() throws ArgumentParseException {
        return this.readResourceLocation(null);
    }

    @Override
    public ResourceKey parseResourceKey(final @NonNull String defaultNamespace) throws ArgumentParseException {
        return this.readResourceLocation(defaultNamespace);
    }

    @Override
    public @NonNull String parseUnquotedString() {
        final int start = this.getCursor();
        while (this.canRead() && !Character.isWhitespace(this.peek())) {
            this.skip();
        }
        return this.getString().substring(start, this.getCursor());
    }

    @Override
    public @NonNull String parseString() throws ArgumentParseException {
        try {
            if (this.canRead() && this.peek() == SpongeStringReader.SYNTAX_QUOTE) {
                return this.readQuotedString();
            } else {
                return this.readUnquotedString();
            }
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(Component.text("Could not parse string"), e, this.getString(), this.getCursor());
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
            throw new ArgumentParseException(Component.text("Could not parse a boolean"), e, this.getString(), this.getCursor());
        }
    }

    @Override
    public String parseNBTString() throws ArgumentParseException {
        final int startCursor = this.getCursor();
        try {
            new TagParser(this).readStruct();
        } catch (final CommandSyntaxException ex) {
            this.setCursor(startCursor);
            throw new ArgumentParseException(
                SpongeAdventure.asAdventure(ex.getRawMessage()),
                ex.getInput(),
                ex.getCursor()
            );
        }
        final int endCursor = this.getCursor(); // this will be just after a }
        return this.input().substring(startCursor, endCursor);
    }

    @Override
    public DataContainer parseDataContainer() throws ArgumentParseException {
        try {
            return NBTTranslator.INSTANCE.translate(new TagParser(this).readStruct());
        } catch (final CommandSyntaxException e) {
            throw this.createException(SpongeAdventure.asAdventure(e.getRawMessage()));
        }
    }

    @Override
    public @NonNull SpongeImmutableArgumentReader immutable() {
        return new SpongeImmutableArgumentReader(this.getString(), this.getCursor());
    }

    @Override
    public void setState(final @NonNull ArgumentReader state) throws IllegalArgumentException {
        if (state.input().equals(this.getString())) {
            this.setCursor(state.cursor());
        } else {
            throw new IllegalArgumentException("The provided ArgumentReader does not match this ArgumentReader");
        }
    }

    @Override
    public @NonNull ArgumentParseException createException(final @Nullable Component errorMessage) {
        return new ArgumentParseException(errorMessage, this.input(), this.getCursor());
    }

    public @NonNull ArgumentParseException createException(final @Nullable Component errorMessage, final @NonNull Throwable inner) {
        return new ArgumentParseException(errorMessage, inner, this.input(), this.getCursor());
    }

    private ResourceKey readResourceLocation(final @Nullable String defaultNamespace) throws ArgumentParseException {
        final int i = this.getCursor();

        while (this.canRead() && ResourceLocation.isAllowedInResourceLocation(this.peek())) {
            this.skip();
        }

        final String s = this.getString().substring(i, this.getCursor());
        if (s.contains(":")) {
            return ResourceKey.resolve(s);
        }

        if (defaultNamespace == null) {
            this.setCursor(i);
            throw this.createException(Component.translatable("argument.id.invalid"));
        }
        return ResourceKey.of(defaultNamespace, s);
    }

}

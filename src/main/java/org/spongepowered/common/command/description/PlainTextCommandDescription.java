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
package org.spongepowered.common.command.description;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import org.spongepowered.api.command.CommandDescription;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.annotation.PlainDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializer;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Represents a plaintext (non-translatable, single-value) command description.
 */
public final class PlainTextCommandDescription implements CommandDescription {

    @Nullable protected final Text description;
    @Nullable protected final Text help;
    protected final Text usage;

    @SuppressWarnings("ConstantConditions")
    public PlainTextCommandDescription(TextSerializer serializer, PlainDescription description) {
        this(
                fromString(serializer, description.description()),
                fromString(serializer, description.help()),
                fromString(serializer, description.usage())
        );
    }

    @Nullable
    private static Text fromString(TextSerializer serializer, String string) {
        return string.isEmpty() ? null : serializer.deserialize(string);
    }

    public PlainTextCommandDescription(@Nullable Text description, @Nullable Text help, Text usage) {
        this.description = description;
        this.help = help;
        this.usage = checkNotNull(usage, "usage");
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.ofNullable(this.description);
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.ofNullable(this.help);
    }

    @Override
    public Text getUsage(CommandSource source) {
        return this.usage;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("description", this.description)
                .add("help", this.help)
                .add("usage", this.usage)
                .toString();
    }

}

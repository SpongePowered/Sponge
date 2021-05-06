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
package org.spongepowered.common.command.parameter.managed.standard;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.common.command.brigadier.argument.ResourceKeyedArgumentValueParser;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class SpongeDurationValueParameter extends ResourceKeyedArgumentValueParser<Duration> {

    public SpongeDurationValueParameter(final ResourceKey key) {
        super(key);
    }

    @Override
    public List<CommandCompletion> complete(final @NonNull CommandCause context, final @NonNull String currentInput) {
        return Collections.emptyList();
    }

    @Override
    public @NonNull Optional<? extends Duration> parseValue(
            final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable reader) throws ArgumentParseException {

        String s = reader.parseString().toUpperCase();
        if (!s.contains("T")) {
            if (s.contains("D")) {
                if (s.contains("H") || s.contains("M") || s.contains("S")) {
                    s = s.replace("D", "DT");
                }
            } else {
                if (s.startsWith("P")) {
                    s = "PT" + s.substring(1);
                } else {
                    s = "T" + s;
                }
            }
        }
        if (!s.startsWith("P")) {
            s = "P" + s;
        }
        try {
            return Optional.of(Duration.parse(s));
        } catch (final DateTimeParseException ex) {
            throw reader.createException(Component.text("Invalid duration!"));
        }
    }

}

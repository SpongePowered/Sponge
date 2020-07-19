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
package org.spongepowered.common.command.parameter;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.brigadier.argument.CustomArgumentParser;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class SpongeDefaultValueParser<T> implements ValueParser<T> {

    private static final List<ClientCompletionType> CLIENT_COMPLETION_TYPE = ImmutableList.of(CustomArgumentParser.NONE_CLIENT_COMPLETION_TYPE);

    private final Function<CommandCause, T> defaultFunction;

    public SpongeDefaultValueParser(final Function<CommandCause, T> defaultFunction) {
        this.defaultFunction = defaultFunction;
    }

    @Override
    public Optional<? extends T> getValue(
            final Parameter.Key<? super T> parameterKey,
            final ArgumentReader.Mutable reader,
            final CommandContext.Builder context)
            throws ArgumentParseException {
        final T result;
        try {
            result = this.defaultFunction.apply(context);
        } catch (final Exception ex) {
            throw ((SpongeStringReader) reader)
                    .createException(Text.of("An exception was thrown obtaining a default value for ", parameterKey.key()), ex);
        }
        if (result == null) {
            throw reader.createException(Text.of("No default value was supplied for ", parameterKey.key()));
        }
        return Optional.of(result);
    }

    @Override
    public List<ClientCompletionType> getClientCompletionType() {
        return SpongeDefaultValueParser.CLIENT_COMPLETION_TYPE;
    }
}

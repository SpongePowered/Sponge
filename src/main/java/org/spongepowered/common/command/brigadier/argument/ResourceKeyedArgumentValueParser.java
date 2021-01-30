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
package org.spongepowered.common.command.brigadier.argument;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.ResourceKeyedValueParameter;

import java.util.List;
import java.util.Optional;

public abstract class ResourceKeyedArgumentValueParser<T> extends AbstractArgumentParser<T> implements ResourceKeyedValueParameter<T>,
        ValueParameter.Simple<T> {

    private final ResourceKey key;

    public ResourceKeyedArgumentValueParser(final ResourceKey key) {
        this.key = key;
    }

    @Override
    public final @NonNull ResourceKey key() {
        return this.key;
    }

    @Override
    public final Optional<? extends T> parseValue(final Parameter.@NonNull Key<? super T> parameterKey,
            final ArgumentReader.@NonNull Mutable reader,
            final CommandContext.@NonNull Builder context) throws ArgumentParseException {
        return this.parseValue(context.cause(), reader);
    }

    @Override
    @NonNull
    public final List<String> complete(final @NonNull CommandContext context, final @NonNull String currentInput) {
        return this.complete(context.cause(), currentInput);
    }

}

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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.ArgumentType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionTypes;
import org.spongepowered.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class CatalogedZeroAdvanceValueParameter<T> extends CatalogedArgumentParser<T> {

    private static final List<ClientCompletionType> NONE_KEY = Collections.singletonList(ClientCompletionTypes.NONE.get());

    public CatalogedZeroAdvanceValueParameter(final ResourceKey key) {
        super(key);
    }

    @Override
    @NonNull
    public final List<String> complete(@NonNull final CommandContext context, @NonNull final String currentInput) {
        return Collections.emptyList();
    }

    @Override
    @NonNull
    public final Optional<? extends T> getValue(
            final Parameter.@NonNull Key<? super T> parameterKey,
            final ArgumentReader.@NonNull Mutable reader,
            final CommandContext.@NonNull Builder context)
            throws ArgumentParseException {
        return this.getValue(context, reader);
    }

    public abstract Optional<? extends T> getValue(final CommandContext.@NonNull Builder context, final ArgumentReader.@NonNull Mutable reader)
            throws ArgumentParseException;

    @Override
    @NonNull
    public final List<ClientCompletionType> getClientCompletionType() {
        return CatalogedZeroAdvanceValueParameter.NONE_KEY;
    }

    @Override
    @NonNull
    public final List<ArgumentType<?>> getClientCompletionArgumentType() {
        // need this as a dummy
        return ImmutableList.of(Constants.Command.STANDARD_STRING_ARGUMENT_TYPE);
    }

    @Override
    public boolean doesNotRead() {
        return true;
    }

}

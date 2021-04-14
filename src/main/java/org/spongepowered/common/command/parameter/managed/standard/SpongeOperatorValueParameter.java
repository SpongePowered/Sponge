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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.OperationArgument;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.managed.operator.Operator;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.accessor.commands.arguments.OperationArgumentAccessor;
import org.spongepowered.common.command.brigadier.argument.ResourceKeyedArgumentValueParser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SpongeOperatorValueParameter extends ResourceKeyedArgumentValueParser.ClientNativeCompletions<Operator> {

    private final OperationArgument operationArgument = OperationArgument.operation();
    private Map<String, Operator> operators;

    public SpongeOperatorValueParameter(final ResourceKey key) {
        super(key);
    }

    private static ArgumentParseException createException(final ArgumentReader.Mutable reader) {
        final CommandSyntaxException exception =
                OperationArgumentAccessor.accessor$ERROR_INVALID_OPERATION().createWithContext((ImmutableStringReader) reader);
        return new ArgumentParseException(Component.text(exception.getMessage()),
                exception,
                exception.getInput(),
                exception.getCursor());
    }

    @Override
    public Optional<? extends Operator> parseValue(final CommandCause commandCause, final ArgumentReader.Mutable reader) throws ArgumentParseException {
        if (!reader.canRead()) {
            throw SpongeOperatorValueParameter.createException(reader);
        }

        final StringBuilder builder = new StringBuilder();
        while(reader.canRead() && reader.peekCharacter() != CommandDispatcher.ARGUMENT_SEPARATOR_CHAR) {
            builder.append(reader.parseChar());
        }

        final String rawOperator = builder.toString();
        if (this.operators == null) {
            this.operators = RegistryTypes.OPERATOR.get().stream().collect(Collectors.toMap(Operator::asString, Function.identity()));
        }
        final Operator operator = this.operators.get(rawOperator);
        if (operator == null) {
            throw SpongeOperatorValueParameter.createException(reader);
        }
        return Optional.of(operator);
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(final com.mojang.brigadier.context.CommandContext<?> context,
                                                          final SuggestionsBuilder builder) {
        return this.operationArgument.listSuggestions(context, builder);
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return Collections.singletonList(this.operationArgument);
    }
}

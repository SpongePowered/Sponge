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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.ArgumentType;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.argument.AbstractArgumentParser;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class SpongeLiteralValueParameter<T> extends AbstractArgumentParser<T> implements ValueCompleter {

    private final Supplier<? extends Collection<String>> literalSupplier;
    private final Supplier<T> returnValue;
    private final ArgumentType<?> type = Constants.Command.STANDARD_STRING_ARGUMENT_TYPE;

    public SpongeLiteralValueParameter(final Supplier<? extends Collection<String>> literalSupplier, final Supplier<T> returnValue) {
        this.literalSupplier = literalSupplier;
        this.returnValue = returnValue;
    }

    @Override
    public @NonNull Optional<? extends T> parseValue(
            final Parameter.@NonNull Key<? super T> parameterKey,
            final ArgumentReader.@NonNull Mutable reader,
            final CommandContext.@NonNull Builder context) throws ArgumentParseException {

        final Collection<String> collection = this.literalSupplier.get();
        final Iterator<String> iterator = collection.iterator();
        final String[] toCompare = reader.parseString().split(" ");
        int x = 0;
        while (iterator.hasNext() && x < toCompare.length) {
            if (!iterator.next().equals(toCompare[x++])) {
                throw reader.createException(Component.text("The provided literal was not " + String.join(" ", collection)));
            }
        }

        if (iterator.hasNext()) {
            throw reader.createException(Component.text("The provided literal was not " + String.join(" ", collection)));
        }

        return Optional.of(this.returnValue.get());
    }

    @Override
    public List<CommandCompletion> complete(final @NonNull CommandContext context, final @NonNull String input) {
        final String literal = String.join(" ", this.literalSupplier.get());
        if (literal.startsWith(input)) {
            return Collections.singletonList(new SpongeCommandCompletion(literal));
        }
        return Collections.emptyList();
    }

    @Override
    public @NonNull String usage(final @NonNull String key) {
        return String.join(" ", this.literalSupplier.get());
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return ImmutableList.of(this.type);
    }

}

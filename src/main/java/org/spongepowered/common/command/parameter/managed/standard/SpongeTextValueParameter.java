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
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.common.command.brigadier.argument.AbstractArgumentParser;
import org.spongepowered.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class SpongeTextValueParameter extends AbstractArgumentParser<Component> {

    private final ComponentSerializer<Component, ? extends Component, String> textSerializer;
    private final boolean consumeAll;
    private final ArgumentType<?> clientCompletionType;

    public SpongeTextValueParameter(final ComponentSerializer<Component, ? extends Component, String> textSerializer, final boolean consumeAll) {
        this.textSerializer = textSerializer;
        this.consumeAll = consumeAll;
        if (this.consumeAll) {
            this.clientCompletionType = Constants.Command.GREEDY_STRING_ARGUMENT_TYPE;
        } else if (this.textSerializer instanceof GsonComponentSerializer) {
            this.clientCompletionType = Constants.Command.NBT_ARGUMENT_TYPE;
        } else {
            this.clientCompletionType = Constants.Command.STANDARD_STRING_ARGUMENT_TYPE;
        }
    }

    @Override
    public List<CommandCompletion> complete(final @NonNull CommandContext context, final @NonNull String currentInput) {
        return ImmutableList.of();
    }

    @Override
    public @NonNull Optional<? extends Component> parseValue(
            final Parameter.@NonNull Key<? super Component> parameterKey,
            final ArgumentReader.@NonNull Mutable reader,
            final CommandContext.@NonNull Builder context) throws ArgumentParseException {

        final String toConvert;
        if (this.consumeAll) {
            toConvert = reader.remaining();
            while (reader.canRead()) {
                reader.parseString();
            }
        } else {
            toConvert = reader.parseString();
        }
        return Optional.of(this.textSerializer.deserialize(toConvert));
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return Collections.singletonList(this.clientCompletionType);
    }

}

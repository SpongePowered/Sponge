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
package org.spongepowered.common.command.annotation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandDescription;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.InvocationCommandException;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.parsing.InputTokenizer;
import org.spongepowered.api.command.provider.ProvisionException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Represents an implementation of an annotation-based command.
 */
final class AnnotationCommandCallable implements CommandCallable {

    private static final Text PROVISION_EXCEPTION_MESSAGE = Text.of("Could not provision arguments");
    private final Object object;
    private final Method method;
    private final ArgumentParser parser;
    private final CommandDescription description;
    @Nullable private final String[] permissions;
    private final InputTokenizer inputTokenizer;
    // The suggestion method and parser are assigned later
    @Nullable private Method suggestionMethod;
    @Nullable private ArgumentParser suggestionMethodParser;

    AnnotationCommandCallable(Object object, Method method, ArgumentParser parser, CommandDescription description,
            @Nullable String[] permissions, InputTokenizer inputTokenizer) {
        this.object = checkNotNull(object, "object");
        this.method = checkNotNull(method, "method");
        this.parser = checkNotNull(parser, "parser");
        this.description = checkNotNull(description, "description");
        this.permissions = permissions;
        this.inputTokenizer = checkNotNull(inputTokenizer, "input tokenizer");
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        return this.process(this.method, this.parser, source, arguments);
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        if (this.suggestionMethod == null || this.suggestionMethodParser == null) {
            return ImmutableList.of();
        }

        return this.process(this.suggestionMethod, this.suggestionMethodParser, source, arguments);
    }

    @SuppressWarnings("unchecked")
    private <T> T process(Method method, ArgumentParser parser, CommandSource source, String arguments) throws ArgumentParseException, InvocationCommandException {
        final CommandArgs stack = new CommandArgs(arguments, this.inputTokenizer.tokenize(arguments, true));

        try {
            final Object[] args = parser.parse(source, stack);
            return (T) method.invoke(this.object, args);
        } catch (ProvisionException e) {
            @Nullable Text message = e.getText();
            throw new InvocationCommandException(message != null ? message : PROVISION_EXCEPTION_MESSAGE, e);
        } catch (IllegalAccessException e) {
            throw new InvocationCommandException(Text.of("Could not invoke method"), e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CommandException) {
                CommandException exception = (CommandException) e.getCause();
                @Nullable Text message = exception.getText();
                if (exception.isFriendly() && message != null) {
                    source.sendMessage(message);
                }
            }

            throw new InvocationCommandException(Text.of("Could not invoke method"), e);
        }
    }

    void setSuggestionMethod(Method suggestionMethod, ArgumentParser parser) {
        this.suggestionMethod = suggestionMethod;
        this.suggestionMethodParser = parser;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        if (this.permissions == null) {
            return true;
        }

        for (String permission : this.permissions) {
            // All permissions are required
            if (!source.hasPermission(permission)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return this.description.getShortDescription(source);
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return this.description.getHelp(source);
    }

    @Override
    public Text getUsage(CommandSource source) {
        return this.description.getUsage(source);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("object", this.object)
                .add("method", this.method)
                .add("parser", this.parser)
                .add("description", this.description)
                .add("permissions", this.permissions)
                .add("inputTokenizer", this.inputTokenizer)
                .add("suggestionMethod", this.suggestionMethod)
                .add("suggestionMethodParser", this.suggestionMethodParser)
                .toString();
    }

}

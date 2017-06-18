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

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

class SpongeFirstOfParameter implements Parameter {

    private final List<Parameter> parameters;
    private final boolean isOptional;
    private final boolean isOptionalWeak;

    SpongeFirstOfParameter(List<Parameter> parameters, boolean isOptional, boolean isOptionalWeak) {
        this.parameters = parameters;
        this.isOptional = isOptional;
        this.isOptionalWeak = isOptionalWeak;
    }

    @Override
    public void parse(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (this.isOptional && !args.hasNext()) {
            return;
        }

        CommandArgs.State startingState = args.getState();
        CommandContext.State startingContext = context.getState();

        List<ArgumentParseException> exceptions = new ArrayList<>();
        for (Parameter parameter : this.parameters) {
            CommandArgs.State argsState = args.getState();
            CommandContext.State contextState = context.getState();
            try {
                parameter.parse(cause, args, context);
                return;
            } catch (ArgumentParseException ex) {
                args.setState(argsState);
                context.setState(contextState);
            }
        }

        if (this.isOptionalWeak) {
            args.setState(startingState);
            context.setState(startingContext);
            return;
        }

        // If we get here, nothing parsed, so we throw an exception.
        throw args.createError(t("Could not parse the arguments.", exceptions));
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        List<String> completions = new ArrayList<>();
        for (Parameter parameter : this.parameters) {
            CommandContext.State state = context.getState();
            try {
                completions.addAll(parameter.complete(cause, args, context));
            } finally {
                context.setState(state);
            }
        }

        return completions;
    }

    @Override
    public Text getUsage(Cause cause) {
        Text.Builder builder = CommandMessageFormatting.LEFT_PARENTHESIS.toBuilder();
        boolean isFirst = true;

        for (Parameter parameter : this.parameters) {
            Text usage = parameter.getUsage(cause);
            if (!usage.isEmpty()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(CommandMessageFormatting.PIPE_TEXT);
                }
                builder.append(usage);
            }
        }

        return builder.append(CommandMessageFormatting.RIGHT_PARENTHESIS).build();
    }

}

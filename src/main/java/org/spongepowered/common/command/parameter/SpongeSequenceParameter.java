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

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.common.command.managed.SpongeCommandContext;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SpongeSequenceParameter implements Parameter {

    private final List<Parameter> parameters;
    private final boolean isOptionalWeak;
    private final boolean isOptional;

    public SpongeSequenceParameter(List<Parameter> parameters, boolean isOptional, boolean isOptionalWeak) {
        this.isOptional = isOptional;
        this.isOptionalWeak = isOptionalWeak;
        this.parameters = parameters;
    }

    @Override
    public void parse(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (this.isOptional && !args.hasNext()) {
            return;
        }

        CommandArgs.State argsState = args.getState();
        CommandContext.State contextState = context.getState();

        try {
            boolean parseFlags = context instanceof SpongeCommandContext && !((SpongeCommandContext) context).getFlags().isAnchored();
            for (Parameter parameter : this.parameters) {
                parameter.parse(cause, args, context);
                if (parseFlags) {
                    ((SpongeCommandContext) context).getFlags().parse(cause, args, context);
                }
            }
        } catch (Exception ex) {
            if (this.isOptionalWeak) {
                args.setState(argsState);
                context.setState(contextState);
                return;
            }

            throw ex;
        }
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        for (Iterator<Parameter> it = this.parameters.iterator(); it.hasNext(); ) {
            Parameter element = it.next();
            CommandArgs.State startState = args.getState();
            try {
                element.parse(cause, args, context);
                CommandArgs.State endState = args.getState();
                if (!args.hasNext()) {
                    args.setState(startState);
                    List<String> inputs = element.complete(cause, args, context);
                    args.previous();
                    if (!inputs.contains(args.next())) {
                        // Tabcomplete returns results to complete the last word in an argument.
                        // If the last word is one of the completions, the command is most likely complete
                        return inputs;
                    }

                    args.setState(endState);
                }
            } catch (ArgumentParseException e) {
                args.setState(startState);
                return element.complete(cause, args, context);
            }

            if (!it.hasNext()) {
                args.setState(startState);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Text getUsage(Cause cause) {
        return Text.joinWith(Text.of(" "), this.parameters.stream()
                .map(x -> x.getUsage(cause))
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList()));
    }
}

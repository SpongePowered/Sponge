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

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.command.managed.CommandExecutor;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.dispatcher.SpongeDispatcher;
import org.spongepowered.common.command.managed.SpongeManagedCommand;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This is designed to allow for child commands to be executed where the
 * subcommand is <strong>NOT</strong> the first element. To use this properly,
 * this should be set as a parameter, AND as the executor - this will then
 * execute the child parameter.
 */
public class SpongeDispatcherParameter extends SpongeDispatcher implements Parameter, CommandExecutor {

    private final Text key;
    private final Text argKey;

    public SpongeDispatcherParameter(Text key) {
        this.key = key;
        this.argKey = Text.of(key, "_args");
    }

    public Text getKey() {
        return this.key;
    }

    @Override
    public void parse(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        String command = args.next().toLowerCase(Locale.ENGLISH);
        CommandMapping mapping = get(command).orElseThrow(() -> args.createError(t("The subcommand %s does not exist", command)));

        Command mappedCommand = mapping.getCommand();
        CommandArgs.State argsState = args.getState();
        CommandContext.State contextState = context.getState();
        try {
            context.putEntry(this.argKey, args.rawArgsFromCurrentPosition());
            if (mappedCommand instanceof SpongeManagedCommand) {
                ((SpongeManagedCommand) mappedCommand).populateContext(cause, args, context);
            }

            context.putEntry(this.key, mappedCommand);
        } catch (Exception e) {
            // Couldn't parse - set the state back.
            args.setState(argsState);
            context.setState(contextState);
            if (e instanceof ArgumentParseException) {
                throw e;
            }

            throw args.createError(t("Could not parse the subcommand %s", command), e);
        }
    }

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        return args.nextIfPresent()
                .map(x -> getAliases().stream().map(String::toLowerCase).filter(y -> y.startsWith(x)).collect(Collectors.toList()))
                .orElse(Lists.newArrayList(getAliases()));
    }

    @Override
    public CommandResult execute(Cause cause, CommandContext context) throws CommandException {
        Command callable = context.getOneUnchecked(this.key);
        if (callable instanceof SpongeManagedCommand) {
            return ((SpongeManagedCommand) callable).getExecutor().execute(cause, context);
        }

        return callable.process(cause, context.getOneUnchecked(this.argKey));
    }

}

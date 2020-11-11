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
package org.spongepowered.test.commandtest;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKeys;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RawCommandTest implements Command.Raw {

    private final List<String> suggestions = ImmutableList.of("eggs", "bacon", "spam");

    @Override
    @NonNull
    public CommandResult process(@NonNull final CommandCause cause, @NonNull final String arguments) throws CommandException {
        if (arguments.isEmpty()) {
            cause.sendMessage(Identity.nil(), Component.text("No arguments"));
        }
        cause.sendMessage(Identity.nil(), Component.text(arguments));
        return CommandResult.success();
    }

    @Override
    @NonNull
    public List<String> getSuggestions(@NonNull final CommandCause cause, @NonNull final String arguments) throws CommandException {
        if (arguments.endsWith(" ")) {
            return this.suggestions;
        }

        final String[] args = arguments.split(" ");
        return this.suggestions.stream().filter(x -> x.startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(@NonNull final CommandCause cause) {
        return cause.hasPermission("commandtest.rawcommand");
    }

    @Override
    @NonNull
    public Optional<Component> getShortDescription(@NonNull final CommandCause cause) {
        return Optional.empty();
    }

    @Override
    @NonNull
    public Optional<Component> getExtendedDescription(@NonNull final CommandCause cause) {
        return Optional.empty();
    }

    @Override
    @NonNull
    public Component getUsage(@NonNull final CommandCause cause) {
        return Component.text("[string] [string]");
    }

    @Override
    public CommandTreeNode.@NonNull Root commandTree() {
        final CommandTreeNode.Argument<?> firstStringKey = ClientCompletionKeys.STRING.get().createNode().customSuggestions().executable();
        final CommandTreeNode.Argument<?> secondStringKey =
                ClientCompletionKeys.STRING.get().createNode().customSuggestions().executable().redirect(firstStringKey);
        firstStringKey.child("s2", secondStringKey);
        return CommandTreeNode.root().executable().child("s1", firstStringKey);
    }
}

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
package org.spongepowered.common.command;

import com.google.inject.Singleton;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.command.registrar.tree.CommandTreeBuilder;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@Singleton
public final class SpongeCommandManager implements CommandManager {

    @Override
    public CommandResult process(String arguments) throws CommandException {
        return null;
    }

    @Override
    public <T extends Subject & MessageReceiver> CommandResult process(T subjectReceiver, String arguments) throws CommandException {
        return null;
    }

    @Override
    public CommandResult process(Subject subject, MessageChannel channel, String arguments) throws CommandException {
        return null;
    }

    @Override public List<String> suggest(String arguments) {
        return null;
    }

    @Override
    public <T extends Subject & MessageReceiver> List<String> suggest(T subjectReceiver, String arguments) {
        return null;
    }

    @Override
    public List<String> suggest(Subject subject, MessageChannel receiver, String arguments) {
        return null;
    }

    @Override
    public CommandMapping registerAlias(CommandRegistrar<?> registrar, PluginContainer container, CommandTreeBuilder.Basic commandTree, Predicate<CommandCause> requirement, String primaryAlias, String... secondaryAliases) throws CommandFailedRegistrationException {
        return null;
    }

    @Override
    public CommandRegistrar<Command> getStandardRegistrar() {
        return null;
    }

    @Override
    public Collection<PluginContainer> getPlugins() {
        return null;
    }
}

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
package org.spongepowered.common.command.managed;

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.IncorrectCommandSourceException;
import org.spongepowered.api.command.managed.ChildExceptionBehavior;
import org.spongepowered.api.command.managed.ChildExceptionBehaviors;
import org.spongepowered.api.command.managed.CommandExecutor;
import org.spongepowered.api.command.managed.TargetedCommandExecutor;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.flag.Flags;
import org.spongepowered.api.command.parameter.token.InputTokenizer;
import org.spongepowered.api.command.parameter.token.InputTokenizers;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.command.parameter.flag.NoFlags;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

public class SpongeCommandBuilder implements Command.Builder {

    private static final Function<Cause, Optional<Text>> EMPTY_DESCRIPTION = cause -> Optional.empty();

    private List<Parameter> parameters = Lists.newArrayList();
    private final Map<String, Command> children = Maps.newHashMap();
    private ChildExceptionBehavior behavior = ChildExceptionBehaviors.SUPPRESS;
    private InputTokenizer inputTokenizer = InputTokenizers.LENIENT_QUOTED_STRING;
    @Nullable private CommandExecutor executor = null;
    @Nullable private Flags flags = null;
    @Nullable private String permission = null;
    private Function<Cause, Optional<Text>> shortDescription = EMPTY_DESCRIPTION;
    private Function<Cause, Optional<Text>> extendedDescription = EMPTY_DESCRIPTION;
    private boolean requirePermissionForChildren = true;
    @Nullable private Text targetedExecutorError = null;

    private static final CommandExecutor SUBCOMMAND_ONLY_EXECUTOR = (s, c) -> {
        throw new CommandException(t("This command requires a subcommand."), true);
    };

    @Override
    public Command.Builder child(Command child, Iterable<String> keys) {

        keys.forEach(x ->
                Preconditions.checkArgument(!this.children.containsKey(x.toLowerCase(Locale.ENGLISH)),
                        "No two children can have the same key. Keys are case insensitive."));

        // Do this separately so we don't just put a few keys in.
        keys.forEach(x -> this.children.put(x.toLowerCase(Locale.ENGLISH), child));
        return this;
    }

    @Override
    public Command.Builder setRequirePermissionForChildren(boolean required) {
        this.requirePermissionForChildren = required;
        return this;
    }

    @Override
    public Command.Builder setChildExceptionBehavior(ChildExceptionBehavior exceptionBehavior) {
        this.behavior = exceptionBehavior;
        return this;
    }

    @Override
    public Command.Builder setExecutor(CommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public Command.Builder setExecutor(TargetedCommandExecutor<CommandSource> executor) {
        this.executor = (cause, context) -> executor.execute(
                cause,
                context.getCommandSource().orElseGet(() -> Sponge.getServer().getConsole()),
                context);
        return this;
    }

    @Override
    public <T extends CommandSource> Command.Builder targetedExecutor(TargetedCommandExecutor<T> executor, Class<T> sourceType) {
        if (!(this.executor instanceof SpongeTargetCommandExecutor)) {
            this.executor = new SpongeTargetCommandExecutor();
        }

        ((SpongeTargetCommandExecutor) this.executor).addExecutor(executor, sourceType);
        return this;
    }

    @Override
    public Command.Builder setTargetedExecutorErrorMessage(Text targetedExecutorError) {
        this.targetedExecutorError = targetedExecutorError;
        return this;
    }

    @Override
    public Command.Builder setExtendedDescription(Function<Cause, Optional<Text>> extendedDescriptionFunction) {
        this.extendedDescription = extendedDescriptionFunction;
        return this;
    }

    @Override
    public Command.Builder setFlags(Flags flags) {
        this.flags = flags;
        return this;
    }

    @Override
    public Command.Builder parameter(Parameter parameter) {
        this.parameters.add(parameter);
        return this;
    }

    @Override
    public Command.Builder setPermission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public Command.Builder setInputTokenizer(InputTokenizer tokenizer) {
        this.inputTokenizer = tokenizer;
        return this;
    }

    @Override
    public Command.Builder setShortDescription(Function<Cause, Optional<Text>> descriptionFunction) {
        this.shortDescription = descriptionFunction;
        return this;
    }

    @Override
    public Command build() {

        Preconditions.checkState(!this.children.isEmpty() || this.executor != null,
                "The command must have an executor or at least one child command.");
        return new SpongeManagedCommand(
                this.parameters,
                this.children,
                this.behavior,
                this.inputTokenizer,
                this.flags == null ? NoFlags.INSTANCE : this.flags,
                this.executor == null ? SUBCOMMAND_ONLY_EXECUTOR : this.executor,
                this.permission,
                this.shortDescription,
                this.extendedDescription,
                this.requirePermissionForChildren
        );
    }

    @Override
    public Optional<CommandMapping> buildAndRegister(PluginContainer pluginContainer, List<String> aliases) {
        return Sponge.getCommandManager().register(pluginContainer, build(), aliases);
    }

    @Override
    public Command.Builder from(Command value) {
        if (!(value instanceof SpongeManagedCommand)) {
            throw new IllegalArgumentException("value must be a SpongeCommand");
        }

        reset();
        ((SpongeManagedCommand) value).populateBuilder(this);
        return this;
    }

    @Override
    public Command.Builder reset() {
        this.parameters = ImmutableList.of();
        this.children.clear();
        this.behavior = ChildExceptionBehaviors.RETHROW;
        this.inputTokenizer = InputTokenizers.LENIENT_QUOTED_STRING;
        this.executor = null;
        this.flags = null;
        this.permission = null;
        this.shortDescription = EMPTY_DESCRIPTION;
        this.extendedDescription = EMPTY_DESCRIPTION;
        this.requirePermissionForChildren = true;

        return this;
    }

    static class SpongeTargetCommandExecutor implements CommandExecutor {

        private final List<Executor<? extends CommandSource>> executors = Lists.newArrayList();

        <T extends CommandSource> void addExecutor(TargetedCommandExecutor<T> executor, Class<T> clazz) {
            this.executors.add(new Executor<>(clazz, executor));
        }

        @Override
        public CommandResult execute(Cause cause, CommandContext context) throws CommandException {
            CommandSource source = context.getCommandSource().orElseGet(() -> Sponge.getServer().getConsole());
            for (Executor<? extends CommandSource> executor : this.executors) {
                if (executor.canExecute(source)) {
                    return executor.execute(cause, source, context);
                }
            }

            throw new IncorrectCommandSourceException();
        }
    }

    static class Executor<T extends CommandSource> {

        private final Class<T> clazz;
        private final TargetedCommandExecutor<T> executor;

        Executor(Class<T> clazz, TargetedCommandExecutor<T> executor) {
            this.clazz = clazz;
            this.executor = executor;
        }

        boolean canExecute(CommandSource source) {
            return this.clazz.isInstance(source);
        }

        @SuppressWarnings("unchecked")
        CommandResult execute(Cause cause, CommandSource source, CommandContext context) throws CommandException {
            return this.executor.execute(cause, (T) source, context);
        }
    }
}

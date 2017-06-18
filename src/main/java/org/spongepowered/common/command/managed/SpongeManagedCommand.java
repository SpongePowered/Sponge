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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.dispatcher.CommandNode;
import org.spongepowered.api.command.dispatcher.Dispatcher;
import org.spongepowered.api.command.managed.ChildExceptionBehavior;
import org.spongepowered.api.command.managed.CommandExecutor;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.flag.Flags;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.command.parameter.token.InputTokenizer;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.command.SpongeCommandMapping;
import org.spongepowered.common.command.dispatcher.SpongeCommandNode;
import org.spongepowered.common.command.managed.childexception.ChildCommandException;
import org.spongepowered.common.command.parameter.flag.NoFlags;
import org.spongepowered.common.command.parameter.token.SpongeCommandArgs;
import org.spongepowered.common.service.pagination.PaginationCalculator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class SpongeManagedCommand implements Command, Dispatcher {

    private final static int SPACE_WIDTH = PaginationCalculator.getWidth(' ', false);

    private final Parameter parameters;
    private final Map<String, CommandMapping> mappings = Maps.newHashMap();
    private final TreeSet<String> primaryAliases = Sets.newTreeSet();
    private final ChildExceptionBehavior childExceptionBehavior;
    private final InputTokenizer inputTokenizer;
    private final Flags flags;
    private final CommandExecutor executor;
    @Nullable private final String permission;
    private final Function<Cause, Optional<Text>> shortDescription;
    private final Function<Cause, Optional<Text>> extendedDescription;
    private final boolean requirePermissionForChildren;
    private final Map<String, Integer> subcommandLengths = Maps.newHashMap();

    SpongeManagedCommand(Iterable<Parameter> parameters,
            Map<String, Command> children,
            ChildExceptionBehavior childExceptionBehavior,
            InputTokenizer inputTokenizer,
            Flags flags,
            CommandExecutor executor, @Nullable String permission,
            Function<Cause, Optional<Text>> shortDescription,
            Function<Cause, Optional<Text>> extendedDescription,
            boolean requirePermissionForChildren) {
        this.parameters = Parameter.seq(parameters);
        this.childExceptionBehavior = childExceptionBehavior;
        this.inputTokenizer = inputTokenizer;
        this.flags = flags;
        this.executor = executor;
        this.permission = permission;
        this.shortDescription = shortDescription;
        this.extendedDescription = extendedDescription;
        this.requirePermissionForChildren = requirePermissionForChildren;

        if (!children.isEmpty()) {
            // Register the commands
            Map<Command, List<String>> intermediate = children.entrySet().stream()
                    .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
            intermediate.forEach((lowLevel, aliases) -> {
                SpongeCommandMapping spongeCommandMapping;
                if (aliases.size() == 1) {
                    spongeCommandMapping = new SpongeCommandMapping(lowLevel, aliases.get(0));
                } else {
                    spongeCommandMapping = new SpongeCommandMapping(lowLevel, aliases.get(0), aliases.subList(1, aliases.size()));
                }

                this.primaryAliases.add(aliases.get(0));
                aliases.forEach(x -> this.mappings.put(x, spongeCommandMapping));
            });
        }
    }

    @Override
    public CommandResult process(Cause cause, String arguments) throws CommandException {
        // Step one, create the CommandArgs and CommandContext
        SpongeCommandArgs args = new SpongeCommandArgs(this.inputTokenizer.tokenize(arguments, true), arguments);
        SpongeCommandContext context = new SpongeCommandContext(cause);
        return processInternal(cause, args, context);
    }

    public CommandResult processInternal(Cause cause, CommandArgs args, SpongeCommandContext context)
            throws CommandException {
        if (this.requirePermissionForChildren) {
            checkPermission(cause);
        }

        ChildCommandException childException = null;

        // Step two, children. If we have any, we parse them now.
        if (!this.mappings.isEmpty() && args.hasNext()) {
            CommandArgs.State argsState = args.getState();
            CommandContext.State contextState = context.getState();
            String subCommand = args.next().toLowerCase(Locale.ENGLISH);
            Optional<? extends CommandMapping> optionalChild = get(subCommand.toLowerCase(Locale.ENGLISH));
            if (optionalChild.isPresent()) {
                // Get the command
                Command cmd = optionalChild.get().getCommand();
                context.setCurrentCommand(subCommand);
                try {
                    if (cmd instanceof SpongeManagedCommand) {
                        SpongeManagedCommand childSpec = (SpongeManagedCommand) cmd;
                        return childSpec.processInternal(cause, args, context);
                    } else {
                        return cmd.process(cause, args.rawArgsFromCurrentPosition());
                    }
                } catch (CommandException ex) {
                    // This might still rethrow, depends on the selected behavior
                    CommandException eex = this.childExceptionBehavior.onChildCommandError(ex);
                    if (eex == null || eex instanceof ChildCommandException) {
                        childException = (ChildCommandException) eex;
                    } else {
                        childException = new ChildCommandException(subCommand, eex, null);
                    }
                }
            }

            // Reset the state.
            args.setState(argsState);
            context.setState(contextState);
        }


        // Step three, this command
        if (!this.requirePermissionForChildren) {
            checkPermission(cause);
        }

        populateContext(cause, args, context);

        try {
            return this.executor.execute(cause, context);
        } catch (CommandException ex) {
            if (childException != null) {
                throw new ChildCommandException(
                        context.getCurrentCommand().orElseGet(() ->
                                Sponge.getCommandManager().getPrimaryAlias(this).orElse("")), ex, childException);
            }

            // Rethrow
            throw ex;
        }
    }

    @Override
    public List<String> getSuggestions(Cause cause, String arguments, @Nullable Location<World> targetPosition)
            throws CommandException {
        checkNotNull(cause, "cause");
        SpongeCommandArgs args = new SpongeCommandArgs(this.inputTokenizer.tokenize(arguments, true), arguments);
        CommandContext context = new SpongeCommandContext(cause, null, true, targetPosition);
        return ImmutableList.copyOf(this.parameters.complete(cause, args, context));
    }

    @Override
    public boolean testPermission(Cause cause) {
        return this.permission == null || Command.getSubjectFromCause(cause).map(x -> x.hasPermission(this.permission)).orElse(true);
    }

    @Override
    public Optional<Text> getShortDescription(Cause cause) {
        return this.shortDescription.apply(cause);
    }

    public Optional<Text> getExtendedDescription(Cause cause) {
        return this.extendedDescription.apply(cause);
    }

    @Override
    public Optional<Text> getHelp(Cause cause) {
        checkNotNull(cause, "cause");
        Text.Builder builder = Text.builder();
        this.getShortDescription(cause).ifPresent((a) -> builder.append(a, Text.NEW_LINE));
        builder.append(Text.of("Usage: "));
        Sponge.getCommandManager().getPrimaryAlias(this).ifPresent(x -> builder.append(Text.of("/", x)));
        builder.append(CommandMessageFormatting.SPACE_TEXT).append(parameterUsageText(cause));
        this.getExtendedDescription(cause).ifPresent((a) -> builder.append(Text.NEW_LINE, a));

        if (!this.getPrimaryAliases().isEmpty()) {
            Text subs = createSubcommands(cause);
            if (!subs.isEmpty()) {
                builder.append(Text.NEW_LINE).append(Text.NEW_LINE).append(subs);
            }
        }
        return Optional.of(builder.build());
    }

    @Override
    public Text getUsage(Cause cause) {
        Text paramUsage = parameterUsageText(cause);
        Text.Builder builder = Text.builder();
        if (!paramUsage.isEmpty()) {
            builder.append(paramUsage);
        }

        subcommandUsageText(cause).ifPresent(x -> builder.append(Text.NEW_LINE).append(x));
        return builder.build();
    }

    private Text createSubcommands(Cause cause) {
        List<String> subs = Lists.newArrayList(getPrimaryAliases());
        subs.sort(Comparator.naturalOrder());
        SortedMap<String, Text> subcommandMap = Maps.newTreeMap();
        for (String element : subs) {
            Command command = get(element).get().getCommand();
            if (command.testPermission(cause)) {
                subcommandMap.put(element, command.getShortDescription(cause).orElse(Text.EMPTY));
                this.subcommandLengths.computeIfAbsent(element, x -> PaginationCalculator.getWidth(Text.of(x)));
            }
        }

        if (subcommandMap.isEmpty()) {
            return Text.EMPTY;
        }

        // Get the max width.
        Text.Builder builder = Text.builder().append(t("Subcommands:")).append(Text.NEW_LINE);
        CommandSource commandSource = Command.getCommandSourceFromCause(cause).orElseGet(() -> Sponge.getServer().getConsole());

        if (commandSource instanceof ConsoleSource) {
            int max = subs.stream().mapToInt(String::length).max().getAsInt();
            for (Map.Entry<String, Text> el : subcommandMap.entrySet()) {
                int add = max - el.getKey().length() + 3;
                builder.append(CommandMessageFormatting.SPACE_TEXT)
                        .append(Text.of(TextColors.GREEN, el.getKey()))
                        .append(Collections.nCopies(add, CommandMessageFormatting.SPACE_TEXT))
                        .append(Text.of(TextColors.RESET, el.getValue()))
                        .append(Text.NEW_LINE);
            }

            return builder.build();
        }

        // Get the max width.
        int max = this.subcommandLengths.entrySet().stream()
                .filter(x -> subs.contains(x.getKey())).mapToInt(Map.Entry::getValue).max().getAsInt() + 1;
        for (Map.Entry<String, Text> el : subcommandMap.entrySet()) {
            int add = 2 + (max - this.subcommandLengths.get(el.getKey()))/SPACE_WIDTH;
            builder.append(CommandMessageFormatting.SPACE_TEXT)
                    .append(Text.of(TextColors.GREEN, el.getKey()))
                    .append(Collections.nCopies(add, CommandMessageFormatting.SPACE_TEXT))
                    .append(Text.of(TextColors.RESET, el.getValue()))
                    .append(Text.NEW_LINE);
        }

        return builder.build();
    }

    private Text parameterUsageText(Cause cause) {
        if (this.flags instanceof NoFlags) {
            return this.parameters.getUsage(cause);
        }

        return Text.joinWith(CommandMessageFormatting.SPACE_TEXT, this.flags.getUsage(cause), this.parameters.getUsage(cause));
    }

    private Optional<Text> subcommandUsageText(Cause cause) {
        List<String> aliasesToDisplay = this.primaryAliases.stream().filter(x -> {
            Command child = this.mappings.get(x).getCommand();
            return child instanceof SpongeManagedCommand && ((SpongeManagedCommand) child).requirePermissionForChildren || child.testPermission(cause);
        }).collect(Collectors.toList());

        if (aliasesToDisplay.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(t("Subcommands: ").toBuilder().color(TextColors.RED).append(Text.of(String.join(", ", aliasesToDisplay)))
                .build());
    }

    void populateBuilder(SpongeCommandBuilder builder) {
        builder.setChildExceptionBehavior(this.childExceptionBehavior)
               .setShortDescription(this.shortDescription)
               .setExtendedDescription(this.extendedDescription)
               .setExecutor(this.executor)
               .setFlags(this.flags)
               .setInputTokenizer(this.inputTokenizer)
               .parameters(this.parameters)
               .setPermission(this.permission)
               .setRequirePermissionForChildren(this.requirePermissionForChildren);

        this.mappings.forEach((alias, mapping) -> builder.child(mapping.getCommand(), mapping.getAllAliases()));
    }

    private void checkPermission(Cause cause) throws CommandPermissionException {
        if (!testPermission(cause)) {
            throw new CommandPermissionException();
        }
    }

    /**
     * Process this command with existing arguments and context objects.
     *
     * @param cause The {@link Cause} to populate the context with
     * @param args The arguments to process with
     * @param context The context to put data in
     * @throws ArgumentParseException if an invalid argument is provided
     */
    public void populateContext(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        // First, the flags.
        this.flags.parse(cause, args, context);
        this.parameters.parse(cause, args, context);
    }

    public CommandExecutor getExecutor() {
        return this.executor;
    }

    @Override
    public Set<? extends CommandMapping> getCommands() {
        return ImmutableSet.copyOf(this.mappings.values());
    }

    @Override
    public Set<String> getPrimaryAliases() {
        return ImmutableSet.copyOf(this.primaryAliases);
    }

    @Override
    public Set<String> getAliases() {
        return ImmutableSet.copyOf(this.mappings.keySet());
    }

    @Override
    public Optional<? extends CommandMapping> get(String alias) {
        return get(alias, null);
    }

    @Override
    public Optional<? extends CommandMapping> get(String alias, @Nullable Cause cause) {
        // No need to use a disambiguator, we don't allow multiple children with the same name.
        CommandMapping mapping = this.mappings.get(alias.toLowerCase(Locale.ENGLISH));
        if (mapping != null && (cause == null || mapping.getCommand().testPermission(cause))) {
            return Optional.of(mapping);
        }

        return Optional.empty();
    }

    @Override
    public Set<? extends CommandMapping> getAll(String alias) {
        return get(alias).map(ImmutableSet::of).orElseGet(ImmutableSet::of);
    }

    @Override
    public Multimap<String, CommandMapping> getAll() {
        ImmutableMultimap.Builder<String, CommandMapping> im = ImmutableMultimap.builder();
        this.mappings.forEach(im::put);
        return im.build();
    }

    @Override
    public boolean containsAlias(String alias) {
        return this.mappings.containsKey(alias.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public boolean containsMapping(CommandMapping mapping) {
        return this.mappings.containsValue(mapping);
    }

    @Override
    public Optional<? extends CommandNode> getCommandNode(String alias) {
        return get(alias).map(SpongeCommandNode::new);
    }

    @Override
    public Map<String, ? extends CommandNode> getCommandNodes() {
        Map<String, CommandNode> nodeMap = Maps.newHashMap();
        for (String alias : this.primaryAliases) {
            getCommandNode(alias).ifPresent(x -> nodeMap.put(alias, x));
        }

        return nodeMap;
    }

}

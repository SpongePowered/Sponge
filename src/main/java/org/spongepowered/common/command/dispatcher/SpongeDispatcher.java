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
package org.spongepowered.common.command.dispatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.command.CommandMessageFormatting.SPACE_TEXT;
import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandNotFoundException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.dispatcher.CommandNode;
import org.spongepowered.api.command.dispatcher.Disambiguator;
import org.spongepowered.api.command.dispatcher.Dispatcher;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.command.SpongeCommandMapping;
import org.spongepowered.common.command.dispatcher.SpongeCommandNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class SpongeDispatcher implements Dispatcher {

    /**
     * This is a disambiguator function that returns the first matching command.
     */
    public static final Disambiguator FIRST_DISAMBIGUATOR = (source, aliasUsed, availableOptions) -> {
        for (CommandMapping mapping : availableOptions) {
            if (mapping.getPrimaryAlias().toLowerCase().equals(aliasUsed.toLowerCase())) {
                return Optional.of(mapping);
            }
        }
        return Optional.of(availableOptions.get(0));
    };

    private final Disambiguator disambiguatorFunc;
    private final ListMultimap<String, CommandMapping> commands = ArrayListMultimap.create();

    /**
     * Creates a basic new dispatcher.
     */
    public SpongeDispatcher() {
        this(FIRST_DISAMBIGUATOR);
    }

    /**
     * Creates a new dispatcher with a specific disambiguator.
     *
     * @param disambiguatorFunc Function that returns the preferred command if
     *     multiple exist for a given alias
     */
    public SpongeDispatcher(Disambiguator disambiguatorFunc) {
        this.disambiguatorFunc = disambiguatorFunc;
    }

    /**
     * Register a given command using the given list of aliases.
     *
     * <p>If there is a conflict with one of the aliases (i.e. that alias
     * is already assigned to another command), then the alias will be skipped.
     * It is possible for there to be no alias to be available out of
     * the provided list of aliases, which would mean that the command would not
     * be assigned to any aliases.</p>
     *
     * <p>The first non-conflicted alias becomes the "primary alias."</p>
     *
     * @param callable The command
     * @param alias An array of aliases
     * @return The registered command mapping, unless no aliases could be
     *     registered
     */
    public Optional<CommandMapping> register(Command callable, String... alias) {
        checkNotNull(alias, "alias");
        return register(callable, Arrays.asList(alias));
    }

    /**
     * Register a given command using the given list of aliases.
     *
     * <p>If there is a conflict with one of the aliases (i.e. that alias
     * is already assigned to another command), then the alias will be skipped.
     * It is possible for there to be no alias to be available out of
     * the provided list of aliases, which would mean that the command would not
     * be assigned to any aliases.</p>
     *
     * <p>The first non-conflicted alias becomes the "primary alias."</p>
     *
     * @param callable The command
     * @param aliases A list of aliases
     * @return The registered command mapping, unless no aliases could be
     *     registered
     */
    public Optional<CommandMapping> register(Command callable, List<String> aliases) {
        checkNotNull(aliases, "aliases");
        checkNotNull(callable, "callable");

        if (!aliases.isEmpty()) {
            String primary = aliases.get(0);
            List<String> secondary = aliases.subList(1, aliases.size());
            CommandMapping mapping = new SpongeCommandMapping(callable, primary, secondary);

            for (String alias : aliases) {
                this.commands.put(alias.toLowerCase(), mapping);
            }

            return Optional.of(mapping);
        }
        return Optional.empty();
    }

    /**
     * Remove a mapping identified by the given alias.
     *
     * @param alias The alias
     * @return The previous mapping associated with the alias, if one was found
     */
    public synchronized Collection<CommandMapping> remove(String alias) {
        return this.commands.removeAll(alias.toLowerCase());
    }

    /**
     * Remove all mappings identified by the given aliases.
     *
     * @param aliases A collection of aliases
     * @return Whether any were found
     */
    public synchronized boolean removeAll(Collection<?> aliases) {
        checkNotNull(aliases, "aliases");

        boolean found = false;

        for (Object alias : aliases) {
            if (!this.commands.removeAll(alias.toString().toLowerCase()).isEmpty()) {
                found = true;
            }
        }

        return found;
    }

    /**
     * Remove a command identified by the given mapping.
     *
     * @param mapping The mapping
     * @return The previous mapping associated with the alias, if one was found
     */
    public synchronized Optional<CommandMapping> removeMapping(CommandMapping mapping) {
        checkNotNull(mapping, "mapping");

        CommandMapping found = null;

        Iterator<CommandMapping> it = this.commands.values().iterator();
        while (it.hasNext()) {
            CommandMapping current = it.next();
            if (current.equals(mapping)) {
                it.remove();
                found = current;
            }
        }

        return Optional.ofNullable(found);
    }

    /**
     * Remove all mappings contained with the given collection.
     *
     * @param mappings The collection
     * @return Whether the at least one command was removed
     */
    public synchronized boolean removeMappings(Collection<?> mappings) {
        checkNotNull(mappings, "mappings");

        boolean found = false;

        Iterator<CommandMapping> it = this.commands.values().iterator();
        while (it.hasNext()) {
            if (mappings.contains(it.next())) {
                it.remove();
                found = true;
            }
        }

        return found;
    }

    @Override
    public synchronized Set<CommandMapping> getCommands() {
        return ImmutableSet.copyOf(this.commands.values());
    }

    @Override
    public synchronized Set<String> getPrimaryAliases() {
        Set<String> aliases = new HashSet<>();

        for (CommandMapping mapping : this.commands.values()) {
            aliases.add(mapping.getPrimaryAlias());
        }

        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public synchronized Set<String> getAliases() {
        Set<String> aliases = new HashSet<>();

        for (CommandMapping mapping : this.commands.values()) {
            aliases.addAll(mapping.getAllAliases());
        }

        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public Optional<CommandMapping> get(String alias) {
        return get(alias, null);
    }

    @Override
    public synchronized Optional<CommandMapping> get(String alias, @Nullable Cause cause) {
        List<CommandMapping> results = this.commands.get(alias.toLowerCase());
        if (results.size() == 1) {
            return Optional.of(results.get(0));
        } else if (results.size() == 0) {
            return Optional.empty();
        } else {
            return this.disambiguatorFunc.disambiguate(cause, alias, results);
        }
    }

    @Override
    public synchronized boolean containsAlias(String alias) {
        return this.commands.containsKey(alias.toLowerCase());
    }

    @Override
    public boolean containsMapping(CommandMapping mapping) {
        checkNotNull(mapping, "mapping");

        for (CommandMapping test : this.commands.values()) {
            if (mapping.equals(test)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<? extends CommandNode> getCommandNode(String alias) {
        return get(alias).map(SpongeCommandNode::new);
    }

    @Override
    public Map<String, ? extends CommandNode> getCommandNodes() {
        return getPrimaryAliases().stream().collect(Collectors.toMap(k -> k, v -> new SpongeCommandNode(get(v).get())));
    }

    @Override
    public CommandResult process(Cause cause, String commandLine) throws CommandException {
        final String[] argSplit = commandLine.split(" ", 2);
        Optional<CommandMapping> cmdOptional = get(argSplit[0], cause);
        if (!cmdOptional.isPresent()) {
            throw new CommandNotFoundException(t("commands.generic.notFound"), argSplit[0]); // TODO: Fix properly to use a SpongeTranslation??
        }
        final String arguments = argSplit.length > 1 ? argSplit[1] : "";
        final Command spec = cmdOptional.get().getCommand();
        try {
            return spec.process(cause, arguments);
        } catch (CommandNotFoundException e) {
            throw new CommandException(t("No such child command: %s", e.getCommand()));
        }
    }

    @Override
    public List<String> getSuggestions(Cause cause, final String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        final String[] argSplit = arguments.split(" ", 2);
        Optional<CommandMapping> cmdOptional = get(argSplit[0], cause);
        if (argSplit.length == 1) {
            return filterCommands(cause, argSplit[0]).stream().collect(ImmutableList.toImmutableList());
        } else if (!cmdOptional.isPresent()) {
            return ImmutableList.of();
        }
        return cmdOptional.get().getCommand().getSuggestions(cause, argSplit[1], targetPosition);
    }

    @Override
    public boolean testPermission(Cause cause) {
        for (CommandMapping mapping : this.commands.values()) {
            if (mapping.getCommand().testPermission(cause)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Text> getShortDescription(Cause cause) {
        return Optional.empty();
    }

    @Override
    public Optional<Text> getHelp(Cause cause) {
        if (this.commands.isEmpty()) {
            return Optional.empty();
        }
        Text.Builder build = t("Available commands:\n").toBuilder();
        for (Iterator<String> it = filterCommands(cause).iterator(); it.hasNext();) {
            final Optional<CommandMapping> mappingOpt = get(it.next(), cause);
            if (!mappingOpt.isPresent()) {
                continue;
            }
            CommandMapping mapping = mappingOpt.get();
            final Optional<Text> description = mapping.getCommand().getShortDescription(cause);
            build.append(Text.builder(mapping.getPrimaryAlias())
                            .color(TextColors.GREEN)
                            .style(TextStyles.UNDERLINE)
                            .onClick(TextActions.suggestCommand("/" + mapping.getPrimaryAlias())).build(),
                    SPACE_TEXT, description.orElse(mapping.getCommand().getUsage(cause)));
            if (it.hasNext()) {
                build.append(Text.NEW_LINE);
            }
        }
        return Optional.of(build.build());
    }

    private Set<String> filterCommands(final Cause cause) {
        return Multimaps.filterValues(this.commands, input -> input.getCommand().testPermission(cause)).keys().elementSet();
    }

    // Filter out commands by String first
    private Set<String> filterCommands(final Cause cause, String start) {
        ListMultimap<String, CommandMapping> map = Multimaps.filterKeys(this.commands, input -> input != null && input.toLowerCase().startsWith(start.toLowerCase()));
        return Multimaps.filterValues(map, input -> input.getCommand().testPermission(cause)).keys().elementSet();
    }

    /**
     * Gets the number of registered aliases.
     *
     * @return The number of aliases
     */
    public synchronized int size() {
        return this.commands.size();
    }

    @Override
    public Text getUsage(final Cause cause) {
        final Text.Builder build = Text.builder();
        Iterable<String> filteredCommands = filterCommands(cause).stream()
                .filter(input -> {
                    if (input == null) {
                        return false;
                    }
                    final Optional<CommandMapping> ret = get(input, cause);
                    return ret.isPresent() && ret.get().getPrimaryAlias().equals(input);
                })
                .collect(Collectors.toList());

        for (Iterator<String> it = filteredCommands.iterator(); it.hasNext();) {
            build.append(Text.of(it.next()));
            if (it.hasNext()) {
                build.append(CommandMessageFormatting.PIPE_TEXT);
            }
        }
        return build.build();
    }

    @Override
    public synchronized Set<CommandMapping> getAll(String alias) {
        return ImmutableSet.copyOf(this.commands.get(alias));
    }

    @Override
    public Multimap<String, CommandMapping> getAll() {
        return ImmutableMultimap.copyOf(this.commands);
    }

}

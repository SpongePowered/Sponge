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

import static org.spongepowered.api.command.args.GenericArguments.dimension;
import static org.spongepowered.api.command.args.GenericArguments.firstParsing;
import static org.spongepowered.api.command.args.GenericArguments.flags;
import static org.spongepowered.api.command.args.GenericArguments.literal;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.seq;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.command.args.GenericArguments.world;

import co.aikar.timings.SpongeTimingsFactory;
import co.aikar.timings.Timings;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.SpongeDimensionType;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
public class SpongeCommand {
    static final String INDENT = "    ";
    static final String LONG_INDENT = INDENT + INDENT;

    static final Text INDENT_TEXT = Text.of(INDENT);
    static final Text NEWLINE_TEXT = Text.NEW_LINE;
    static final Text SEPARATOR_TEXT = Text.of(", ");

    /**
     * Create a new instance of the Sponge command structure.
     *
     * @return The newly created command
     */
    public static CommandSpec getCommand() {
        final ChildCommandElementExecutor flagChildren = new ChildCommandElementExecutor(null);
        final ChildCommandElementExecutor nonFlagChildren = new ChildCommandElementExecutor(flagChildren);
        nonFlagChildren.register(getVersionCommand(), "version");
        nonFlagChildren.register(getAuditCommand(), "audit");
        nonFlagChildren.register(getHeapCommand(), "heap");
        nonFlagChildren.register(getPluginsCommand(), "plugins");
        nonFlagChildren.register(getTimingsCommand(), "timings");
        flagChildren.register(getChunksCommand(), "chunks");
        flagChildren.register(getConfigCommand(), "config");
        flagChildren.register(getReloadCommand(), "reload"); // TODO: Should these two be subcommands of config, and what is now config be set?
        flagChildren.register(getSaveCommand(), "save");
        return CommandSpec.builder()
                .description(Text.of("Text description"))
                .extendedDescription(Text.of("commands:\n", // TODO: Automatically generate from child executors (wait for help system on this)
                        INDENT, title("chunks"), LONG_INDENT, "Prints chunk data for a specific dimension or world(s)\n",
                        INDENT, title("conf"), LONG_INDENT, "Configure sponge settings\n",
                        INDENT, title("heap"), LONG_INDENT, "Dump live JVM heap\n",
                        INDENT, title("reload"), LONG_INDENT, "Reloads a global, dimension, or world config\n",
                        INDENT, title("save"), LONG_INDENT, "Saves a global, dimension, or world config\n",
                        INDENT, title("version"), LONG_INDENT, "Prints current Sponge version\n",
                        INDENT, title("audit"), LONG_INDENT, "Audit mixin classes for implementation",
                        INDENT, title("plugins"), LONG_INDENT, "List currently installed plugins"))
                .arguments(firstParsing(nonFlagChildren, flags()
                        .flag("-global", "g")
                        .valueFlag(world(Text.of("world")), "-world", "w")
                        .valueFlag(dimension(Text.of("dimension")), "-dimension", "d")
                        .buildWith(flagChildren)))
                .executor(nonFlagChildren)
                .build();
    }


    // TODO: Have some sort of separator between outputs for each world/dimension/global/whatever (that are exactly one line?)
    private abstract static class ConfigUsingExecutor implements CommandExecutor {
        ConfigUsingExecutor() {
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            int successes = 0;
            if (args.hasAny("global")) {
                src.sendMessage(Text.of("Global: ", processGlobal(SpongeImpl.getGlobalConfig(), src, args)));
                ++successes;
            }
            if (args.hasAny("dimension")) {
                for (DimensionType dimension : args.<DimensionType>getAll("dimension")) {
                    WorldProvider provider = DimensionManager.getWorldFromDimId(((SpongeDimensionType) dimension).getDimensionTypeId()).provider;
                    src.sendMessage(Text.of("Dimension ", dimension.getName(), ": ", processDimension(((IMixinWorldProvider) provider)
                                    .getDimensionConfig(), dimension, src, args)));
                    ++successes;
                }
            }
            if (args.hasAny("world")) {
                for (WorldProperties properties : args.<WorldProperties>getAll("world")) {
                    Optional<World> world = SpongeImpl.getGame().getServer().getWorld(properties.getUniqueId());
                    if (!world.isPresent()) {
                        throw new CommandException(Text.of("World ", properties.getWorldName(), " is not loaded, cannot work with it"));
                    }
                    src.sendMessage(Text.of("World ", properties.getWorldName(), ": ", processWorld(((IMixinWorld) world.get()).getWorldConfig(),
                            world.get(), src, args)));
                    ++successes;
                }
            }
            if (successes == 0) {
                throw new CommandException(Text.of("At least one target flag must be specified"));
            }
            return CommandResult.builder().successCount(successes).build(); // TODO: How do we handle results?
        }

        protected Text processGlobal(SpongeConfig<SpongeConfig.GlobalConfig> config, CommandSource source, CommandContext args)
                throws CommandException {
            return process(config, source, args);
        }

        protected Text processDimension(SpongeConfig<SpongeConfig.DimensionConfig> config, DimensionType dim, CommandSource source,
                CommandContext args) throws CommandException {
            return process(config, source, args);
        }

        protected Text processWorld(SpongeConfig<SpongeConfig.WorldConfig> config, World world, CommandSource source,
                CommandContext args) throws CommandException {
            return process(config, source, args);
        }

        protected Text process(SpongeConfig<?> config, CommandSource source, CommandContext args) throws CommandException {
            return Text.of("Unimplemented");
        }
    }

    // Flag children

    private static CommandSpec getChunksCommand() {
        return CommandSpec.builder()
                .description(Text.of("Print chunk information, optionally dump"))
                .arguments(optional(seq(literal(Text.of("dump"), "dump"), optional(literal(Text.of("dump-all"), "all")))))
                .permission("sponge.command.chunks")
                .executor(new ConfigUsingExecutor() {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        CommandResult res = super.execute(src, args);
                        if (args.hasAny("dump")) {
                            File file = new File(new File(new File("."), "chunk-dumps"),
                                "chunk-info-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(Instant.now()) + "-server.txt");
                            src.sendMessage(Text.of("Writing chunk info to: ", file));
                            ChunkSaveHelper.writeChunks(file, args.hasAny("dump-all"));
                            src.sendMessage(Text.of("Chunk info complete"));
                        }
                        return res;
                    }

                    @Override
                    protected Text processGlobal(SpongeConfig<SpongeConfig.GlobalConfig> config, CommandSource source, CommandContext args)
                            throws CommandException {
                        for (World world : SpongeImpl.getGame().getServer().getWorlds()) {
                            source.sendMessage(Text.of("World ", Text.of(TextStyles.BOLD, world.getName()),
                                    getChunksInfo(((WorldServer) world))));
                        }
                        return Text.of("Printed chunk info for all worlds ");
                    }

                    @Override
                    protected Text processDimension(SpongeConfig<SpongeConfig.DimensionConfig> config, DimensionType dim, CommandSource source,
                            CommandContext args)
                            throws CommandException {
                        SpongeImpl.getGame().getServer().getWorlds().stream().filter(world -> world.getDimension().getType().equals(dim))
                            .forEach(world -> source.sendMessage(Text.of("World ", Text.of(TextStyles.BOLD, world.getName()),
                                                                      getChunksInfo(((WorldServer) world)))));
                        return Text.of("Printed chunk info for all worlds in dimension ", dim.getName());
                    }

                    @Override
                    protected Text processWorld(SpongeConfig<SpongeConfig.WorldConfig> config, World world, CommandSource source, CommandContext args)
                            throws CommandException {
                        return getChunksInfo((WorldServer) world);
                    }

                    protected Text key(Object text) {
                        return Text.of(TextColors.GOLD, text);
                    }

                    protected Text value(Object text) {
                        return Text.of(TextColors.GRAY, text);
                    }

                    protected Text getChunksInfo(WorldServer worldserver) {
                        return Text.of(NEWLINE_TEXT, key("Dimension: "), value(worldserver.provider.getDimensionId()), NEWLINE_TEXT,
                                key("Loaded chunks: "), value(worldserver.theChunkProviderServer.getLoadedChunkCount()), NEWLINE_TEXT,
                                key("Active chunks: "), value(worldserver.activeChunkSet.size()), NEWLINE_TEXT,
                                key("Entities: "), value(worldserver.loadedEntityList.size()), NEWLINE_TEXT,
                                key("Tile Entities: "), value(worldserver.loadedTileEntityList.size()), NEWLINE_TEXT,
                                key("Removed Entities:"), value(worldserver.unloadedEntityList.size()), NEWLINE_TEXT,
                                key("Removed Tile Entities: "), value(worldserver.tileEntitiesToBeRemoved), NEWLINE_TEXT
                        );
                    }
                })
                .build();
    }

    private static CommandSpec getConfigCommand() {
        return CommandSpec.builder()
                .description(Text.of("Inspect the Sponge config"))
                .arguments(seq(string(Text.of("key")), optional(string(Text.of("value")))))
                .permission("sponge.command.config")
                .executor(new ConfigUsingExecutor() {
                    @Override
                    protected Text process(SpongeConfig<?> config, CommandSource source, CommandContext args) throws CommandException {
                        final Optional<String> key = args.getOne("key");
                        final Optional<String> value = args.getOne("value");
                        if (config.getSetting(key.get()) == null || config.getSetting(key.get()).isVirtual()) {
                            throw new CommandException(Text.of("Key ", Text.builder(key.get()).color(TextColors.GREEN).build(), " is not "
                                    + "valid"));
                        }

                        if (value.isPresent()) { // Set
                            config.updateSetting(key.get(), value.get());

                            return Text.builder().append(Text.of(TextColors.GOLD, key), Text.of(" set to "),
                                    title(value.get())).build();
                        } else {
                            return Text.builder().append(Text.of(TextColors.GOLD, key), Text.of(" is "),
                                    title(String.valueOf(config.getSetting(key.get()).getValue()))).build();
                        }
                    }
                })
                .build();
    }

    private static CommandSpec getReloadCommand() {
        return CommandSpec.builder()
                .description(Text.of("Reload the Sponge configuration"))
                .permission("sponge.command.reload")
                .executor(new ConfigUsingExecutor() {
                    @Override
                    protected Text process(SpongeConfig<?> config, CommandSource source, CommandContext args) throws CommandException {
                        config.reload();
                        SpongeHooks.refreshActiveConfigs();
                        return Text.of("Reloaded configuration");
                    }
                })
                .build();
    }

    private static CommandSpec getSaveCommand() {
        return CommandSpec.builder()
                .description(Text.of("Save the configuration"))
                .permission("sponge.command.save")
                .executor(new ConfigUsingExecutor() {
                    @Override
                    protected Text process(SpongeConfig<?> config, CommandSource source, CommandContext args) throws CommandException {
                        config.save();
                        return Text.of("Saved");
                    }
                })
                .build();
    }

    // Non-flag children

    private static CommandSpec getHeapCommand() {
        return CommandSpec.builder()
                .description(Text.of("Generate a dump of the Sponge heap"))
                .permission("sponge.command.heap")
                .executor((src, args) -> {
                    File file = new File(new File(new File("."), "dumps"),
                            "heap-dump-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + "-server.bin");
                    src.sendMessage(Text.of("Writing JVM heap data to: ", file));
                    SpongeHooks.dumpHeap(file, true);
                    src.sendMessage(Text.of("Heap dump complete"));
                    return CommandResult.success();
                })
                .build();

    }

    private static final Text IMPLEMENTATION_NAME = Text.of(TextColors.YELLOW, TextStyles.BOLD,
            Sponge.getPlatform().getImplementation().getName());

    private static CommandSpec getVersionCommand() {
        return CommandSpec.builder()
                .description(Text.of("Display Sponge's current version"))
                .permission("sponge.command.version")
                .executor((src, args) -> {
                    Text.Builder builder = Text.builder().append(IMPLEMENTATION_NAME);

                    for (PluginContainer container : SpongeImpl.getInternalPlugins()) {
                        builder.append(NEWLINE_TEXT, Text.of(TextColors.GRAY, INDENT + container.getName(), ": "), Text.of(container.getVersion().orElse("unknown")));
                    }

                    src.sendMessage(builder.build());
                    return CommandResult.success();
                })
                .build();
    }

    private static CommandSpec getAuditCommand() {
        return CommandSpec.builder()
                .description(Text.of("Audit Mixin classes for implementation"))
                .permission("sponge.command.audit")
                .executor((src, args) -> {
                    MixinEnvironment.getCurrentEnvironment().audit();
                    return CommandResult.empty();
                })
                .build();
    }

    private static class PluginsCommandElement extends PatternMatchingCommandElement {

        protected PluginsCommandElement(@Nullable Text key) {
            super(key);
        }

        @Override
        protected Iterable<String> getChoices(CommandSource source) {
            return Sponge.getPluginManager().getPlugins().stream().map(PluginContainer::getId).collect(Collectors.toList());
        }

        @Override
        protected Object getValue(String choice) throws IllegalArgumentException {
            Optional<PluginContainer> plugin = Sponge.getPluginManager().getPlugin(choice);
            return plugin.orElseThrow(() -> new IllegalArgumentException("Plugin " + choice + " was not found"));
        }
    }

    static Text title(String title) {
        return Text.of(TextColors.GREEN, title);
    }

    private static CommandSpec getPluginsCommand() {
        return CommandSpec.builder()
                .description(Text.of("List currently installed plugins"))
                .permission("sponge.command.plugins")
                .arguments(optional(new PluginsCommandElement(Text.of("plugin"))))
                .executor((src, args) -> {
                    if (args.hasAny("plugin")) {
                        for (PluginContainer container : args.<PluginContainer>getAll("plugin")) {
                            Text.Builder builder = Text.builder().append(title(container.getName()));
                            container.getVersion().ifPresent(version -> builder.append(Text.of((" v" + version))));

                            appendPluginMeta(builder, "ID", container.getId());
                            appendPluginMeta(builder, "Description", container.getDescription());
                            appendPluginMeta(builder, "URL", container.getUrl());

                            if (!container.getAuthors().isEmpty()) {
                                appendPluginMeta(builder, "Authors", String.join(", ", container.getAuthors()));
                            }

                            appendPluginMeta(builder, "Main class", container.getInstance().map(instance -> instance.getClass().getCanonicalName()));

                            src.sendMessage(builder.build());
                        }
                    } else {
                        Collection<PluginContainer> plugins = SpongeImpl.getGame().getPluginManager().getPlugins();
                        Text.Builder build = Text.builder(String.format("Plugins (%d): ", plugins.size()));
                        boolean first = true;
                        for (PluginContainer next : plugins) {
                            if (!first) {
                                build.append(SEPARATOR_TEXT);
                            }
                            first = false;

                            Text.Builder pluginBuilder = Text.builder(next.getName())
                                    .color(TextColors.GREEN)
                                    .onClick(TextActions.runCommand("/sponge:sponge plugins " + next.getId()));

                            next.getVersion()
                                    .ifPresent(version -> pluginBuilder.onHover(TextActions.showText(Text.of("Version " + version))));

                            build.append(pluginBuilder.build());
                        }
                        src.sendMessage(build.build());
                    }
                    return CommandResult.success();
                }).build();
    }

    private static void appendPluginMeta(Text.Builder builder, String key, Optional<String> value) {
        if (value.isPresent()) {
            appendPluginMeta(builder, key, value.get());
        }
    }

    private static void appendPluginMeta(Text.Builder builder, String key, String value) {
        builder.append(NEWLINE_TEXT, INDENT_TEXT, title(key + ": "), Text.of(value));
    }


    private static CommandCallable getTimingsCommand() {
        return CommandSpec.builder()
                .permission("sponge.command.timings")
                .description(Text.of("Manages Sponge Timings data to see performance of the server."))
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.reset();
                            src.sendMessage(Text.of("Timings reset"));
                            return CommandResult.success();
                        })
                        .build(), "reset")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.generateReport(src);
                            return CommandResult.success();
                        })
                        .build(), "report", "paste")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            Timings.setTimingsEnabled(true);
                            src.sendMessage(Text.of("Enabled Timings & Reset"));
                            return CommandResult.success();
                        })
                        .build(), "on")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            Timings.setTimingsEnabled(false);
                            src.sendMessage(Text.of("Disabled Timings"));
                            return CommandResult.success();
                        })
                        .build(), "off")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(true);
                            src.sendMessage(Text.of("Enabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verbon")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(false);
                            src.sendMessage(Text.of("Disabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verboff")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            src.sendMessage(Text.of("Timings cost: " + SpongeTimingsFactory.getCost()));
                            return CommandResult.success();
                        })
                        .build(), "cost")
                .build();
    }
}

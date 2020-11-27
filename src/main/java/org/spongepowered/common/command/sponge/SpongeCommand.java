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
package org.spongepowered.common.command.sponge;

import co.aikar.timings.Timings;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.util.math.MathHelper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.common.hooks.SpongeHooks;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginContributor;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeCommand {

    protected static final String INDENT = "    ";
    protected static final String LONG_INDENT = SpongeCommand.INDENT + SpongeCommand.INDENT;
    protected static final Component INDENT_COMPONENT = Component.text(SpongeCommand.INDENT);
    protected static final Component LONG_INDENT_COMPONENT = Component.text(SpongeCommand.LONG_INDENT);
    protected static final DecimalFormat THREE_DECIMAL_DIGITS_FORMATTER = new DecimalFormat("########0.000");

    private final Parameter.Key<PluginContainer> pluginContainerKey = Parameter.key("plugin", PluginContainer.class);
    private final Parameter.Key<CommandMapping> commandMappingKey = Parameter.key("command", CommandMapping.class);
    private final Parameter.Key<WorldProperties> worldPropertiesKey = Parameter.key("world", WorldProperties.class);

    @Nullable private Component versionText = null;

    public Command.Parameterized createSpongeCommand() {
        // /sponge audit
        final Command.Parameterized auditCommand = Command.builder()
                .setPermission("sponge.command.audit")
                .setShortDescription(Component.text("Audit mixin classes for implementation"))
                .setExecutor(this::auditSubcommandExecutor)
                .build();

        // /sponge chunks
        final Command.Parameterized chunksCommand = this.chunksSubcommand();

        // /sponge heap
        final Command.Parameterized heapCommand = Command.builder()
                .setPermission("sponge.command.heap")
                .setShortDescription(Component.text("Dump live JVM heap"))
                .setExecutor(this::heapSubcommandExecutor)
                .build();

        // /sponge plugins
        final Command.Parameterized pluginsReloadCommand = Command.builder()
                .setPermission("sponge.command.plugins.refresh")
                .setShortDescription(Component.text("Refreshes supported plugins, typically causing plugin configuration reloads."))
                .parameter(Parameter.builder(PluginContainer.class)
                        .optional()
                        .parser(new FilteredPluginContainerParameter())
                        .setKey(this.pluginContainerKey)
                        .build())
                .setExecutor(this::pluginsRefreshSubcommandExecutor)
                .build();
        final Command.Parameterized pluginsListCommand = Command.builder()
                .setPermission("sponge.command.plugins.list")
                .setShortDescription(Component.text("Lists all currently installed plugins."))
                .setExecutor(this::pluginsListSubcommand)
                .build();
        final Command.Parameterized pluginsInfoCommand = Command.builder()
                .setPermission("sponge.command.plugins.info")
                .setShortDescription(Component.text("Displays information about a specific plugin."))
                .parameter(Parameter.plugin().setKey(this.pluginContainerKey).build())
                .setExecutor(this::pluginsInfoSubcommand)
                .build();

        final Command.Parameterized pluginsCommand = Command.builder()
                .child(pluginsReloadCommand, "refresh")
                .child(pluginsListCommand, "list")
                .child(pluginsInfoCommand, "info")
                .build();

        // /sponge timings
        final Command.Parameterized timingsCommand = this.timingsSubcommand();

        // /sponge tps
        final Command.Parameterized tpsCommand = Command.builder()
                .setPermission("sponge.command.tps")
                .setShortDescription(Component.text("Provides TPS (ticks per second) data for loaded worlds."))
                .setExecutor(this::tpsExecutor)
                .build();

        // /sponge version
        final Command.Parameterized versionCommand = Command.builder()
                .setPermission("sponge.command.version")
                .setShortDescription(Component.text("Display Sponge's current version"))
                .setExecutor(this::versionExecutor)
                .build();

        // /sponge which
        final Command.Parameterized whichCommand = Command.builder()
                .setPermission("sponge.command.which")
                .parameter(Parameter.builder(CommandMapping.class).setKey(this.commandMappingKey).parser(new CommandAliasesParameter()).build())
                .setShortDescription(Component.text("Find the plugin that owns a specific command"))
                .setExecutor(this::whichExecutor)
                .build();

        // /sponge
        final Command.Builder commandBuilder = Command.builder()
                .setPermission("sponge.command.root")
                .setExecutor(this::rootCommand)
                .child(auditCommand, "audit")
                .child(chunksCommand, "chunks")
                .child(heapCommand, "heap")
                .child(pluginsCommand, "plugins")
                .child(timingsCommand, "timings")
                .child(tpsCommand, "tps")
                .child(versionCommand, "version")
                .child(whichCommand, "which");

        this.additionalActions(commandBuilder);
        return commandBuilder.build();
    }

    protected void additionalActions(final Command.Builder builder) {
        // no-op for vanilla, SF might like to add a /sponge mods command, for example.
    }

    @NonNull
    private CommandResult rootCommand(final CommandContext context) {
        final PluginContainer platformPlugin = Launch.getInstance().getPlatformPlugin();
        final PluginContainer apiPlugin = Launch.getInstance().getApiPlugin();
        final PluginContainer minecraftPlugin = Launch.getInstance().getMinecraftPlugin();

        context.sendMessage(Identity.nil(), Component.text().append(
                Component.text("SpongePowered", NamedTextColor.YELLOW, TextDecoration.BOLD).append(Component.space()),
                Component.text("Plugin Platform (running on Minecraft " + minecraftPlugin.getMetadata().getVersion() + ")"),
                Component.newline(),
                Component.text(apiPlugin.getMetadata().getName().get() + ": " + apiPlugin.getMetadata().getVersion()),
                Component.newline(),
                Component.text(platformPlugin.getMetadata().getName().get() + ": " + platformPlugin.getMetadata().getVersion())
            ).build()
        );

        final Optional<Command.Parameterized> parameterized = context.getExecutedCommand();
        if (parameterized.isPresent()) {
            final String subcommands = parameterized.get()
                    .subcommands()
                    .stream()
                    .filter(x -> x.getCommand().canExecute(context.getCause()))
                    .flatMap(x -> x.getAliases().stream())
                    .collect(Collectors.joining(", "));
            if (!subcommands.isEmpty()) {
                context.sendMessage(Identity.nil(), Component.text().append(
                        Component.newline(),
                        Component.text("Available subcommands:"),
                        Component.newline(),
                        Component.text(subcommands)).build()
                );
            }
        }

        return CommandResult.success();
    }

    @NonNull
    private CommandResult auditSubcommandExecutor(final CommandContext context) {
        SpongeCommon.getLogger().info("Starting Mixin Audit");
        Launch.getInstance().auditMixins();
        return CommandResult.success();
    }

    private Command.Parameterized chunksSubcommand() {
        final Command.Parameterized globalCommand = Command.builder()
                .setExecutor(context -> {
                    for (final ServerWorld world : SpongeCommon.getGame().getServer().getWorldManager().getWorlds()) {
                        context.sendMessage(Identity.nil(), Component.text().content("World ")
                                        .append(Component.text(world.getKey().toString(), Style.style(TextDecoration.BOLD)))
                                        .append(this.getChunksInfo(world))
                                        .build());
                    }
                    return CommandResult.success();
                })
                .build();
        final Command.Parameterized worldCommand = Command.builder()
                .parameter(Parameter.worldProperties().setKey(this.worldPropertiesKey).build())
                .setExecutor(context -> {
                    final WorldProperties properties = context.requireOne(this.worldPropertiesKey);
                    final ServerWorld world = properties.getWorld()
                            .orElseThrow(() -> new CommandException(Component.text("The world " + properties.getKey().toString() + " is not loaded!")));
                    context.sendMessage(Identity.nil(), Component.text().content("World ")
                            .append(Component.text(world.getKey().toString(), Style.style(TextDecoration.BOLD)))
                            .append(this.getChunksInfo(world))
                            .build());
                    return CommandResult.success();
                })
                .build();
        final Parameter.Key<Boolean> dumpAllKey = Parameter.key("dumpAll", Boolean.class);
        final Command.Parameterized dumpCommand = Command.builder()
                .parameter(Parameter.literal(Boolean.class, true, "all").optional().setKey(dumpAllKey).build())
                .setExecutor(context -> {
                    final File file = new File(new File(new File("."), "chunk-dumps"),
                            "chunk-info-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + "-server.txt");
                    context.sendMessage(Identity.nil(), Component.text("Writing chunk info to: " + file.getAbsolutePath()));
                    // ChunkSaveHelper.writeChunks(file, context.hasAny(dumpAllKey));
                    context.sendMessage(Identity.nil(), Component.text("Chunk info complete"));
                    return CommandResult.success();
                })
                .build();
        return Command.builder()
                .child(globalCommand, "global")
                .child(worldCommand, "world")
                .child(dumpCommand, "dump")
                .setPermission("sponge.command.chunk")
                .build();
    }

    @NonNull
    private CommandResult heapSubcommandExecutor(final CommandContext context) {
        final File file = new File(new File(new File("."), "dumps"),
                "heap-dump-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + "-server.hprof");
        // src.sendMessage(Text.of("Writing JVM heap data to: ", file));
        SpongeCommon.getLogger().info("Writing JVM heap data to: {}", file.getAbsolutePath());
        SpongeHooks.dumpHeap(file, true);
        // src.sendMessage(Text.of("Heap dump complete"));
        SpongeCommon.getLogger().info("Heap dump complete");
        return CommandResult.success();
    }

    @NonNull
    private CommandResult pluginsListSubcommand(final CommandContext context) {
        final Collection<PluginContainer> plugins = Launch.getInstance().getPluginManager().getPlugins();
        context.sendMessage(Identity.nil(), this.title("Plugins (" + plugins.size() + ")"));
        for (final PluginContainer specificContainer : plugins) {
            final PluginMetadata metadata = specificContainer.getMetadata();
            final TextComponent.Builder builder = Component.text();
            this.createShortContainerMeta(builder.append(INDENT_COMPONENT), metadata);
            // builder.clickEvent(SpongeComponents.executeCallback(cause ->
            //         cause.sendMessage(this.createContainerMeta(metadata))));
            context.sendMessage(Identity.nil(), builder.build());
        }

        return CommandResult.success();
    }

    @NonNull
    private CommandResult pluginsInfoSubcommand(final CommandContext context) {
        final PluginContainer pluginContainer = context.requireOne(this.pluginContainerKey);
        context.sendMessage(Identity.nil(), this.createContainerMeta(pluginContainer.getMetadata()));
        return CommandResult.success();
    }

    @NonNull
    private CommandResult pluginsRefreshSubcommandExecutor(final CommandContext context) {
        final Optional<PluginContainer> pluginContainer = context.getOne(this.pluginContainerKey);
        final RefreshGameEvent event = SpongeEventFactory.createRefreshGameEvent(
                PhaseTracker.getCauseStackManager().getCurrentCause(),
                SpongeCommon.getGame()
        );
        if (pluginContainer.isPresent()) {
            // just send the reload event to that
            context.sendMessage(Identity.nil(), Component.text("Sending refresh event to" + pluginContainer.get().getMetadata().getId() + ", please wait..."));
            ((SpongeEventManager) SpongeCommon.getGame().getEventManager()).post(event, pluginContainer.get());
        } else {
            context.sendMessage(Identity.nil(), Component.text("Sending refresh event to all plugins, please wait..."));
            SpongeCommon.getGame().getEventManager().post(event);
        }

        context.sendMessage(Identity.nil(), Component.text("Completed plugin refresh."));
        return CommandResult.success();
    }

    private Command.@NonNull Parameterized timingsSubcommand() {
        return Command.builder()
                .setPermission("sponge.command.timings")
                .setShortDescription(Component.text("Manages Sponge Timings data to see performance of the server."))
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Identity.nil(), Component.text("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.reset();
                            context.sendMessage(Identity.nil(), Component.text("Timings reset"));
                            return CommandResult.success();
                        })
                        .build(), "reset")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Identity.nil(), Component.text("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.generateReport(context.getCause().getAudience());
                            return CommandResult.success();
                        })
                        .build(), "report", "paste")
                .child(Command.builder()
                        .setExecutor(context -> {
                            Timings.setTimingsEnabled(true);
                            context.sendMessage(Identity.nil(), Component.text("Enabled Timings & Reset"));
                            return CommandResult.success();
                        })
                        .build(), "on")
                .child(Command.builder()
                        .setExecutor(context -> {
                            Timings.setTimingsEnabled(false);
                            context.sendMessage(Identity.nil(), Component.text("Disabled Timings"));
                            return CommandResult.success();
                        })
                        .build(), "off")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Identity.nil(), Component.text("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(true);
                            context.sendMessage(Identity.nil(), Component.text("Enabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verbon")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Identity.nil(), Component.text("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(false);
                            context.sendMessage(Identity.nil(), Component.text("Disabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verboff")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Identity.nil(), Component.text("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            context.sendMessage(Identity.nil(), Component.text("Timings cost: " + SpongeTimingsFactory.getCost()));
                            return CommandResult.success();
                        })
                        .build(), "cost")
                .build();
    }

    private CommandResult tpsExecutor(final CommandContext context) {
         final List<Component> tps = new ArrayList<>();
          // Uncomment when per-world TPS is in and working.
//        for (final ServerWorld world : Sponge.getServer().getWorldManager().getWorlds()) {
//            // Add code to get the average here.
//            final TextComponent.Builder builder =
//                    TextComponent.builder("World [")
//                            .append(Component.text(world.getKey().asString(), NamedTextColor.DARK_GREEN))
//                            .append(Component.text("]"));
//            tps.add(this.appendTickTime(((MinecraftServerBridge) SpongeCommon.getServer()).bridge$getWorldTickTimes()));
//        }

        tps.add(this.appendTickTime(SpongeCommon.getServer().tickTimeArray, Component.text().content("Overall TPS: ")).build());

        SpongeCommon.getGame().getServiceProvider()
                .paginationService()
                .builder()
                .contents(tps)
                .title(Component.text("Server TPS", NamedTextColor.WHITE))
                .padding(Component.text("-", NamedTextColor.WHITE))
                .sendTo(context.getCause().getAudience());

        return CommandResult.success();
    }

    private TextComponent.Builder appendTickTime(final long[] tickTimes, final TextComponent.Builder builder) {
        final double averageTickTime = MathHelper.average(tickTimes) * 1.0E-6D;
        builder.append(Component.text(THREE_DECIMAL_DIGITS_FORMATTER.format(Math.min(1000.0 / (averageTickTime), 20)), NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(", Mean: "))
                .append(Component.text(THREE_DECIMAL_DIGITS_FORMATTER.format(averageTickTime) + "ms", NamedTextColor.RED));
        return builder;
    }

    @NonNull
    private CommandResult versionExecutor(final CommandContext context) {
        if (this.versionText == null) {
            final PluginContainer platformPlugin = Launch.getInstance().getPlatformPlugin();

            final TextComponent.Builder builder = Component.text()
                    .append(
                            Component.text(platformPlugin.getMetadata().getName().get(), Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD))
                    );

            final Component colon = Component.text(": ", NamedTextColor.GRAY);
            for (final PluginContainer container : Launch.getInstance().getLauncherPlugins()) {
                final PluginMetadata metadata = container.getMetadata();
                builder.append(
                        Component.newline(),
                        SpongeCommand.INDENT_COMPONENT,
                        Component.text(metadata.getName().orElseGet(metadata::getId), NamedTextColor.GRAY),
                        colon,
                        Component.text(container.getMetadata().getVersion())
                );
            }

            final String arch = System.getProperty("sun.arch.data.model");
            final String javaArch = arch != null ? arch + "-bit" : "UNKNOWN";

            final String javaVendor = System.getProperty("java.vendor");
            final String javaVersion = System.getProperty("java.version");
            final String osName = System.getProperty("os.name");
            final String osVersion = System.getProperty("os.version");
            final String osArch = System.getProperty("os.arch");

            builder.append(
                    Component.newline(),
                    SpongeCommand.INDENT_COMPONENT,
                    Component.text("JVM", NamedTextColor.GRAY),
                    colon,
                    Component.text(javaVersion + "/" + javaArch + " (" + javaVendor + ")"),
                    Component.newline(),
                    SpongeCommand.INDENT_COMPONENT,
                    Component.text("OS", NamedTextColor.GRAY),
                    colon,
                    Component.text(osName + "/" + osVersion + " (" + osArch + ")")
            );
            this.versionText = builder.build();
        }

        context.sendMessage(Identity.nil(), this.versionText);
        return CommandResult.success();
    }

    @NonNull
    private CommandResult whichExecutor(final CommandContext context) {
        final CommandMapping mapping = context.requireOne(this.commandMappingKey);
        context.sendMessage(Identity.nil(), Component.text().append(
                this.title("Aliases: "),
                Component.join(Component.text(", "),
                    mapping.getAllAliases().stream().map(x -> Component.text(x, NamedTextColor.YELLOW)).collect(Collectors.toList())),
                Component.newline(),
                this.title("Owned by: "),
                this.hl(mapping.getPlugin().getMetadata().getName().orElseGet(() -> mapping.getPlugin().getMetadata().getId())))
                .build());
        return CommandResult.success();
    }

    // --

    protected Component getChunksInfo(final ServerWorld worldserver) {
        if (((WorldBridge) worldserver).bridge$isFake() || worldserver.getWorldStorage().getWorldProperties() == null) {
            return Component.text().append(Component.newline(), Component.text("Fake world")).build();
        }
        return Component.text().append(Component.newline(), Component.text("chunk stuff here")).build();
        /*
                key("DimensionId: "), value(((WorldServerBridge) worldserver).bridge$getDimensionId()), TextComponent.newline(),
                key("Loaded chunks: "), value(worldserver.getChunkProvider().getLoadedChunkCount()), TextComponent.newline(),
                key("Active chunks: "), value(worldserver.getChunkProvider().getLoadedChunks().size()), TextComponent.newline(),
                key("Entities: "), value(worldserver.loadedEntityList.size()), TextComponent.newline(),
                key("Tile Entities: "), value(worldserver.loadedTileEntityList.size()), TextComponent.newline(),
                key("Removed Entities:"), value(((WorldAccessor) worldserver).accessor$getUnloadedEntityList().size()), TextComponent.newline(),
                key("Removed Tile Entities: "), value(((WorldAccessor) worldserver).accessor$getTileEntitiesToBeRemoved()), TextComponent.newline()*/
    }

    protected Component key(final String text) {
        return Component.text(text, NamedTextColor.GOLD);
    }

    protected Component value(final String text) {
        return Component.text(text, NamedTextColor.GRAY);
    }

    private Component title(final String title) {
        return Component.text(title, NamedTextColor.GREEN);
    }

    private Component hl(final String toHighlight) {
        return Component.text(toHighlight, NamedTextColor.DARK_GREEN);
    }

    private void appendPluginMeta(final TextComponent.Builder builder, final String key, final String value) {
        this.appendPluginMeta(builder, key, Component.text(value));
    }

    private void appendPluginMeta(final TextComponent.Builder builder, final String key, final URL value) {
        final String url = value.toString();
        this.appendPluginMeta(builder, key, Component.text().content(url).clickEvent(ClickEvent.openUrl(url))
                .decoration(TextDecoration.UNDERLINED, true).build());
    }

    private void appendPluginMeta(final TextComponent.Builder builder, final String key, final Component value) {
        builder.append(Component.newline())
                .append()
                .append(SpongeCommand.INDENT_COMPONENT, this.title(key + ": "), value);
    }

    private void createShortContainerMeta(final TextComponent.Builder builder, final PluginMetadata pluginMetadata) {
        builder.append(this.title(pluginMetadata.getName().orElse(pluginMetadata.getId())));
        builder.append(Component.text(" v" + pluginMetadata.getVersion()));
    }

    private Component createContainerMeta(final PluginMetadata pluginMetadata) {
        final TextComponent.Builder builder = Component.text();
        this.createShortContainerMeta(builder, pluginMetadata);

        this.appendPluginMeta(builder, "ID", pluginMetadata.getId());
        pluginMetadata.getDescription().ifPresent(x -> this.appendPluginMeta(builder, "Description", x));
        pluginMetadata.getLinks().getHomepage().ifPresent(x -> this.appendPluginMeta(builder, "Homepage", x));
        pluginMetadata.getLinks().getIssues().ifPresent(x -> this.appendPluginMeta(builder, "Issues", x));
        pluginMetadata.getLinks().getSource().ifPresent(x -> this.appendPluginMeta(builder, "Source", x));
        final Collection<PluginContributor> contributors = pluginMetadata.getContributors();
        if (!contributors.isEmpty()) {
            builder.append(Component.newline()).append(SpongeCommand.INDENT_COMPONENT).append(this.title("Contributors:"));
            for (final PluginContributor contributor : contributors) {
                builder.append(Component.newline()).append(SpongeCommand.LONG_INDENT_COMPONENT).append(Component.text(contributor.getName()));
                contributor.getDescription().ifPresent(x -> builder.append(Component.text(" (" + x + ")")));
            }
        }

        this.appendPluginMeta(builder, "Main class", pluginMetadata.getMainClass());
        return builder.build();
    }

}

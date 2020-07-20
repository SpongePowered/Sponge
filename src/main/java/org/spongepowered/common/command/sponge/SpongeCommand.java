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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginContributor;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeCommand {

    protected static final String INDENT = "    ";
    protected static final String LONG_INDENT = SpongeCommand.INDENT + SpongeCommand.INDENT;
    protected static final TextComponent INDENT_COMPONENT = TextComponent.of(SpongeCommand.INDENT);
    protected static final TextComponent LONG_INDENT_COMPONENT = TextComponent.of(SpongeCommand.LONG_INDENT);

    private final Parameter.Key<PluginContainer> pluginContainerKey = Parameter.key("plugin", TypeTokens.PLUGIN_CONTAINER_TOKEN);
    private final Parameter.Key<CommandMapping> commandMappingKey = Parameter.key("command", TypeTokens.COMMAND_MAPPING);

    @Nullable private TextComponent versionText = null;

    public Command.Parameterized createSpongeCommand() {
        // /sponge audit
        final Command.Parameterized auditCommand = Command.builder()
                .setPermission("sponge.command.audit")
                .setShortDescription(TextComponent.of("Audit mixin classes for implementation"))
                .setExecutor(this::auditSubcommandExecutor)
                .build();

        // /sponge heap
        final Command.Parameterized heapCommand = Command.builder()
                .setPermission("sponge.command.heap")
                .setShortDescription(TextComponent.of("Dump live JVM heap"))
                .setExecutor(this::heapSubcommandExecutor)
                .build();

        // /sponge plugins
        final Command.Parameterized pluginsReloadCommand = Command.builder()
                .setPermission("sponge.command.plugins.refresh")
                .setShortDescription(TextComponent.of("Refreshes supported plugin, typically causing plugin configuration reloads."))
                .parameter(Parameter.builder(PluginContainer.class)
                        .optional()
                        .parser(new FilteredPluginContainerParameter())
                        .setKey(this.pluginContainerKey)
                        .build())
                .setExecutor(this::pluginsRefreshSubcommandExecutor)
                .build();
        final Command.Parameterized pluginsCommand = Command.builder()
                .setPermission("sponge.command.plugins.root")
                .setShortDescription(TextComponent.of("Lists all currently installed plugins."))
                .child(pluginsReloadCommand, "refresh")
                .parameter(Parameter.plugin().setKey(this.pluginContainerKey).optional().build())
                .setExecutor(this::pluginsSubcommand)
                .build();

        // /sponge timings
        final Command.Parameterized timingsCommand = this.timingsSubcommand();

        // /sponge version
        final Command.Parameterized versionCommand = Command.builder()
                .setPermission("sponge.command.version")
                .setShortDescription(TextComponent.of("Display Sponge's current version"))
                .setExecutor(this::versionExecutor)
                .build();

        // /sponge which
        final Command.Parameterized whichCommand = Command.builder()
                .setPermission("sponge.command.which")
                .parameter(Parameter.builder(CommandMapping.class).setKey(this.commandMappingKey).parser(new CommandAliasesParameter()).build())
                .setShortDescription(TextComponent.of("Find the plugin that owns a specific command"))
                .setExecutor(this::whichExecutor)
                .build();

        // /sponge
        final Command.Builder commandBuilder = Command.builder()
                .setPermission("sponge.command.root")
                .setExecutor(this::rootCommand)
                .child(auditCommand, "audit")
                .child(heapCommand, "heap")
                .child(pluginsCommand, "plugins")
                .child(timingsCommand, "timings")
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
        final PluginContainer platformPlugin = Launcher.getInstance().getPlatformPlugin();
        final PluginContainer apiPlugin = Launcher.getInstance().getApiPlugin();
        final PluginContainer minecraftPlugin = Launcher.getInstance().getMinecraftPlugin();

        context.sendMessage(TextComponent.builder().append(
                TextComponent.of("SpongePowered", NamedTextColor.YELLOW, TextDecoration.BOLD).append(TextComponent.space()),
                TextComponent.of("Plugin Platform (running on Minecraft " + minecraftPlugin.getMetadata().getVersion() + ")"),
                TextComponent.newline(),
                TextComponent.of(apiPlugin.getMetadata().getName().get() + ": " + apiPlugin.getMetadata().getVersion()),
                TextComponent.newline(),
                TextComponent.of(platformPlugin.getMetadata().getName().get() + ": " + platformPlugin.getMetadata().getVersion())
            ).build()
        );
        return CommandResult.success();
    }

    @NonNull
    private CommandResult auditSubcommandExecutor(final CommandContext context) {
        SpongeCommon.getLogger().info("Starting Mixin Audit");
        Launcher.getInstance().auditMixins();
        return CommandResult.success();
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
    private CommandResult pluginsSubcommand(final CommandContext context) {
        final Optional<PluginContainer> pluginContainer = context.getOne(this.pluginContainerKey);
        if (pluginContainer.isPresent()) {
            context.sendMessage(this.createContainerMeta(pluginContainer.get().getMetadata()));
        } else {
            final Collection<PluginContainer> plugins = Launcher.getInstance().getPluginManager().getPlugins();
            context.sendMessage(this.title("Plugins (" + plugins.size() + ")"));
            for (final PluginContainer specificContainer : plugins) {
                final PluginMetadata metadata = specificContainer.getMetadata();
                final TextComponent.Builder builder = TextComponent.builder();
                this.createShortContainerMeta(builder.append(INDENT_COMPONENT), metadata);
               // builder.clickEvent(SpongeComponents.executeCallback(cause ->
               //         cause.sendMessage(this.createContainerMeta(metadata))));
                context.sendMessage(builder.build());
            }
        }

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
            // src.sendMessage(Text.of("Sending reload event to " + pluginContainer.get().getMetadata().getId() + ". Please wait."));
            SpongeCommon.getLogger().info("Sending refresh event to {}, please wait...", pluginContainer.get().getMetadata().getId());
            ((SpongeEventManager) SpongeCommon.getGame().getEventManager()).post(event, pluginContainer.get());
        } else {
            SpongeCommon.getLogger().info("Sending refresh event to all plugins, please wait...");
            SpongeCommon.getGame().getEventManager().post(event);
        }

        SpongeCommon.getLogger().info("Completed plugin refresh.");
        return CommandResult.success();
    }

    private Command.@NonNull Parameterized timingsSubcommand() {
        return Command.builder()
                .setPermission("sponge.command.timings")
                .setShortDescription(TextComponent.of("Manages Sponge Timings data to see performance of the server."))
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(TextComponent.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.reset();
                            context.sendMessage(TextComponent.of("Timings reset"));
                            return CommandResult.success();
                        })
                        .build(), "reset")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(TextComponent.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.generateReport(context.getAudience());
                            return CommandResult.success();
                        })
                        .build(), "report", "paste")
                .child(Command.builder()
                        .setExecutor(context -> {
                            Timings.setTimingsEnabled(true);
                            context.sendMessage(TextComponent.of("Enabled Timings & Reset"));
                            return CommandResult.success();
                        })
                        .build(), "on")
                .child(Command.builder()
                        .setExecutor(context -> {
                            Timings.setTimingsEnabled(false);
                            context.sendMessage(TextComponent.of("Disabled Timings"));
                            return CommandResult.success();
                        })
                        .build(), "off")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(TextComponent.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(true);
                            context.sendMessage(TextComponent.of("Enabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verbon")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(TextComponent.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(false);
                            context.sendMessage(TextComponent.of("Disabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verboff")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(TextComponent.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            context.sendMessage(TextComponent.of("Timings cost: " + SpongeTimingsFactory.getCost()));
                            return CommandResult.success();
                        })
                        .build(), "cost")
                .build();
    }

    @NonNull
    private CommandResult versionExecutor(final CommandContext context) {
        if (this.versionText == null) {
            final PluginContainer platformPlugin = Launcher.getInstance().getPlatformPlugin();

            final TextComponent.Builder builder = TextComponent.builder()
                    .append(
                            TextComponent.of(platformPlugin.getMetadata().getName().get(), Style.of(NamedTextColor.YELLOW, TextDecoration.BOLD))
                    );

            final TextComponent colon = TextComponent.of(": ", NamedTextColor.GRAY);
            for (final PluginContainer container : Launcher.getInstance().getLauncherPlugins()) {
                final PluginMetadata metadata = container.getMetadata();
                builder.append(
                        TextComponent.newline(),
                        SpongeCommand.INDENT_COMPONENT,
                        TextComponent.of(metadata.getName().orElseGet(metadata::getId), NamedTextColor.GRAY),
                        colon,
                        TextComponent.of(container.getMetadata().getVersion())
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
                    TextComponent.newline(),
                    SpongeCommand.INDENT_COMPONENT,
                    TextComponent.of("JVM", NamedTextColor.GRAY),
                    colon,
                    TextComponent.of(javaVersion + "/" + javaArch + " (" + javaVendor + ")"),
                    TextComponent.newline(),
                    SpongeCommand.INDENT_COMPONENT,
                    TextComponent.of("OS", NamedTextColor.GRAY),
                    colon,
                    TextComponent.of(osName + "/" + osVersion + " (" + osArch + ")")
            );
            this.versionText = builder.build();
        }

        context.sendMessage(this.versionText);
        return CommandResult.success();
    }

    @NonNull
    private CommandResult whichExecutor(final CommandContext context) {
        final CommandMapping mapping = context.requireOne(this.commandMappingKey);
        context.sendMessage(TextComponent.builder().append(
                this.title("Aliases: "),
                TextComponent.join(TextComponent.of(", "),
                    mapping.getAllAliases().stream().map(x -> TextComponent.of(x, NamedTextColor.YELLOW)).collect(Collectors.toList())),
                TextComponent.newline(),
                this.title("Owned by: "),
                this.hl(mapping.getPlugin().getMetadata().getName().orElseGet(() -> mapping.getPlugin().getMetadata().getId())))
                .build());
        return CommandResult.success();
    }

    // --

    private TextComponent title(final String title) {
        return TextComponent.of(title, NamedTextColor.GREEN);
    }

    private TextComponent hl(final String toHighlight) {
        return TextComponent.of(toHighlight, NamedTextColor.DARK_GREEN);
    }

    private void appendPluginMeta(final TextComponent.Builder builder, final String key, final String value) {
        this.appendPluginMeta(builder, key, TextComponent.of(value));
    }

    private void appendPluginMeta(final TextComponent.Builder builder, final String key, final URL value) {
        final String url = value.toString();
        this.appendPluginMeta(builder, key, TextComponent.builder(url).clickEvent(ClickEvent.openUrl(url))
                .decoration(TextDecoration.UNDERLINED, true).build());
    }

    private void appendPluginMeta(final TextComponent.Builder builder, final String key, final TextComponent value) {
        builder.append(TextComponent.newline())
                .append()
                .append(SpongeCommand.INDENT_COMPONENT, this.title(key + ": "), value);
    }

    private void createShortContainerMeta(final TextComponent.Builder builder, final PluginMetadata pluginMetadata) {
        builder.append(this.title(pluginMetadata.getName().orElse(pluginMetadata.getId())));
        builder.append(" v" + pluginMetadata.getVersion());
    }

    private TextComponent createContainerMeta(final PluginMetadata pluginMetadata) {
        final TextComponent.Builder builder = TextComponent.builder();
        this.createShortContainerMeta(builder, pluginMetadata);

        this.appendPluginMeta(builder, "ID", pluginMetadata.getId());
        pluginMetadata.getDescription().ifPresent(x -> this.appendPluginMeta(builder, "Description", x));
        pluginMetadata.getLinks().getHomepage().ifPresent(x -> this.appendPluginMeta(builder, "Homepage", x));
        pluginMetadata.getLinks().getIssues().ifPresent(x -> this.appendPluginMeta(builder, "Issues", x));
        pluginMetadata.getLinks().getSource().ifPresent(x -> this.appendPluginMeta(builder, "Source", x));
        final Collection<PluginContributor> contributors = pluginMetadata.getContributors();
        if (!contributors.isEmpty()) {
            builder.append(TextComponent.newline()).append(SpongeCommand.INDENT_COMPONENT).append(this.title("Contributors:"));
            for (final PluginContributor contributor : contributors) {
                builder.append(TextComponent.newline()).append(SpongeCommand.LONG_INDENT_COMPONENT).append(contributor.getName());
                contributor.getDescription().ifPresent(x -> builder.append(" (" + x + ")"));
            }
        }

        this.appendPluginMeta(builder, "Main class", pluginMetadata.getMainClass());
        return builder.build();
    }

}

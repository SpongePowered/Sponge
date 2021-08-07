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
import co.aikar.timings.sponge.SpongeTimingsFactory;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.event.manager.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginContributor;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.management.MBeanServer;

public class SpongeCommand {

    protected static final String INDENT = "    ";
    protected static final String LONG_INDENT = SpongeCommand.INDENT + SpongeCommand.INDENT;
    protected static final Component INDENT_COMPONENT = Component.text(SpongeCommand.INDENT);
    protected static final Component LONG_INDENT_COMPONENT = Component.text(SpongeCommand.LONG_INDENT);
    protected static final DecimalFormat THREE_DECIMAL_DIGITS_FORMATTER = new DecimalFormat("########0.000");

    protected static final TextColor GREEN = TextColor.color(0x42C742);
    protected static final TextColor MINT = TextColor.color(0x69A877);
    protected static final TextColor LIGHT_BLUE = TextColor.color(0x5EB3DA);
    protected static final TextColor YELLOW = TextColor.color(0xDEDE00);
    protected static final TextColor ORANGE = TextColor.color(0xE36504);
    protected static final TextColor RED = TextColor.color(0xC74242);

    private static final Component EMPTY = Component.text("Empty", TextColor.color(SpongeCommand.RED));

    private final Parameter.Key<PluginContainer> pluginContainerKey = Parameter.key("plugin", PluginContainer.class);
    private final Parameter.Key<CommandMapping> commandMappingKey = Parameter.key("command", CommandMapping.class);
    private final Parameter.Key<ServerWorld> worldKey = Parameter.key("world", ServerWorld.class);

    private @Nullable Component versionText = null;

    public Command.Parameterized createSpongeCommand() {
        // /sponge audit
        final Command.Parameterized auditCommand = Command.builder()
                .permission("sponge.command.audit")
                .shortDescription(Component.text("Audit mixin classes for implementation"))
                .executor(this::auditSubcommandExecutor)
                .build();

        // /sponge chunks
        final Command.Parameterized chunksCommand = this.chunksSubcommand();

        // /sponge heap
        final Command.Parameterized heapCommand = Command.builder()
                .permission("sponge.command.heap")
                .shortDescription(Component.text("Dump live JVM heap"))
                .executor(this::heapSubcommandExecutor)
                .build();

        // /sponge plugins
        final Command.Parameterized pluginsReloadCommand = Command.builder()
                .permission("sponge.command.plugins.refresh")
                .shortDescription(Component.text("Refreshes supported plugins, typically causing plugin configuration reloads."))
                .addParameter(Parameter.builder(PluginContainer.class)
                        .optional()
                        .addParser(new FilteredPluginContainerParameter())
                        .key(this.pluginContainerKey)
                        .build())
                .executor(this::pluginsRefreshSubcommandExecutor)
                .build();
        final Command.Parameterized pluginsListCommand = Command.builder()
                .permission("sponge.command.plugins.list")
                .shortDescription(Component.text("Lists all currently installed plugins."))
                .executor(this::pluginsListSubcommand)
                .build();
        final Command.Parameterized pluginsInfoCommand = Command.builder()
                .permission("sponge.command.plugins.info")
                .shortDescription(Component.text("Displays information about a specific plugin."))
                .addParameter(Parameter.plugin().key(this.pluginContainerKey).build())
                .executor(this::pluginsInfoSubcommand)
                .build();

        final Command.Parameterized pluginsCommand = Command.builder()
                .addChild(pluginsReloadCommand, "refresh")
                .addChild(pluginsListCommand, "list")
                .addChild(pluginsInfoCommand, "info")
                .build();

        // /sponge timings
        final Command.Parameterized timingsCommand = this.timingsSubcommand();

        // /sponge tps
        final Command.Parameterized tpsCommand = Command.builder()
                .permission("sponge.command.tps")
                .shortDescription(Component.text("Provides TPS (ticks per second) data for loaded worlds."))
                .executor(this::tpsExecutor)
                .build();

        // /sponge version
        final Command.Parameterized versionCommand = Command.builder()
                .permission("sponge.command.version")
                .shortDescription(Component.text("Display Sponge's current version"))
                .executor(this::versionExecutor)
                .build();

        // /sponge which
        final Command.Parameterized whichCommand = Command.builder()
                .permission("sponge.command.which")
                .addParameter(Parameter.builder(CommandMapping.class).key(this.commandMappingKey).addParser(new CommandAliasesParameter()).build())
                .shortDescription(Component.text("Find the plugin that owns a specific command"))
                .executor(this::whichExecutor)
                .build();

        // /sponge reload global|world [id]
        final Command.Parameterized reloadGlobalCommand = Command.builder()
            .permission("sponge.command.reload.global")
            .shortDescription(Component.text("Reload Sponge's common configuration"))
            .executor(this::reloadGlobalExecutor)
            .build();

        final Command.Parameterized reloadWorldCommand = Command.builder()
            .permission("sponge.command.reload.world")
            .addParameter(Parameter.world().key(this.worldKey).build())
            .shortDescription(Component.text("Reload Sponge's configuration for a single world"))
            .executor(this::reloadWorldExecutor)
            .build();

        final Command.Parameterized reloadCommand = Command.builder()
            .addChild(reloadGlobalCommand, "global")
            .addChild(reloadWorldCommand, "world")
            .build();

        final Command.Parameterized infoCommand = this.infoSubcommand();


        // /sponge
        final Command.Builder commandBuilder = Command.builder()
                .permission("sponge.command.root")
                .executor(this::rootCommand)
                .addChild(auditCommand, "audit")
                .addChild(chunksCommand, "chunks")
                .addChild(heapCommand, "heap")
                .addChild(pluginsCommand, "plugins")
                .addChild(timingsCommand, "timings")
                .addChild(tpsCommand, "tps")
                .addChild(versionCommand, "version")
                .addChild(whichCommand, "which")
                .addChild(reloadCommand, "reload")
                .addChild(infoCommand, "info")
            ;

        this.additionalActions(commandBuilder);
        return commandBuilder.build();
    }

    protected void additionalActions(final Command.Builder builder) {
        // no-op for vanilla, SF might like to add a /sponge mods command, for example.
    }

    private @NonNull CommandResult rootCommand(final CommandContext context) {
        final PluginContainer platformPlugin = Launch.instance().platformPlugin();
        final PluginContainer apiPlugin = Launch.instance().apiPlugin();
        final PluginContainer minecraftPlugin = Launch.instance().minecraftPlugin();

        context.sendMessage(Identity.nil(), Component.text().append(
                Component.text("SpongePowered", NamedTextColor.YELLOW, TextDecoration.BOLD).append(Component.space()),
                Component.text("Plugin Platform (running on Minecraft " + minecraftPlugin.metadata().version() + ")"),
                Component.newline(),
                Component.text(apiPlugin.metadata().name().get() + ": " + apiPlugin.metadata().version()),
                Component.newline(),
                Component.text(platformPlugin.metadata().name().get() + ": " + platformPlugin.metadata().version())
            ).build()
        );

        final Optional<Command.Parameterized> parameterized = context.executedCommand();
        if (parameterized.isPresent()) {
            final String subcommands = parameterized.get()
                    .subcommands()
                    .stream()
                    .filter(x -> x.command().canExecute(context.cause()))
                    .flatMap(x -> x.aliases().stream())
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

    private @NonNull CommandResult auditSubcommandExecutor(final CommandContext context) {
        SpongeCommon.logger().info("Starting Mixin Audit");
        Launch.instance().auditMixins();
        return CommandResult.success();
    }

    private CompletableFuture<Component> userIdToComponent(final @Nullable UUID uuid) {
        if (uuid != null) {
            return Sponge.server().userManager().load(uuid).handleAsync((userOptional, throwable) -> {
                if (throwable != null) {
                    return SpongeCommand.EMPTY;
                }
                return userOptional.map(user ->
                        user.require(Keys.DISPLAY_NAME)
                            .color(TextColor.color(SpongeCommand.LIGHT_BLUE))
                            .hoverEvent(HoverEvent.showText(Component.text(user.uniqueId().toString())))).orElse(SpongeCommand.EMPTY);
            }, SpongeCommon.server());
        }
        return CompletableFuture.completedFuture(SpongeCommand.EMPTY);
    }

    private Command.Parameterized infoSubcommand() {
        final Command.Parameterized blockInfoAtCommand = Command.builder()
            .addParameter(CommonParameters.LOCATION_ONLINE_ONLY)
            .executor(context -> {
                final ServerLocation serverLocation = context.requireOne(CommonParameters.LOCATION_ONLINE_ONLY);
                final CompletableFuture<Component> creator = this.userIdToComponent(serverLocation.get(Keys.CREATOR).orElse(null));
                final CompletableFuture<Component> notifier = this.userIdToComponent(serverLocation.get(Keys.NOTIFIER).orElse(null));
                CompletableFuture.allOf(creator, notifier).thenAcceptAsync(x ->
                        context.sendMessage(Identity.nil(), Component.text()
                            .content("Block Info: ")
                            .color(TextColor.color(SpongeCommand.GREEN))
                            .append(Component.text(serverLocation.blockPosition().toString())
                                    .hoverEvent(ItemStack.builder().fromBlockState(serverLocation.block()).build().createSnapshot())
                            )
                            .append(Component.newline())
                            .append(Component.text("Creator: ", TextColor.color(SpongeCommand.MINT)))
                            .append(creator.join())
                            .append(Component.newline())
                            .append(Component.text("Notifier: ", TextColor.color(SpongeCommand.MINT)))
                            .append(notifier.join())
                            .build()), SpongeCommon.server());
                return CommandResult.success();
            })
            .build();

        final Command.Parameterized blockInfoLookingAt = Command.builder()
            .executor(context -> {
                if (!(context.cause().root() instanceof Player)) {
                    return CommandResult.error(Component.text("Player required", TextColor.color(SpongeCommand.RED)));
                }
                final Player player = (Player) context.cause().root();
                return RayTrace.block()
                    .sourceEyePosition(player)
                    .direction(player)
                    .select(RayTrace.nonAir())
                    .limit(10)
                    .execute()
                    .map(result -> {
                        final LocatableBlock locatableBlock = result.selectedObject();
                        final CompletableFuture<Component> creator = this.userIdToComponent(locatableBlock.world()
                                .get(locatableBlock.blockPosition(), Keys.CREATOR).orElse(null));
                        final CompletableFuture<Component> notifier = this.userIdToComponent(locatableBlock.world()
                                .get(locatableBlock.blockPosition(), Keys.CREATOR).orElse(null));
                        CompletableFuture.allOf(creator, notifier).thenAcceptAsync(x ->
                                context.sendMessage(Identity.nil(), Component.text()
                                    .content("Block Info: ")
                                    .color(TextColor.color(SpongeCommand.GREEN))
                                    .append(Component.text(locatableBlock.blockPosition().toString())
                                            .hoverEvent(ItemStack.builder().fromBlockState(locatableBlock.blockState()).build().createSnapshot())
                                    )
                                    .append(Component.newline())
                                    .append(Component.text("Creator: ", TextColor.color(SpongeCommand.MINT)))
                                    .append(creator.join())
                                    .append(Component.newline())
                                    .append(Component.text("Notifier: ", TextColor.color(SpongeCommand.MINT)))
                                    .append(notifier.join())
                                    .build()), SpongeCommon.server());
                        return CommandResult.success();
                    }).orElseGet(() -> CommandResult.error(Component.text("Failed to find any block in range", NamedTextColor.RED)));
            })
            .build();
        final Command.Parameterized entityLookingAt = Command.builder()
            .executor(context -> {
                if (!(context.cause().root() instanceof Player)) {
                    return CommandResult.error(Component.text("Player required", TextColor.color(SpongeCommand.RED)));
                }
                final Player player = (Player) context.cause().root();
                return RayTrace.entity()
                    .sourceEyePosition(player)
                    .direction(player)
                    .limit(10)
                    .execute()
                    .map(result -> {
                        final org.spongepowered.api.entity.Entity entity = result.selectedObject();
                        final CompletableFuture<Component> creator = this.userIdToComponent(entity.get(Keys.CREATOR).orElse(null));
                        final CompletableFuture<Component> notifier = this.userIdToComponent(entity.get(Keys.NOTIFIER).orElse(null));
                        CompletableFuture.allOf(creator, notifier).thenAcceptAsync(x ->
                                context.sendMessage(Identity.nil(), Component.text()
                                    .content("Entity Info: ")
                                    .color(TextColor.color(SpongeCommand.GREEN))
                                    .append(entity.type().asComponent().hoverEvent(entity))
                                    .append(Component.newline())
                                    .append(Component.text("Creator: ", TextColor.color(SpongeCommand.MINT)))
                                    .append(creator.join())
                                    .append(Component.newline())
                                    .append(Component.text("Notifier: ", TextColor.color(SpongeCommand.MINT)))
                                    .append(notifier.join())
                                    .build()), SpongeCommon.server());
                        return CommandResult.success();
                    }).orElseGet(() -> CommandResult.error(Component.text("Failed to find any block in range", NamedTextColor.RED)));
            })
            .build();
        return Command.builder()
            .addChild(blockInfoAtCommand, "blockAt")
            .addChild(blockInfoLookingAt, "block")
            .addChild(entityLookingAt, "entity")
            .permission("sponge.command.info")
            .build();
    }

    private Command.Parameterized chunksSubcommand() {
        final Command.Parameterized globalCommand = Command.builder()
                .executor(context -> {
                    for (final ServerWorld world : SpongeCommon.game().server().worldManager().worlds()) {
                        context.sendMessage(Identity.nil(), Component.text().content("World: ")
                                        .append(Component.text(world.key().toString(), NamedTextColor.GREEN))
                                        .append(Component.newline())
                                        .append(this.getChunksInfo(world))
                                        .build());
                    }
                    return CommandResult.success();
                })
                .build();
        final Command.Parameterized worldCommand = Command.builder()
                .addParameter(CommonParameters.WORLD)
                .executor(context -> {
                    final ServerWorld world = context.requireOne(CommonParameters.WORLD);
                    context.sendMessage(Identity.nil(), Component.text().content("World: ")
                            .append(Component.text(world.key().toString(), NamedTextColor.GREEN))
                            .append(Component.newline())
                            .append(this.getChunksInfo(world))
                            .build());
                    return CommandResult.success();
                })
                .build();
        final Parameter.Key<Boolean> dumpAllKey = Parameter.key("dumpAll", Boolean.class);
        final Command.Parameterized dumpCommand = Command.builder()
                .addParameter(Parameter.literal(Boolean.class, true, "all").optional().key(dumpAllKey).build())
                .executor(context -> {
                    final File file = new File(new File(new File("."), "chunk-dumps"),
                            "chunk-info-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + "-server.txt");
                    context.sendMessage(Identity.nil(), Component.text("Writing chunk info to: " + file.getAbsolutePath()));
                    // ChunkSaveHelper.writeChunks(file, context.hasAny(dumpAllKey));
                    context.sendMessage(Identity.nil(), Component.text("Chunk info complete"));
                    return CommandResult.success();
                })
                .build();
        return Command.builder()
                .addChild(globalCommand, "global")
                .addChild(worldCommand, "world")
                .addChild(dumpCommand, "dump")
                .permission("sponge.command.chunk")
                .build();
    }

    private @NonNull CommandResult heapSubcommandExecutor(final CommandContext context) {
        final File file = new File(new File(new File("."), "dumps"),
                "heap-dump-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + "-server.hprof");
        // src.sendMessage(Text.of("Writing JVM heap data to: ", file));
        SpongeCommon.logger().info("Writing JVM heap data to: {}", file.getAbsolutePath());
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            final Class<?> clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            final Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", clazz);
            final Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            m.invoke(hotspotMBean, file.getPath(), true);
        } catch (final Throwable t) {
            SpongeCommon.logger().fatal(MessageFormat.format("Could not write heap to {0}", file));
        }
        // src.sendMessage(Text.of("Heap dump complete"));
        SpongeCommon.logger().info("Heap dump complete");
        return CommandResult.success();
    }

    private @NonNull CommandResult pluginsListSubcommand(final CommandContext context) {
        final Collection<PluginContainer> plugins = Launch.instance().pluginManager().plugins();
        context.sendMessage(Identity.nil(), this.title("Plugins (" + plugins.size() + ")"));
        for (final PluginContainer specificContainer : plugins) {
            final PluginMetadata metadata = specificContainer.metadata();
            final TextComponent.Builder builder = Component.text();
            this.createShortContainerMeta(builder.append(SpongeCommand.INDENT_COMPONENT), metadata);
            builder.clickEvent(SpongeComponents.executeCallback(cause ->
                    cause.sendMessage(Identity.nil(), this.createContainerMeta(metadata))));
            context.sendMessage(Identity.nil(), builder.build());
        }

        return CommandResult.success();
    }

    private @NonNull CommandResult pluginsInfoSubcommand(final CommandContext context) {
        final PluginContainer pluginContainer = context.requireOne(this.pluginContainerKey);
        context.sendMessage(Identity.nil(), this.createContainerMeta(pluginContainer.metadata()));
        return CommandResult.success();
    }

    private @NonNull CommandResult pluginsRefreshSubcommandExecutor(final CommandContext context) {
        final Optional<PluginContainer> pluginContainer = context.one(this.pluginContainerKey);
        final RefreshGameEvent event = SpongeEventFactory.createRefreshGameEvent(
                PhaseTracker.getCauseStackManager().currentCause(),
                SpongeCommon.game()
        );
        if (pluginContainer.isPresent()) {
            // just send the reload event to that
            context.sendMessage(Identity.nil(), Component.text("Sending refresh event to " + pluginContainer.get().metadata().id() + ", please wait..."));
            ((SpongeEventManager) SpongeCommon.game().eventManager()).postToPlugin(event, pluginContainer.get());
        } else {
            context.sendMessage(Identity.nil(), Component.text("Sending refresh event to all plugins, please wait..."));
            SpongeCommon.game().eventManager().post(event);
        }

        context.sendMessage(Identity.nil(), Component.text("Completed plugin refresh."));
        return CommandResult.success();
    }

    private Command.@NonNull Parameterized timingsSubcommand() {
        return Command.builder()
                .permission("sponge.command.timings")
                .shortDescription(Component.text("Manages Sponge Timings data to see performance of the server."))
                .addChild(Command.builder()
                        .executor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            Timings.reset();
                            context.sendMessage(Identity.nil(), Component.text("Timings reset"));
                            return CommandResult.success();
                        })
                        .build(), "reset")
                .addChild(Command.builder()
                        .executor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            Timings.generateReport(context.cause().audience());
                            return CommandResult.success();
                        })
                        .build(), "report", "paste")
                .addChild(Command.builder()
                        .executor(context -> {
                            Timings.setTimingsEnabled(true);
                            context.sendMessage(Identity.nil(), Component.text("Enabled Timings & Reset"));
                            return CommandResult.success();
                        })
                        .build(), "on")
                .addChild(Command.builder()
                        .executor(context -> {
                            Timings.setTimingsEnabled(false);
                            context.sendMessage(Identity.nil(), Component.text("Disabled Timings"));
                            return CommandResult.success();
                        })
                        .build(), "off")
                .addChild(Command.builder()
                        .executor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            Timings.setVerboseTimingsEnabled(true);
                            context.sendMessage(Identity.nil(), Component.text("Enabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verbon")
                .addChild(Command.builder()
                        .executor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            Timings.setVerboseTimingsEnabled(false);
                            context.sendMessage(Identity.nil(), Component.text("Disabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verboff")
                .addChild(Command.builder()
                        .executor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                return CommandResult.error(Component.text("Please enable timings by typing /sponge timings on"));
                            }
                            context.sendMessage(Identity.nil(), Component.text("Timings cost: " + SpongeTimingsFactory.getCost()));
                            return CommandResult.success();
                        })
                        .build(), "cost")
                .build();
    }

    private @NonNull CommandResult tpsExecutor(final CommandContext context) {
        if (SpongeCommon.game().isServerAvailable()) {
            final List<Component> tps = new ArrayList<>();
            for (final ServerWorld world : Sponge.server().worldManager().worlds()) {
                final TextComponent.Builder builder =
                  Component.text()
                    .append(Component.text(world.key().asString(), TextColor.color(0xC9C9C9)))
                    .append(Component.text(": "));
                tps.add(this.appendTickTime(((ServerLevelBridge) world).bridge$recentTickTimes(), builder).build());
            }

            tps.add(Component.newline());
            tps.add(this.appendTickTime(SpongeCommon.server().tickTimes, Component.text().content("Overall: ")).build());
            SpongeCommon.game().serviceProvider()
              .paginationService()
              .builder()
              .contents(tps)
              .title(Component.text("Ticks Per Second (TPS)", NamedTextColor.WHITE))
              .padding(Component.text("-", NamedTextColor.WHITE))
              .sendTo(context.cause().audience());
        } else {
            context.sendMessage(Identity.nil(), Component.text("Server is not running."));
        }

        return CommandResult.success();
    }

    private TextComponent.Builder appendTickTime(final long[] tickTimes, final TextComponent.Builder builder) {
        final double averageTickTime = Mth.average(tickTimes) * 1.0E-6D;
        final double tps = Math.min(1000.0 / (averageTickTime), 20);
        builder.append(Component.text(SpongeCommand.THREE_DECIMAL_DIGITS_FORMATTER.format(tps), this.tpsColor(tps)))
          .append(Component.text(" (", NamedTextColor.GRAY)
            .append(Component.text(SpongeCommand.THREE_DECIMAL_DIGITS_FORMATTER.format(averageTickTime), NamedTextColor.GRAY)
              .append(Component.text("ms avg"))
              .append(Component.text(")"))))
        ;
        return builder;
    }

    private TextColor tpsColor(final double tps) {
        if (tps >= 18) {
            return SpongeCommand.GREEN;
        } else if (tps >= 15) {
            return SpongeCommand.YELLOW;
        } else if (tps >= 10) {
            return SpongeCommand.ORANGE;
        } else {
            return SpongeCommand.RED;
        }
    }

    private @NonNull CommandResult versionExecutor(final CommandContext context) {
        if (this.versionText == null) {
            final PluginContainer platformPlugin = Launch.instance().platformPlugin();

            final TextComponent.Builder builder = Component.text()
                    .append(
                            Component.text(platformPlugin.metadata().name().get(), Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD))
                    );

            final Component colon = Component.text(": ", NamedTextColor.GRAY);
            for (final PluginContainer container : Launch.instance().launcherPlugins()) {
                final PluginMetadata metadata = container.metadata();
                builder.append(
                        Component.newline(),
                        SpongeCommand.INDENT_COMPONENT,
                        Component.text(metadata.name().orElseGet(metadata::id), NamedTextColor.GRAY),
                        colon,
                        Component.text(container.metadata().version())
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

    private @NonNull CommandResult whichExecutor(final CommandContext context) {
        final CommandMapping mapping = context.requireOne(this.commandMappingKey);
        context.sendMessage(Identity.nil(), Component.text().append(
                this.title("Aliases: "),
                Component.join(Component.text(", "),
                    mapping.allAliases().stream().map(x -> Component.text(x, NamedTextColor.YELLOW)).collect(Collectors.toList())),
                Component.newline(),
                this.title("Owned by: "),
                this.hl(mapping.plugin().metadata().name().orElseGet(() -> mapping.plugin().metadata().id())))
                .build());
        return CommandResult.success();
    }

    private @NonNull CommandResult reloadGlobalExecutor(final CommandContext context) {
        SpongeConfigs.getCommon().reload()
            .whenComplete(($, error) -> {
                if (error != null) {
                    context.sendMessage(Identity.nil(), Component.text("Failed to reload global configuration. See the console for details.", NamedTextColor.RED));
                    SpongeCommon.logger().error("Failed to reload global configuration", error);
                } else {
                    context.sendMessage(Identity.nil(), Component.text("Successfully reloaded global configuration!", NamedTextColor.GREEN));
                }
            });
        return CommandResult.success();
    }

    private @NonNull CommandResult reloadWorldExecutor(final CommandContext context) {
        final ServerWorld target = context.requireOne(this.worldKey);
        final ResourceKey worldId = target.key();
        SpongeGameConfigs.getForWorld(target).reload()
            .whenComplete(($, error) -> {
            if (error != null) {
                context.sendMessage(Identity.nil(), Component.text(b ->
                    b.content("Failed to reload configuration for world ")
                        .append(Component.text(worldId.toString(), Style.style(TextDecoration.BOLD)))
                        .append(Component.text(". See the console for details."))
                        .color(NamedTextColor.RED)));
                SpongeCommon.logger().error("Failed to reload configuration of world '{}'", worldId, error);
            } else {
                context.sendMessage(Identity.nil(), Component.text("Successfully reloaded configuration for world ", NamedTextColor.GREEN)
                    .append(Component.text(worldId.toString(), Style.style(TextDecoration.BOLD)))
                    .append(Component.text("!")));
            }
        });
        return CommandResult.success();
    }

    // --

    protected Component getChunksInfo(final ServerWorld serverWorld) {
        if (((LevelBridge) serverWorld).bridge$isFake()) {
            return Component.text().append(Component.newline(), Component.text(serverWorld.key().asString() + " is a fake world")).build();
        }
        final ServerLevel serverLevel = (ServerLevel) serverWorld;
        final int entitiesToRemove = (int) serverWorld.entities().stream().filter(x -> ((Entity) x).removed).count();
        return LinearComponents.linear(
                this.key("Loaded chunks: "), this.value(serverLevel.getChunkSource().chunkMap.size()),
                Component.newline(),
                this.key("Entities: "), this.value(serverWorld.entities().size()),
                Component.newline(),
                this.key("Block Entities: "), this.value(serverWorld.blockEntities().size()),
                Component.newline(),
                this.key("Removed Entities:"), this.value(entitiesToRemove),
                Component.newline(),
                this.key("Removed Block Entities: "), this.value(((LevelBridge) serverLevel).bridge$blockEntitiesToUnload().size())
        );
    }

    protected Component key(final String text) {
        return Component.text(text, NamedTextColor.GOLD);
    }

    protected Component value(final int text) {
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
        builder.append(this.title(pluginMetadata.name().orElse(pluginMetadata.id())));
        builder.append(Component.text(" v" + pluginMetadata.version()));
    }

    private Component createContainerMeta(final PluginMetadata pluginMetadata) {
        final TextComponent.Builder builder = Component.text();
        this.createShortContainerMeta(builder, pluginMetadata);

        this.appendPluginMeta(builder, "ID", pluginMetadata.id());
        pluginMetadata.description().ifPresent(x -> this.appendPluginMeta(builder, "Description", x));
        pluginMetadata.links().homepage().ifPresent(x -> this.appendPluginMeta(builder, "Homepage", x));
        pluginMetadata.links().issues().ifPresent(x -> this.appendPluginMeta(builder, "Issues", x));
        pluginMetadata.links().source().ifPresent(x -> this.appendPluginMeta(builder, "Source", x));
        final Collection<PluginContributor> contributors = pluginMetadata.contributors();
        if (!contributors.isEmpty()) {
            builder.append(Component.newline()).append(SpongeCommand.INDENT_COMPONENT).append(this.title("Contributors:"));
            for (final PluginContributor contributor : contributors) {
                builder.append(Component.newline()).append(SpongeCommand.LONG_INDENT_COMPONENT).append(Component.text(contributor.name()));
                contributor.description().ifPresent(x -> builder.append(Component.text(" (" + x + ")")));
            }
        }

        this.appendPluginMeta(builder, "Main class", pluginMetadata.mainClass());
        return builder.build();
    }

}

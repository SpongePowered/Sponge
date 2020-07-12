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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class SpongeCommand {

    private final static Parameter.Value<PluginContainer> PLUGIN_CONTAINER_PARAMETER =
            Parameter.builder(PluginContainer.class)
                    .optional()
                    .parser(new FilteredPluginContainerParameter())
                    .setKey("plugin")
                    .build();


    private SpongeCommand() {
    }

    public static Command.Parameterized createSpongeCommand() {
        // /sponge audit
        final Command.Parameterized auditCommand = Command.builder()
                .setPermission("sponge.command.audit")
                .setExecutor(SpongeCommand::auditSubcommandExecutor)
                .build();

        // /sponge heap
        final Command.Parameterized heapCommand = Command.builder()
                .setPermission("sponge.command.heap")
                .setExecutor(SpongeCommand::heapSubcommandExecutor)
                .build();

        // /sponge plugins
        final Command.Parameterized pluginsReloadCommand = Command.builder()
                .setPermission("sponge.command.plugins.refresh")
                .setExecutor(SpongeCommand::pluginsRefreshSubcommandExecutor)
                .parameter(SpongeCommand.PLUGIN_CONTAINER_PARAMETER)
                .build();
        final Command.Parameterized pluginsCommand = Command.builder()
                .setPermission("sponge.command.plugins")
                .child(pluginsReloadCommand, "refresh")
                .setExecutor(SpongeCommand::pluginsSubcommand)
                .build();

        // /sponge timings
        final Command.Parameterized timingsCommand = SpongeCommand.timingsSubcommand();

        // /sponge
        return Command.builder()
                .setPermission("sponge.command.root")
                .setExecutor(SpongeCommand::rootCommand)
                .child(auditCommand, "audit")
                .child(heapCommand, "heap")
                .child(pluginsCommand, "plugins")
                .child(timingsCommand, "timings")
                .build();
    }

    @NonNull
    public static CommandResult rootCommand(final CommandContext context) {
        final PluginContainer platformPlugin = Launcher.getInstance().getPlatformPlugin();
        final PluginContainer apiPlugin = Launcher.getInstance().getApiPlugin();
        final PluginContainer minecraftPlugin = Launcher.getInstance().getMinecraftPlugin();

        SpongeCommon.getLogger().info("SpongePowered Minecraft Plugin Platform (running on Minecraft {})",
                minecraftPlugin.getMetadata().getVersion());
        SpongeCommon.getLogger().info("{} {}", apiPlugin.getMetadata().getName().get(), apiPlugin.getMetadata().getVersion());
        SpongeCommon.getLogger().info("Implementation: {}, {}",
                platformPlugin.getMetadata().getName().get(),
                platformPlugin.getMetadata().getVersion());

        /*
        context.sendMessage(Text.of(
                "SpongePowered Minecraft Plugin Platform (running on Minecraft " + minecraftPlugin.getMetadata().getVersion() + ")"));
        context.sendMessage(Text.of("SpongeAPI: ", apiPlugin.getMetadata().getName(), " ", apiPlugin.getMetadata().getVersion()));
        context.sendMessage(Text.of("Implementation: ", platformPlugin.getMetadata().getName(), " ", platformPlugin.getMetadata().getVersion()));
        */
        return CommandResult.success();
    }

    @NonNull
    public static CommandResult auditSubcommandExecutor(final CommandContext context) {
        SpongeCommon.getLogger().info("Starting Mixin Audit");
        Launcher.getInstance().auditMixins();
        return CommandResult.success();
    }

    @NonNull
    private static CommandResult heapSubcommandExecutor(final CommandContext context) {
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
    private static CommandResult pluginsSubcommand(final CommandContext context) {
        // we'll make it better in a bit.
        Launcher.getInstance().getPluginManager().getPlugins().forEach(x -> SpongeCommon.getLogger().info(x.getMetadata().getId()));
        return CommandResult.success();
    }

    @NonNull
    private static CommandResult pluginsRefreshSubcommandExecutor(final CommandContext context) {
        final Optional<PluginContainer> pluginContainer = context.getOne(SpongeCommand.PLUGIN_CONTAINER_PARAMETER);
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

    private static Command.Parameterized timingsSubcommand() {
        return Command.builder()
                .setPermission("sponge.command.timings")
                // .setShortDescription(Text.of("Manages Sponge Timings data to see performance of the server.")) //TODO: when text comes back
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.reset();
                            context.sendMessage(Text.of("Timings reset"));
                            return CommandResult.success();
                        })
                        .build(), "reset")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.generateReport(context.getMessageChannel());
                            return CommandResult.success();
                        })
                        .build(), "report", "paste")
                .child(Command.builder()
                        .setExecutor(context -> {
                            Timings.setTimingsEnabled(true);
                            context.sendMessage(Text.of("Enabled Timings & Reset"));
                            return CommandResult.success();
                        })
                        .build(), "on")
                .child(Command.builder()
                        .setExecutor(context -> {
                            Timings.setTimingsEnabled(false);
                            context.sendMessage(Text.of("Disabled Timings"));
                            return CommandResult.success();
                        })
                        .build(), "off")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(true);
                            context.sendMessage(Text.of("Enabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verbon")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(false);
                            context.sendMessage(Text.of("Disabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verboff")
                .child(Command.builder()
                        .setExecutor(context -> {
                            if (!Timings.isTimingsEnabled()) {
                                context.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            context.sendMessage(Text.of("Timings cost: " + SpongeTimingsFactory.getCost()));
                            return CommandResult.success();
                        })
                        .build(), "cost")
                .build();
    }
}

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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SpongeCommand {

    private final static Parameter.Value<PluginContainer> PLUGIN_CONTAINER_PARAMETER =
            Parameter.builder(PluginContainer.class)
                    .optional()
                    .parser(new FilteredPluginContainerParameter())
                    .setKey("plugin")
                    .build();

    public static Command.Parameterized createSpongeCommand() {
        final Command.Parameterized pluginsReloadCommand = Command.builder()
                .setExecutor(SpongeCommand::pluginsReloadSubcommand)
                .parameter(PLUGIN_CONTAINER_PARAMETER)
                .build();

        final Command.Parameterized auditCommand = Command.builder()
                .setExecutor(SpongeCommand::auditSubcommand)
                .build();

        final Command.Parameterized heapCommand = Command.builder()
                .setExecutor(SpongeCommand::heapSubcommand)
                .build();

        final Command.Parameterized pluginsCommand = Command.builder()
                .child(pluginsReloadCommand, "reload")
                .setExecutor(SpongeCommand::pluginsSubcommand)
                .build();


        return Command.builder()
                .setExecutor(SpongeCommand::rootCommand)
                .child(auditCommand, "audit")
                .child(pluginsCommand, "plugins")
                .child(heapCommand, "heap")
                .build();
    }

    @NonNull
    public static CommandResult rootCommand(final CommandContext context) {
        final PluginContainer platformPlugin = Launcher.getInstance().getPlatformPlugin();
        final PluginContainer apiPlugin =  Launcher.getInstance().getApiPlugin();
        final PluginContainer minecraftPlugin =  Launcher.getInstance().getMinecraftPlugin();

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
    public static CommandResult auditSubcommand(final CommandContext context) {
        SpongeCommon.getLogger().info("Starting Mixin Audit");
        // MixinEnvironment.getCurrentEnvironment().audit();
        return CommandResult.success();
    }

    @NonNull
    private static CommandResult heapSubcommand(final CommandContext context) {
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
    private static CommandResult pluginsReloadSubcommand(final CommandContext context) {
        final Optional<PluginContainer> pluginContainer = context.getOne(PLUGIN_CONTAINER_PARAMETER);
        final RefreshGameEvent event = SpongeEventFactory.createRefreshGameEvent(
                PhaseTracker.getCauseStackManager().getCurrentCause(),
                SpongeCommon.getGame()
        );
        if (pluginContainer.isPresent()) {
            // just send the reload event to that
            // src.sendMessage(Text.of("Sending reload event to " + pluginContainer.get().getMetadata().getId() + ". Please wait."));
            SpongeCommon.getLogger().info("Sending refresh event to {}. Please wait.", pluginContainer.get().getMetadata().getId());
            ((SpongeEventManager) SpongeCommon.getGame().getEventManager()).post(event, pluginContainer.get());
        } else {
            SpongeCommon.getLogger().info("Sending refresh event to all plugins. Please wait.");
            SpongeCommon.getGame().getEventManager().post(event);
        }

        SpongeCommon.getLogger().info("Completed plugin refresh.");
        return CommandResult.success();
    }

}

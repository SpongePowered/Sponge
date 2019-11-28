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
package org.spongepowered.common.mixin.core.command;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.command.ServerCommandManagerBridge;
import org.spongepowered.common.command.MinecraftCommandWrapper;
import org.spongepowered.common.command.SpongeCommandManager;
import org.spongepowered.common.command.WrapperCommandSource;
import org.spongepowered.common.config.category.PhaseTrackerCategory;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.util.VecHelper;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(ServerCommandManager.class)
public abstract class ServerCommandManagerMixin extends CommandHandler implements ServerCommandManagerBridge {

    private List<MinecraftCommandWrapper> impl$lowPriorityCommands = Lists.newArrayList();
    private List<MinecraftCommandWrapper> impl$earlyRegisterCommands = Lists.newArrayList();

    private static final CopyOnWriteArrayList<String> ASYNC_MOD_COMMAND_EXECUTORS = new CopyOnWriteArrayList<>();

    @Redirect(method = "notifyListener",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/WorldServer;getGameRules()Lnet/minecraft/world/GameRules;"))
    private GameRules impl$useSenderWorldGamerules(final ServerWorld overworld, final ICommandSender sender, final ICommand command,
        final int flags, final String translationKey, final Object... translationArgs) {
        // Check the game rules of the current world instead of overworld game rules
        return sender.getEntityWorld().getGameRules();
    }

    /**
     * @author zml
     * @author gabizou - April 18th, 2019 - 1.12.2
     * @reason Reroute MC command handling through Sponge, all commands
     * should flow through our systems for cause tracking, events, etc.
     * instead of Sponge having to inject into all the underlying command
     * handlers, potentially not targeting other command handlers.
     * Update April 18th, 2019 - 1.12.2:
     *  Force commands that are called asynchronously to be re-synced
     *  to the main thread. Some mods will attempt to call commands off
     *  their packet handling threads and end up causing exceptions
     */
    @Override
    public int executeCommand(final ICommandSender sender, String command) {
        command = command.trim();
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        final String cleanedCommand = command;
        if (!SpongeImplHooks.isMainThread()) {
            final PhaseTrackerCategory trackerConfig = SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker();

            final PluginContainer activeModContainer = SpongeImplHooks.getActiveModContainer();
            final String id = activeModContainer.getId();
            if (!trackerConfig.resyncCommandsAsync()) {
                if (trackerConfig.isVerbose()) {
                    new PrettyPrinter(60)
                        .add("Async Command Execution Warning").centre().hr()
                        .add("A plugin/mod attempted to perform a command asynchronously")
                        .add()
                        .add("Performing the command asynchronously and resycing it\n"
                             + "is disabled in Sponge's main configuration, therefor the command\n"
                             + "will be ignored. To enable re-submitting the command on the main\n"
                             + "thread, please enable \"resync-commands-from-async\" in the global\n"
                             + "config.")
                        .add()
                        .add("Details of the command:")
                        .add("%s : %s", "Command", command)
                        .add("%s : %s", "Offending Mod", id)
                        .add("%s : %s", "Sender", sender.getDisplayName() == null ? "null" : sender.getDisplayName().getUnformattedText())
                        .add("Stacktrace")
                        .add(new Exception("Async Command Executor"))
                        .trace(SpongeImpl.getLogger(), Level.WARN);
                }
                return 0;
            }
            if (!ASYNC_MOD_COMMAND_EXECUTORS.contains(id)) {
                ASYNC_MOD_COMMAND_EXECUTORS.add(id);
                if (trackerConfig.isVerbose()) {
                    new PrettyPrinter(60)
                        .add("Async Command Execution Warning").centre().hr()
                        .add("A plugin/mod attempted to perform a command asynchronously")
                        .add()
                        .add("Performing the command asynchronously and resycing it\n"
                             + "is enabled, so Sponge will resync the execution of the\n"
                             + "command on the main thread. Some cases where a mod is\n"
                             + "expecting the command to be run asynchronously to\n"
                             + "modify other objects may cause issues in said mod.")
                        .add()
                        .add("Details of the command:")
                        .add("%s : %s", "Command", command)
                        .add("%s : %s", "Offending Mod", id)
                        .add("%s : %s", "Sender", sender == null || sender.getDisplayName() == null ? "null" : sender.getDisplayName().getUnformattedText())
                        .add("Stacktrace")
                        .add(new Exception("Async Command Executor"))
                        .trace(SpongeImpl.getLogger(), Level.WARN);
                }

            }
            Task.builder().name("Sponge Async to Sync Command Executor")
                .execute(() -> {
                    executeCommand(sender, cleanedCommand);
                })
                .submit(activeModContainer);
            return 0;
        }

        final CommandSource source = WrapperCommandSource.of(sender);
        final CommandResult result = SpongeImpl.getGame().getCommandManager().process(source, cleanedCommand);
        result.getAffectedBlocks().ifPresent(integer4 -> sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, integer4));
        result.getAffectedEntities().ifPresent(integer3 -> sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, integer3));
        result.getAffectedItems().ifPresent(integer2 -> sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, integer2));
        result.getQueryResult().ifPresent(integer1 -> sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, integer1));
        result.getSuccessCount().ifPresent(integer -> sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, integer));
        return result.getSuccessCount().orElse(0);

        //return super.executeCommand(sender, command); // Try Vanilla instead
    }

    /**
     * @author zml
     *
     * Purpose: Reroute MC command handling through Sponge
     * Reasoning: All commands should go through one system -- we need none of the MC handling code
     */
    @Override
    public ICommand registerCommand(final ICommand command) {
        final MinecraftCommandWrapper cmd = bridge$wrapCommand(command);
        if ("minecraft".equalsIgnoreCase(cmd.getOwner().getId())) {
            this.impl$lowPriorityCommands.add(cmd);
        } else if (!SpongeImpl.isInitialized()) { // TODO: How?
            this.impl$earlyRegisterCommands.add(cmd);
        } else {
            SpongeImpl.getGame().getCommandManager().register(cmd.getOwner(), cmd, cmd.getNames());
            registerDefaultPermissions(SpongeImpl.getGame(), cmd);
        }
        return super.registerCommand(command);
    }

    @Override
    public MinecraftCommandWrapper bridge$wrapCommand(final ICommand command) {
        return new MinecraftCommandWrapper(SpongeImpl.getMinecraftPlugin(), command);
    }

    private void registerDefaultPermissions(final Game game, final MinecraftCommandWrapper cmd) {
        final Optional<PermissionService> perms = game.getServiceManager().provide(PermissionService.class);
        if (perms.isPresent()) {
            final PermissionService service = perms.get();
            final int opLevel = cmd.getPermissionLevel();
            if (opLevel == -1) {
                return;
            }
            if (service instanceof SpongePermissionService) {
                ((SpongePermissionService) service).getGroupForOpLevel(opLevel).getTransientSubjectData()
                    .setPermission(SubjectData.GLOBAL_CONTEXT, cmd.getCommandPermission(), Tristate.TRUE);
            } else if (opLevel == 0) {
                service.getDefaults().getTransientSubjectData()
                    .setPermission(SubjectData.GLOBAL_CONTEXT, cmd.getCommandPermission(), Tristate.TRUE);
            }
        }
    }

    private void registerCommandsList(final List<MinecraftCommandWrapper> cmds, final Game game) {
        for (final Iterator<MinecraftCommandWrapper> it = cmds.iterator(); it.hasNext();) {
            final MinecraftCommandWrapper cmd = it.next();
            it.remove();
            game.getCommandManager().register(cmd.getOwner(), cmd, cmd.getNames());
            registerDefaultPermissions(game, cmd);
        }
    }

    @Override
    public void bridge$registerLowPriorityCommands(final Game game) {
        registerCommandsList(this.impl$lowPriorityCommands, game);
    }

    @Override
    public void bridge$registerEarlyCommands(final Game game) {
        registerCommandsList(this.impl$earlyRegisterCommands, game);
    }

    /**
     * @author zml
     *
     * Purpose: Reroute MC command handling through Sponge
     * Reasoning: All commands should go through one system -- we need none of the MC handling code
     *
     * We redirect this method in MinecraftServer, to provide the real value of 'usingBlock'. This override is just for mods
     */
    @Override
    public List<String> getTabCompletions(final ICommandSender sender, final String input, @Nullable final BlockPos pos) {
        @Nullable Location<org.spongepowered.api.world.World> targetPos = null;
        if (pos != null) {
            targetPos = new Location<>((org.spongepowered.api.world.World) sender.getEntityWorld(), VecHelper.toVector3i(pos));
        }
        return ((SpongeCommandManager) SpongeImpl.getGame().getCommandManager()).getSuggestions((CommandSource) sender, input, targetPos, false);
    }
}

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
package org.spongepowered.common.mixin.core.commands.execution.tasks;

import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.tasks.ExecuteCommand;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.CommandPhaseContext;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;

import java.util.Optional;

@Mixin(ExecuteCommand.class)
public abstract class ExecuteCommandMixin {

    // @formatter:off
    @Shadow @Final private String commandInput;
    // @formatter:on

    @Redirect(method = "execute(Lnet/minecraft/commands/ExecutionCommandSource;Lnet/minecraft/commands/execution/ExecutionContext;Lnet/minecraft/commands/execution/Frame;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/context/ContextChain;runExecutable(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/Object;Lcom/mojang/brigadier/ResultConsumer;Z)I"))
    private <S> int impl$onRunExecutable(final CommandContext<S> executable, final S source, final ResultConsumer<S> resultConsumer, final boolean forkedMode) throws CommandSyntaxException {
        final @Nullable ServerPlayer player = ((CommandSourceStack) source).getPlayer();

        final Cause cause = ((CommandSourceStackBridge) source).bridge$getCause();
        final String originalRawCommand = cause.context().get(EventContextKeys.COMMAND).orElse(this.commandInput);
        final String[] origSplitArg = originalRawCommand.split(" ", 2);
        final String originalCommand = origSplitArg[0];
        final String originalArgs = origSplitArg.length == 2 ? origSplitArg[1] : "";

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final CommandSourceStackBridge sourceBridge = (CommandSourceStackBridge) source;
            frame.addContext(EventContextKeys.COMMAND, this.commandInput);
            sourceBridge.bridge$updateFrameFromCommandSource(frame);

            final CommandCause commandCause = sourceBridge.bridge$withCurrentCause();

            final String[] splitArg = this.commandInput.split(" ", 2);
            final String baseCommand = splitArg[0];
            final String args = splitArg.length == 2 ? splitArg[1] : "";
            final CommandMapping mapping = Sponge.server().commandManager().commandMapping(baseCommand).orElse(null);

            try (final CommandPhaseContext context = GeneralPhase.State.COMMAND
                    .createPhaseContext(PhaseTracker.getInstance())
                    .source(source)
                    .command(this.commandInput)
                    .commandMapping(mapping)) {
                if (player != null) {
                    context.creator(player.getUUID());
                    context.notifier(player.getUUID());
                }
                context.buildAndSwitch();

                final int result =  ContextChain.runExecutable(executable.copyFor((S) commandCause), source, resultConsumer, forkedMode);
                SpongeCommon.post(SpongeEventFactory.createExecuteCommandEventPost(
                        commandCause.cause(),
                        originalArgs,
                        args,
                        originalCommand,
                        baseCommand,
                        commandCause,
                        CommandResult.builder().result(result).build()
                ));
                return result;
            }
            catch (Exception e) {
                var owner = Optional.ofNullable(mapping)
                        .flatMap(CommandMapping::plugin)
                        .map(pc -> pc.metadata().id())
                        .orElse("UNKNOWN");
                SpongeCommon.logger().error("Could not execute command '{}' from plugin '{}'", this.commandInput, owner, e);
                throw e;
            }
        }
    }
}

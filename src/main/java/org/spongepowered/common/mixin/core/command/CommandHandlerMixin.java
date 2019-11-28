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

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.command.CommandHandlerBridge;
import org.spongepowered.common.bridge.command.ICommandBridge;
import org.spongepowered.common.command.MinecraftCommandWrapper;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;

@Mixin(CommandHandler.class)
public abstract class CommandHandlerMixin implements CommandHandlerBridge {

    private boolean impl$expandedSelector;

    @Inject(method = "tryExecute", at = @At(value = "HEAD"))
    private void impl$onExecuteCommandHead(
        final ICommandSender sender, final String[] args, final ICommand command, final String input, final CallbackInfoReturnable<Boolean> ci) {
        SpongeTimings.playerCommandTimer.startTimingIfSync();
        if (command instanceof ICommandBridge) {
            ((ICommandBridge) command).bridge$setExpandedSelector(this.bridge$isExpandedSelector());
        }
    }

    @Inject(method = "tryExecute", at = @At(value = "RETURN"))
    private void impl$nExecuteCommandReturn(
        final ICommandSender sender, final String[] args, final ICommand command, final String input, final CallbackInfoReturnable<Boolean> ci) {
        SpongeTimings.playerCommandTimer.stopTimingIfSync();
        if (command instanceof ICommandBridge) {
            ((ICommandBridge) command).bridge$setExpandedSelector(false);
        }
    }

    @Inject(method = "tryExecute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/command/ICommandSender;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V",
            ordinal = 2),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void impl$onCommandError(
        final ICommandSender sender, final String[] args, final ICommand command, final String input, final CallbackInfoReturnable<Boolean> cir,
                    final TranslationTextComponent comp, final Throwable error) {
        MinecraftCommandWrapper.setError(error);
        cir.setReturnValue(false);
    }

    @Surrogate
    public void impl$onCommandError(
        final ICommandSender sender, final String[] args, final ICommand command, final String input, final CallbackInfoReturnable<Boolean> cir,
                               final Throwable error, final TranslationTextComponent comp) {
        MinecraftCommandWrapper.setError(error);
        cir.setReturnValue(false);
    }

    @Override
    public boolean bridge$isExpandedSelector() {
        return this.impl$expandedSelector;
    }

    @Override
    public void bridge$setExpandedSelector(final boolean expandedSelector) {
        this.impl$expandedSelector = expandedSelector;
    }
}

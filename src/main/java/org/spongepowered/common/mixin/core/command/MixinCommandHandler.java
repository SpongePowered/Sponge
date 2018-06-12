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

import co.aikar.timings.SpongeTimings;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.command.MinecraftCommandWrapper;
import org.spongepowered.common.interfaces.command.IMixinCommandBase;
import org.spongepowered.common.interfaces.command.IMixinCommandHandler;

@Mixin(CommandHandler.class)
public abstract class MixinCommandHandler implements IMixinCommandHandler {

    private boolean expandedSelector;

    @Inject(method = "tryExecute", at = @At(value = "HEAD"))
    public void onExecuteCommandHead(ICommandSender sender, String[] args, ICommand command, String input, CallbackInfoReturnable<Boolean> ci) {
        SpongeTimings.playerCommandTimer.startTimingIfSync();
        if (command instanceof IMixinCommandBase) {
            ((IMixinCommandBase) command).setExpandedSelector(this.isExpandedSelector());
        }
    }

    @Inject(method = "tryExecute", at = @At(value = "RETURN"))
    public void onExecuteCommandReturn(ICommandSender sender, String[] args, ICommand command, String input, CallbackInfoReturnable<Boolean> ci) {
        SpongeTimings.playerCommandTimer.stopTimingIfSync();
        if (command instanceof IMixinCommandBase) {
            ((IMixinCommandBase) command).setExpandedSelector(false);
        }
    }

    @Inject(method = "tryExecute", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/ICommandSender;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V",
            ordinal = 2), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onCommandError(ICommandSender sender, String[] args, ICommand command, String input, CallbackInfoReturnable<Boolean> cir,
                    TextComponentTranslation comp, Throwable error) {
        MinecraftCommandWrapper.setError(error);
        cir.setReturnValue(false);
    }

    @Surrogate
    public void onCommandError(ICommandSender sender, String[] args, ICommand command, String input, CallbackInfoReturnable<Boolean> cir,
                               Throwable error, TextComponentTranslation comp) {
        MinecraftCommandWrapper.setError(error);
        cir.setReturnValue(false);
    }

    @Override
    public boolean isExpandedSelector() {
        return this.expandedSelector;
    }

    @Override
    public void setExpandedSelector(boolean expandedSelector) {
        this.expandedSelector = expandedSelector;
    }
}

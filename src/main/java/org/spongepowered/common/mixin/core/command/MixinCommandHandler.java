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
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.common.command.WrapperCommandSource;

import java.io.PrintWriter;
import java.io.StringWriter;

@Mixin(CommandHandler.class)
public abstract class MixinCommandHandler {

    @Inject(method = "tryExecute", at = @At(value = "INVOKE", target = "addChatComponent(Lnet/minecraft/util/IChatComponent)V;"))
    public void onCommandError(ICommandSender sender, String[] args, ICommand command, String input, Throwable error, ChatComponentTranslation comp) {
        if (WrapperCommandSource.of(sender).hasPermission("sponge.debug.hover-stacktrace")) {
            final StringWriter writer = new StringWriter();
            error.printStackTrace(new PrintWriter(writer));
            comp.getChatStyle().chatHoverEvent =
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(writer.toString().replaceAll("\t", "    ")));
        }
    }
}

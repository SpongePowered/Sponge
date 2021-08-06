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
package org.spongepowered.forge.mixin.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Commands.class)
public abstract class CommandsMixin_Forge {

    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;
    @Shadow @Final private static Logger LOGGER;

    /**
     * @author - Zidane
     *
     * TODO: THIS IS NOT TO REMAIN. THIS IS ONLY HERE FOR THE MOMENT
     */
    @Overwrite
    public int performCommand(CommandSourceStack param0, String param1) {
        StringReader var0 = new StringReader(param1);
        if (var0.canRead() && var0.peek() == '/') {
            var0.skip();
        }

        param0.getServer().getProfiler().push(param1);

        try {
            try {
                return this.dispatcher.execute(var0, param0);
            } catch (CommandRuntimeException var13) {
                param0.sendFailure(var13.getComponent());
                return 0;
            } catch (CommandSyntaxException var14) {
                param0.sendFailure(ComponentUtils.fromMessage(var14.getRawMessage()));
                if (var14.getInput() != null && var14.getCursor() >= 0) {
                    int var3 = Math.min(var14.getInput().length(), var14.getCursor());
                    MutableComponent var4 = (new TextComponent("")).withStyle(
                            ChatFormatting.GRAY).withStyle((param1x) -> param1x.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, param1)));
                    if (var3 > 10) {
                        var4.append("...");
                    }

                    var4.append(var14.getInput().substring(Math.max(0, var3 - 10), var3));
                    if (var3 < var14.getInput().length()) {
                        Component var5 = (new TextComponent(var14.getInput().substring(var3))).withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.UNDERLINE});
                        var4.append(var5);
                    }

                    var4.append((new TranslatableComponent("command.context.here")).withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}));
                    param0.sendFailure(var4);
                }
            } catch (Exception var15) {
                MutableComponent var7 = new TextComponent(var15.getMessage() == null ? var15.getClass().getName() : var15.getMessage());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Command exception: {}", param1, var15);
                    StackTraceElement[] var8 = var15.getStackTrace();

                    for(int var9 = 0; var9 < Math.min(var8.length, 3); ++var9) {
                        var7.append("\n\n").append(var8[var9].getMethodName()).append("\n ").append(var8[var9].getFileName()).append(":").append(String.valueOf(var8[var9].getLineNumber()));
                    }
                }

                param0.sendFailure((new TranslatableComponent("command.failed")).withStyle((param1x) -> param1x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, var7))));
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    param0.sendFailure(new TextComponent(Util.describeError(var15)));
                    LOGGER.error("'" + param1 + "' threw an exception", (Throwable)var15);
                }

                return 0;
            }

            return 0;
        } finally {
            param0.getServer().getProfiler().pop();
        }
    }
}

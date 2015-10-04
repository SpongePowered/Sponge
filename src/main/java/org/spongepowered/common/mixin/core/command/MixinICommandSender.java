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

import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.command.MinecraftCommandWrapper;
import org.spongepowered.common.interfaces.IMixinCommandSender;

import java.util.Optional;

@Mixin(value = {EntityPlayerMP.class, MinecraftServer.class, RConConsoleSource.class, CommandBlockLogic.class}, targets = IMixinCommandSender.SIGN_CLICK_SENDER)
public abstract class MixinICommandSender implements ICommandSender, IMixinCommandSender {

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String commandName) {
        Optional<? extends CommandMapping> mapping = Sponge.getGame().getCommandDispatcher().get(commandName);
        if (mapping.isPresent()) {
            CommandCallable callable = mapping.get().getCallable();
            if (callable instanceof MinecraftCommandWrapper) {
                return asCommandSource().hasPermission(((MinecraftCommandWrapper) callable).getCommandPermission());
            } else {
                return callable.testPermission(asCommandSource());
            }
        }
        return asCommandSource().hasPermission(commandName);
    }
}

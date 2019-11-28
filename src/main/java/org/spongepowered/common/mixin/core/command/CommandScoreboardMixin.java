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

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandScoreboard;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.bridge.command.ICommandBridge;

import java.util.Iterator;
import java.util.UUID;

/**
 * This mixin is required to make entity selectors work with Humans, when using
 * the '/scoreboard' command. Without it, Humans will be added by their UUID,
 */
@Mixin(CommandScoreboard.class)
public abstract class CommandScoreboardMixin extends CommandBase implements ICommandBridge {

    private String impl$realName;

    @Redirect(method = "joinTeam", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", ordinal = 0, remap = false))
    private Object onGetUUIDJoin(final Iterator<Entity> iterator) {
        final Entity entity = iterator.next();
        this.impl$onGetUUID(entity);
        return entity;
    }

    @Redirect(method = "leaveTeam", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", ordinal = 0, remap = false))
    private Object onGetUUIDLeave(final Iterator<Entity> iterator) {
        final Entity entity = iterator.next();
        this.impl$onGetUUID(entity);
        return entity;
    }

    @Redirect(method = "joinTeam",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/command/server/CommandScoreboard;getEntityName(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;Ljava/lang/String;)Ljava/lang/String;",
            ordinal = 0))
    private String impl$getEntityNameOrProxy(final MinecraftServer server, final ICommandSender sender, final String string) throws CommandException {
        return this.impl$onGetEntityName(server, sender, string);
    }

    @Redirect(method = "leaveTeam",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/command/server/CommandScoreboard;getEntityName"
                                                                        + "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;Ljava/lang/String;)Ljava/lang/String;",
            ordinal = 0))
    private String onGetEntityNameLeaveFirst(final MinecraftServer server, final ICommandSender sender, final String string) throws CommandException {
        return this.impl$onGetEntityName(server, sender, string);
    }

    private void impl$onGetUUID(final Entity entity) {
        if (entity instanceof EntityHuman) {
            this.impl$realName = entity.func_95999_t();
        }
    }

    private String impl$onGetEntityName(final MinecraftServer server, final ICommandSender sender, final String string) throws CommandException {
        if (this.impl$realName != null) {
            final String newString = this.impl$realName;
            this.impl$realName = null;
            return newString;
        }
        return CommandBase.func_184891_e(server, sender, string);
    }

    @Redirect(method = "leaveTeam",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/command/server/CommandScoreboard;getEntityName"
                     + "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/command/ICommandSender;Ljava/lang/String;)Ljava/lang/String;",
            ordinal = 1))
    private String onGetEntityNameLeaveSecond(final MinecraftServer server, final ICommandSender sender, final String string) throws CommandException {
        final String entityName = CommandBase.func_184891_e(server, sender, string);
        if (this.bridge$isExpandedSelector()) {
            try {
                final UUID uuid = UUID.fromString(entityName);
                final Entity entity = sender.getServer().func_175576_a(uuid);
                if (entity != null && entity instanceof EntityHuman) {
                    return entity.func_95999_t();
                }
            } catch (IllegalArgumentException e) {
                // Fall through to normal return
            }
        }
        return entityName;
    }
}

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
package org.spongepowered.common.mixin.command.multiworld;

import net.minecraft.command.CommandDefaultGameMode;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;

@Mixin(CommandDefaultGameMode.class)
public abstract class CommandDefaultGameModeMixin_MultiWorldCommand {

    /**
     * @author Minecrell - September 28, 2016
     * @author dualspiral - December 29, 2017
     * @reason Change game mode only in the world the command was executed in
     *         Only apply game mode to those without the override permission
     */
    @Redirect(method = "execute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/command/CommandDefaultGameMode;setDefaultGameType(Lnet/minecraft/world/GameType;Lnet/minecraft/server/MinecraftServer;)V"))
    private void multiWorldCommand$onSetDefaultGameType(final CommandDefaultGameMode self, final GameType type, final MinecraftServer server,
        final MinecraftServer server2, final ICommandSender sender, final String[] args) {

        final World world = sender.func_130014_f_();
        world.func_72912_H().func_76060_a(type);

        if (server.func_104056_am()) {
            for (final PlayerEntity player : world.field_73010_i) {
                if (!((EntityPlayerMPBridge) player).bridge$hasForcedGamemodeOverridePermission()) {
                    player.func_71033_a(type);
                }
            }
        }
    }

}

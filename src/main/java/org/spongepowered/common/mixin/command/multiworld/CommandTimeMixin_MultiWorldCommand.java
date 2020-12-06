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

import net.minecraft.command.CommandTime;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandTime.class)
public abstract class CommandTimeMixin_MultiWorldCommand {

    private boolean modificationCancelled = false;
    private boolean incrementCancelled = false;

    @Inject(method = "execute", cancellable = true,
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/command/CommandTime;setAllWorldTimes(Lnet/minecraft/server/MinecraftServer;I)V"))
    private void multiWorldCommand$cancelAfterSetWorldTime(CallbackInfo ci) {
        if (modificationCancelled) {
            ci.cancel();
            modificationCancelled = false;
        }
    }

    /**
     * @author Minecrell
     * @reason Modify time only in the command sender's world
     */
    @Redirect(method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/CommandTime;setAllWorldTimes(Lnet/minecraft/server/MinecraftServer;I)V"))
    private void multiWorldCommand$setWorldTime(final CommandTime self, final MinecraftServer server, final int time, final MinecraftServer server2,
                                                final ICommandSender sender, final String[] args) {
        sender.getEntityWorld().setWorldTime(time);

        if (time != sender.getEntityWorld().getWorldTime()) {
            modificationCancelled = true;
        }
    }

    @Inject(method = "execute", cancellable = true,
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/command/CommandTime;incrementAllWorldTimes(Lnet/minecraft/server/MinecraftServer;I)V"))
    private void multiWorldCommand$cancelAfterIncrementWorldTime(CallbackInfo ci) {
        if (incrementCancelled) {
            ci.cancel();
            incrementCancelled = false;
        }
    }

    /**
     * @author Minecrell
     * @reason Modify time only in the command sender's world
     */
    @Redirect(method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/CommandTime;incrementAllWorldTimes(Lnet/minecraft/server/MinecraftServer;I)V"))
    private void multiWorldCommand$incrementWorldTime(
        final CommandTime self, final MinecraftServer server, final int time, final MinecraftServer server2, final ICommandSender sender,
        final String[] args) {
        final long worldTime = sender.getEntityWorld().getWorldTime() + time;

        sender.getEntityWorld().setWorldTime(worldTime);

        if (worldTime != sender.getEntityWorld().getWorldTime()) {
            incrementCancelled = true;
        }
    }

}

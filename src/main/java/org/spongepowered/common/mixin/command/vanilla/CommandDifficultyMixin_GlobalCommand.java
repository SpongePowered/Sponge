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
package org.spongepowered.common.mixin.command.vanilla;

import net.minecraft.command.CommandDifficulty;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.world.WorldManager;

@Mixin(CommandDifficulty.class)
public abstract class CommandDifficultyMixin_GlobalCommand {

    /**
     * @author Minecrell - September 28, 2016
     * @author Zidane - January 31, 2018
     * @reason Change difficulty only in the world the command was executed in
     * @reason Redirect to the WorldManager
     */
    @Redirect(method = "execute",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;setDifficultyForAllWorlds(Lnet/minecraft/world/EnumDifficulty;)V"))
    private void globalCommand$adjustWorldsThroughManager(final MinecraftServer server, final Difficulty difficulty, final MinecraftServer server2,
        final ICommandSender sender, final String[] args) {
        for (final ServerWorld world : SpongeImpl.getServer().worlds) {
            WorldManager.adjustWorldForDifficulty(world, difficulty, true);
        }
    }
}

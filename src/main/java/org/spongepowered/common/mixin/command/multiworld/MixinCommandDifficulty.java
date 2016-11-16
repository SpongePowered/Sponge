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

import net.minecraft.command.CommandDifficulty;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandDifficulty.class)
public abstract class MixinCommandDifficulty {

    /**
     * @author Minecrell - September 28, 2016
     * @reason Change difficulty only in the world the command was executed in
     */
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;"
            + "setDifficultyForAllWorlds(Lnet/minecraft/world/EnumDifficulty;)V"))
    private void onSetDifficulty(MinecraftServer server, EnumDifficulty difficulty, MinecraftServer server2, ICommandSender sender, String[] args) {
        World world = sender.getEntityWorld();

        // Coped from MinecraftServer.setDifficultyForAllWorlds
        if (world.getWorldInfo().isHardcoreModeEnabled()) {
            world.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
            world.setAllowedSpawnTypes(true, true);
        } else if (server.isSinglePlayer()) {
            world.getWorldInfo().setDifficulty(difficulty);
            world.setAllowedSpawnTypes(world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
        } else {
            world.getWorldInfo().setDifficulty(difficulty);
            world.setAllowedSpawnTypes(server.allowSpawnMonsters(), server.getCanSpawnAnimals());
        }
    }

}

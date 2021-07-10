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
package org.spongepowered.common.mixin.core.server.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.server.commands.DifficultyCommand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;

@Mixin(DifficultyCommand.class)
public abstract class DifficultyCommandMixin {

    // @formatter:off
    @Shadow @Final private static DynamicCommandExceptionType ERROR_ALREADY_DIFFICULT;
    // @formatter:on

    /**
     * @author Zidane
     * @reason Only apply difficulty for the world the command was ran in (as in Sponge, all worlds have difficulties)
     */
    @Overwrite
    public static int setDifficulty(CommandSourceStack source, Difficulty difficulty) throws CommandSyntaxException {
        if (source.getLevel().getDifficulty() == difficulty) {
            throw DifficultyCommandMixin.ERROR_ALREADY_DIFFICULT.create(difficulty.getKey());
        } else {
            final LevelData levelData = source.getLevel().getLevelData();
            ((ServerWorldProperties) levelData).setDifficulty((org.spongepowered.api.world.difficulty.Difficulty) (Object) difficulty);
            source.getLevel().setSpawnSettings(((MinecraftServerAccessor) SpongeCommon.server()).invoker$isSpawningMonsters(), SpongeCommon.server().isSpawningAnimals());
            source.getLevel().getPlayers(p -> true).forEach(p -> p.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(),
                    levelData.isDifficultyLocked())));
            source.sendSuccess(new TranslatableComponent("commands.difficulty.success", difficulty.getDisplayName()), true);
            return 0;
        }
    }
}

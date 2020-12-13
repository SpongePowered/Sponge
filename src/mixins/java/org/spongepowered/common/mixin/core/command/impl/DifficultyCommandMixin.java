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
package org.spongepowered.common.mixin.core.command.impl;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.DifficultyCommand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DifficultyCommand.class)
public abstract class DifficultyCommandMixin {

    @Shadow @Final private static DynamicCommandExceptionType FAILED_EXCEPTION;

    /**
     * @author Zidane
     * @reason Only apply difficulty for the world the command was ran in (as in Sponge, all worlds have difficulties)
     */
    @Overwrite
    public static int setDifficulty(CommandSource source, Difficulty difficulty) throws CommandSyntaxException {
        if (source.getWorld().getDifficulty() == difficulty) {
            throw DifficultyCommandMixin.FAILED_EXCEPTION.create(difficulty.getTranslationKey());
        } else {
            source.getWorld().getWorldInfo().setDifficulty(difficulty);
            source.sendFeedback(new TranslationTextComponent("commands.difficulty.success", difficulty.getDisplayName()), true);
            return 0;
        }
    }
}

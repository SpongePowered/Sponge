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
package org.spongepowered.common.mixin.core.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParseContext;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla parses a command with a context that only knows the sender's
 * permissions and registries. Sponge's dispatcher parses for the sender
 * itself (its cause, its subject), and a source is a parse context of its
 * own, so that is what a command is parsed with.
 */
@Mixin(ParseContext.class)
public interface ParseContextMixin {

    @Inject(method = "of(Lnet/minecraft/commands/CommandSourceStack;)Lnet/minecraft/commands/ParseContext;", at = @At("HEAD"), cancellable = true)
    private static void impl$parseAsSender(final CommandSourceStack sender, final CallbackInfoReturnable<ParseContext> cir) {
        cir.setReturnValue((ParseContext) sender);
    }

    @Inject(method = "of(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/commands/ParseContext;", at = @At("HEAD"), cancellable = true)
    private static void impl$parseAsPlayer(final ServerPlayer sender, final CallbackInfoReturnable<ParseContext> cir) {
        cir.setReturnValue((ParseContext) sender.createCommandSourceStack());
    }
}

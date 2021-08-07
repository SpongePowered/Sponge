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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Commands.class)
public abstract class CommandsMixin_Forge {

    // @formatter:off
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;
    // @formatter:on

    // The event fired by Forge is fired in SpongeForgeCommandManager at the appropriate time.
    @Inject(method = "performCommand",
            slice = @Slice(from = @At("HEAD"),
                    to = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;"
                            + "parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;", remap = false)),
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/commands/Commands;dispatcher:Lcom/mojang/brigadier/CommandDispatcher;"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            cancellable = true)
    private void forge$redirectToSpongeCommandManager(final CommandSourceStack commandSourceStack,
            final String command,
            final CallbackInfoReturnable<Integer> cir,
            final StringReader stringReader) throws CommandSyntaxException {
        cir.setReturnValue(this.dispatcher.execute(stringReader, commandSourceStack));
    }

}

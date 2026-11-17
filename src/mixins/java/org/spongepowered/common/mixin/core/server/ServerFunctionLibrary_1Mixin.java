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
package org.spongepowered.common.mixin.core.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.ParseBox;
import net.minecraft.commands.ParseContext;
import net.minecraft.resources.Identifier;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.accessor.commands.CommandBoxAccessor;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeBoxDispatcher;
import org.spongepowered.common.event.tracking.PhaseTracker;

/**
 * The parser a function's lines are compiled with. The source Sponge parses
 * them as is created inside the parse, so the function's subject has to be on
 * the cause stack by then.
 */
@Mixin(targets = "net/minecraft/server/ServerFunctionLibrary$1")
public abstract class ServerFunctionLibrary_1Mixin {

    // @formatter:off
    @Shadow @Final Identifier val$id;
    // @formatter:on

    @WrapOperation(method = "parse", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/commands/ParseBox;parse(Lcom/mojang/brigadier/StringReader;Lnet/minecraft/commands/ParseContext;)Lnet/minecraft/commands/ParseBox$Result;"))
    private ParseBox.Result impl$compileAsFunctionSubject(
        final ParseBox box, final StringReader input, final ParseContext context, final Operation<ParseBox.Result> original) {
        if (!(((CommandBoxAccessor) box).accessor$dispatcher() instanceof final SpongeBoxDispatcher<?> dispatcher)) {
            return original.call(box, input, context);
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(dispatcher.permissionService()
                .newSubjectReference(PermissionService.SUBJECTS_FUNCTION, this.val$id.toString()).resolve().join());
            return original.call(box, input, context);
        }
    }
}

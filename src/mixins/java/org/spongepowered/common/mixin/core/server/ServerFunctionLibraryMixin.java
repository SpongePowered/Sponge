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

import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.server.ServerFunctionLibraryBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(ServerFunctionLibrary.class)
public abstract class ServerFunctionLibraryMixin implements ServerFunctionLibraryBridge {

    // @formatter:off
    @Shadow
    public abstract CompletableFuture<Void> shadow$reload(PreparableReloadListener.PreparationBarrier param0, ResourceManager param1, ProfilerFiller param2, ProfilerFiller param3, Executor param4, Executor param5);
    // @formatter:on

    private @Nullable Supplier<CompletableFuture<Void>> impl$capturedReload;

    @Override
    public void bridge$triggerCapturedReload() {
        if (this.impl$capturedReload != null) {
            this.impl$capturedReload.get();
            this.impl$capturedReload = null;
        }
    }

    @Inject(method = "reload", cancellable = true, at = @At("HEAD"))
    private void impl$captureReloadForReplay(
            final PreparableReloadListener.PreparationBarrier param0,
            final ResourceManager param1,
            final ProfilerFiller param2,
            final ProfilerFiller param3,
            final Executor param4,
            final Executor param5,
            final CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (!Sponge.isServerAvailable()) {
            this.impl$capturedReload = () -> this.shadow$reload(param0, param1, param2, param3, param4, param5);
            cir.setReturnValue(CompletableFuture.completedFuture(null));
        }
    }

    @Redirect(method = "reload",
            slice = @Slice(
                    from = @At(value = "INVOKE",
                            target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"),
                    to = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenCombine(Ljava/util/concurrent/CompletionStage;Ljava/util/function/BiFunction;)Ljava/util/concurrent/CompletableFuture;")
            ),
            at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<CommandFunction> impl$setCause(final CompletableFuture<Collection<ResourceLocation>> instance, final Function<Collection<ResourceLocation>, CompletionStage<CommandFunction>> function) {
        return instance.thenCompose(input -> {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(CommandSource.NULL);
                return function.apply(input);
            }
        });
    }

}

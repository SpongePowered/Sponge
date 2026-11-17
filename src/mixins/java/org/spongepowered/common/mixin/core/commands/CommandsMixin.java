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

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ParseBox;
import net.minecraft.commands.SuggestionBox;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.accessor.commands.ParseBoxAccessor;
import org.spongepowered.common.accessor.commands.SuggestionBoxAccessor;
import org.spongepowered.common.bridge.commands.CommandsBridge;
import org.spongepowered.common.command.brigadier.dispatcher.DelegatingCommandDispatcher;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeBoxDispatcher;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launch;

import java.util.function.Consumer;

@Mixin(Commands.class)
public abstract class CommandsMixin implements CommandsBridge {

    private CauseStackManager.@MonotonicNonNull StackFrame impl$initFrame = null;
    private @MonotonicNonNull SpongeCommandManager impl$commandManager;

    // We prepare our own dispatcher and commands manager, to redirect registrations to our system
    @Redirect(method = "<init>", at = @At(
        value = "NEW",
        args = "class=com/mojang/brigadier/CommandDispatcher",
        remap = false
    ))
    private CommandDispatcher<CommandSourceStack> impl$useSpongeDispatcher(final Consumer<CommandDispatcher<CommandSourceStack>> commandRegistration) {
        final @Nullable RegistryHolder registries = SpongeCommandManager.loadingRegistries();
        if (registries == null || !Launch.instance().pluginManager().isReady()) {
            return new CommandDispatcher<>();
        }
        final SpongeCommandManager manager = Launch.instance().lifecycle().platformInjector().getInstance(SpongeCommandManager.class);
        manager.init(registries);
        this.impl$commandManager = manager;
        return new DelegatingCommandDispatcher(manager.getBrigadierRegistrar());
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", remap = false))
    private void impl$setupStackFrameOnInit(final Consumer<Object> commandRegistration, final Object dispatcher) {
        if (Launch.instance().pluginManager().isReady()) {
            this.impl$initFrame = PhaseTracker.getInstance().pushCauseFrame();
            this.impl$initFrame.pushCause(Launch.instance().minecraftPlugin());
        }
        commandRegistration.accept(dispatcher);
    }

    // Vanilla copies the tree as it is now into its boxes. Ours changes afterwards and is not made of nodes
    // it can copy, so the boxes go through the Sponge dispatcher instead.
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/commands/SuggestionBox;fromFullTree(Lcom/mojang/brigadier/CommandDispatcher;)Lnet/minecraft/commands/SuggestionBox;"))
    private SuggestionBox impl$suggestWithSpongeDispatcher(final CommandDispatcher<CommandSourceStack> dispatcher) {
        if (dispatcher instanceof final DelegatingCommandDispatcher delegating) {
            return SuggestionBoxAccessor.invoker$new(new SpongeBoxDispatcher<>(delegating, true));
        }
        return SuggestionBox.fromFullTree(dispatcher);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/commands/ParseBox;fromFullTree(Lcom/mojang/brigadier/CommandDispatcher;)Lnet/minecraft/commands/ParseBox;"))
    private ParseBox impl$parseWithSpongeDispatcher(final CommandDispatcher<CommandSourceStack> dispatcher) {
        if (dispatcher instanceof final DelegatingCommandDispatcher delegating) {
            return ParseBoxAccessor.invoker$new(new SpongeBoxDispatcher<>(delegating, false));
        }
        return ParseBox.fromFullTree(dispatcher);
    }

    public void bridge$endVanillaRegistration() {
        if (this.impl$initFrame != null) {
            this.impl$initFrame.popCause();
            PhaseTracker.getInstance().popCauseFrame(this.impl$initFrame);
            this.impl$initFrame = null;
        }
    }

    @Override
    public SpongeCommandManager bridge$commandManager() {
        return this.impl$commandManager;
    }
}

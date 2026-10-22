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
package org.spongepowered.common.mixin.gametest;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.api.Server;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;

/**
 * Sponge only publishes dedicated and integrated servers, and only tracks the
 * vanilla {@code tickServer}; the game test server overrides both.
 */
@Mixin(GameTestServer.class)
public abstract class GameTestServerMixin_GameTest implements MinecraftServerBridge {

    // @formatter:off
    @Shadow private MultipleTestTracker testTracker;
    // @formatter:on

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gametest$setServerOnGame(final CallbackInfo ci) {
        SpongeCommon.game().setServer((Server) this);
    }

    // Implemented by every other server mixin; throwaway test worlds have no per-world autosave settings to honour
    @Override
    public boolean bridge$performAutosaveChecks() {
        return false;
    }

    // Test structures are spawned after super.tickServer() returns, outside the tracked server tick.
    // Under JUnit the engine starts the selected tests itself, so the server tracks nothing and starts nothing.
    @WrapMethod(method = "startTests")
    private void gametest$trackStartTests(final ServerLevel level, final Operation<Void> original) {
        if (this.bridge$insideTestEnvironment()) {
            this.testTracker = new MultipleTestTracker();
            return;
        }
        try (final PhaseContext<?> context = TickPhase.Tick.SERVER_TICK.createPhaseContext(PhaseTracker.SERVER)
            .server((MinecraftServer) (Object) this)
        ) {
            context.buildAndSwitch();
            original.call(level);
        }
    }

    // An empty tracker is done at once, which would halt the server before the engine runs anything
    @ModifyExpressionValue(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/gametest/framework/MultipleTestTracker;isDone()Z"))
    private boolean gametest$keepRunningUnderJUnit(final boolean done) {
        return done && !this.bridge$insideTestEnvironment();
    }
}

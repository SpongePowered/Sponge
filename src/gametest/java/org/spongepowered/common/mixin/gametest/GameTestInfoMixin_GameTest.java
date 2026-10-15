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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestRunner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.launch.Launch;

/**
 * Test functions and their delayed steps run from the game test ticker inside
 * the server tick, so every test would share one capture and one set of events.
 * Each test ticks in its own plugin context instead, like plugin code does.
 */
@Mixin(GameTestInfo.class)
public abstract class GameTestInfoMixin_GameTest {

    @WrapMethod(method = "tick")
    private void gametest$trackTestTick(final GameTestRunner runner, final Operation<Void> original) {
        try (final PhaseContext<?> context = PluginPhase.State.PLUGIN.createPhaseContext(PhaseTracker.SERVER)
            .source(this)
            .container(Launch.instance().commonPlugin())
        ) {
            context.buildAndSwitch();
            original.call(runner);
        }
    }

    // Until 1.21.5 a test starts in the tick its structure is placed, so the placement and the test's first actions share a
    // capture and its first events. A tick later, as later versions do, the test sees only what it does.
    @ModifyArg(method = "ensureStructureIsPlaced", at = @At(value = "INVOKE", target = "Lnet/minecraft/gametest/framework/GameTestInfo;startExecution(I)Lnet/minecraft/gametest/framework/GameTestInfo;"))
    private int gametest$startAfterPlacement(final int delay) {
        return delay + 1;
    }
}

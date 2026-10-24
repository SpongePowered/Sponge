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

import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.test.gametest.SpongeGameTests;

import java.util.Collection;
import java.util.Set;

/**
 * Test functions live in this static registry, which Forge and NeoForge fill from their own events. Adding
 * Sponge's as it loads works the same on every platform, before anything can look a test up.
 */
@Mixin(GameTestRegistry.class)
public abstract class GameTestRegistryMixin_GameTest {

    // @formatter:off
    @Shadow @Final private static Collection<TestFunction> TEST_FUNCTIONS;
    @Shadow @Final private static Set<String> TEST_CLASS_NAMES;
    // @formatter:on

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void gametest$registerSpongeFunctions(final CallbackInfo ci) {
        SpongeGameTests.all().forEach(test -> {
            GameTestRegistryMixin_GameTest.TEST_FUNCTIONS.add(test);
            GameTestRegistryMixin_GameTest.TEST_CLASS_NAMES.add(test.batchName());
        });
    }
}

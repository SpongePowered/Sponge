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

import net.minecraft.gametest.framework.BuiltinTestFunctions;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.test.gametest.SpongeGameTests;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Test functions live in a built-in registry that is filled and frozen during
 * {@code Bootstrap.bootStrap()}. That happens before mods load on Forge and
 * NeoForge, so hooking vanilla's own loader is the one point shared by every platform.
 */
@Mixin(BuiltinTestFunctions.class)
public abstract class BuiltinTestFunctionsMixin_GameTest {

    @Inject(method = "load", at = @At("TAIL"))
    private void gametest$registerSpongeFunctions(final BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> registry,
        final CallbackInfo ci
    ) {
        SpongeGameTests.all().forEach(test -> registry.accept(test.functionKey(), test.function()));
    }
}

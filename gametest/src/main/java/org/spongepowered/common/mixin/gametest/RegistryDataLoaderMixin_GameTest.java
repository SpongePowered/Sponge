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

import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.resources.RegistryLoadTaskAccessor;
import org.spongepowered.common.test.gametest.SpongeGameTests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Test instances are a datapack registry. Registering them from code here
 * mirrors what NeoForge's RegisterGameTestsEvent does, but works on Vanilla and Forge too.
 * Each registry loads asynchronously, so the instances are added once the datapack ones are in.
 */
@Mixin(ResourceManagerRegistryLoadTask.class)
public abstract class RegistryDataLoaderMixin_GameTest {

    @SuppressWarnings("unchecked")
    @Inject(method = "load", at = @At("RETURN"), cancellable = true)
    private void gametest$registerSpongeInstances(
        final RegistryOps.RegistryInfoLookup context,
        final Executor executor,
        final CallbackInfoReturnable<CompletableFuture<?>> cir
    ) {
        final WritableRegistry<?> registry = ((RegistryLoadTaskAccessor<?>) this).accessor$registry();
        if (!Registries.TEST_INSTANCE.equals(registry.key())) {
            return;
        }
        final HolderGetter<TestEnvironmentDefinition<?>> environments = context.<TestEnvironmentDefinition<?>>lookup(Registries.TEST_ENVIRONMENT)
            .orElseThrow()
            .getter();
        final WritableRegistry<GameTestInstance> instances = (WritableRegistry<GameTestInstance>) registry;
        cir.setReturnValue(cir.getReturnValue().thenRun(() -> SpongeGameTests.all().forEach(test -> test.tests().forEach((id, data) -> instances.register(
            ResourceKey.create(Registries.TEST_INSTANCE, id),
            new FunctionGameTestInstance(test.functionKey(), data.map(environments::getOrThrow)),
            RegistrationInfo.BUILT_IN
        )))));
    }
}

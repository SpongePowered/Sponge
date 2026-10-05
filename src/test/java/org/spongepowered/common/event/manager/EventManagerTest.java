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
package org.spongepowered.common.event.manager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.event.listener.SimpleListener;
import org.spongepowered.plugin.PluginContainer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EventManagerTest {

    private static ExecutorService executor;

    @BeforeAll
    public static void prepareExecutor() {
        EventManagerTest.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @AfterAll
    public static void closeExecutor() {
        EventManagerTest.executor.shutdownNow();
    }

    @Test
    public void testRegistrationThreadSafety() {
        final SpongeEventManager eventManager = (SpongeEventManager) Sponge.eventManager();
        final PluginContainer plugin = Mockito.mock(PluginContainer.class);

        final Object[] listeners = Stream.generate(SimpleListener::new).limit(2000).toArray();

        final int originalSize = eventManager.registeredListeners.size();
        doParallel(listeners, (listener) -> eventManager.registerListeners(plugin, listener));
        final int sizeAfterRegistration = eventManager.registeredListeners.size();
        doParallel(listeners, eventManager::unregisterListeners);
        final int finalSize = eventManager.registeredListeners.size();

        Assertions.assertEquals(2000, sizeAfterRegistration - originalSize, "registered listeners");
        Assertions.assertEquals(2000, sizeAfterRegistration - finalSize, "cleaned up listeners");
    }

    private static void doParallel(final Object[] listeners, Consumer<Object> action) {
        CompletableFuture.allOf(Stream.of(listeners).map((listener) -> CompletableFuture.runAsync(() -> action.accept(listener), EventManagerTest.executor)).toArray(CompletableFuture[]::new)).join();
    }

}

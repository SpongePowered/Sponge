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
package org.spongepowered.common.test.gametest;

import io.leangen.geantyref.TypeToken;
import net.minecraft.gametest.framework.GameTestEnvironments;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GeneratedTest;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.Order;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.test.gametest.block.ChangeBlockEventGameTests;
import org.spongepowered.common.test.gametest.block.PistonGameTests;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Collects every gametest visible to the running platform: the common tests
 * listed here, plus whatever the platform's own gametest source set provides
 * through {@link Provider} in {@code META-INF/services}.
 *
 * <p>Common tests are listed in code rather than in a service file because
 * all Sponge source sets are unioned into one module in development, where
 * same-named resources shadow each other and only one service file would win.</p>
 */
public final class SpongeGameTests {

    public interface Provider {
        List<GeneratedTest> tests();
    }

    private static final List<Provider> COMMON = List.of(
        new ChangeBlockEventGameTests(),
        new PistonGameTests()
    );

    public static List<GeneratedTest> all() {
        return Holder.ALL;
    }

    public static GeneratedTest test(final String name, final int maxTicks, final Consumer<GameTestHelper> function) {
        return SpongeGameTests.test(name, Identifier.withDefaultNamespace("empty"), maxTicks, function);
    }

    /** @param structure a template at {@code data/<namespace>/structure/<path>.nbt} */
    public static GeneratedTest test(final String name, final Identifier structure, final int maxTicks, final Consumer<GameTestHelper> function) {
        return SpongeGameTests.test(name, structure, maxTicks, true, function);
    }

    /** @param required false for a known issue: it still runs and is reported, but does not fail the run */
    public static GeneratedTest test(final String name, final Identifier structure, final int maxTicks, final boolean required,
        final Consumer<GameTestHelper> function
    ) {
        return new GeneratedTest(
            Identifier.fromNamespaceAndPath("sponge", name),
            new TestData<>(GameTestEnvironments.DEFAULT_KEY, structure, maxTicks, 0, required),
            function
        );
    }

    public static <E extends Event> void listen(final Class<E> type, final EventListener<? super E> listener) {
        SpongeGameTests.listen(TypeToken.get(type), Order.DEFAULT, listener);
    }

    // ponytail: a test that times out never reaches its unregister; harmless since listeners filter by position
    // and the gametest server exits afterwards. Unregister from a GameTestListener if tests ever share a long-lived server.
    public static <E extends Event> void listen(final TypeToken<E> type, final Order order, final EventListener<? super E> listener) {
        Sponge.eventManager().registerListener(EventListenerRegistration.builder(type)
            .plugin(Launch.instance().commonPlugin())
            .order(order)
            .listener(listener)
            .build());
    }

    // Resolved once: functions are registered at registry bootstrap, instances on every datapack load,
    // and both must agree on the same function objects.
    private static final class Holder {
        static final List<GeneratedTest> ALL = Stream.concat(
                SpongeGameTests.COMMON.stream(),
                ServiceLoader.load(Provider.class, Provider.class.getClassLoader()).stream().map(ServiceLoader.Provider::get)
            )
            .flatMap(provider -> provider.tests().stream())
            .toList();
    }

    private SpongeGameTests() {
    }
}

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
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.Order;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.test.gametest.block.ChangeBlockEventGameTests;
import org.spongepowered.common.test.gametest.block.PistonGameTests;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Collects every gametest visible to the running platform: the {@link GameTest}
 * methods of the common classes listed here, plus those of the platform classes
 * that implement {@link Provider} and are listed in {@code META-INF/services}.
 *
 * <p>Common classes are listed in code rather than in a service file because
 * all Sponge source sets are unioned into one module in development, where
 * same-named resources shadow each other and only one service file would win.</p>
 */
public final class SpongeGameTests {

    /** Marks a platform's gametest class, so it can be listed in {@code META-INF/services} */
    public interface Provider {
    }

    private static final List<Class<?>> COMMON = List.of(
        ChangeBlockEventGameTests.class,
        PistonGameTests.class
    );
    private static final Pattern WORD_START = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])");
    // 1.21.1 ships no empty test structure, so the default is ours
    private static final String EMPTY = "minecraft:empty";
    private static final String SPONGE_EMPTY = "sponge:empty";

    public static List<TestFunction> all() {
        return Holder.ALL;
    }

    public static boolean isTest(final Method method) {
        return method.isAnnotationPresent(GameTest.class);
    }

    /**
     * {@code <group>.<method_name>}, e.g. {@code piston.push_row} for {@code PistonGameTests#pushRow}. Test functions
     * are named rather than keyed before 1.21.5, and {@code /test} only takes unquoted words, so this is the
     * {@code sponge:<group>/<method_name>} id of later versions in the form vanilla names its own tests.
     */
    public static String name(final Method method) {
        return SpongeGameTests.group(method) + "." + SpongeGameTests.WORD_START.matcher(method.getName()).replaceAll("_").toLowerCase();
    }

    /** Runs every test of the class with {@code /test runall <group>} */
    public static String group(final Method method) {
        final GameTestGroup group = method.getDeclaringClass().getAnnotation(GameTestGroup.class);
        if (group == null) {
            throw new IllegalStateException(method.getDeclaringClass().getName() + " has gametests but no @GameTestGroup");
        }
        return group.value();
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

    private static Stream<TestFunction> tests(final Class<?> type) {
        final Object instance;
        try {
            final Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Gametest class " + type.getName() + " needs a no-argument constructor", e);
        }
        return Arrays.stream(type.getDeclaredMethods()).filter(SpongeGameTests::isTest).map(method -> SpongeGameTests.test(instance, method));
    }

    // Instance methods rather than static: IntelliJ only gives non-static methods a run icon
    private static TestFunction test(final Object instance, final Method method) {
        if (Modifier.isStatic(method.getModifiers()) || !Arrays.equals(method.getParameterTypes(), new Class<?>[] {GameTestHelper.class})) {
            throw new IllegalStateException("@GameTest " + method + " must not be static, and must take only a GameTestHelper");
        }
        final GameTest test = method.getAnnotation(GameTest.class);
        final String structure = SpongeGameTests.EMPTY.equals(test.structure()) ? SpongeGameTests.SPONGE_EMPTY : test.structure();
        return new TestFunction(SpongeGameTests.group(method), SpongeGameTests.name(method), structure, Rotation.NONE, test.maxTicks(),
            test.setupTicks(), test.required(), false, 1, 1, test.skyAccess(), SpongeGameTests.function(instance, method));
    }

    private static Consumer<GameTestHelper> function(final Object instance, final Method method) {
        final MethodHandle handle;
        try {
            method.setAccessible(true);
            handle = MethodHandles.lookup().unreflect(method).bindTo(instance);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return helper -> {
            try {
                handle.invoke(helper);
            } catch (final RuntimeException | Error e) {
                throw e;
            } catch (final Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }

    // Resolved once, when GameTestRegistry is first loaded
    private static final class Holder {
        static final List<TestFunction> ALL = Stream.concat(
                SpongeGameTests.COMMON.stream(),
                ServiceLoader.load(Provider.class, Provider.class.getClassLoader()).stream().map(ServiceLoader.Provider::type)
            )
            .flatMap(SpongeGameTests::tests)
            .toList();
    }

    private SpongeGameTests() {
    }
}

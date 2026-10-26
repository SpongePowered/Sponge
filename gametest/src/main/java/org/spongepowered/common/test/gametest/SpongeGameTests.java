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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.Order;
import org.spongepowered.common.launch.Launch;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Collects every gametest of the running launch: the {@link GameTest} methods of every {@link GameTestGroup} class
 * under the roots the build names, whether Sponge's own or a plugin's.
 */
public final class SpongeGameTests {

    /**
     * Where the gametest classes are, as the build knows: {@code [<plugin id>=]<path>&<path>;...}, each path a directory
     * of compiled classes or a jar. Sponge's own roots name no plugin, so its tests are in the {@code sponge} namespace.
     */
    public static final String ROOTS = "sponge.gametest.roots";

    private static final Pattern WORD_START = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])");
    private static final String GROUP = Type.getDescriptor(GameTestGroup.class);
    // Each found test class and the plugin its root names, if any
    private static final Map<Class<?>, String> OWNERS = new HashMap<>();

    public static List<GeneratedTest> all() {
        return Holder.ALL;
    }

    /** Whether the method is a gametest this launch can run: a {@link GameTest#client()} test only where a client is present */
    public static boolean isTest(final Method method) {
        final GameTest test = method.getAnnotation(GameTest.class);
        return test != null && (!test.client() || !Launch.instance().dedicatedServer());
    }

    /**
     * {@code <namespace>:<group>/<method_name>}, e.g. {@code sponge:piston/push_row} for {@code PistonGameTests#pushRow}.
     * The namespace is the id of the plugin the tests belong to, or {@code sponge} for Sponge's own.
     */
    public static ResourceLocation id(final Method method) {
        final Class<?> type = method.getDeclaringClass();
        final GameTestGroup group = type.getAnnotation(GameTestGroup.class);
        if (group == null) {
            throw new IllegalStateException(type.getName() + " has gametests but no @GameTestGroup");
        }
        final String plugin = group.plugin().isEmpty() ? SpongeGameTests.OWNERS.getOrDefault(type, "") : group.plugin();
        return ResourceLocation.fromNamespaceAndPath(plugin.isEmpty() ? "sponge" : plugin,
            group.value() + "/" + SpongeGameTests.WORD_START.matcher(method.getName()).replaceAll("_").toLowerCase());
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

    private static Stream<GeneratedTest> tests(final Class<?> type) {
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
    private static GeneratedTest test(final Object instance, final Method method) {
        if (Modifier.isStatic(method.getModifiers()) || !Arrays.equals(method.getParameterTypes(), new Class<?>[] {GameTestHelper.class})) {
            throw new IllegalStateException("@GameTest " + method + " must not be static, and must take only a GameTestHelper");
        }
        final GameTest test = method.getAnnotation(GameTest.class);
        return new GeneratedTest(
            SpongeGameTests.id(method),
            new TestData<>(GameTestEnvironments.DEFAULT_KEY, ResourceLocation.parse(test.structure()), test.maxTicks(), test.setupTicks(), test.required(),
                Rotation.NONE, false, 1, 1, test.skyAccess()),
            SpongeGameTests.function(instance, method)
        );
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

    // Resolved once: functions are registered at registry bootstrap, instances on every datapack load,
    // and both must agree on the same function objects. Every plugin is in the game layer by then
    private static final class Holder {
        static final List<GeneratedTest> ALL = SpongeGameTests.scan().stream().flatMap(SpongeGameTests::tests).toList();
    }

    /**
     * Every {@link GameTestGroup} class under the {@link #ROOTS}, read with ASM so that only test classes are loaded.
     * The roots only say where to look: the classes load through the game layer, so each root must be part of a
     * loaded plugin, as a plugin's test output is part of the plugin under test.
     */
    private static List<Class<?>> scan() {
        final @Nullable String roots = System.getProperty(SpongeGameTests.ROOTS);
        if (roots == null || roots.isBlank()) {
            throw new IllegalStateException("No gametest roots: the build passes -D" + SpongeGameTests.ROOTS + "=[<plugin id>=]<path>&<path>;...");
        }
        final Set<Class<?>> found = new LinkedHashSet<>();
        for (final String entry : roots.split(";")) {
            final int equals = entry.indexOf('=');
            final String prefix = equals < 0 ? "" : entry.substring(0, equals);
            // A path may hold '=', but a plugin id never holds a path separator
            final @Nullable String owner = prefix.isEmpty() || prefix.indexOf('/') >= 0 || prefix.indexOf('\\') >= 0 ? null : prefix;
            for (final String path : (owner == null ? entry : entry.substring(equals + 1)).split("&")) {
                if (!path.isEmpty()) {
                    SpongeGameTests.scan(Path.of(path), owner, found);
                }
            }
        }
        return List.copyOf(found);
    }

    private static void scan(final Path path, final @Nullable String owner, final Set<Class<?>> found) {
        if (Files.isDirectory(path)) {
            SpongeGameTests.scanRoot(path, owner, found);
        } else if (Files.isRegularFile(path)) {
            try (final FileSystem jar = FileSystems.newFileSystem(path)) {
                SpongeGameTests.scanRoot(jar.getPath("/"), owner, found);
            } catch (final IOException e) {
                throw new UncheckedIOException("Opening " + path + " for gametests", e);
            }
        }
        // A root that isn't there holds no tests: a source set with nothing in it is never compiled
    }

    private static void scanRoot(final Path root, final @Nullable String owner, final Set<Class<?>> found) {
        try (final Stream<Path> files = Files.walk(root)) {
            for (final Path file : files.filter(file -> file.toString().endsWith(".class")).toList()) {
                final String name = root.relativize(file).toString().replace(root.getFileSystem().getSeparator(), "/");
                if (name.startsWith("META-INF/") || name.equals("module-info.class")) {
                    continue;
                }
                try (final InputStream in = Files.newInputStream(file)) {
                    if (SpongeGameTests.isGroup(in)) {
                        // One loader serves the whole game layer, so this one reaches every plugin's classes
                        final Class<?> type = Class.forName(name.substring(0, name.length() - ".class".length()).replace('/', '.'), false, SpongeGameTests.class.getClassLoader());
                        if (owner != null) {
                            SpongeGameTests.OWNERS.put(type, owner);
                        }
                        found.add(type);
                    }
                }
            }
        } catch (final IOException e) {
            throw new UncheckedIOException("Scanning " + root + " for gametests", e);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Gametest class under " + root + " is not in a loaded plugin", e);
        }
    }

    private static boolean isGroup(final InputStream in) throws IOException {
        final boolean[] group = new boolean[1];
        new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
                group[0] |= SpongeGameTests.GROUP.equals(descriptor);
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return group[0];
    }

    private SpongeGameTests() {
    }
}

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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.opentest4j.TestAbortedException;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Runs {@link GameTest} methods under JUnit, whatever the selection: one method
 * from an IDE's run icon, or every test from a platform's {@code gameTest} task.
 * The selected tests run together, batched the way {@code /test run} batches them,
 * and each is reported as it passes or fails.
 *
 * <p>In the test environment the game test server is created on this thread,
 * starts no tests of its own, and is not ticked, so this ticks it until the
 * selected tests have finished.</p>
 */
public final class SpongeGameTestEngine implements TestEngine {

    // Where the first structure is placed; the game test world is a superflat with its surface at y=-60
    private static final BlockPos ORIGIN = new BlockPos(0, -59, 0);

    @Override
    public String getId() {
        return "sponge-gametest";
    }

    @Override
    public TestDescriptor discover(final EngineDiscoveryRequest request, final UniqueId uniqueId) {
        final EngineDescriptor engine = new EngineDescriptor(uniqueId, "Sponge gametests");
        EngineDiscoveryRequestResolver.<EngineDescriptor>builder()
            // Packages and classpath roots, when a launcher sends them; Gradle sends classes and leaves out the mixins
            .addClassContainerSelectorResolver(type -> !SpongeGameTestEngine.tests(type).isEmpty())
            .addSelectorResolver(new Resolver())
            .build()
            .resolve(request, engine);
        return engine;
    }

    @Override
    public void execute(final ExecutionRequest request) {
        final EngineExecutionListener listener = request.getEngineExecutionListener();
        final TestDescriptor root = request.getRootTestDescriptor();
        listener.executionStarted(root);
        root.getChildren().forEach(listener::executionStarted);
        SpongeGameTestEngine.run(root.getDescendants().stream()
            .filter(TestMethod.class::isInstance)
            .map(TestMethod.class::cast)
            .toList(), listener);
        root.getChildren().forEach(child -> listener.executionFinished(child, TestExecutionResult.successful()));
        listener.executionFinished(root, TestExecutionResult.successful());
    }

    private static void run(final List<TestMethod> tests, final EngineExecutionListener listener) {
        if (tests.isEmpty()) {
            return;
        }
        final MinecraftServer server = (MinecraftServer) Sponge.server();
        final ServerLevel level = server.overworld();
        final Map<GameTestInfo, TestMethod> running = new LinkedHashMap<>();
        for (final TestMethod test : tests) {
            listener.executionStarted(test);
            final Optional<Holder.Reference<GameTestInstance>> instance = server.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE)
                .get(ResourceKey.create(Registries.TEST_INSTANCE, test.id));
            if (instance.isEmpty()) {
                listener.executionFinished(test, TestExecutionResult.failed(new IllegalStateException(
                    test.id + " is not registered: list its class in SpongeGameTests, or in META-INF/services for a platform test")));
                continue;
            }
            final GameTestInfo info = new GameTestInfo(instance.get(), Rotation.NONE, level, RetryOptions.noRetries());
            info.addListener(new GameTestListener() {
                @Override
                public void testStructureLoaded(final GameTestInfo info) {
                }

                @Override
                public void testPassed(final GameTestInfo info, final GameTestRunner runner) {
                    listener.executionFinished(test, TestExecutionResult.successful());
                }

                @Override
                public void testFailed(final GameTestInfo info, final GameTestRunner runner) {
                    listener.executionFinished(test, SpongeGameTestEngine.failure(info));
                }

                @Override
                public void testAddedForRerun(final GameTestInfo original, final GameTestInfo rerun, final GameTestRunner runner) {
                }
            });
            running.put(info, test);
        }
        if (running.isEmpty()) {
            return;
        }

        // Starting places the first batch's structures right away, so like GameTestServer#startTests it needs a tracked tick
        try (final PhaseContext<?> context = TickPhase.Tick.SERVER_TICK.createPhaseContext(PhaseTracker.getServerInstanceExplicitly()).server(server)) {
            context.buildAndSwitch();
            GameTestRunner.Builder.fromInfo(running.keySet(), level)
                .newStructureSpawner(new StructureGridSpawner(SpongeGameTestEngine.ORIGIN, GameTestRunner.DEFAULT_TESTS_PER_ROW, false))
                .build()
                .start();
        }
        final MultipleTestTracker tracker = new MultipleTestTracker(running.keySet());
        while (!tracker.isDone() && server.isRunning()) {
            ((MinecraftServerBridge) server).bridge$tickServer(1);
        }
    }

    private static TestExecutionResult failure(final GameTestInfo info) {
        final String description = info.getError().getDescription().getString();
        if (info.isOptional()) {
            return TestExecutionResult.aborted(new TestAbortedException("Optional test failed: " + description, info.getError()));
        }
        return TestExecutionResult.failed(new AssertionError(description, info.getError()));
    }

    private static List<Method> tests(final Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods()).filter(SpongeGameTests::isTest).toList();
    }

    private static final class Resolver implements SelectorResolver {

        @Override
        public Resolution resolve(final ClassSelector selector, final Context context) {
            final Class<?> type = selector.getJavaClass();
            final List<Method> tests = SpongeGameTestEngine.tests(type);
            if (tests.isEmpty()) {
                return Resolution.unresolved();
            }
            return context.addToParent(parent -> Optional.of(new TestClass(parent.getUniqueId().append("class", type.getName()), type)))
                .map(descriptor -> Resolution.match(Match.exact(descriptor, () -> tests.stream()
                    .map(method -> DiscoverySelectors.selectMethod(type, method))
                    .collect(Collectors.toSet()))))
                .orElseGet(Resolution::unresolved);
        }

        @Override
        public Resolution resolve(final MethodSelector selector, final Context context) {
            final Method method = selector.getJavaMethod();
            if (!SpongeGameTests.isTest(method)) {
                return Resolution.unresolved();
            }
            return context.addToParent(() -> DiscoverySelectors.selectClass(selector.getJavaClass()),
                    parent -> Optional.of(new TestMethod(parent.getUniqueId().append("method", method.getName()), method)))
                .map(descriptor -> Resolution.match(Match.exact(descriptor)))
                .orElseGet(Resolution::unresolved);
        }
    }

    private static final class TestClass extends AbstractTestDescriptor {

        TestClass(final UniqueId uniqueId, final Class<?> type) {
            super(uniqueId, type.getSimpleName(), ClassSource.from(type));
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }
    }

    private static final class TestMethod extends AbstractTestDescriptor {

        final Identifier id;
        private final String methodName;

        TestMethod(final UniqueId uniqueId, final Method method) {
            super(uniqueId, SpongeGameTests.id(method).toString(), MethodSource.from(method));
            this.id = SpongeGameTests.id(method);
            this.methodName = method.getName();
        }

        // Gradle reports this as the test's name, and IntelliJ matches that against methods to mark them passed or failed
        @Override
        public String getLegacyReportingName() {
            return this.methodName;
        }

        @Override
        public Type getType() {
            return Type.TEST;
        }
    }
}

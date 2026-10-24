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
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureGridSpawner;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.checkerframework.checker.nullness.qual.Nullable;
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
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    // How long a passed test's structure must then stay as it was
    private static final int SETTLE_TICKS = 20;

    // The copy of this engine in the game layer, when the launcher found this one outside it
    private @Nullable TestEngine gameLayer;
    private boolean resolvedGameLayer;

    @Override
    public String getId() {
        return "sponge-gametest";
    }

    @Override
    public TestDescriptor discover(final EngineDiscoveryRequest request, final UniqueId uniqueId) {
        final @Nullable TestEngine gameLayer = this.gameLayer();
        if (gameLayer != null) {
            return gameLayer.discover(request, uniqueId);
        }
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
        final @Nullable TestEngine gameLayer = this.gameLayer();
        if (gameLayer != null) {
            gameLayer.execute(request);
            return;
        }
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

    /**
     * On NeoForge the launcher finds its engines outside the game layer (SpongeLauncherInterceptor leaves one call
     * alone because getResources is broken there), but tests, Sponge and Minecraft are only reachable inside it, where
     * the launcher runs discovery and execution. The game layer's copy of this engine does the work then.
     */
    private @Nullable TestEngine gameLayer() {
        if (!this.resolvedGameLayer) {
            this.resolvedGameLayer = true;
            final @Nullable ClassLoader context = Thread.currentThread().getContextClassLoader();
            if (context != null) {
                try {
                    final Class<?> engine = Class.forName(SpongeGameTestEngine.class.getName(), true, context);
                    if (engine != SpongeGameTestEngine.class) {
                        this.gameLayer = (TestEngine) engine.getConstructor().newInstance();
                    }
                } catch (final ReflectiveOperationException ignored) {
                    // Not running in a game layer at all
                }
            }
        }
        return this.gameLayer;
    }

    private static void run(final List<TestMethod> tests, final EngineExecutionListener listener) {
        if (tests.isEmpty()) {
            return;
        }
        final MinecraftServer server = (MinecraftServer) Sponge.server();
        final ServerLevel level = server.overworld();
        final Map<GameTestInfo, TestMethod> running = new LinkedHashMap<>();
        // Reported at the end, once the capture and any settle checks are in
        final Set<GameTestInfo> passed = new LinkedHashSet<>();
        // Tests opted in to settling, each with its structure's blocks as they were when it passed
        final Map<GameTestInfo, Map<BlockPos, BlockState>> settling = new LinkedHashMap<>();
        final Map<GameTestInfo, List<String>> problems = new HashMap<>();
        for (final TestMethod test : tests) {
            listener.executionStarted(test);
            final Optional<TestFunction> function = GameTestRegistry.getAllTestFunctions().stream()
                .filter(registered -> registered.testName().equals(test.name))
                .findFirst();
            if (function.isEmpty()) {
                listener.executionFinished(test, TestExecutionResult.failed(new IllegalStateException(
                    test.name + " is not registered: its class must be in a plugin of the game layer with @GameTestGroup")));
                continue;
            }
            final GameTestInfo info = new GameTestInfo(function.get(), Rotation.NONE, level, RetryOptions.noRetries());
            info.addListener(new GameTestListener() {
                @Override
                public void testStructureLoaded(final GameTestInfo info) {
                }

                @Override
                public void testPassed(final GameTestInfo info, final GameTestRunner runner) {
                    passed.add(info);
                    if (test.settle) {
                        settling.put(info, SpongeGameTestEngine.blocks(info));
                    }
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

        // Sponge lets a captured entity into the world, then throws its spawn event as the capture unwinds and removes
        // it again if that or a parent event is cancelled. So by the end of each tick, every entity added in a test's
        // structure must have had a spawn event or be gone; one still there without an event got past the capture.
        final Set<UUID> spawned = new HashSet<>();
        final EventListener<SpawnEntityEvent> spawns = event -> event.entities().forEach(spawn -> {
            final Entity entity = (Entity) spawn;
            spawned.add(entity.getUUID());
            SpongeGameTestEngine.containing(settling.keySet(), entity).ifPresent(info ->
                SpongeGameTestEngine.problem(problems, info, "spawned " + SpongeGameTestEngine.describe(info, entity) + " after passing"));
        });
        SpongeGameTests.listen(TypeToken.get(SpawnEntityEvent.class), Order.POST, spawns);
        final Map<Entity, GameTestInfo> added = new LinkedHashMap<>();
        CreatedEntities.listen(entity -> {
            if (!(entity instanceof Player)) {
                SpongeGameTestEngine.containing(running.keySet(), entity).ifPresent(info -> added.put(entity, info));
            }
        });
        final Runnable tick = () -> {
            ((MinecraftServerBridge) server).bridge$tickServer(1);
            added.forEach((entity, info) -> {
                if (!entity.isRemoved() && !spawned.contains(entity.getUUID())) {
                    SpongeGameTestEngine.problem(problems, info, SpongeGameTestEngine.describe(info, entity) + " was added without a SpawnEntityEvent");
                }
            });
            added.clear();
        };
        try {
            // Starting places the first batch's structures right away, so like GameTestServer#startTests it needs a tracked tick
            SpongeGameTestEngine.inServerTick(server, () -> GameTestRunner.Builder.fromInfo(running.keySet(), level)
                .newStructureSpawner(new StructureGridSpawner(SpongeGameTestEngine.ORIGIN, GameTestRunner.DEFAULT_TESTS_PER_ROW, false))
                .build()
                .start());
            final MultipleTestTracker tracker = new MultipleTestTracker(running.keySet());
            while (!tracker.isDone() && server.isRunning()) {
                tick.run();
            }

            // Nothing a settling test leaves behind may still act, such as a queued block event or a moving block. The
            // runner releases a batch's chunks once it finishes, and queued block events only run in ticking chunks, so
            // they're held loaded for the settle like a player standing nearby would.
            final Set<ChunkPos> chunks = new HashSet<>();
            settling.keySet().forEach(info -> SpongeGameTestEngine.bounds(info).intersectingChunks().forEach(chunks::add));
            chunks.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, true));
            for (int settled = 0; !settling.isEmpty() && settled < SpongeGameTestEngine.SETTLE_TICKS && server.isRunning(); settled++) {
                tick.run();
            }
            chunks.forEach(chunk -> level.setChunkForced(chunk.x, chunk.z, false));
            settling.forEach((info, blocks) -> blocks.forEach((pos, before) -> {
                final BlockState now = level.getBlockState(pos);
                if (now != before) {
                    SpongeGameTestEngine.problem(problems, info, SpongeGameTestEngine.relative(info, pos) + " changed from " + before + " to " + now + " after passing");
                }
            }));
        } finally {
            CreatedEntities.listen(null);
            Sponge.eventManager().unregisterListeners(spawns);
        }

        passed.forEach(info -> {
            final List<String> found = problems.getOrDefault(info, List.of());
            listener.executionFinished(running.get(info), found.isEmpty()
                ? TestExecutionResult.successful()
                : TestExecutionResult.failed(new AssertionError("Passed, but: " + String.join("; ", found))));
        });
    }

    private static void inServerTick(final MinecraftServer server, final Runnable action) {
        try (final PhaseContext<?> context = TickPhase.Tick.SERVER_TICK.createPhaseContext(PhaseTracker.SERVER).server(server)) {
            context.buildAndSwitch();
            action.run();
        }
    }

    private static BoundingBox bounds(final GameTestInfo info) {
        return StructureUtils.getStructureBoundingBox(info.getStructureBlockEntity());
    }

    private static Map<BlockPos, BlockState> blocks(final GameTestInfo info) {
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        BlockPos.betweenClosedStream(SpongeGameTestEngine.bounds(info))
            .forEach(pos -> blocks.put(pos.immutable(), info.getLevel().getBlockState(pos)));
        return blocks;
    }

    // The test whose structure the entity is in or next to: vanilla clears entities that close when a test passes
    private static Optional<GameTestInfo> containing(final Collection<GameTestInfo> infos, final Entity entity) {
        return infos.stream()
            .filter(info -> info.getLevel() == entity.level() && info.getStructureBlockPos() != null
                && info.getLevel().getBlockEntity(info.getStructureBlockPos()) instanceof StructureBlockEntity)
            .filter(info -> info.getStructureBounds().inflate(1.0).contains(entity.position()))
            .findFirst();
    }

    private static void problem(final Map<GameTestInfo, List<String>> problems, final GameTestInfo info, final String problem) {
        problems.computeIfAbsent(info, key -> new ArrayList<>()).add(problem);
    }

    private static String describe(final GameTestInfo info, final Entity entity) {
        return EntityType.getKey(entity.getType()).getPath() + " at " + SpongeGameTestEngine.relative(info, entity.blockPosition());
    }

    // Relative to the structure, as tests address it; the engine never rotates tests
    private static String relative(final GameTestInfo info, final BlockPos pos) {
        final BlockPos relative = pos.subtract(StructureUtils.getStructureOrigin(info.getStructureBlockEntity()));
        return "(" + relative.getX() + "," + relative.getY() + "," + relative.getZ() + ")";
    }

    private static TestExecutionResult failure(final GameTestInfo info) {
        final String description = String.valueOf(info.getError().getMessage());
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

        final String name;
        final boolean settle;
        private final String methodName;

        TestMethod(final UniqueId uniqueId, final Method method) {
            super(uniqueId, SpongeGameTests.name(method), MethodSource.from(method));
            this.name = SpongeGameTests.name(method);
            this.settle = method.getAnnotation(GameTest.class).settle();
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

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

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.TestReporter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Rotation;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Runs the gametests under JUnit, so the platform {@code gameTest} tasks report
 * them like any other test and collect their coverage.
 *
 * <p>In the test environment the game test server is created on this thread and
 * not ticked, so this ticks it until every test has finished and it halts.</p>
 */
public final class GameTestSuite {

    @TestFactory
    Stream<DynamicTest> gameTests() {
        final List<GameTestInfo> finished = new ArrayList<>();
        GlobalTestReporter.replaceWith(new TestReporter() {
            @Override
            public void onTestFailed(final GameTestInfo info) {
                finished.add(info);
            }

            @Override
            public void onTestSuccess(final GameTestInfo info) {
                finished.add(info);
            }
        });

        final MinecraftServer server = (MinecraftServer) Sponge.server();
        while (server.isRunning()) {
            ((MinecraftServerBridge) server).bridge$tickServer(1);
        }

        return finished.stream().map(info -> DynamicTest.dynamicTest(GameTestSuite.name(info), () -> {
            if (info.getError() == null) {
                return;
            }
            final String description = info.getError().getDescription().getString();
            if (!info.isRequired()) {
                Assumptions.abort("Optional test failed: " + description);
            }
            throw new AssertionError(description, info.getError());
        }));
    }

    private static String name(final GameTestInfo info) {
        return info.getRotation() == Rotation.NONE ? info.id().toString() : info.id() + " " + info.getRotation().getSerializedName();
    }
}

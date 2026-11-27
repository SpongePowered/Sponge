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
package org.spongepowered.common.test.gametest.world;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * Tests that saving the server writes its own data along with its worlds' data: the default world's
 * {@code level.dat}, and the server-level saved data that holds the scoreboard.
 */
@GameTestGroup("server_save")
public final class ServerSaveGameTests {

    @GameTest(maxTicks = 1)
    void saveWritesServerData(final GameTestHelper helper) {
        final MinecraftServer server = helper.getLevel().getServer();
        final ServerScoreboard scoreboard = server.getScoreboard();
        final Objective objective = scoreboard.addObjective(
            "gametest_save",
            ObjectiveCriteria.DUMMY,
            Component.literal("gametest_save"),
            ObjectiveCriteria.RenderType.INTEGER,
            false,
            null
        );
        try {
            final long before = Util.getEpochMillis();
            server.saveAllChunks(true, true, true);

            final CompoundTag levelData = ServerSaveGameTests.read(server.getWorldPath(LevelResource.LEVEL_DATA_FILE))
                .getCompoundOrEmpty("Data");
            GameTestAssertions.assertTrue(
                helper,
                levelData.getLongOr("LastPlayed", 0) >= before,
                "level.dat was not written by the save"
            );
            final long time = server.getWorldData().overworldData().getGameTime();
            GameTestAssertions.assertTrue(
                helper,
                levelData.getLongOr("Time", -1) == time,
                "level.dat has time " + levelData.getLongOr("Time", -1) + " rather than the default world's " + time
            );

            final Path scores = server.getWorldPath(LevelResource.DATA).resolve("minecraft").resolve("scoreboard.dat");
            boolean saved = false;
            for (final Tag tag : ServerSaveGameTests.read(scores)
                .getCompoundOrEmpty("data")
                .getListOrEmpty("Objectives")) {
                saved |= tag instanceof final CompoundTag saveData
                    && objective.getName().equals(saveData.getStringOr("Name", ""));
            }
            GameTestAssertions.assertTrue(helper, saved, "The saved scoreboard is missing the objective");
        } finally {
            scoreboard.removeObjective(objective);
        }
        helper.succeed();
    }

    private static CompoundTag read(final Path file) {
        try {
            return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

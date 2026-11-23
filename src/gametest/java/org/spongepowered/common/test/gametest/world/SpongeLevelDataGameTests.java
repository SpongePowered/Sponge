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
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * A world's {@link ServerWorld#uniqueId() unique id} is written to its {@code level.dat} on save and can be read back
 * from there. Sponge keeps that id in its own data compound inside the vanilla level data, so saving the world and
 * parsing the file back exercises the write side of Sponge's level data together with the on-disk layout it depends on.
 */
@GameTestGroup("level_data")
public final class SpongeLevelDataGameTests {

    @GameTest(maxTicks = 20 * 60 * 5)
    void levelDataStoresTheWorldId(final GameTestHelper helper) {
        final ServerWorld overworld = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT)
            .orElseThrow(() -> new IllegalStateException("The overworld is not loaded"));
        final UUID expected = overworld.uniqueId();

        // Writes level.dat through the same path a normal save uses
        SpongeCommon.server().saveAllChunks(false, true, true);

        final Path levelDat = SpongeCommon.server().getWorldPath(LevelResource.ROOT).resolve("level.dat");
        try {
            helper.assertTrue(Files.isRegularFile(levelDat), "level.dat was not written to " + levelDat);
            final CompoundTag root = NbtIo.readCompressed(levelDat, NbtAccounter.unlimitedHeap());
            final CompoundTag spongeData = root.getCompound("SpongeData");
            helper.assertTrue(spongeData.contains("UUID"), "SpongeData has no UUID at the root of level.dat");
            helper.assertTrue(SpongeLevelDataGameTests.readUuid(spongeData.getIntArray("UUID")).equals(expected),
                "level.dat stores a different world id than the loaded world");
            helper.succeed();
        } catch (final IOException e) {
            helper.fail("Could not read level.dat: " + e.getMessage());
        }
    }

    private static UUID readUuid(final int[] parts) {
        final long most = ((long) parts[0] << 32) | (parts[1] & 0xffffffffL);
        final long least = ((long) parts[2] << 32) | (parts[3] & 0xffffffffL);
        return new UUID(most, least);
    }
}

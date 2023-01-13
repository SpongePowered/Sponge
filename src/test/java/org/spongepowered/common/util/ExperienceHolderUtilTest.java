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
package org.spongepowered.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Streams;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spongepowered.common.launch.SpongeExtension;

import java.util.Iterator;
import java.util.UUID;
import java.util.stream.Stream;

@ExtendWith(SpongeExtension.class)
public final class ExperienceHolderUtilTest {

    private Iterable<int[]> generateTestParameters() {
        final Level level = mock(Level.class);
        when(level.getLevelData()).thenReturn(mock(PrimaryLevelData.class));

        final Player player = new Player(level, BlockPos.ZERO, 0, new GameProfile(UUID.randomUUID(), "Player")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };

        return () -> new Iterator<int[]>() {
            private int level;
            private int startExp;

            @Override
            public boolean hasNext() {
                return level < 50;
            }

            @Override
            public int[] next() {
                player.experienceLevel = this.level;
                int[] data = {this.level, this.startExp, player.getXpNeededForNextLevel()};
                this.startExp += player.getXpNeededForNextLevel();
                this.level++;
                return data;
            }
        };
    }

    @TestFactory
    public Stream<DynamicTest> generateExpTests() {
        return Streams.stream(generateTestParameters()).map(params -> {
            final int level = params[0], startExp = params[1], expInLevel = params[2];

            return DynamicTest.dynamicTest("Exp level " + level, () -> {
                assertEquals(expInLevel, ExperienceHolderUtil.getExpBetweenLevels(level));
                assertEquals(startExp, ExperienceHolderUtil.xpAtLevel(level));
                assertEquals(level, ExperienceHolderUtil.getLevelForExp(startExp));
                assertEquals(level, ExperienceHolderUtil.getLevelForExp(startExp + 1));
            });
        });
    }
}

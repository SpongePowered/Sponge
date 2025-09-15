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

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ExperienceHolderUtilTest {

    public static List<Arguments> testExp() {
        final Level world = mock(Level.class);
        when(world.getLevelData()).thenReturn(mock(PrimaryLevelData.class));

        final Player player = new Player(world, BlockPos.ZERO, 0, new GameProfile(UUID.randomUUID(), "Player")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };

        final List<Arguments> list = new ArrayList<>();

        int level = 0;
        int startExp = 0;
        while (level < 50) {
            player.experienceLevel = level;
            list.add(Arguments.of(level, startExp, player.getXpNeededForNextLevel()));
            startExp += player.getXpNeededForNextLevel();
            level++;
        }

        return list;
    }

    @MethodSource
    @ParameterizedTest
    public void testExp(final int level, final int startExp, final int expInLevel) {
        assertEquals(expInLevel, ExperienceHolderUtil.getExpBetweenLevels(level));
        assertEquals(startExp, ExperienceHolderUtil.xpAtLevel(level));
        assertEquals(level, ExperienceHolderUtil.getLevelForExp(startExp));
        assertEquals(level, ExperienceHolderUtil.getLevelForExp(startExp + 1));
    }
}

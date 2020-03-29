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
package org.spongepowered.common.data.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;

import java.util.Iterator;
import java.util.UUID;

@RunWith(Parameterized.class)
public final class ExperienceHolderUtilsTest {

    private final int level;
    private final int startExp;
    private final int expInLevel;

    public ExperienceHolderUtilsTest(int level, int startExp, int expInLevel) {
        this.level = level;
        this.startExp = startExp;
        this.expInLevel = expInLevel;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        World world = new World(null, new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false) {

            @Override
            protected IChunkProvider createChunkProvider() {
                return null;
            }

            @Override
            protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
                return false;
            }
        };
        //noinspection EntityConstructor
        EntityPlayer player = new EntityPlayer(world, new GameProfile(UUID.randomUUID(), "Player")) {

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        return () -> new Iterator<Object[]>() {

            private int level;
            private int startExp;

            @Override
            public boolean hasNext() {
                return level < 50;
            }

            @Override
            public Object[] next() {
                player.experienceLevel = level;
                Object[] data = {level, startExp, player.xpBarCap()};
                startExp += player.xpBarCap();
                level++;
                return data;
            }
        };
    }

    @Test
    public void testGetExpBetweenLevels() {
        Assert.assertEquals(expInLevel, ExperienceHolderUtils.getExpBetweenLevels(level));
    }

    @Test
    public void testXpAtLevel() {
        Assert.assertEquals(startExp, ExperienceHolderUtils.xpAtLevel(level));
    }

    @Test
    public void testGetLevelForXpStart() {
        Assert.assertEquals(level, ExperienceHolderUtils.getLevelForExp(startExp));
    }

    @Test
    public void testGetLevelForXpMiddle() {
        Assert.assertEquals(level, ExperienceHolderUtils.getLevelForExp(startExp + 1));
    }
}

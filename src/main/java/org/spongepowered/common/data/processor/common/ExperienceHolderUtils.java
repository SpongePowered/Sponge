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
package org.spongepowered.common.data.processor.common;

public class ExperienceHolderUtils {

    private static final int XP_AT_LEVEL_30 = xpAtLevel(30);
    private static final int XP_AT_LEVEL_15 = xpAtLevel(15);

    // If these formulas change, make sure to change all these methods and then
    // run ExperienceHolderUtilsTest to check your results.

    /**
     * A static version of {@link EntityPlayer#xpBarCap()}.
     *
     * @param level The player's level
     * @return The amount of XP between the specified level and the next level
     */
    public static int getExpBetweenLevels(final int level) {
        return level >= 30 ? 112 + (level - 30) * 9 : level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
    }

    /**
     * Utility method for getting the total experience at an arbitrary level.
     * The formulas here are basically (slightly modified) integrals of those
     * of {@link EntityPlayer#xpBarCap()}.
     *
     * @param level The player's level
     * @return The total amount of XP a player would have if they are exactly
     * at the start of the specified level
     */
    public static int xpAtLevel(final int level) {
        if (level > 30) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level > 15) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return level * level + 6 * level;
        }
    }

    /**
     * Utility method for getting the level a player would have if they had the
     * provided amount of experience. The formulas here are inverses of the
     * integrals in {@link #xpAtLevel(int)} using the quadratic formula, with
     * several values precomputed.
     *
     * @param experience The player's experience
     * @return The level the player would be at
     */
    public static int getLevelForExp(final int experience) {
        if (experience >= XP_AT_LEVEL_30) {
            return (int) ((162.5 + Math.sqrt(-13553.75 + 18 * experience)) / 9);
        } else if (experience >= XP_AT_LEVEL_15) {
            return (int) ((40.5 + Math.sqrt(-1959.75 + 10 * experience)) / 5);
        } else {
            return (int) (-3 + Math.sqrt(9 + experience));
        }
    }
}

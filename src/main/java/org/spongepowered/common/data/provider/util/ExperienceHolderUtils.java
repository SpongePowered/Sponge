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
package org.spongepowered.common.data.provider.util;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;

public final class ExperienceHolderUtils {

    public static final int XP_AT_LEVEL_30 = ExperienceHolderUtils.xpAtLevel(30);
    public static final int XP_AT_LEVEL_15 = ExperienceHolderUtils.xpAtLevel(15);

    private ExperienceHolderUtils() {
    }

    // If these formulas change, make sure to change all these methods and then
    // run ExperienceHolderUtilsTest to check your results.

    /**
     * A static version of {@link PlayerEntity#xpBarCap()}.
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
     * of {@link PlayerEntity#xpBarCap()}.
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

    public static void setExperience(final PlayerEntity holder, final Integer value) {
        int level = -1;

        int experienceForCurrentLevel;
        int experienceAtNextLevel = -1;

        // We work iteratively to get the level. Remember, the level variable contains the CURRENT level and the method
        // calculates what we need to get to the NEXT level, so we work our way up, summing up all these intervals, until
        // we get an experience value that is larger than the value. This gives us our level.
        //
        // If the cumulative experience required for level+1 is still below that (or in the edge case, equal to) our
        // value, we need to go up a level. So, if the boundary is at 7 exp, and we have 7 exp, we need one more loop
        // to increment the level as we are at 100% and therefore should be at level+1.
        do {
            // We need this later.
            experienceForCurrentLevel = experienceAtNextLevel;

            // Increment level, as we know we are at least that level (in the first instance -1 -> 0)
            // and add the next amount of experience to the variable.
            experienceAtNextLevel += getExpBetweenLevels(++level);
        } while (experienceAtNextLevel <= value && experienceAtNextLevel > 0);

        // If experience for current level is still -1 that means that the holder has never gained a level nor has
        // ever gained any experience. Negative experience makes no sense at all. If this is not set to 0 the below math calculation
        // will always result in the actual experience on the holder being above 0 when in reality there is no experience gained.
        if (value == 0) {
            experienceForCurrentLevel = Math.max(0, experienceForCurrentLevel);
        }

        // Once we're here, we have the correct level. The experience is the decimal fraction that we are through the
        // current level. This is why we require the experienceForCurrentLevel variable, we need the difference between
        // the current value and the beginning of the level.
        holder.experience = (float) (value - experienceForCurrentLevel) / getExpBetweenLevels(level);
        holder.experienceLevel = level;
        holder.experienceTotal = value;

        if (holder instanceof ServerPlayerEntityBridge) {
            ((ServerPlayerEntityBridge) holder).bridge$refreshExp();
        }
    }

    public static void setExperienceSinceLevel(final PlayerEntity holder, Integer value) {
        while (value >= holder.xpBarCap()) {
            value -= holder.xpBarCap();
        }
        ((PlayerEntityBridge) holder).bridge$setExperienceSinceLevel(value);

        if (holder instanceof ServerPlayerEntityBridge) {
            ((ServerPlayerEntityBridge) holder).bridge$refreshExp();
        }
    }
}

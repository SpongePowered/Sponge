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
package org.spongepowered.common.data.type;

import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.api.statistic.StatisticFormats;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.statistic.StatisticGroups;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;

public class SpongeStatisticGroup implements StatisticGroup {

    private final String id;

    public SpongeStatisticGroup(String id) {
        this.id = id;
    }

    public SpongeStatisticGroup() {
        this("GENERAL");
    }

    public static StatisticGroup getGroupByID(String stat) {
        if (stat.startsWith("stat.entityKilledBy.")) {
            return StatisticGroups.KILLED_BY_ENTITY;
        } else if (stat.startsWith("stat.killEntity.")) {
            return StatisticGroups.HAS_KILLED_ENTITY;
        } else if (stat.startsWith("stat.craftItem.")) {
            return StatisticGroups.CRAFT_ITEM;
        } else if (stat.startsWith("stat.useItem.")) {
            return StatisticGroups.USE_ITEM;
        } else if (stat.startsWith("stat.breakItem.")) {
            return StatisticGroups.BREAK_ITEM;
        } else if (stat.startsWith("stat.mineBlock.")) {
            return StatisticGroups.MINE_BLOCK;
        } else if (stat.startsWith("stat.craftBlock.")) {
            return StatisticGroups.CRAFT_BLOCK;
        } else if (stat.startsWith("stat.useBlock.")) {
            return StatisticGroups.USE_BLOCK;
        } else if (stat.startsWith("teamkill.")) {
            return StatisticGroups.HAS_KILLED_TEAM;
        } else if (stat.startsWith("killedByTeam.")) {
            return StatisticGroups.KILLED_BY_TEAM;
        } else if (stat.startsWith("stat.")) {
            return StatisticGroups.GENERAL;
        } else {
            return StatisticGroups.HIDDEN;
        }
    }

    @Override
    public StatisticFormat getDefaultStatisticFormat() {
        return StatisticFormats.COUNT;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id;
    }

    @Override
    public Translation getTranslation() {
        switch (this.id) {
            case "HIDDEN":
                return new FixedTranslation("Hidden");
            case "HAS_KILLED_ENTITY":
                return new FixedTranslation("HasKilledEntity");
            case "KILLED_BY_ENTITY":
                return new FixedTranslation("KilledByEntity");
            case "CRAFT_ITEM":
                return new FixedTranslation("CraftItem");
            case "USE_ITEM":
                return new FixedTranslation("UseItem");
            case "BREAK_ITEM":
                return new FixedTranslation("BreakItem");
            case "CRAFT_BLOCK":
                return new FixedTranslation("CraftBlock");
            case "USE_BLOCK":
                return new FixedTranslation("UseBlock");
            case "MINE_BLOCK":
                return new FixedTranslation("MineBlock");
            case "HAS_KILLED_TEAM":
                return new FixedTranslation("HasKilledTeam");
            case "KILLED_BY_TEAM":
                return new FixedTranslation("KilledByTeam");
            case "GENERAL":
            default:
                return new FixedTranslation("General");
        }
    }
}

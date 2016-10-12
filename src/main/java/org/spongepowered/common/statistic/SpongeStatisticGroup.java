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
package org.spongepowered.common.statistic;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.statistic.StatisticGroups;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Map;

public class SpongeStatisticGroup extends SpongeCatalogType.Translatable implements StatisticGroup {

    // Initialization happens too early for SpongeRegistry
    private static boolean initialized = false;

    public static synchronized void init() {
        if (!initialized) {
            initialized = true;
            Map<String, StatisticGroup> groups = Maps.newLinkedHashMap();
            groups.put("general", of("sponge:general", "GENERAL", "stat.generalButton"));
            groups.put("hidden", of("sponge:hidden", "HIDDEN", "stats.tooltip.type.achievement"));
            groups.put("has_killed_entity", of("sponge:hasKilledEntity", "HAS_KILLED_ENTITY", "stat.entityKills"));
            groups.put("killed_by_entity", of("sponge:killedByEntity", "KILLED_BY_ENTITY", "stat.entityKilledBy"));
            groups.put("craft_item", of("sponge:craftItem", "CRAFT_ITEM", "stat.crafted"));
            groups.put("use_item", of("sponge:useItem", "USE_ITEM", "stat.used"));
            groups.put("break_item", of("sponge:breakItem", "BREAK_ITEM", "stat.depleted"));
            groups.put("pick_up_item", of("sponge:pickUpItem", "PICK_UP_ITEM", "stat.pickup"));
            groups.put("drop_item", of("sponge:dropItem", "DROP_ITEM", "stat.drop"));
            groups.put("craft_block", of("sponge:craftBlock", "CRAFT_BLOCK", "stat.crafted"));
            groups.put("use_block", of("sponge:useBlock", "USE_BLOCK", "stat.used"));
            groups.put("mine_block", of("sponge:mineBlock", "MINE_BLOCK", "stat.mined"));
            groups.put("pick_up_block", of("sponge:pickUpBlock", "PICK_UP_BLOCK", "stat.pickup"));
            groups.put("drop_block", of("sponge:dropItem", "DROP_BLOCK", "stat.drop"));
            // TODO: Add proper Translation or remove them entirely
            groups.put("has_killed_team", of("sponge:hasKilledTeam", "HAS_KILLED_TEAM", "sponge:hasKilledTeam"));
            groups.put("killed_by_team", of("sponge:killedByTeam", "KILLED_BY_TEAM", "sponge:killedByTeam"));
            RegistryHelper.mapFields(StatisticGroups.class, groups);
        }
    }

    private static StatisticGroup of(String id, String name, String translation) {
        return new SpongeStatisticGroup(checkNotNull(id, "id"), checkNotNull(name, "name"),
                new SpongeTranslation(checkNotNull(translation, "translation")));
    }

    private final String name;
    private final StatisticFormat format;

    public SpongeStatisticGroup(String id, String name, Translation translation) {
        this(id, name, translation, (StatisticFormat) StatBase.simpleStatType);
    }

    public SpongeStatisticGroup(String id, String name, Translation translation, StatisticFormat format) {
        super(id, translation);
        this.name = checkNotNull(name, "name");
        this.format = checkNotNull(format, "format");
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public StatisticFormat getDefaultStatisticFormat() {
        return this.format;
    }

}

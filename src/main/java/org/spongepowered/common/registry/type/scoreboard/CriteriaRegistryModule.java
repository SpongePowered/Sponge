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
package org.spongepowered.common.registry.type.scoreboard;

import com.google.common.collect.Maps;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;
import org.spongepowered.common.registry.type.text.TextColorRegistryModule;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Map;

@RegistrationDependency(TextColorRegistryModule.class)
@RegisterCatalog(Criteria.class)
public final class CriteriaRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<Criterion> {

    public static CriteriaRegistryModule getInstance() {
        return CriteriaRegistryModule.Holder.INSTANCE;
    }

    public final Map<String, Criterion> teamKillMappings = Maps.newLinkedHashMap();
    public final Map<String, Criterion> killedByTeamMappings = Maps.newLinkedHashMap();

    CriteriaRegistryModule() {
        super("minecraft", new String[]{"minecraft"}, id -> id.replace("_count", "s"));
    }

    @Override
    public void registerDefaults() {
        register((Criterion) IScoreCriteria.DUMMY);
        register((Criterion) IScoreCriteria.TRIGGER);
        register((Criterion) IScoreCriteria.HEALTH);
        register((Criterion) IScoreCriteria.PLAYER_KILL_COUNT);
        register((Criterion) IScoreCriteria.TOTAL_KILL_COUNT);
        register((Criterion) IScoreCriteria.DEATH_COUNT);
        register((Criterion) IScoreCriteria.FOOD);
        register((Criterion) IScoreCriteria.AIR);
        register((Criterion) IScoreCriteria.ARMOR);
        register((Criterion) IScoreCriteria.XP);
        register((Criterion) IScoreCriteria.LEVEL);

        for (Map.Entry<TextFormatting, SpongeTextColor> entry : TextColorRegistryModule.enumChatColor.entrySet()) {
            final int colorIndex = entry.getKey().getColorIndex();

            if (colorIndex < 0) {
                continue;
            }

            final String id = entry.getValue().getId();

            final Criterion teamKillCriterion = (Criterion) IScoreCriteria.TEAM_KILL[colorIndex];
            register(teamKillCriterion);
            teamKillMappings.put(id, teamKillCriterion);

            final Criterion killedByTeamCriterion = (Criterion) IScoreCriteria.KILLED_BY_TEAM[colorIndex];
            register(killedByTeamCriterion);
            killedByTeamMappings.put(id, killedByTeamCriterion);
        }
    }

    private static final class Holder {
        static final CriteriaRegistryModule INSTANCE = new CriteriaRegistryModule();
    }

}

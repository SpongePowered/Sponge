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
package org.spongepowered.common.mixin.core.scoreboard;

import com.google.common.collect.Maps;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(Scoreboard.class)
public interface ScoreboardAccessor {

    @Accessor("entitiesScoreObjectives") Map<String, Map<ScoreObjective, Score>>  accessor$getEntitiesScoreObjectivesMap();

    @Accessor("scoreObjectives") Map<String, ScoreObjective> accessor$getScoreObjectives();

    @Accessor("scoreObjectiveCriterias") Map<IScoreCriteria, List<ScoreObjective>> accessor$getScoreObjectiveCriterias();

    @Accessor("entitiesScoreObjectives") Map<String, Map<ScoreObjective, Score>> accessor$getEntitiesScoreObjectives();

    @Accessor("objectiveDisplaySlots") ScoreObjective[] accessor$getObjectiveDisplaySlots();

    @Accessor("teams") Map<String, ScorePlayerTeam> accessor$getTeams();

    @Accessor("teamMemberships") Map<String, ScorePlayerTeam> accessor$getTeamMemberships();

    @Accessor("displaySlots") String[] accessor$getDisplaySlots();


}

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

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.scoreboard.ScoreboardBridge;

import java.util.List;
import java.util.Map;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin implements ScoreboardBridge {

    @Shadow @Final private Map<ScoreCriteria, List<ScoreObjective>> scoreObjectiveCriterias;
    @Shadow @Final private Map<String, ScoreObjective> scoreObjectives;
    @Shadow @Final private Map<String, Map<ScoreObjective, Score>> entitiesScoreObjectives;
    @Shadow @Final private Map<String, ScorePlayerTeam> teams;
    @Shadow @Final private Map<String, ScorePlayerTeam> teamMemberships;
    @Shadow @Final private ScoreObjective[] objectiveDisplaySlots;

    private boolean impl$isClient;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setIsClient(final CallbackInfo ci) {
        this.impl$isClient = !((Scoreboard) (Object) this instanceof ServerScoreboard);
    }

    @Override
    public boolean bridge$isClient() {
        return this.impl$isClient;
    }

    // TODO - Remove all of these once Mixin 0.8 is released to fix AccessorMixins in Mixins
    @Override
    public Map<ScoreCriteria, List<ScoreObjective>> accessor$getScoreObjectiveCriterias() {
        return this.scoreObjectiveCriterias;
    }

    @Override
    public Map<String, ScoreObjective> accessor$getScoreObjectives() {
        return this.scoreObjectives;
    }

    @Override
    public Map<String, Map<ScoreObjective, Score>> accessor$getEntitiesScoreObjectives() {
        return this.entitiesScoreObjectives;
    }

    @Override
    public Map<String, ScorePlayerTeam> accessor$getTeams() {
        return this.teams;
    }

    @Override
    public Map<String, ScorePlayerTeam> accessor$getTeamMemberships() {
        return this.teamMemberships;
    }

    @Override
    public ScoreObjective[] accessor$getObjectiveDisplaySlots() {
        return this.objectiveDisplaySlots;
    }
}

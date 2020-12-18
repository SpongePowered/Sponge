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
package org.spongepowered.common.mixin.api.mcp.scoreboard;

import net.kyori.adventure.text.Component;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.registry.SimpleRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.accessor.scoreboard.ScoreboardAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.scoreboard.ScoreObjectiveBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.scoreboard.SpongeObjective;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(ServerScoreboard.class)
@Implements(@Interface(iface = Scoreboard.class, prefix = "scoreboard$"))
public abstract class ServerScoreboardMixin_API implements Scoreboard {

    @Intrinsic
    public Optional<Objective> scoreboard$getObjective(final String name) {
        Objects.requireNonNull(name);

        final ScoreObjective objective = ((ScoreboardAccessor) this).accessor$objectivesByName().get(name);
        return Optional.ofNullable(objective == null ? null : ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
    }

    @Override
    public Optional<Objective> getObjective(final DisplaySlot slot) {
        final SimpleRegistry<DisplaySlot> registry = (SimpleRegistry<DisplaySlot>) Sponge.getGame().registries().registry(RegistryTypes.DISPLAY_SLOT);
        final ScoreObjective objective = ((ScoreboardAccessor) this).accessor$displayObjectives()[registry.getId(slot)];
        if (objective != null) {
            return Optional.of(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective());
        }
        return Optional.empty();
    }

    @Override
    public Set<Objective> getObjectivesByCriterion(final Criterion criterion) {
        if (((ScoreboardAccessor) this).accessor$objectivesByCriteria().containsKey(criterion)) {
            return ((ScoreboardAccessor) this).accessor$objectivesByCriteria().get(criterion).stream()
                .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective()).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    @Override
    public void addObjective(final Objective objective) throws IllegalArgumentException {
        Objects.requireNonNull(objective);

        ((ServerScoreboardBridge) this).bridge$addObjective(objective);
    }

    @Override
    public void updateDisplaySlot(@Nullable final Objective objective, final DisplaySlot displaySlot) throws IllegalStateException {
        Objects.requireNonNull(displaySlot);

        ((ServerScoreboardBridge) this).bridge$updateDisplaySlot(objective, displaySlot);
    }

    @Override
    public Set<Objective> getObjectives() {
        return ((ScoreboardAccessor) this).accessor$objectivesByName().values().stream()
            .map(objective -> ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective())
            .collect(Collectors.toSet());
    }

    @Override
    public void removeObjective(final Objective objective) {
        Objects.requireNonNull(objective);

        ((ServerScoreboardBridge) this).bridge$removeObjective(objective);
    }

    @Override
    public Set<org.spongepowered.api.scoreboard.Score> getScores() {
        final Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (final ScoreObjective objective: ((ScoreboardAccessor) this).accessor$objectivesByName().values()) {
            scores.addAll(((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getScores().values());
        }
        return scores;
    }

    @Override
    public Set<org.spongepowered.api.scoreboard.Score> getScores(final Component name) {
        Objects.requireNonNull(name);

        final Set<org.spongepowered.api.scoreboard.Score> scores = new HashSet<>();
        for (final ScoreObjective objective: ((ScoreboardAccessor) this).accessor$objectivesByName().values()) {
            ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective().getScore(name).ifPresent(scores::add);
        }
        return scores;
    }

    @Override
    public void removeScores(final Component name) {
        Objects.requireNonNull(name);

        for (final ScoreObjective objective: ((ScoreboardAccessor) this).accessor$objectivesByName().values()) {
            final SpongeObjective spongeObjective = ((ScoreObjectiveBridge) objective).bridge$getSpongeObjective();
            spongeObjective.getScore(name).ifPresent(spongeObjective::removeScore);
        }
    }

    @Intrinsic
    public Optional<Team> scoreboard$getTeam(final String name) {
        Objects.requireNonNull(name);

        return Optional.ofNullable((Team) ((ScoreboardAccessor) this).accessor$teamsByName().get(name));
    }

    @Override
    public void registerTeam(final Team team) throws IllegalArgumentException {
        Objects.requireNonNull(team);

        ((ServerScoreboardBridge) this).bridge$registerTeam(team);
    }

    @Intrinsic
    public Set<Team> scoreboard$getTeams() {
        return new HashSet<>((Collection<Team>) (Object) ((ScoreboardAccessor) this).accessor$teamsByName().values());
    }

    @Override
    public Optional<Team> getMemberTeam(final Component member) {
        Objects.requireNonNull(member);

        return Optional.ofNullable((Team) ((ScoreboardAccessor) this).accessor$teamsByPlayer().get(SpongeAdventure.legacySection(member)));
    }
}

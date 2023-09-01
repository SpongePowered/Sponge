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
package org.spongepowered.common.bridge.server;

import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.objective.Objective;

import java.util.Optional;
import java.util.Set;


public interface ServerScoreboardBridge {

    void bridge$addPlayer(ServerPlayer player, boolean sendPackets);

    void bridge$removePlayer(ServerPlayer player, boolean sendPackets);

    Optional<Objective> bridge$getObjective(String name);

    void bridge$addObjective(Objective objective);

    void bridge$updateDisplaySlot(@Nullable Objective objective, DisplaySlot displaySlot) throws IllegalStateException;

    void bridge$updateDisplaySlot(@Nullable Objective objective, int slot) throws IllegalStateException;

    Optional<Objective> bridge$getObjective(DisplaySlot slot);

    Set<Objective> bridge$getObjectivesByCriterion(Criterion criterion);

    void bridge$removeObjective(Objective objective);

    void bridge$registerTeam(Team spongeTeam);

    Set<org.spongepowered.api.scoreboard.Score> bridge$getScores();

    Set<org.spongepowered.api.scoreboard.Score> bridge$getScores(Component name);

    void bridge$removeScores(Component name);

    void bridge$sendToPlayers(Packet<?> packet);
}

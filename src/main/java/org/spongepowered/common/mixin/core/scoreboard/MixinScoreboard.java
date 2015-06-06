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

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinScoreObjective;
import org.spongepowered.common.interfaces.IMixinScoreboard;
import org.spongepowered.common.interfaces.IMixinTeam;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.scoreboard.SpongeObjective;
import org.spongepowered.common.scoreboard.SpongeScore;
import org.spongepowered.common.scoreboard.SpongeScoreboard;
import org.spongepowered.common.scoreboard.SpongeTeam;

import java.util.Collection;

@NonnullByDefault
@Mixin(Scoreboard.class)
public abstract class MixinScoreboard implements IMixinScoreboard {

    public SpongeScoreboard scoreboard;

    private boolean echoToSponge = true;

    @Shadow public abstract Collection getTeams();
    @Shadow public abstract Collection getScoreObjectives();

    @Override
    public void setSpongeScoreboard(SpongeScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public SpongeScoreboard getSpongeScoreboard() {
        return this.scoreboard;
    }

    @Override
    public boolean echoToSponge() {
        return this.echoToSponge;
    }

    @Inject(method = "addScoreObjective", at = @At("HEAD"), cancellable = true)
    public void onAddToScoreboardStart(String name, IScoreObjectiveCriteria criterion, CallbackInfoReturnable<ScoreObjective> ci) {
        if (this.shouldEcho()) {
            this.scoreboard.allowRecursion = false;
            SpongeObjective spongeObjective = new SpongeObjective(name, (Criterion) criterion);
            this.scoreboard.addObjective(spongeObjective);
            this.scoreboard.allowRecursion = true;
            ci.setReturnValue(spongeObjective.getObjective((Scoreboard) (Object) this));
        }
    }

    @Inject(method = "removeObjectiveFromEntity", at = @At("HEAD"), cancellable = true)
    public void onRemoveObjectiveFromEntity(String name, ScoreObjective objective, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.scoreboard.allowRecursion = false;
            if (objective == null) {
                for (Score score: this.scoreboard.getScores(Texts.fromLegacy(name))) {
                    for (Objective spongeObjective: score.getObjectives()) {
                        spongeObjective.removeScore(score);
                    }
                }
            } else {
                SpongeObjective spongeObjective = ((IMixinScoreObjective) objective).getSpongeObjective();
                if (spongeObjective != null) {
                    spongeObjective.removeScore(spongeObjective.getScore(Texts.fromLegacy(name)));
                }
            }
            this.scoreboard.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "removeObjective", at = @At("HEAD"), cancellable = true)
    public void onRemoveObjective(ScoreObjective objective, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.scoreboard.allowRecursion = false;
            SpongeObjective spongeObjective = ((IMixinScoreObjective) objective).getSpongeObjective();
            if (spongeObjective != null) {
                this.scoreboard.removeObjective(spongeObjective);
            }
            this.scoreboard.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "setObjectiveInDisplaySlot", at = @At("HEAD"), cancellable = true)
    public void setObjectiveInDisplaySlot(int slot, ScoreObjective objective, CallbackInfo ci) {
        if (shouldEcho()) {
            this.scoreboard.allowRecursion = false;
            // The objective is nullable, so no need to check the result of getSpongeObjective
            System.out.format("Scoreboard: {} Objecite: {} DisplaySlot: ", new Object[] {this.scoreboard, objective,Iterables.get(((SpongeGameRegistry) Sponge.getGame().getRegistry()).displaySlotMappings.values(), slot) });
            this.scoreboard.addObjective(((IMixinScoreObjective) objective).getSpongeObjective(),
                                         Iterables.get(((SpongeGameRegistry) Sponge.getGame().getRegistry()).displaySlotMappings.values(), slot));
            this.scoreboard.allowRecursion = true;
            ci.cancel();
        }
    }

    @Inject(method = "createTeam", at = @At("HEAD"), cancellable = true)
    public void onCreateTeam(String name, CallbackInfoReturnable<ScorePlayerTeam> ci) {
        if (this.shouldEcho()) {
            SpongeTeam spongeTeam = new SpongeTeam(name);
            this.scoreboard.allowRecursion = false;
            this.scoreboard.addTeam(spongeTeam);
            this.scoreboard.allowRecursion = true;
            ci.setReturnValue(spongeTeam.getTeam((Scoreboard) (Object) this));
        }
    }

    @Inject(method = "removeTeam", at = @At("HEAD"), cancellable = true)
    public void onRemoveTeam(ScorePlayerTeam team, CallbackInfo ci) {
        if (this.shouldEcho()) {
            SpongeTeam spongeTeam = ((IMixinTeam) team).getSpongeTeam();
            if (spongeTeam != null) {
                this.scoreboard.allowRecursion = false;
                this.scoreboard.removeTeam(spongeTeam);
                this.scoreboard.allowRecursion = true;
            }
            ci.cancel();
        }
    }

    @Inject(method = "addPlayerToTeam", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/scoreboard/Scoreboard;getTeam(Ljava/lang/String;)Lnet/minecraft/scoreboard/ScorePlayerTeam;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void onAddPlayerToTeam(String player, String newTeam, CallbackInfoReturnable<Boolean> ci, ScorePlayerTeam team) {
        if (shouldEcho()) {
            SpongeTeam spongeTeam = ((IMixinTeam) team).getSpongeTeam();
            if (spongeTeam != null) {
                Optional<Player> spongePlayer = Sponge.getGame().getServer().getPlayer(player);
                if (spongePlayer.isPresent()) {
                    this.scoreboard.allowRecursion = false;
                    spongeTeam.addUser(spongePlayer.get());
                    this.scoreboard.allowRecursion = true;
                    ci.setReturnValue(true);
                } else {
                    // Well, we tried ¯\_(ツ)_/¯
                }
            }
        }
    }

    @Inject(method = "removePlayerFromTeam", at = @At("HEAD"), cancellable = true)
    public void onRemovePlayerFromTeam(String name, ScorePlayerTeam team, CallbackInfo ci) {
        if (shouldEcho()) {
            SpongeTeam spongeTeam = ((IMixinTeam) team).getSpongeTeam();
            if (spongeTeam != null) {
                Optional<Player> spongePlayer = Sponge.getGame().getServer().getPlayer(name);
                if (spongePlayer.isPresent()) {
                    this.scoreboard.allowRecursion = false;
                    spongeTeam.removeUser(spongePlayer.get());
                    this.scoreboard.allowRecursion = true;
                    ci.cancel();
                } else {
                    // Well, we tried ¯\_(ツ)_/¯
                }
            }
        }
    }

    @Inject(method = "getValueFromObjective", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BY, by = 3, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void onGetValueFromObjective(String name, ScoreObjective objective, CallbackInfoReturnable<net.minecraft.scoreboard.Score> cir, Object map, net.minecraft.scoreboard.Score score) {
        if (shouldEcho() && score == null) {
            this.scoreboard.allowRecursion = false;
            SpongeScore spongeScore = (SpongeScore) ((IMixinScoreObjective) objective).getSpongeObjective().getScore(Texts.fromLegacy(name));
            this.scoreboard.allowRecursion = true;
            cir.setReturnValue(spongeScore.getScore(objective));
        }
    }

    private boolean shouldEcho() {
        return ((this.echoToSponge || this.scoreboard.getScoreboards().size() == 1) && this.scoreboard.allowRecursion);
    }
}

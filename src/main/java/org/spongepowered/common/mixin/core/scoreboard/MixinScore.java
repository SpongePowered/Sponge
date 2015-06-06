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
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinScore;
import org.spongepowered.common.interfaces.IMixinScoreboard;
import org.spongepowered.common.scoreboard.SpongeScore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NonnullByDefault
@Mixin(Score.class)
public abstract class MixinScore implements IMixinScore {

    @Shadow public Scoreboard theScoreboard;

    public boolean spongeCreated;

    public SpongeScore score;

    private boolean allowRecursion = true;

    @Override
    public boolean spongeCreated() {
        return this.spongeCreated;
    }

    @Override
    public void setSpongeCreated() {
        this.spongeCreated = true;
    }

    @Override
    public void setSpongeScore(SpongeScore score) {
        this.score = score;
    }

    @Override
    public SpongeScore getSpongeScore() {
        return this.score;
    }

    @Inject(method = "setScorePoints", at = @At("RETURN"), cancellable = true)
    public void onSetScorePoints(int points, CallbackInfo ci) {
        if (this.shouldEcho()) {
            this.allowRecursion = false;
            this.score.setScore(points);
            this.allowRecursion = true;
            ci.cancel();
        }
    }

    private boolean shouldEcho() {
        return (((IMixinScoreboard) theScoreboard).echoToSponge() || ((IMixinScoreboard) this.theScoreboard).getSpongeScoreboard().getScoreboards().size() == 1) && this.allowRecursion;
    }

}

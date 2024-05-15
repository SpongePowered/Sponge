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
package org.spongepowered.common.mixin.core.world.scores;

import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.bridge.world.scores.ScoreboardBridge;
import org.spongepowered.common.scoreboard.SpongeScore;

@SuppressWarnings("ConstantConditions")
@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin implements ScoreboardBridge {

    private boolean impl$isClient;

    @Override
    public boolean bridge$isClient() {
        return this.impl$isClient;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setIsClient(final CallbackInfo ci) {
        this.impl$isClient = !((Scoreboard) (Object) this instanceof ServerScoreboard);
    }

    @Inject(method = "getOrCreatePlayerScore(Lnet/minecraft/world/scores/ScoreHolder;Lnet/minecraft/world/scores/Objective;Z)Lnet/minecraft/world/scores/ScoreAccess;", at = @At("RETURN"), cancellable = true)
    private void impl$onGetScoreAccess(final ScoreHolder $$0, final Objective $$1, final boolean $$2, final CallbackInfoReturnable<ScoreAccess> cir) {
        if ((Scoreboard) (Object) this instanceof ServerScoreboard) {
            // wrap to intercept setting data
            cir.setReturnValue(new SpongeScore.SpongeScoreAccess($$0, $$1, cir.getReturnValue()));
        }
    }

    @Inject(method = "addObjective", at = @At("RETURN"))
    public void impl$onAddObjective(final String name,
            final ObjectiveCriteria criteria,
            final net.minecraft.network.chat.Component text,
            final ObjectiveCriteria.RenderType type,
            final boolean displayAutoUpdate,
            @Nullable final NumberFormat numberFormat,
            final CallbackInfoReturnable<Objective> cir) {
        if (this instanceof ServerScoreboardBridge ssb) {
            ssb.bridge$addMCObjective(cir.getReturnValue());
        }
    }

    @Inject(method = "removeObjective", at = @At("RETURN"))
    public void impl$onRemoveObjective(final Objective objective, final CallbackInfo ci) {
        if (this instanceof ServerScoreboardBridge ssb) {
            ssb.bridge$removeMCObjective(objective);
        }
    }

    @Inject(method = "resetSinglePlayerScore", at = @At("RETURN"))
    public void impl$onResetScore(final ScoreHolder holder, final Objective mcObjective, final CallbackInfo ci) {
        if (this instanceof ServerScoreboardBridge ssb) {
            ssb.bridge$removeMCScore(holder, mcObjective);
        }
    }
}

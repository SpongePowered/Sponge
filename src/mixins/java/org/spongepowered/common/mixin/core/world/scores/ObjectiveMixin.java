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

import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.scores.ObjectiveBridge;
import org.spongepowered.common.bridge.world.scores.ScoreboardBridge;
import org.spongepowered.common.scoreboard.SpongeObjective;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

@Mixin(Objective.class)
public abstract class ObjectiveMixin implements ObjectiveBridge {

    @Shadow @Final private Scoreboard scoreboard;

    @Nullable private SpongeObjective impl$spongeScoreboard;

    @SuppressWarnings("ConstantConditions")
    @Override
    public SpongeObjective bridge$getSpongeObjective() {
        return this.impl$spongeScoreboard;
    }

    @Override
    public void bridge$setSpongeObjective(final SpongeObjective spongeObjective) {
        this.impl$spongeScoreboard = spongeObjective;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "setDisplayName", at = @At("HEAD"), cancellable = true)
    private void onSetDisplayName(final Component name, final CallbackInfo ci) {
        if (this.scoreboard != null && ((ScoreboardBridge) this.scoreboard).bridge$isClient()) {
            return; // Let the normal logic take over.
        }

        if (this.impl$spongeScoreboard == null) {
            SpongeCommon.logger().warn("Returning objective cause null!");
            ci.cancel();
            return;
        }
        this.impl$spongeScoreboard.setDisplayName(SpongeAdventure.asAdventure(name));
        ci.cancel();
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "setRenderType", at = @At("HEAD"), cancellable = true)
    private void onSetRenderType(final ObjectiveCriteria.RenderType type, final CallbackInfo ci) {
        if (this.scoreboard != null && ((ScoreboardBridge) this.scoreboard).bridge$isClient()) {
            return; // Let the normal logic take over.
        }

        if (this.impl$spongeScoreboard == null) {
            SpongeCommon.logger().warn("Returning render objective cause null!");
            ci.cancel();
            return;
        }
        this.impl$spongeScoreboard.setDisplayMode((ObjectiveDisplayMode) (Object) type);
        ci.cancel();
    }
}

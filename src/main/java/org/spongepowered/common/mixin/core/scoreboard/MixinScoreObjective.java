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

import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinScoreObjective;
import org.spongepowered.common.interfaces.IMixinScoreboard;
import org.spongepowered.common.interfaces.text.IMixinTextComponent;
import org.spongepowered.common.scoreboard.SpongeObjective;

@Mixin(ScoreObjective.class)
public abstract class MixinScoreObjective implements IMixinScoreObjective {

    @Shadow @Final public Scoreboard scoreboard;

    public SpongeObjective spongeObjective;

    @Override
    public SpongeObjective getSpongeObjective() {
        return this.spongeObjective;
    }

    @Override
    public void setSpongeObjective(SpongeObjective spongeObjective) {
        this.spongeObjective = spongeObjective;
    }

    @Inject(method = "setDisplayName", at = @At("HEAD"), cancellable = true)
    public void onSetDisplayName(ITextComponent name, CallbackInfo ci) {
        if (this.scoreboard != null && ((IMixinScoreboard) this.scoreboard).isClient()) {
            return; // Let the normal logic take over.
        }

        if (this.spongeObjective == null) {
            SpongeImpl.getLogger().warn("Returning objective cause null!");
            ci.cancel();
            return;
        }
        this.spongeObjective.setDisplayName(((IMixinTextComponent) name).toText());
        ci.cancel();
    }

    @Inject(method = "setRenderType", at = @At("HEAD"), cancellable = true)
    public void onSetRenderType(ScoreCriteria.RenderType type, CallbackInfo ci) {
        if (this.scoreboard != null && ((IMixinScoreboard) this.scoreboard).isClient()) {
            return; // Let the normal logic take over.
        }

        if (this.spongeObjective == null) {
            SpongeImpl.getLogger().warn("Returning render objective cause null!");
            ci.cancel();
            return;
        }
        this.spongeObjective.setDisplayMode((ObjectiveDisplayMode) (Object) type);
        ci.cancel();
    }
}

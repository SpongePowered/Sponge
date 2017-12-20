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
package org.spongepowered.common.mixin.core.advancement;

import com.google.common.collect.ImmutableSet;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancementProgress;
import org.spongepowered.common.interfaces.advancement.IMixinPlayerAdvancements;

import java.util.Map;
import java.util.Set;

@Mixin(PlayerAdvancements.class)
public class MixinPlayerAdvancements implements IMixinPlayerAdvancements {

    @Shadow @Final private Map<Advancement, AdvancementProgress> progress;
    @Shadow private EntityPlayerMP player;

    @Inject(method = "startProgress", at = @At("RETURN"))
    private void onStartProgress(Advancement advancement, AdvancementProgress progress, CallbackInfo ci) {
        final IMixinAdvancementProgress advancementProgress = (IMixinAdvancementProgress) progress;
        advancementProgress.setAdvancement(((org.spongepowered.api.advancement.Advancement) advancement).getId());
        advancementProgress.setPlayerAdvancements((PlayerAdvancements) (Object) this);
    }

    @Override
    public Set<AdvancementTree> getAdvancementTrees() {
        final ImmutableSet.Builder<AdvancementTree> builder = ImmutableSet.builder();
        for (Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            final org.spongepowered.api.advancement.Advancement advancement = (org.spongepowered.api.advancement.Advancement) entry.getKey();
            if (!advancement.getParent().isPresent()) {
                advancement.getTree().ifPresent(builder::add);
            }
        }
        return builder.build();
    }

    @Override
    public Player getPlayer() {
        return (Player) this.player;
    }
}

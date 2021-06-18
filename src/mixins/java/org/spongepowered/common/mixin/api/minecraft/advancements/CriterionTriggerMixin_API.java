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
package org.spongepowered.common.mixin.api.minecraft.advancements;

import com.google.common.base.Preconditions;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.advancements.CriterionTriggerBridge;

@Mixin(CriterionTrigger.class)
public interface CriterionTriggerMixin_API<C extends FilteredTriggerConfiguration> extends Trigger<C> {

    @Shadow ResourceLocation shadow$getId();

    @Override
    default ResourceKey key() {
        return (ResourceKey) (Object) this.shadow$getId();
    }

    @Override
    default void trigger() {
        this.trigger(Sponge.server().onlinePlayers());
    }

    @Override
    default void trigger(final Iterable<ServerPlayer> players) {
        Preconditions.checkNotNull(players);
        players.forEach(((CriterionTriggerBridge) this)::bridge$trigger);
    }

    @Override
    default void trigger(final ServerPlayer player) {
        Preconditions.checkNotNull(player);
        ((CriterionTriggerBridge) this).bridge$trigger(player);
        // This could possibly be implemented in all the vanilla triggers
        // and construct trigger method arguments based on context values
        // Not needed for now, just assume it always fails
    }
}

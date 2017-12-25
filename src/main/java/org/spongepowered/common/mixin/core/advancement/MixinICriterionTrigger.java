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

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.advancement.ITrigger;
import org.spongepowered.common.advancement.UnknownFilteredTriggerConfiguration;

@Implements(@Interface(iface = Trigger.class, prefix = "type$"))
@Mixin(ICriterionTrigger.class)
public interface MixinICriterionTrigger extends ITrigger {

    @Shadow ResourceLocation getId();

    default String type$getId() {
        return getId().toString();
    }

    default String type$getName() {
        return getId().getResourcePath();
    }

    default void type$trigger() {
        type$trigger(Sponge.getServer().getOnlinePlayers());
    }

    default void type$trigger(Iterable<Player> players) {
        players.forEach(this::trigger);
    }

    @SuppressWarnings("unchecked")
    @Override
    default Class<FilteredTriggerConfiguration> getConfigurationType() {
        return (Class) UnknownFilteredTriggerConfiguration.class;
    }

    @Override
    default void trigger(Player player) {
        // This could possibly be implemented in all the vanilla triggers
        // and construct trigger method arguments based on context values
        // Not needed for now, just assume it always fails
    }
}

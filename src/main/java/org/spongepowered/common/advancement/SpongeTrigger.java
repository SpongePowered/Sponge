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
package org.spongepowered.common.advancement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.advancements.ICriterionTrigger_ListenerBridge;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
public class SpongeTrigger implements ICriterionTrigger<SpongeFilteredTrigger>, TriggerBridge {

    private final Class<FilteredTriggerConfiguration> triggerConfigurationClass;
    final Function<JsonObject, FilteredTriggerConfiguration> constructor;
    private final ResourceLocation id;
    private final Multimap<PlayerAdvancements, Listener> listeners = HashMultimap.create();
    @Nullable final Consumer<CriterionEvent.Trigger> eventHandler;
    private final String name;

    SpongeTrigger(final Class<FilteredTriggerConfiguration> triggerConfigurationClass,
            final Function<JsonObject, FilteredTriggerConfiguration> constructor,
            final ResourceLocation id, @Nullable final Consumer<CriterionEvent.Trigger> eventHandler,
            final String name) {
        this.triggerConfigurationClass = triggerConfigurationClass;
        this.eventHandler = eventHandler;
        this.constructor = constructor;
        this.id = id;
        this.name = name;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void addListener(final PlayerAdvancements playerAdvancementsIn, final Listener listener) {
        this.listeners.put(playerAdvancementsIn, listener);
    }

    @Override
    public void removeListener(final PlayerAdvancements playerAdvancementsIn, final Listener listener) {
        this.listeners.remove(playerAdvancementsIn, listener);
    }

    @Override
    public void removeAllListeners(final PlayerAdvancements playerAdvancementsIn) {
        this.listeners.removeAll(playerAdvancementsIn);
    }

    @Override
    public SpongeFilteredTrigger deserializeInstance(final JsonObject json, final JsonDeserializationContext context) {
        return new SpongeFilteredTrigger(this, this.constructor.apply(json));
    }

    @Override
    public void bridge$trigger(final Player player) {
        final PlayerAdvancements playerAdvancements = ((ServerPlayerEntity) player).getAdvancements();
        final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
        final TypeToken<FilteredTriggerConfiguration> typeToken = TypeToken.of(this.triggerConfigurationClass);
        for (final Listener listener : new ArrayList<>(this.listeners.get(playerAdvancements))) {
            final ICriterionTrigger_ListenerBridge mixinListener = (ICriterionTrigger_ListenerBridge) listener;
            final Advancement advancement = (Advancement) mixinListener.bridge$getAdvancement();
            final AdvancementCriterion advancementCriterion = (AdvancementCriterion)
                    ((net.minecraft.advancements.Advancement) advancement).getCriteria().get(mixinListener.bridge$getCriterionName());
            final CriterionEvent.Trigger event = SpongeEventFactory.createCriterionEventTrigger(cause, advancement, advancementCriterion,
                    typeToken, player, (FilteredTrigger) listener.getCriterionInstance(), this.eventHandler == null);
            if (this.eventHandler != null) {
                this.eventHandler.accept(event);
                if (!event.getResult()) {
                    continue;
                }
            }
            SpongeImpl.postEvent(event);
            if (event.getResult()) {
                listener.grantCriterion(playerAdvancements);
            }
        }
    }

    @Nullable
    public Consumer<CriterionEvent.Trigger> getEventHandler() {
        return this.eventHandler;
    }
}

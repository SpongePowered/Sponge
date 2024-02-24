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
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import io.leangen.geantyref.TypeToken;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.advancements.CriterionTriggerBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class SpongeCriterionTrigger implements CriterionTrigger<SpongeFilteredTrigger>, CriterionTriggerBridge {

    private final Type triggerConfigurationType;
    final Function<JsonObject, FilteredTriggerConfiguration> constructor;
    private final Multimap<PlayerAdvancements, Listener> listeners = HashMultimap.create();
    final @Nullable Consumer<CriterionEvent.Trigger> eventHandler;
    private final String name;


    @Override
    public Codec<SpongeFilteredTrigger> codec() {
        return Codec.PASSTHROUGH.comapFlatMap(in -> {
                    var json = in.convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
                    final FilteredTriggerConfiguration config = this.constructor.apply(json);
                    return DataResult.success(new SpongeFilteredTrigger(config));
                }, trigger -> new Dynamic<>(JsonOps.INSTANCE, trigger.serializeToJson())
        );
    }

    SpongeCriterionTrigger(final Type triggerConfigurationType,
        final Function<JsonObject, FilteredTriggerConfiguration> constructor,
        final @Nullable Consumer<CriterionEvent.Trigger> eventHandler,
        final String name) {
        this.triggerConfigurationType = triggerConfigurationType;
        this.eventHandler = eventHandler;
        this.constructor = constructor;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void addPlayerListener(final PlayerAdvancements playerAdvancementsIn, final Listener listener) {
        this.listeners.put(playerAdvancementsIn, listener);
    }

    @Override
    public void removePlayerListener(final PlayerAdvancements playerAdvancementsIn, final Listener listener) {
        this.listeners.remove(playerAdvancementsIn, listener);
    }

    @Override
    public void removePlayerListeners(final PlayerAdvancements playerAdvancementsIn) {
        this.listeners.removeAll(playerAdvancementsIn);
    }

    @Override
    public void bridge$trigger(final ServerPlayer player) {
        final PlayerAdvancements playerAdvancements = ((net.minecraft.server.level.ServerPlayer) player).getAdvancements();
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();

        @SuppressWarnings("unchecked") // correct type verified in builder
        final TypeToken<FilteredTriggerConfiguration> typeToken = (TypeToken<FilteredTriggerConfiguration>) TypeToken.get(this.triggerConfigurationType);
        for (final Listener listener : new ArrayList<>(this.listeners.get(playerAdvancements))) {
            final var advancement = listener.advancement().value();
            final var advancementKey = (ResourceKey) (Object) listener.advancement().id();
            final AdvancementCriterion advancementCriterion = (AdvancementCriterion) (Object) advancement.criteria().get(listener.criterion());
            final CriterionEvent.Trigger event = SpongeEventFactory.createCriterionEventTrigger(cause, (Advancement) (Object) advancement, advancementKey, advancementCriterion,
                typeToken, player, (FilteredTrigger) listener.trigger(), (Trigger<FilteredTriggerConfiguration>) advancementCriterion.type().get(), this.eventHandler == null);
            if (this.eventHandler != null) {
                this.eventHandler.accept(event);
                if (!event.result()) {
                    continue;
                }
            }
            SpongeCommon.post(event);
            if (event.result()) {
                listener.run(playerAdvancements);
            }
        }
    }

    public @org.checkerframework.checker.nullness.qual.Nullable Consumer<CriterionEvent.Trigger> getEventHandler() {
        return this.eventHandler;
    }
}

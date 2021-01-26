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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataSerializable;

import java.io.IOException;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("rawtypes")
public final class SpongeFilteredTrigger implements CriterionTriggerInstance, FilteredTrigger {

    private final static Gson GSON = new Gson();

    private final SpongeTrigger triggerType;
    private final FilteredTriggerConfiguration configuration;

    SpongeFilteredTrigger(final SpongeTrigger triggerType, final FilteredTriggerConfiguration configuration) {
        this.triggerType = triggerType;
        this.configuration = configuration;
    }

    @Override
    public ResourceLocation getCriterion() {
        return this.triggerType.getId();
    }

    @Override
    public Trigger getType() {
        return (Trigger) this.triggerType;
    }

    @Override
    public FilteredTriggerConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public JsonObject serializeToJson(final SerializationContext arraySerializer) {
        if (this.configuration instanceof DataSerializable) {
            final DataContainer dataContainer = ((DataSerializable) this.configuration).toContainer();
            try {
                final String json = DataFormats.JSON.get().write(dataContainer);
                return SpongeFilteredTrigger.GSON.fromJson(json, JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return SpongeFilteredTrigger.GSON.toJsonTree(this.configuration).getAsJsonObject();
    }
}

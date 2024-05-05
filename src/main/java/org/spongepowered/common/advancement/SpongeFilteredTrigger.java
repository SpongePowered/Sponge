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
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.CriterionValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataSerializable;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public final class SpongeFilteredTrigger implements CriterionTriggerInstance, FilteredTrigger {

    private static final Logger LOGGER = LogManager.getLogger();
    private final static Gson GSON = new Gson();

    private final FilteredTriggerConfiguration configuration;

    SpongeFilteredTrigger(final FilteredTriggerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public FilteredTriggerConfiguration configuration() {
        return this.configuration;
    }

    public JsonObject serializeToJson() {
        if (this.configuration instanceof DataSerializable) {
            final DataContainer dataContainer = ((DataSerializable) this.configuration).toContainer();
            try {
                final String json = DataFormats.JSON.get().write(dataContainer);
                return SpongeFilteredTrigger.GSON.fromJson(json, JsonObject.class);
            } catch (IOException e) {
                SpongeFilteredTrigger.LOGGER.error("Failed to serialize trigger to json", e);
            }
        }

        return SpongeFilteredTrigger.GSON.toJsonTree(this.configuration).getAsJsonObject();
    }

    @Override
    public void validate(final CriterionValidator var1) {

    }

}

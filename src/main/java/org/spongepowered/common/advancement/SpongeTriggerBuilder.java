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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.data.persistence.JsonDataFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class SpongeTriggerBuilder<C extends FilteredTriggerConfiguration> implements Trigger.Builder<C> {

    private final static Gson GSON = new Gson();

    private final static FilteredTriggerConfiguration.Empty EMPTY_TRIGGER_CONFIGURATION = new FilteredTriggerConfiguration.Empty();
    private final static Function<JsonObject, FilteredTriggerConfiguration.Empty> EMPTY_TRIGGER_CONFIGURATION_CONSTRUCTOR =
            jsonObject -> EMPTY_TRIGGER_CONFIGURATION;

    @Nullable private Class<C> configType;
    @Nullable private Function<JsonObject, C> constructor;
    @Nullable private Consumer<CriterionEvent.Trigger<C>> eventHandler;
    @Nullable private String id;
    @Nullable private String name;

    @Override
    @SuppressWarnings("rawtypes")
    public <T extends FilteredTriggerConfiguration & DataSerializable> Trigger.Builder<T> dataSerializableConfig(final Class<T> dataConfigClass) {
        checkNotNull(dataConfigClass, "dataConfigClass");
        this.configType = (Class<C>) dataConfigClass;
        this.constructor = new DataSerializableConstructor(dataConfigClass);
        return (Trigger.Builder<T>) this;
    }

    private static class DataSerializableConstructor<C extends FilteredTriggerConfiguration & DataSerializable> implements Function<JsonObject, C> {

        private final Class<C> dataConfigClass;

        private DataSerializableConstructor(final Class<C> dataConfigClass) {
            this.dataConfigClass = dataConfigClass;
        }

        @Override
        public C apply(final JsonObject jsonObject) {
            final DataBuilder<C> builder = Sponge.getDataManager().getBuilder(this.dataConfigClass).get();
            try {
                final DataView dataView = JsonDataFormat.serialize(GSON, jsonObject);
                return builder.build(dataView).get();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> typeSerializableConfig(final Class<T> configClass) {
        return typeSerializableConfig(configClass, TypeSerializers.getDefaultSerializers());
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> typeSerializableConfig(final Class<T> configClass,
            final TypeSerializerCollection typeSerializerCollection) {
        checkNotNull(configClass, "configClass");
        checkNotNull(typeSerializerCollection, "typeSerializerCollection");
        this.configType = (Class<C>) configClass;
        this.constructor = new ConfigurateConstructor(configClass, typeSerializerCollection);
        return (Trigger.Builder<T>) this;
    }

    private static class ConfigurateConstructor<C extends FilteredTriggerConfiguration> implements Function<JsonObject, C> {

        private final TypeToken<C> typeToken;
        private final TypeSerializerCollection typeSerializerCollection;

        private ConfigurateConstructor(final Class<C> typeToken, final TypeSerializerCollection typeSerializerCollection) {
            this.typeToken = TypeToken.of(typeToken);
            this.typeSerializerCollection = typeSerializerCollection;
        }

        @Override
        public C apply(final JsonObject jsonObject) {
            final GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                    .setSource(() -> new BufferedReader(new StringReader(GSON.toJson(jsonObject))))
                    .build();
            try {
                final ConfigurationNode node = loader.load();
                return this.typeSerializerCollection.get(this.typeToken).deserialize(this.typeToken, node);
            } catch (IOException | ObjectMappingException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> jsonSerializableConfig(final Class<T> configClass, final Gson gson) {
        checkNotNull(configClass, "configClass");
        this.configType = (Class<C>) configClass;
        this.constructor = new JsonConstructor(configClass, gson);
        return (Trigger.Builder<T>) this;
    }

    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> jsonSerializableConfig(final Class<T> configClass) {
        return jsonSerializableConfig(configClass, GSON);
    }

    private static class JsonConstructor<C extends FilteredTriggerConfiguration> implements Function<JsonObject, C> {

        private final Class<C> configClass;
        private final Gson gson;

        private JsonConstructor(final Class<C> configClass, final Gson gson) {
            this.configClass = configClass;
            this.gson = gson;
        }

        @Override
        public C apply(final JsonObject jsonObject) {
            return this.gson.fromJson(jsonObject, this.configClass);
        }
    }

    @Override
    public Trigger.Builder<FilteredTriggerConfiguration.Empty> emptyConfig() {
        this.configType = (Class<C>) FilteredTriggerConfiguration.Empty.class;
        this.constructor = (Function<JsonObject, C>) EMPTY_TRIGGER_CONFIGURATION_CONSTRUCTOR;
        return (Trigger.Builder<FilteredTriggerConfiguration.Empty>) this;
    }

    @Override
    public Trigger.Builder<C> listener(final Consumer<CriterionEvent.Trigger<C>> eventListener) {
        this.eventHandler = eventListener;
        return this;
    }

    @Override
    public Trigger.Builder<C> id(final String id) {
        checkNotNull(id, "id");
        this.id = id;
        return this;
    }

    @Override
    public Trigger.Builder<C> name(final String name) {
        checkNotNull(name, "name");
        this.name = name;
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Trigger<C> build() {
        checkState(this.id != null, "The id must be set");
        checkState(this.configType != null, "The configType must be set");
        final PluginContainer plugin = Sponge.getCauseStackManager().getCurrentCause().first(PluginContainer.class).get();
        final String name = StringUtils.isNotEmpty(this.name) ? this.name : this.id;
        return (Trigger<C>) new SpongeTrigger((Class) this.configType, (Function) this.constructor,
                new ResourceLocation(plugin.getId(), this.id), (Consumer) this.eventHandler, name);
    }

    @Override
    public Trigger.Builder<C> reset() {
        this.configType = null;
        this.constructor = null;
        this.eventHandler = null;
        this.id = null;
        this.name = null;
        return this;
    }
}

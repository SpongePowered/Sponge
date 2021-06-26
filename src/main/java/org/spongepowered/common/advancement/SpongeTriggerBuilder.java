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
import static java.util.Objects.requireNonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.event.advancement.CriterionEvent;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unchecked")
public final class SpongeTriggerBuilder<C extends FilteredTriggerConfiguration> extends AbstractResourceKeyedBuilder<Trigger<C>,
        Trigger.Builder<C>> implements Trigger.Builder<C> {

    private static final Gson GSON = new Gson();

    private final static FilteredTriggerConfiguration.Empty EMPTY_TRIGGER_CONFIGURATION = new FilteredTriggerConfiguration.Empty();
    private final static Function<JsonObject, FilteredTriggerConfiguration.Empty> EMPTY_TRIGGER_CONFIGURATION_CONSTRUCTOR =
            jsonObject -> SpongeTriggerBuilder.EMPTY_TRIGGER_CONFIGURATION;

    private @Nullable Type configType;
    private @Nullable Function<JsonObject, C> constructor;
    private @Nullable Consumer<CriterionEvent.Trigger<C>> eventHandler;
    private @Nullable String name;

    @Override
    @SuppressWarnings("rawtypes")
    public <T extends FilteredTriggerConfiguration & DataSerializable> Trigger.Builder<T> dataSerializableConfig(final Class<T> dataConfigClass) {
        checkNotNull(dataConfigClass, "dataConfigClass");
        this.configType = dataConfigClass;
        this.constructor = new DataSerializableConstructor(dataConfigClass);
        return (Trigger.Builder<T>) this;
    }

    // TODO: do these need to be hooked up for saving as well? currently they just load

    private static ConfigurationOptions defaultOptions() {
        return ConfigurationOptions.defaults()
                .serializers(SpongeCommon.game().configManager().serializers());
    }

    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> typeSerializableConfig(final TypeToken<T> configType) {
        return this.typeSerializableConfig(configType, SpongeTriggerBuilder.defaultOptions());
    }

    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> typeSerializableConfig(final TypeToken<T> configType,
            final ConfigurationOptions options) {
        checkNotNull(configType, "configType");
        checkNotNull(options, "options");
        this.configType = configType.getType();
        this.constructor = new ConfigurateConstructor<>(configType.getType(), options);
        return (Trigger.Builder<T>) this;
    }

    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> typeSerializableConfig(final TypeToken<T> configType,
            final UnaryOperator<ConfigurationOptions> transformer) {
        return this.typeSerializableConfig(configType, transformer.apply(SpongeTriggerBuilder.defaultOptions()));
    }

    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> typeSerializableConfig(final Class<T> configClass) {
        return this.typeSerializableConfig(configClass, SpongeTriggerBuilder.defaultOptions());
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> typeSerializableConfig(final Class<T> configClass,
            final ConfigurationOptions options) {
        checkNotNull(configClass, "configClass");
        checkNotNull(options, "options");
        this.configType = configClass;
        this.constructor = new ConfigurateConstructor(configClass, options);
        return (Trigger.Builder<T>) this;
    }

    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> typeSerializableConfig(final Class<T> configClass,
            final UnaryOperator<ConfigurationOptions> transformer) {
        requireNonNull(transformer, "transformer");
        return this.typeSerializableConfig(configClass, transformer.apply(SpongeTriggerBuilder.defaultOptions()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> jsonSerializableConfig(final Class<T> configClass, final Gson gson) {
        checkNotNull(configClass, "configClass");
        this.configType = configClass;
        this.constructor = new JsonConstructor(configClass, gson);
        return (Trigger.Builder<T>) this;
    }

    @Override
    public <T extends FilteredTriggerConfiguration> Trigger.Builder<T> jsonSerializableConfig(final Class<T> configClass) {
        return this.jsonSerializableConfig(configClass, SpongeTriggerBuilder.GSON);
    }

    @Override
    public Trigger.Builder<FilteredTriggerConfiguration.Empty> emptyConfig() {
        this.configType = FilteredTriggerConfiguration.Empty.class;
        this.constructor = (Function<JsonObject, C>) SpongeTriggerBuilder.EMPTY_TRIGGER_CONFIGURATION_CONSTRUCTOR;
        return (Trigger.Builder<FilteredTriggerConfiguration.Empty>) this;
    }

    @Override
    public Trigger.Builder<C> listener(final Consumer<CriterionEvent.Trigger<C>> eventListener) {
        this.eventHandler = eventListener;
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
    public Trigger<C> build0() {
        Objects.requireNonNull(this.name, "name");
        checkState(!this.name.isEmpty(), "The name cannot be empty!");
        checkState(this.configType != null, "The configType must be set");
        return (Trigger<C>) new SpongeCriterionTrigger(this.configType, (Function) this.constructor, (ResourceLocation) (Object) this.key, (Consumer) this.eventHandler, this.name);
    }

    @Override
    public Trigger.Builder<C> from(final Trigger<C> value) {
        this.configType = value.configurationType();
        if (value instanceof SpongeCriterionTrigger) {
            this.constructor = (Function<JsonObject, C>) ((SpongeCriterionTrigger) value).constructor;
            this.eventHandler = (Consumer) ((SpongeCriterionTrigger) value).getEventHandler();
            this.name = ((SpongeCriterionTrigger) value).getName();
        }
        return this;
    }

    @Override
    public Trigger.Builder<C> reset() {
        this.key = null;
        this.configType = null;
        this.constructor = null;
        this.eventHandler = null;
        this.name = null;
        return this;
    }

    private static class JsonConstructor<C extends FilteredTriggerConfiguration> implements Function<JsonObject, C> {

        private final Type configClass;
        private final Gson gson;

        private JsonConstructor(final Type configClass, final Gson gson) {
            this.configClass = configClass;
            this.gson = gson;
        }

        @Override
        public C apply(final JsonObject jsonObject) {
            return this.gson.fromJson(jsonObject, this.configClass);
        }
    }

    private static class DataSerializableConstructor<C extends FilteredTriggerConfiguration & DataSerializable> implements Function<JsonObject, C> {

        private final Class<C> dataConfigClass;

        private DataSerializableConstructor(final Class<C> dataConfigClass) {
            this.dataConfigClass = dataConfigClass;
        }

        @Override
        public C apply(final JsonObject jsonObject) {
            final DataBuilder<C> builder = Sponge.dataManager().builder(this.dataConfigClass).get();
            try {
                final DataView dataView = JsonDataFormat.serialize(SpongeTriggerBuilder.GSON, jsonObject);
                return builder.build(dataView).get();
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static class ConfigurateConstructor<C extends FilteredTriggerConfiguration> implements Function<JsonObject, C> {

        private final Type typeToken;
        private final ConfigurationOptions options;

        private ConfigurateConstructor(final Type typeToken, final ConfigurationOptions options) {
            this.typeToken = typeToken;
            this.options = options;
        }

        @Override
        public C apply(final JsonObject jsonObject) {
            final GsonConfigurationLoader loader = GsonConfigurationLoader.builder()
                    .defaultOptions(this.options)
                    .source(() -> new BufferedReader(new StringReader(SpongeTriggerBuilder.GSON.toJson(jsonObject))))
                    .build();
            try {
                final ConfigurationNode node = loader.load();
                return (C) node.get(this.typeToken);
            } catch (final ConfigurateException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}

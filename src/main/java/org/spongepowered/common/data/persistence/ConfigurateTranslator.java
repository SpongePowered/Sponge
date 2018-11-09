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
package org.spongepowered.common.data.persistence;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * A translator for translating {@link DataView}s into {@link ConfigurationNode}
 * s.
 */
public class ConfigurateTranslator implements DataTranslator<ConfigurationNode> {

    private static final ConfigurateTranslator instance = new ConfigurateTranslator();
    private static final TypeToken<ConfigurationNode> TOKEN = TypeToken.of(ConfigurationNode.class);

    private ConfigurateTranslator() {
    }

    /**
     * Get the instance of this translator.
     *
     * @return The instance of this translator
     */
    public static ConfigurateTranslator instance() {
        return instance;
    }

    private static void populateNode(ConfigurationNode node, DataView container) {
        checkNotNull(node, "node");
        checkNotNull(container, "container");
        node.setValue(container.getMap(of()).get());
    }

    private static DataContainer translateFromNode(ConfigurationNode node) {
        checkNotNull(node, "node");
        DataContainer dataContainer = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        populateView(dataContainer, node);
        return dataContainer;
    }

    private static void populateView(DataView view, ConfigurationNode node) {
        for (Map.Entry<?, ? extends ConfigurationNode> entry : node.getChildrenMap().entrySet()) {
            DataQuery key = of(entry.getKey().toString());
            if (entry.getValue().hasMapChildren()) {
                populateView(view.createView(key), entry.getValue());
            } else {
                Object value = getObjectFromNode(entry.getValue());
                if (value != null) {
                    view.set(key, value);
                }
            }
        }
    }

    @Nullable
    private static Object getObjectFromNode(ConfigurationNode node) {
        if (node.hasListChildren()) {
            return node.getChildrenList().stream().map(ConfigurateTranslator::getObjectFromNode)
                    .filter(Objects::nonNull).collect(Collectors.toList());
        } else if (node.hasMapChildren()) {
            return translateFromNode(node);
        } else {
            return node.getValue();
        }
    }

    public ConfigurationNode translateData(DataView container) {
        ConfigurationNode node = SimpleConfigurationNode.root();
        translateContainerToData(node, container);
        return node;
    }

    public void translateContainerToData(ConfigurationNode node, DataView container) {
        ConfigurateTranslator.populateNode(node, container);
    }

    public DataContainer translateFrom(ConfigurationNode node) {
        return ConfigurateTranslator.translateFromNode(node);
    }

    @Override
    public TypeToken<ConfigurationNode> getToken() {
        return TOKEN;
    }

    @Override
    public ConfigurationNode translate(DataView view) throws InvalidDataException {
        final SimpleConfigurationNode node = SimpleConfigurationNode.root();
        populateNode(node, view);
        return node;
    }

    @Override
    public DataContainer translate(ConfigurationNode obj) throws InvalidDataException {
        return ConfigurateTranslator.translateFromNode(obj);
    }

    @Override
    public DataView addTo(ConfigurationNode node, DataView dataView) {
        checkNotNull(node, "node");
        checkNotNull(dataView, "dataView");
        populateView(dataView, node);
        return dataView;
    }

    @Override
    public String getId() {
        return "sponge:configuration_node";
    }

    @Override
    public String getName() {
        return "ConfigurationNodeTranslator";
    }
}
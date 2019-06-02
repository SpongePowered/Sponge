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
import ninja.leaping.configurate.ValueType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        ConfigurateTranslator.instance().addTo(node, dataContainer);
        return dataContainer;
    }

    private void translate(Map<Object, ? extends ConfigurationNode> node, DataView container, Map<?, ?> value) {
        for (Map.Entry<?, ?> o : value.entrySet()) {
            translate(node.get(o.getKey()), container);
        }
    }

    private List<Object> translate(List<? extends ConfigurationNode> node, DataView container, List<?> value) {
        List<Object> list = new ArrayList<>(value.size());
        for (int i = 0; i < value.size(); i++) {
            ConfigurationNode n = node.get(i);
            if (n.getValueType() == ValueType.SCALAR) {
                list.add(n.getValue());
            } else {
                DataContainer clean = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
                translate(n, clean);
                list.add(clean);
            }
        }
        return list;
    }

    private void translate(ConfigurationNode node, DataView container) {
        Object key = node.getKey();
        Object value = node.getValue();
        if (node.hasMapChildren()) {
            DataView view;
            if (key != null) {
                view = container.createView(of(key.toString()));
            } else {
                view = container;
            }
            translate(node.getChildrenMap(), view, (Map<?, ?>) value);
        } else if (node.hasListChildren()) {
            List<Object> list = translate(node.getChildrenList(), container, (List<?>) node.getValue());
            container.set(of(String.valueOf(key)), list);
        } else {
            container.set(of('.', String.valueOf(node.getKey())), node.getValue());
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
        translate(node, dataView);
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
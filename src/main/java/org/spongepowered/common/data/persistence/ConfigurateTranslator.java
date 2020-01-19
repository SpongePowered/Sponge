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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.ValueType;
import org.checkerframework.checker.nullness.qual.NonNull;
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

    public void translateContainerToData(ConfigurationNode node, DataView container) {
        ConfigurateTranslator.populateNode(node, container);
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
        checkNotNull(obj, "node");
        DataContainer dataContainer = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        ConfigurateTranslator.instance().addTo(obj, dataContainer);
        return dataContainer;
    }

    @Override
    public DataView addTo(ConfigurationNode node, DataView dataView) {
        if (node.hasMapChildren()) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) node.getValue()).entrySet()) {
                final Object k = entry.getKey();
                final ConfigurationNode n = node.getNode(k);
                if (n.getValueType() == ValueType.MAP) {
                    addTo(n, dataView.createView(of(k.toString())));
                } else if (n.getValueType() == ValueType.LIST) {
                    addTo(n, dataView);
                } else {
                    checkArgument(k != null, "map key");
                    dataView.set(of(k.toString()), entry.getValue());
                }
            }
        } else if (node.hasListChildren()) {
            final List<Object> l = new ArrayList<>();
            for (ConfigurationNode o : node.getChildrenList()) {
                if (o.getValueType().canHaveChildren()) {
                    l.add(addTo(o, DataContainer.createNew()));
                } else {
                    l.add(o.getValue());
                }
            }
            dataView.set(of(node.getKey().toString()), l);
        } else {
            final Object key = node.getKey();
            final Object value = node.getValue();
            checkArgument(key != null, "key");
            checkArgument(value != null, "value");
            dataView.set(DataQuery.of(key.toString()), node.getValue());
        }
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
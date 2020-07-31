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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.ConfigurationVisitor;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A translator for translating {@link DataView}s into {@link ConfigurationNode} s.
 */
public class ConfigurateTranslator implements DataTranslator<ConfigurationNode> {
    private static final ConfigurateTranslator INSTANCE = new ConfigurateTranslator();
    private static final TypeToken<ConfigurationNode> TOKEN = TypeToken.of(ConfigurationNode.class);
    private static final ConfigurationOptions DEFAULT_OPTS = ConfigurationOptions.defaults()
            .withNativeTypes(ImmutableSet.of(Map.class,
                    List.class,
                    Double.class,
                    Long.class,
                    Integer.class,
                    Boolean.class,
                    String.class
                    )
            );

    /**
     * Get the instance of this translator.
     *
     * @return The instance of this translator
     */
    public static ConfigurateTranslator instance() {
        return INSTANCE;
    }

    private ConfigurateTranslator() {
    }

    @Override
    public String getId() {
        return "sponge:configuration_node";
    }

    @Override
    public String getName() {
        return "ConfigurationNodeTranslator";
    }

    @Override
    public TypeToken<ConfigurationNode> getToken() {
        return TOKEN;
    }

    // DataView -> ConfigurationNode

    /**
     * Given a node and a data view, replace data in the node with the data contained within the data view
     *
     * @param node destination node
     * @param view source data view
     */
    public void translateDataToNode(ConfigurationNode node, DataView view) {
        Preconditions.checkNotNull(node, "node");
        Preconditions.checkNotNull(view, "container");

        final Map<Object, ? extends ConfigurationNode> originalMap = node.getChildrenMap();
        if (originalMap.isEmpty()) {
            node.setValue(ImmutableMap.of());
        }

        // Unvisited hijinks to preserve any comments that may be present
        final Set<Object> unvisitedKeys = new HashSet<>(originalMap.keySet());
        for (DataQuery key : view.getKeys(false)) {
            valueToNode(node.getNode(key.getParts()), view.get(key).orElse(null));
            unvisitedKeys.remove(key.getParts().get(0));
        }

        for (Object unusedChild : unvisitedKeys) {
            node.removeChild(unusedChild);
        }
    }

    /**
     * Convert between Configurate and SpongeData type models.
     * <p>
     * {@link ConfigurationNode} and {@link DataContainer} are two different semi-dynamic data structures that have type
     * models just different enough to cause difficulty when trying to convert between the two. We do our best to shove
     * the values from a DataView into a ConfigurationNode by inspecting the node's native types.
     *
     * @param node destination node
     * @param value Source value
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void valueToNode(ConfigurationNode node, @Nullable Object value) {
        if (value instanceof DataView) {
            translateDataToNode(node, (DataView) value);
        } else if (value instanceof Collection<?>) {
            node.setValue(ImmutableList.of());
            for (Object child : ((Collection<?>) value)) {
                valueToNode(node.appendListNode(), child);
            }
        } else if (value == null) {
            node.setValue(null);
        } else {
            Class<?> vClazz = value.getClass();
            if (node.getOptions().acceptsType(vClazz)) {
                node.setValue(value);
            } else {
                final TypeToken<?> token = TypeToken.of(vClazz); // hey let's guess at a type
                @Nullable TypeSerializer serial = node.getOptions().getSerializers().get(token);
                if (serial != null) {
                    try {
                        serial.serialize(token, value, node);
                    } catch (ObjectMappingException e) {
                        throw new IllegalArgumentException(e);
                    }
                } else {
                    throw new IllegalArgumentException("DataView value type of " + token + " is not supported by the provided ConfigurationNode");
                }
            }
        }
    }

    @Override
    public ConfigurationNode translate(DataView view) throws InvalidDataException {
        final ConfigurationNode node = ConfigurationNode.root(DEFAULT_OPTS);
        translateDataToNode(node, view);
        return node;
    }

    // ConfigurationNode -> DataContainer

    @Override
    public DataContainer translate(ConfigurationNode obj) throws InvalidDataException {
        DataView view = obj.visit(ToDataView.INSTANCE);
        if (!(view instanceof DataContainer)) {
            throw new IllegalStateException("Returned data view was not the original container!");
        }
        return (DataContainer) view;
    }

    @Override
    public DataView addTo(ConfigurationNode node, DataView dataView) {
        final VisitState state = new VisitState();
        state.add(dataView);
        node.visit(ToDataView.INSTANCE, state);
        return dataView;
    }

    static class VisitState extends LinkedList<Object> {
        @Nullable ConfigurationNode start;
    }

    static class ToDataView implements ConfigurationVisitor.Safe<VisitState, DataView> {

        static ToDataView INSTANCE = new ToDataView();

        private ToDataView() {
        }

        private DataQuery queryFrom(ConfigurationNode node) {
            final Object key = node.getKey();
            if (key == null) {
                throw new IllegalArgumentException("Null keys are not supported in data views (at " + Arrays.toString(node.getPath()) + ")");
            }
            return DataQuery.of(key.toString());
        }

        @Override
        public VisitState newState() {
            final VisitState ret = new VisitState();
            ret.add(DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED));
            return ret;
        }

        @Override
        public void beginVisit(ConfigurationNode node, VisitState state) {
            if (!node.isEmpty() && !node.isMap()) {
                throw new IllegalArgumentException("Only mapping nodes can be represented in DataViews");
            }
            state.start = node;
        }

        @Override
        public void enterNode(ConfigurationNode node, VisitState state) {}

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void enterMappingNode(ConfigurationNode node, VisitState state) {
            if (state.start == node) { // we're at root
                state.addFirst(state.getFirst()); // keep things balanced
                return;
            }

            final Object peek = state.getFirst();
            DataView ret;
            if (peek instanceof DataView) {
                ret = ((DataView) peek).createView(queryFrom(node));
            } else if (peek instanceof List<?>) {
                ((List) peek).add(ret = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED));
            } else {
                throw new Error("Unknown peek type of " + peek.getClass());
            }
            state.addFirst(ret);
        }

        @Override
        public void enterListNode(ConfigurationNode node, VisitState state) {
            state.addFirst(new LinkedList<>());
        }

        @Override
        public void enterScalarNode(ConfigurationNode node, VisitState state) {
            addToFirst(state, node, node.getValue());
        }

        @Override
        public void exitMappingNode(ConfigurationNode node, VisitState state) {
            if (!(state.removeFirst() instanceof DataView)) {
                throw new IllegalStateException("Exited a mapping node but the top value was not a DataView");
            }
        }

        @Override
        public void exitListNode(ConfigurationNode node, VisitState state) {
            final Object popped = state.removeFirst();
            addToFirst(state, node, popped);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void addToFirst(Deque<Object> stack, ConfigurationNode keySource, Object value) {
            final Object peek = stack.getFirst();
            if (peek instanceof DataView) {
                ((DataView) peek).set(queryFrom(keySource), value);
            } else if (peek instanceof List<?>) {
                ((List) peek).add(value);
            }
        }

        @Override
        public DataView endVisit(VisitState state) {
            state.start = null;
            return (DataView) state.remove();
        }
    }

}

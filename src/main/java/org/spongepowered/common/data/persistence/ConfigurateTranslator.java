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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.ConfigurationVisitor;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A translator for translating {@link DataView}s into {@link ConfigurationNode} s.
 */
public final class ConfigurateTranslator implements DataTranslator<ConfigurationNode> {

    private static final ResourceKey KEY = ResourceKey.sponge("configuration_node");
    private static final ConfigurateTranslator INSTANCE = new ConfigurateTranslator();
    private static final TypeToken<ConfigurationNode> TOKEN = TypeToken.get(ConfigurationNode.class);
    private static final ConfigurationOptions DEFAULT_OPTS = ConfigurationOptions.defaults()
            .nativeTypes(ImmutableSet.of(
                    Map.class,
                    List.class,
                    Double.class,
                    Long.class,
                    Integer.class,
                    Boolean.class,
                    String.class
                    )
            )
            .serializers(coll -> coll.registerAll(SpongeAdventure.CONFIGURATE.serializers()));

    /**
     * Get the instance of this translator.
     *
     * @return The instance of this translator
     */
    public static ConfigurateTranslator instance() {
        return ConfigurateTranslator.INSTANCE;
    }

    private ConfigurateTranslator() {
    }

    @Override
    public ResourceKey getKey() {
        return ConfigurateTranslator.KEY;
    }

    @Override
    public TypeToken<ConfigurationNode> getToken() {
        return ConfigurateTranslator.TOKEN;
    }

    // DataView -> ConfigurationNode

    /**
     * Given a node and a data view, replace data in the node with the data contained within the data view
     *
     * @param node destination node
     * @param view source data view
     */
    public void translateDataToNode(final ConfigurationNode node, final DataView view) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(view, "container");

        final Map<Object, ? extends ConfigurationNode> originalMap = node.childrenMap();
        if (originalMap.isEmpty()) {
            node.raw(ImmutableMap.of());
        }

        // Unvisited hijinks to preserve any comments that may be present
        final Set<Object> unvisitedKeys = new HashSet<>(originalMap.keySet());
        for (final DataQuery key : view.getKeys(false)) {
            this.valueToNode(node.node(key), view.get(key).orElse(null));
            unvisitedKeys.remove(key.getParts().get(0));
        }

        for (final Object unusedChild : unvisitedKeys) {
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
    private void valueToNode(final ConfigurationNode node, final @Nullable Object value) {
        if (value instanceof DataView) {
            this.translateDataToNode(node, (DataView) value);
        } else if (value instanceof Collection<?>) {
            node.raw(ImmutableList.of());
            for (final Object child : ((Collection<?>) value)) {
                this.valueToNode(node.appendListNode(), child);
            }
        } else if (value == null) {
            node.raw(null);
        } else {
            final Class<?> vClazz = value.getClass();
            if (node.options().acceptsType(vClazz)) {
                node.raw(value);
            } else {
                final @Nullable TypeSerializer serial = node.options().serializers().get(vClazz);
                if (serial != null) {
                    try {
                        serial.serialize(vClazz, value, node);
                    } catch (final SerializationException e) {
                        throw new IllegalArgumentException(e);
                    }
                } else {
                    throw new IllegalArgumentException("DataView value type of " + vClazz + " is not supported by the provided ConfigurationNode");
                }
            }
        }
    }

    @Override
    public ConfigurationNode translate(final DataView view) throws InvalidDataException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(ConfigurateTranslator.DEFAULT_OPTS);
        this.translateDataToNode(node, view);
        return node;
    }

    // ConfigurationNode -> DataContainer

    @Override
    public DataContainer translate(final ConfigurationNode obj) throws InvalidDataException {
        final DataView view = obj.visit(ToDataView.INSTANCE);
        if (!(view instanceof DataContainer)) {
            throw new IllegalStateException("Returned data view was not the original container!");
        }
        return (DataContainer) view;
    }

    @Override
    public DataView addTo(final ConfigurationNode node, final DataView dataView) {
        final VisitState state = new VisitState();
        state.add(dataView);
        node.visit(ToDataView.INSTANCE, state);
        return dataView;
    }

    static class VisitState extends LinkedList<Object> {
        @Nullable ConfigurationNode start;
    }

    static class ToDataView implements ConfigurationVisitor.Safe<VisitState, DataView> {

        static final ToDataView INSTANCE = new ToDataView();

        private ToDataView() {
        }

        private DataQuery queryFrom(final ConfigurationNode node) {
            final Object key = node.key();
            if (key == null) {
                throw new IllegalArgumentException("Null keys are not supported in data views (at " + node.path() + ")");
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
        public void beginVisit(final ConfigurationNode node, final VisitState state) {
            if (!node.empty() && !node.isMap()) {
                throw new IllegalArgumentException("Only mapping nodes can be represented in DataViews");
            }
            state.start = node;
        }

        @Override
        public void enterNode(final ConfigurationNode node, final VisitState state) {}

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void enterMappingNode(final ConfigurationNode node, final VisitState state) {
            if (state.start == node) { // we're at root
                state.addFirst(state.getFirst()); // keep things balanced
                return;
            }

            final Object peek = state.getFirst();
            final DataView ret;
            if (peek instanceof DataView) {
                ret = ((DataView) peek).createView(this.queryFrom(node));
            } else if (peek instanceof List<?>) {
                ((List) peek).add(ret = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED));
            } else {
                throw new IllegalArgumentException("Unknown peek type of " + peek.getClass());
            }
            state.addFirst(ret);
        }

        @Override
        public void enterListNode(final ConfigurationNode node, final VisitState state) {
            state.addFirst(new LinkedList<>());
        }

        @Override
        public void enterScalarNode(final ConfigurationNode node, final VisitState state) {
            this.addToFirst(state, node, node.raw());
        }

        @Override
        public void exitMappingNode(final ConfigurationNode node, final VisitState state) {
            if (!(state.removeFirst() instanceof DataView)) {
                throw new IllegalStateException("Exited a mapping node but the top value was not a DataView");
            }
        }

        @Override
        public void exitListNode(final ConfigurationNode node, final VisitState state) {
            final Object popped = state.removeFirst();
            this.addToFirst(state, node, popped);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void addToFirst(final Deque<Object> stack, final ConfigurationNode keySource, final Object value) {
            final Object peek = stack.getFirst();
            if (peek instanceof DataView) {
                ((DataView) peek).set(this.queryFrom(keySource), value);
            } else if (peek instanceof List<?>) {
                ((List) peek).add(value);
            }
        }

        @Override
        public DataView endVisit(final VisitState state) {
            state.start = null;
            return (DataView) state.remove();
        }

    }

}

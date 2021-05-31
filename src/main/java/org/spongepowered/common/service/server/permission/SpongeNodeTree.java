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
package org.spongepowered.common.service.server.permission;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.service.permission.NodeTree;
import org.spongepowered.api.util.Tristate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class SpongeNodeTree implements NodeTree {

    private static final Pattern NODE_SPLIT = Pattern.compile("\\.");
    private final Node rootNode;

    SpongeNodeTree(final Tristate value) {
        this.rootNode = new Node(new HashMap<>());
        this.rootNode.value = value;
    }

    SpongeNodeTree(final Node rootNode) {
        this.rootNode = rootNode;
    }

    <T> void populate(final Map<String, T> values, final Function<T, Tristate> converter) {
        for (final Map.Entry<String, T> value : values.entrySet()) {
            final String[] parts = SpongeNodeTree.NODE_SPLIT.split(value.getKey().toLowerCase(), -1);
            Node currentNode = this.rootNode;
            for (final String part : parts) {
                if (currentNode.children.containsKey(part)) {
                    currentNode = currentNode.children.get(part);
                } else {
                    final Node newNode = new Node(new HashMap<>());
                    currentNode.children.put(part, newNode);
                    currentNode = newNode;
                }
            }
            currentNode.value = converter.apply(value.getValue());
        }
    }

    @Override
    public Tristate get(final String node) {
        final String[] parts = SpongeNodeTree.NODE_SPLIT.split(node.toLowerCase(), -1);
        Node currentNode = this.rootNode;
        Tristate lastUndefinedVal = Tristate.UNDEFINED;
        for (String str : parts) {
            if (!currentNode.children.containsKey(str)) {
                break;
            }
            currentNode = currentNode.children.get(str);
            if (currentNode.value != Tristate.UNDEFINED) {
                lastUndefinedVal = currentNode.value;
            }
        }
        return lastUndefinedVal;

    }

    @Override
    public Tristate rootValue() {
        return this.rootNode.value;
    }

    @Override
    public NodeTree withRootValue(Tristate state) {
        final Node newRoot = new Node(this.rootNode.children);
        newRoot.value = Objects.requireNonNull(state, "state");
        return new SpongeNodeTree(newRoot);
    }


    @Override
    public Map<String, Boolean> asMap() {
        final ImmutableMap.Builder<String, Boolean> ret = ImmutableMap.builder();
        for (final Map.Entry<String, Node> ent : this.rootNode.children.entrySet()) {
            this.populateMap(ret, ent.getKey(), ent.getValue());
        }
        return ret.build();
    }

    private void populateMap(final ImmutableMap.Builder<String, Boolean> values, final String prefix, final Node currentNode) {
        if (currentNode.value != Tristate.UNDEFINED) {
            values.put(prefix, currentNode.value.asBoolean());
        }
        for (final Map.Entry<String, Node> ent : currentNode.children.entrySet()) {
            this.populateMap(values, prefix + '.' + ent.getKey(), ent.getValue());
        }
    }

    @Override
    public NodeTree withValue(final String node, final Tristate value) {
        final String[] parts = SpongeNodeTree.NODE_SPLIT.split(node.toLowerCase(), -1);
        final Node newRoot = new Node(new HashMap<>(this.rootNode.children));
        Node newPtr = newRoot;
        Node currentPtr = this.rootNode;

        newPtr.value = currentPtr == null ? Tristate.UNDEFINED : currentPtr.value;
        for (String part : parts) {
            final Node oldChild = currentPtr == null ? null : currentPtr.children.get(part);
            final Node newChild = new Node(oldChild != null ? new HashMap<>(oldChild.children) : new HashMap<>());
            newPtr.children.put(part, newChild);
            currentPtr = oldChild;
            newPtr = newChild;
        }
        newPtr.value = value;
        return new SpongeNodeTree(newRoot);
    }

    @Override
    public NodeTree withAll(final Map<String, Boolean> values) {
        NodeTree ret = this;
        for (final Map.Entry<String, Boolean> ent : values.entrySet()) {
            ret = ret.withValue(ent.getKey(), Tristate.fromBoolean(ent.getValue()));
        }
        return ret;
    }

    @Override
    public NodeTree withAllTristates(final Map<String, Tristate> values) {
        NodeTree ret = this;
        for (Map.Entry<String, Tristate> ent : values.entrySet()) {
            ret = ret.withValue(ent.getKey(), ent.getValue());
        }
        return ret;
    }

    public static class Node {

        final Map<String, SpongeNodeTree.Node> children;
        Tristate value = Tristate.UNDEFINED;

        Node(Map<String, SpongeNodeTree.Node> children) {
            this.children = children;
        }
    }

    public static final class FactoryImpl implements Factory {

        @Override
        public NodeTree ofBooleans(final Map<String, Boolean> values, final Tristate defaultValue) {
            SpongeNodeTree newTree = new SpongeNodeTree(defaultValue);
            newTree.populate(values, Tristate::fromBoolean);
            return newTree;
        }

        @Override
        public NodeTree ofTristates(final Map<String, Tristate> values, final Tristate defaultValue) {
            final SpongeNodeTree newTree = new SpongeNodeTree(defaultValue);
            newTree.populate(values, Function.identity());
            return newTree;
        }

    }

}

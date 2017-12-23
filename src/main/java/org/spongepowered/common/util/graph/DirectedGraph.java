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
package org.spongepowered.common.util.graph;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A directed graph type for performing graph operations.
 */
public class DirectedGraph<D> {

    private final Map<D, DataNode<D>> nodes = new HashMap<>();

    public DirectedGraph() {
    }

    /**
     * Gets the count of nodes in the graph.
     */
    public int getNodeCount() {
        return this.nodes.size();
    }

    /**
     * Gets the count of edges in the graph.
     */
    public int getEdgeCount() {
        int count = 0;
        for (DataNode<D> n : this.nodes.values()) {
            count += n.getEdgeCount();
        }
        return count;
    }

    /**
     * Gets if the graph contains a node with the given data.
     */
    public boolean contains(D data) {
        return this.nodes.containsKey(data);
    }

    /**
     * Gets the node corresponding to the given data.
     */
    public DataNode<D> get(D data) {
        return this.nodes.get(data);
    }

    /**
     * Adds a directed edge between the two given nodes. If either the start or
     * the end data does not have a corresponding node in the graph then a new
     * node is added.
     */
    public void addEdge(D from, D to) {
        DataNode<D> fromNode = add(from);
        DataNode<D> toNode = add(to);
        if (!fromNode.isAdjacent(toNode)) {
            fromNode.addEdge(toNode);
        }
    }

    /**
     * Gets all nodes in the graph.
     */
    public Collection<DataNode<D>> getNodes() {
        return this.nodes.values();
    }

    /**
     * Returns a directed graph which represents the reverse of this graph. The
     * reverse of a directed graph is a graph with the same nodes but the
     * direction of each edge is reversed.
     */
    public DirectedGraph<D> reverse() {
        DirectedGraph<D> rev = new DirectedGraph<>();
        Map<DataNode<D>, DataNode<D>> siblings = new HashMap<>();
        for (DataNode<D> n : this.nodes.values()) {
            DataNode<D> b = rev.add(n.getData());
            siblings.put(n, b);
        }
        for (DataNode<D> n : this.nodes.values()) {
            DataNode<D> n_sibling = siblings.get(n);
            for (DataNode<D> b : n.getAdjacent()) {
                siblings.get(b).addEdge(n_sibling);
            }
        }
        return rev;
    }

    /**
     * Adds the given node to the graph.
     */
    public DataNode<D> add(D d) {
        DataNode<D> node = this.nodes.get(d);
        if (node == null) {
            node = new DataNode<>(d);
            this.nodes.put(d, node);
        }
        return node;
    }

    /**
     * Deletes the given node from the graph, and all edges that originated from
     * or connected to the node.
     */
    public boolean remove(D n) {
        DataNode<D> node = this.nodes.get(n);
        if (node == null) {
            return false;
        }
        for (DataNode<D> b : this.nodes.values()) {
            b.removeEdge(node);
        }
        this.nodes.remove(n);
        return true;
    }

    /**
     * Clears all nodes and edges from this graph.
     */
    public void clear() {
        this.nodes.clear();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Node count: ").append(getNodeCount());
        str.append(" Edge count: ").append(getEdgeCount()).append("\n");
        for (DataNode<D> n : this.nodes.values()) {
            str.append(n.getData().toString()).append(" Edges: (");
            for (DataNode<D> a : n.getAdjacent()) {
                str.append(a.getData().toString()).append(" ");
            }
            str.append(")\n");
        }
        return str.toString();
    }

    /**
     * The representation of a node in a graph.
     */
    public static class DataNode<D> {

        private final List<DataNode<D>> adj = Lists.newArrayList();
        final D data;

        public DataNode(D obj) {
            this.data = obj;
        }

        /**
         * Gets the data that this node represents.
         */
        public D getData() {
            return this.data;
        }

        /**
         * Adds an edge from this node to the given node.
         */
        public void addEdge(DataNode<D> other) {
            this.adj.add(other);
        }

        /**
         * Deletes the node from this node to the given node if it exists.
         */
        public boolean removeEdge(DataNode<D> other) {
            return this.adj.remove(other);
        }

        /**
         * Returns if this node has an edge to the given node.
         */
        public boolean isAdjacent(DataNode<D> other) {
            return this.adj.contains(other);
        }

        /**
         * Gets the count of edges originating from this node.
         */
        public int getEdgeCount() {
            return this.adj.size();
        }

        /**
         * Gets all nodes for which there is an edge from this node.
         */
        public Collection<DataNode<D>> getAdjacent() {
            return this.adj;
        }

        @Override
        public int hashCode() {
            return this.data.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof DataNode)) {
                return false;
            }
            DataNode<?> d = (DataNode<?>) o;
            return d.getData().equals(this.data);
        }

    }

}

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

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.common.util.graph.DirectedGraph.DataNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TopologicalOrder {

    /**
     * Performs a topological sort over the directed graph, fir the purpose of
     * determining load order between a set of components where an edge is
     * representing a load-after dependency. For example an edge from node A to
     * node B signifies that A depends on B and that B must load before A, the
     * resulting topological order would therefore be {@code [B, A]}.
     * 
     * @throws CyclicGraphException if the graph contains a cycle.
     */
    public static <T> List<T> createOrderedLoad(DirectedGraph<T> graph) {
        final List<T> orderedList = new ArrayList<>();
        while (graph.getNodeCount() != 0) {
            DirectedGraph.DataNode<T> next = null;
            for (DirectedGraph.DataNode<T> node : graph.getNodes()) {
                if (node.getEdgeCount() == 0) {
                    next = node;
                    break;
                }
            }
            if (next == null) {
                // We have a cycle
                // Find all cycles for reporting purposes
                TarjanCycleDetector detector = new TarjanCycleDetector(graph);
                List<DataNode<?>[]> cycles = detector.getCycles();
                StringBuilder msg = new StringBuilder();
                msg.append("Graph is cyclic! Cycles:\n");
                for (DataNode<?>[] cycle : cycles) {
                    msg.append("[");
                    for (DataNode<?> node : cycle) {
                        msg.append(node.getData().toString()).append(" ");
                    }
                    msg.append("]\n");
                }
                throw new CyclicGraphException(cycles, msg.toString());
            }
            orderedList.add(next.getData());
            graph.remove(next.getData());
        }
        return orderedList;
    }

    /**
     * Uses Tarjan's strongly connected components algorithm to find all cycles
     * in a graph.
     */
    private static class TarjanCycleDetector {

        private DirectedGraph<?> graph;
        private int index = 0;
        private Deque<DataNode<?>> stack = new ArrayDeque<>();
        private Object2IntOpenHashMap<DataNode<?>> node_indices = new Object2IntOpenHashMap<>();
        private Object2IntOpenHashMap<DataNode<?>> lowlinks = new Object2IntOpenHashMap<>();
        private List<DataNode<?>[]> result = null;

        public TarjanCycleDetector(DirectedGraph<?> graph) {
            this.graph = graph;
        }

        public List<DataNode<?>[]> getCycles() {
            if (this.result != null) {
                return this.result;
            }
            this.result = new ArrayList<>();
            for (DataNode<?> node : this.graph.getNodes()) {
                if (!this.node_indices.containsKey(node)) {
                    strongconnect(node);
                }
            }
            return this.result;
        }

        private void strongconnect(DataNode<?> node) {
            this.node_indices.put(node, this.index);
            this.lowlinks.put(node, this.index);
            this.index++;
            this.stack.push(node);

            for (DataNode<?> adj : node.getAdjacent()) {
                if (!this.node_indices.containsKey(adj)) {
                    strongconnect(adj);
                    int lowlink = Math.min(this.lowlinks.getInt(node), this.lowlinks.getInt(adj));
                    this.lowlinks.put(node, lowlink);
                } else if (this.stack.contains(adj)) {
                    int lowlink = Math.min(this.lowlinks.getInt(node), this.node_indices.getInt(adj));
                    this.lowlinks.put(node, lowlink);
                }
            }

            if (this.lowlinks.getInt(node) == this.node_indices.getInt(node)) {
                List<DataNode<?>> cycle = new ArrayList<>();
                DataNode<?> w;
                do {
                    w = this.stack.pop();
                    cycle.add(w);
                } while (w != node);
                this.result.add(cycle.toArray(new DataNode<?>[cycle.size()]));
            }
        }

    }
}

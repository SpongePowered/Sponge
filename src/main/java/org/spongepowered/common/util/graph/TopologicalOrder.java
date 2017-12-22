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

import com.google.common.collect.Sets;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class TopologicalOrder<T> {

    private final DirectedGraph<T> digraph;
    private Set<T> marked;

    public TopologicalOrder(DirectedGraph<T> graph) {
        this.digraph = graph;
    }

    @Nullable
    public Iterable<T> order(DirectedGraph.DataNode<T> root) {
        final CycleDetector<T> cycleDetector = new CycleDetector<>(this.digraph);
        if (cycleDetector.hasCycle()) {
            return null;
        }
        this.marked = Sets.newHashSet();
        ArrayDeque<T> order = new ArrayDeque<>();
        dfs(order, root);

        return order;
    }

    private void dfs(ArrayDeque<T> order, DirectedGraph.DataNode<T> root) {
        this.marked.add(root.getData());
        for (DirectedGraph.DataNode<T> n : root.getAdjacent()) {
            if (!this.marked.contains(n)) {
                dfs(order, n);
            }
        }
        order.push(root.getData());
    }

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
                throw new IllegalStateException("Graph is cyclic!");
            }
            orderedList.add(next.getData());
            graph.delete(next);
        }
        return orderedList;
    }
}
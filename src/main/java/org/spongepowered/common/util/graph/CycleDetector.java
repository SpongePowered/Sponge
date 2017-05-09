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
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class CycleDetector<T> {

    private final DirectedGraph<T> graph;
    private Set<DirectedGraph.DataNode<T>> marked;

    public CycleDetector(DirectedGraph<T> g) {
        this.graph = g;
    }

    public boolean hasCycle() {
        this.marked = Sets.newHashSet();
        List<DirectedGraph.DataNode<T>> all = Lists.newArrayList(this.graph.getNodes());
        while (!all.isEmpty()) {
            DirectedGraph.DataNode<T> n = all.get(0);
            boolean cycle = dfs(n);
            if (cycle) {
                return true;
            }
            all.removeAll(this.marked);
            this.marked.clear();
        }
        return false;
    }

    private boolean dfs(DirectedGraph.DataNode<T> root) {
        this.marked.add(root);
        for (DirectedGraph.DataNode<T> a : root.getAdjacent()) {
            if (!this.marked.contains(a)) {
                return dfs(a);
            }
            return true;
        }
        return false;
    }

}

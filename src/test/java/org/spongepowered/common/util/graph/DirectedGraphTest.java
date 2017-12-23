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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.spongepowered.common.util.graph.DirectedGraph.DataNode;

public class DirectedGraphTest {

    @Test
    public void testNodeCount() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        assertEquals(0, graph.getNodeCount());
        graph.add(1);
        graph.add(2);
        assertEquals(2, graph.getNodeCount());
        graph.add(3);
        // 2 is a duplicate and shouldn't create a new node.
        graph.add(2);
        assertEquals(3, graph.getNodeCount());
        graph.remove(3);
        assertEquals(2, graph.getNodeCount());
        graph.clear();
        assertEquals(0, graph.getNodeCount());
    }

    @Test
    public void testEdgeCount() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);
        assertEquals(2, graph.getEdgeCount());
        graph.addEdge(3, 4);
        graph.addEdge(2, 3);
        assertEquals(3, graph.getEdgeCount());
        graph.get(1).removeEdge(graph.get(3));
        assertEquals(2, graph.getEdgeCount());
        graph.remove(2);
        assertEquals(1, graph.getEdgeCount());
    }

    @Test
    public void testContains() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.add(1);
        graph.add(2);
        assertTrue(graph.contains(1));
        assertFalse(graph.contains(3));
    }

    @Test
    public void testGet() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        // any sequence of add and get should return the same node instance
        DataNode<Integer> a = graph.add(1);
        DataNode<Integer> b = graph.get(1);
        DataNode<Integer> c = graph.add(1);
        DataNode<Integer> d = graph.get(1);
        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(c, d);
    }

    @Test
    public void testReverse() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.addEdge(1, 2);
        graph.addEdge(2, 4);
        graph.addEdge(3, 1);
        graph.addEdge(3, 4);
        graph.addEdge(4, 5);

        DirectedGraph<Integer> rev = graph.reverse();
        assertEquals(5, rev.getEdgeCount());
        assertTrue(rev.get(2).isAdjacent(rev.get(1)));
        assertTrue(rev.get(4).isAdjacent(rev.get(2)));
        assertTrue(rev.get(1).isAdjacent(rev.get(3)));
        assertTrue(rev.get(4).isAdjacent(rev.get(3)));
        assertTrue(rev.get(5).isAdjacent(rev.get(4)));
    }

}

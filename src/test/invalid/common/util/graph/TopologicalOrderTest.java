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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.common.util.graph.DirectedGraph.DataNode;

import java.util.List;

public class TopologicalOrderTest {

    @Test
    public void testEmptyGraph() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        List<Integer> order = TopologicalOrder.createOrderedLoad(graph);
        assertNotNull(order);
        assertTrue(order.isEmpty());
    }

    @Test
    public void testSingleGraph() {
//        
//          1
//        
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.add(1);

        List<Integer> order = TopologicalOrder.createOrderedLoad(graph);
        assertNotNull(order);
        assertEquals(1, order.size());
        assertEquals(Integer.valueOf(1), order.get(0));
    }

    @Test
    public void testSimpleGraph() {
//        
//          1 - 2 - 3 - 4
//        
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 4);

        List<Integer> order = TopologicalOrder.createOrderedLoad(graph);
        assertNotNull(order);
        assertEquals(4, order.size());
        assertEquals(Integer.valueOf(4), order.get(0));
        assertEquals(Integer.valueOf(3), order.get(1));
        assertEquals(Integer.valueOf(2), order.get(2));
        assertEquals(Integer.valueOf(1), order.get(3));
    }

    @Test
    public void testSelfCycle() {
//        
//          1 - .
//           \ /
//            .
//        
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.addEdge(1, 1);
        try {
            TopologicalOrder.createOrderedLoad(graph);
            Assert.fail();
        } catch (CyclicGraphException e) {
            List<DataNode<?>[]> cycles = e.getCycles();
            assertEquals(1, cycles.size());
            DataNode<?>[] cycle1 = cycles.get(0);
            assertEquals(1, cycle1.length);
            assertEquals(1, cycle1[0].getData());
        }
    }

    @Test
    public void testSimpleCycle() {
//        
//          1 - 2
//           \ /
//            3
//        
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);
        try {
            TopologicalOrder.createOrderedLoad(graph);
            Assert.fail();
        } catch (CyclicGraphException e) {
            List<DataNode<?>[]> cycles = e.getCycles();
            assertEquals(1, cycles.size());
            DataNode<?>[] cycle1 = cycles.get(0);
            assertEquals(3, cycle1.length);
            assertEquals(3, cycle1[0].getData());
            assertEquals(2, cycle1[1].getData());
            assertEquals(1, cycle1[2].getData());
        }
    }

    @Test
    public void testSimpleCycle2() {
//        
//          1 - 2
//          |\  |
//          | \ |
//          4 - 3
//        
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);
        graph.addEdge(3, 4);
        graph.addEdge(4, 1);
        try {
            TopologicalOrder.createOrderedLoad(graph);
            Assert.fail();
        } catch (CyclicGraphException e) {
            // The cycle detected outputs the largest cycle for any strongly
            // connected group
            List<DataNode<?>[]> cycles = e.getCycles();
            assertEquals(1, cycles.size());
            DataNode<?>[] cycle1 = cycles.get(0);
            assertEquals(4, cycle1.length);
            assertEquals(4, cycle1[0].getData());
            assertEquals(3, cycle1[1].getData());
            assertEquals(2, cycle1[2].getData());
            assertEquals(1, cycle1[3].getData());
        }
    }

    @Test
    public void testMulti() {
//        
//          1 - 2 - 4 - 5
//           \ /     \ /
//            3       6
//        
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);
        graph.addEdge(2, 4);
        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(6, 4);
        try {
            TopologicalOrder.createOrderedLoad(graph);
            Assert.fail();
        } catch (CyclicGraphException e) {
            List<DataNode<?>[]> cycles = e.getCycles();
            assertEquals(2, cycles.size());
            DataNode<?>[] cycle1 = cycles.get(0);
            assertEquals(3, cycle1.length);
            assertEquals(6, cycle1[0].getData());
            assertEquals(5, cycle1[1].getData());
            assertEquals(4, cycle1[2].getData());
            DataNode<?>[] cycle2 = cycles.get(1);
            assertEquals(3, cycle2.length);
            assertEquals(3, cycle2[0].getData());
            assertEquals(2, cycle2[1].getData());
            assertEquals(1, cycle2[2].getData());
        }
    }

}

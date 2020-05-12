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
package org.spongepowered.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.junit.Test;

public class DuplicateRemovalVisitorTest {

    @Test
    public void testEmpty() {
        final ConfigurationNode parent = ConfigurationNode.root();
        final ConfigurationNode child = ConfigurationNode.root();
        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);
    }

    @Test
    public void testClearEqualValues() {
        final ConfigurationNode parent = ConfigurationNode.root();
        final ConfigurationNode child = ConfigurationNode.root();
        parent.setValue("test");
        child.setValue("test");
        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);
        assertNull(child.getValue());
    }

    @Test
    public void testClearsEqualDoubles() {
        final ConfigurationNode parent = ConfigurationNode.root();
        final ConfigurationNode child = ConfigurationNode.root();

        parent.setValue(3d);
        child.setValue(3d);
        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);
        assertEquals(3d, parent.getValue());
        assertNull(child.getValue());

        child.setValue(3);
        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);
        assertNull(child.getValue());
    }

    @Test
    public void testClearsEqualDoublesInChild() {
        final ConfigurationNode parent = ConfigurationNode.root();
        final ConfigurationNode child = ConfigurationNode.root();

        parent.getNode("test").setValue(42);
        child.getNode("test").setValue(42d);

        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);

        assertTrue(child.isEmpty());
        assertNull(child.getNode("test").getValue());
    }

    @Test
    public void testClearsMapKeys() {
        final ConfigurationNode parent = ConfigurationNode.root(n -> {
            n.getNode("test1").setValue("yeet");
            n.getNode("test2").setValue("yoink");
        });
        final ConfigurationNode child = ConfigurationNode.root(n -> {
            n.getNode("test1").setValue("yeet");
            n.getNode("test2").setValue("yikes");
        });

        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);
        assertTrue(child.getNode("test1").isVirtual());
        assertEquals("yikes", child.getNode("test2").getValue());
    }

    @Test
    public void testPreservesListElements() {
        final ConfigurationNode parent = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().setValue("red");
            n.appendListNode().setValue("blue");
        });
        final ConfigurationNode child = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().setValue("red");
            n.appendListNode().setValue("green");
        });
        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);
        assertEquals(ImmutableList.of("one", "two", "red", "green"), child.getList(String::valueOf));
    }

    @Test
    public void testEqualListCleared() {
        final ConfigurationNode parent = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().act(c -> {
                c.getNode("zombie").setValue(false);
                c.getNode("villager").setValue(false);
                c.getNode("cat").setValue(true);
                c.getNode("ocelot").setValue(true);
            });
            n.appendListNode().setValue("blue");
        });
        final ConfigurationNode child = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().act(c -> {
                c.getNode("zombie").setValue(false);
                c.getNode("villager").setValue(false);
                c.getNode("cat").setValue(true);
                c.getNode("ocelot").setValue(true);
            });
            n.appendListNode().setValue("blue");
        });
        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);
        assertTrue(child.isEmpty());
        assertNull(child.getValue());
    }

    @Test
    public void testMapKeysClearedInList() {
        final ConfigurationNode parent = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().act(c -> {
                c.getNode("zombie").setValue(false);
                c.getNode("villager").setValue(false);
                c.getNode("cat").setValue(true);
                c.getNode("ocelot").setValue(true);
            });
            n.appendListNode().setValue("blue");
        });
        final ConfigurationNode child = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().act(c -> {
                c.getNode("zombie").setValue(false);
                c.getNode("villager").setValue(false);
                c.getNode("cat").setValue(true);
                c.getNode("ocelot").setValue(false);
            });
            n.appendListNode().setValue("blue");
        });
        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);

        assertEquals(4, child.getChildrenList().size());
        assertNull(child.getNode(2, "cat").getValue());
        assertEquals(false, child.getNode(2, "ocelot").getValue());
    }

    @Test
    public void testEmptyMapsPreservedInList() {
        final ConfigurationNode parent = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().act(c -> {
                c.getNode("zombie").setValue(false);
                c.getNode("villager").setValue(false);
                c.getNode("cat").setValue(true);
                c.getNode("ocelot").setValue(true);
            });
            n.appendListNode().setValue("blue");
        });
        final ConfigurationNode child = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().act(c -> {
                c.getNode("zombie").setValue(false);
                c.getNode("villager").setValue(false);
                c.getNode("cat").setValue(true);
                c.getNode("ocelot").setValue(true);
            });
            n.appendListNode().setValue("green");
        });

        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);

        final ConfigurationNode expected = ConfigurationNode.root(n -> {
            n.appendListNode().setValue("one");
            n.appendListNode().setValue("two");
            n.appendListNode().setValue(ImmutableMap.of());
            n.appendListNode().setValue("green");
        });

        assertEquals(expected, child);
    }

    @Test
    public void testSpongeExample() {
        final ConfigurationNode parent = CommentedConfigurationNode.root(p -> {
            p.getNode("sponge", "world-generation-modifiers").setValue(ImmutableList.of()).setComment("World Generation Modifiers to apply to the "
                    + "world");
            p.getNode("sponge", "player-block-tracker").act(pBT -> {
                ((CommentedConfigurationNode) pBT.getNode("block-blacklist")).setValue(ImmutableList.of()).setComment("Block IDs that will be "
                        + "blacklisted for player block placement tracking");
                pBT.getNode("enabled").setValue(true);
            });
        });
        final ConfigurationNode child = parent.copy();

        child.visit(DuplicateRemovalVisitor.INSTANCE, parent);
        assertTrue(child.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVisitWithoutParentFails() {
        ConfigurationNode.root().visit(DuplicateRemovalVisitor.INSTANCE);
    }

    @Test(expected = NullPointerException.class)
    public void testVisitWithNullParentFails() {
        ConfigurationNode.root().visit(DuplicateRemovalVisitor.INSTANCE, null);
    }

}

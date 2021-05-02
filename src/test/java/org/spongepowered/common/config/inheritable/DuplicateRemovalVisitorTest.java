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
package org.spongepowered.common.config.inheritable;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class DuplicateRemovalVisitorTest {

    @Test
    void testEmpty() {
        final ConfigurationNode parent = BasicConfigurationNode.root();
        final ConfigurationNode child = BasicConfigurationNode.root();
        DuplicateRemovalVisitor.visit(child, parent);
    }

    @Test
    void testClearEqualValues() {
        final ConfigurationNode parent = BasicConfigurationNode.root();
        final ConfigurationNode child = BasicConfigurationNode.root();
        parent.raw("test");
        child.raw("test");
        DuplicateRemovalVisitor.visit(child, parent);
        assertNull(child.raw());
    }

    @Test
    void testClearsEqualDoubles() {
        final ConfigurationNode parent = BasicConfigurationNode.root();
        final ConfigurationNode child = BasicConfigurationNode.root();

        parent.raw(3d);
        child.raw(3d);
        DuplicateRemovalVisitor.visit(child, parent);
        assertEquals(3d, parent.raw());
        assertNull(child.raw());

        child.raw(3);
        DuplicateRemovalVisitor.visit(child, parent);
        assertNull(child.raw());
    }

    @Test
    void testClearsEqualDoublesInChild() {
        final ConfigurationNode parent = BasicConfigurationNode.root();
        final ConfigurationNode child = BasicConfigurationNode.root();

        parent.node("test").raw(42);
        child.node("test").raw(42d);

        DuplicateRemovalVisitor.visit(child, parent);

        assertTrue(child.empty());
        assertNull(child.node("test").raw());
    }

    @Test
    void testClearsMapKeys() {
        final ConfigurationNode parent = BasicConfigurationNode.root(n -> {
            n.node("test1").raw("yeet");
            n.node("test2").raw("yoink");
        });
        final ConfigurationNode child = BasicConfigurationNode.root(n -> {
            n.node("test1").raw("yeet");
            n.node("test2").raw("yikes");
        });

        DuplicateRemovalVisitor.visit(child, parent);
        assertTrue(child.node("test1").virtual());
        assertEquals("yikes", child.node("test2").raw());
    }

    @Test
    void testPreservesListElements() throws SerializationException {
        final ConfigurationNode parent = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().raw("red");
            n.appendListNode().raw("blue");
        });
        final ConfigurationNode child = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().raw("red");
            n.appendListNode().raw("green");
        });
        DuplicateRemovalVisitor.visit(child, parent);
        assertEquals(ImmutableList.of("one", "two", "red", "green"), child.getList(String.class));
    }

    @Test
    void testEqualListCleared() {
        final ConfigurationNode parent = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().act(c -> {
                c.node("zombie").raw(false);
                c.node("villager").raw(false);
                c.node("cat").raw(true);
                c.node("ocelot").raw(true);
            });
            n.appendListNode().raw("blue");
        });
        final ConfigurationNode child = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().act(c -> {
                c.node("zombie").raw(false);
                c.node("villager").raw(false);
                c.node("cat").raw(true);
                c.node("ocelot").raw(true);
            });
            n.appendListNode().raw("blue");
        });
        DuplicateRemovalVisitor.visit(child, parent);
        assertTrue(child.empty());
        assertNull(child.raw());
    }

    @Test
    void testMapKeysClearedInList() {
        final ConfigurationNode parent = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().act(c -> {
                c.node("zombie").raw(false);
                c.node("villager").raw(false);
                c.node("cat").raw(true);
                c.node("ocelot").raw(true);
            });
            n.appendListNode().raw("blue");
        });
        final ConfigurationNode child = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().act(c -> {
                c.node("zombie").raw(false);
                c.node("villager").raw(false);
                c.node("cat").raw(true);
                c.node("ocelot").raw(false);
            });
            n.appendListNode().raw("blue");
        });
        DuplicateRemovalVisitor.visit(child, parent);

        assertEquals(4, child.childrenList().size());
        assertNull(child.node(2, "cat").raw());
        assertEquals(false, child.node(2, "ocelot").raw());
    }

    @Test
    void testEmptyMapsPreservedInList() {
        final ConfigurationNode parent = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().act(c -> {
                c.node("zombie").raw(false);
                c.node("villager").raw(false);
                c.node("cat").raw(true);
                c.node("ocelot").raw(true);
            });
            n.appendListNode().raw("blue");
        });
        final ConfigurationNode child = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().act(c -> {
                c.node("zombie").raw(false);
                c.node("villager").raw(false);
                c.node("cat").raw(true);
                c.node("ocelot").raw(true);
            });
            n.appendListNode().raw("green");
        });

        DuplicateRemovalVisitor.visit(child, parent);

        final ConfigurationNode expected = BasicConfigurationNode.root(n -> {
            n.appendListNode().raw("one");
            n.appendListNode().raw("two");
            n.appendListNode().raw(ImmutableMap.of());
            n.appendListNode().raw("green");
        });

        assertEquals(expected, child);
    }

    @Test
    void testSpongeExample() {
        final ConfigurationNode parent = CommentedConfigurationNode.root(p -> {
            p.node("sponge", "world-generation-modifiers").raw(ImmutableList.of()).comment("World Generation Modifiers to apply to the "
                    + "world");
            p.node("sponge", "player-block-tracker").act(pBT -> {
                pBT.node("block-blacklist").raw(ImmutableList.of()).comment("Block IDs that will be "
                        + "blacklisted for player block placement tracking");
                pBT.node("enabled").raw(true);
            });
        });
        final ConfigurationNode child = parent.copy();

        DuplicateRemovalVisitor.visit(child, parent);
        assertTrue(child.empty());
    }

    @Test
    void testVisitWithNullParentFails() {
        assertThrows(NullPointerException.class, () -> DuplicateRemovalVisitor.visit(BasicConfigurationNode.root(), null));
    }

}

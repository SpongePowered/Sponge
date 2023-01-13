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
package org.spongepowered.common.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.launch.SpongeExtension;
import org.spongepowered.plugin.PluginContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@ExtendWith(SpongeExtension.class)
public class ShouldFireTest {

    @BeforeEach
    public void resetShouldFireFields() throws IllegalAccessException {
        for (Field field : ShouldFire.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                field.set(null, false);
            }
        }
    }

    @Test
    public void testSpawn() {
        final EventManager eventManager = Sponge.eventManager();
        final PluginContainer plugin = Mockito.mock(PluginContainer.class);

        final SpawnListener listener = new SpawnListener();

        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT, "SPAWN_ENTITY_EVENT is not false!");
        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT_PRE, "SPAWN_ENTITY_EVENT_PRE is not false!");
        assertFalse(ShouldFire.DROP_ITEM_EVENT_DISPENSE, "DROP_ITEM_EVENT_DISPENSE is not false!");

        eventManager.registerListeners(plugin, listener);

        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT, "SPAWN_ENTITY_EVENT is not true!");
        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT_PRE, "SPAWN_ENTITY_EVENT_PRE is not true!");
        assertTrue(ShouldFire.DROP_ITEM_EVENT_DISPENSE, "DROP_ITEM_EVENT_DISPENSE it not true!");

        eventManager.unregisterListeners(listener);

        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT, "SPAWN_ENTITY_EVENT is not false!");
        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT_PRE, "SPAWN_ENTITY_EVENT_PRE is not false!");
        assertFalse(ShouldFire.DROP_ITEM_EVENT_DISPENSE, "DROP_ITEM_EVENT_DISPENSE is not false!");
    }

    @Test
    public void testMultipleListeners() {
        final EventManager eventManager = Sponge.eventManager();
        final PluginContainer plugin = Mockito.mock(PluginContainer.class);

        SpawnListener spawnBaseListener = new SpawnListener();
        SubListener spawnCustomListener = new SubListener();

        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT, "SPAWN_ENTITY_EVENT is not false!");
        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM, "SPAWN_ENTITY_EVENT_CUSTOM is not false!");
        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT_PRE, "SPAWN_ENTITY_EVENT_PRE is not false!");

        eventManager.registerListeners(plugin, spawnCustomListener);

        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT, "SPAWN_ENTITY_EVENT is not true!");
        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM, "SPAWN_ENTITY_EVENT_CUSTOM is not true!");
        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT_PRE, "SPAWN_ENTITY_EVENT_PRE is not false!");

        eventManager.registerListeners(plugin, spawnBaseListener);

        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT, "SPAWN_ENTITY_EVENT is not true!");
        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM, "SPAWN_ENTITY_EVENT_CUSTOM is not true!");
        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT_PRE, "SPAWN_ENTITY_EVENT_PRE is not true!");

        eventManager.unregisterListeners(spawnCustomListener);

        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT, "SPAWN_ENTITY_EVENT is not true!");
        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM, "SPAWN_ENTITY_EVENT_CUSTOM is not true!");
        assertTrue(ShouldFire.SPAWN_ENTITY_EVENT_PRE, "SPAWN_ENTITY_EVENT_PRE is not true!");

        eventManager.unregisterListeners(spawnBaseListener);

        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT, "SPAWN_ENTITY_EVENT is not false!");
        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM, "SPAWN_ENTITY_EVENT_CUSTOM is not false!");
        assertFalse(ShouldFire.SPAWN_ENTITY_EVENT_PRE, "SPAWN_ENTITY_EVENT_PRE is not false!");
    }

    private static class SpawnListener {

        @Listener
        public void onSpawn(SpawnEntityEvent event) {}
    }

    private static class SubListener {

        @Listener
        public void onCustom(SpawnEntityEvent.Custom event) {}
    }

}

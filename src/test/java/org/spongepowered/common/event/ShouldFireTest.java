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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.InjectedTest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

public class ShouldFireTest extends InjectedTest {

    private EventManager eventManager;
    private Object plugin;
    private PluginContainer container;

    @Before
    public void init() throws Exception {
        PluginManager manager = Mockito.mock(PluginManager.class);
        this.eventManager = new SpongeEventManager(this.logger, manager);

        this.plugin = new Object();
        this.container = Mockito.mock(PluginContainer.class);
        Mockito.when(manager.fromInstance(this.plugin)).thenReturn(Optional.of(this.container));

        this.resetStatics();
    }

    private void resetStatics() throws IllegalAccessException {
        for (Field field: ShouldFire.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                field.set(null, false);
            }
        }
    }

    @Test
    public void testSpawn() {
        SpawnListener listener = new SpawnListener();

        Assert.assertFalse("SPAWN_ENTITY_EVENT is not false!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNK_LOAD);
        Assert.assertFalse("DROP_ITEM_EVENT_DISPENSE is not false!", ShouldFire.DROP_ITEM_EVENT_DISPENSE);

        this.eventManager.registerListeners(this.plugin, listener);
        Assert.assertTrue("SPAWN_ENTITY_EVENT is not true!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertTrue("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not true!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNK_LOAD);
        Assert.assertTrue("DROP_ITEM_EVENT_DISPENSE it not true!", ShouldFire.DROP_ITEM_EVENT_DISPENSE);

        this.eventManager.unregisterListeners(listener);
        Assert.assertFalse("SPAWN_ENTITY_EVENT is not false!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNK_LOAD);
        Assert.assertFalse("DROP_ITEM_EVENT_DISPENSE is not false!", ShouldFire.DROP_ITEM_EVENT_DISPENSE);
    }

    @Test
    public void testMultipleListeners() {
        SpawnListener spawnBaseListener = new SpawnListener();
        SubListener spawnCustomListener = new SubListener();

        Assert.assertFalse("SPAWN_ENTITY_EVENT is not false!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CUSTOM is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNK_LOAD);

        this.eventManager.registerListeners(this.plugin, spawnCustomListener);

        Assert.assertTrue("SPAWN_ENTITY_EVENT is not true!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertTrue("SPAWN_ENTITY_EVENT_CUSTOM is not true!", ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNK_LOAD);

        this.eventManager.registerListeners(this.plugin, spawnBaseListener);

        Assert.assertTrue("SPAWN_ENTITY_EVENT is not true!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertTrue("SPAWN_ENTITY_EVENT_CUSTOM is not true!", ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM);
        Assert.assertTrue("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not true!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNK_LOAD);

        this.eventManager.unregisterListeners(spawnCustomListener);

        Assert.assertTrue("SPAWN_ENTITY_EVENT is not true!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertTrue("SPAWN_ENTITY_EVENT_CUSTOM is not true!", ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM);
        Assert.assertTrue("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not true!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNK_LOAD);

        this.eventManager.unregisterListeners(spawnBaseListener);

        Assert.assertFalse("SPAWN_ENTITY_EVENT is not false!", ShouldFire.SPAWN_ENTITY_EVENT);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CUSTOM is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CUSTOM);
        Assert.assertFalse("SPAWN_ENTITY_EVENT_CHUNK_LOAD is not false!", ShouldFire.SPAWN_ENTITY_EVENT_CHUNK_LOAD);
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

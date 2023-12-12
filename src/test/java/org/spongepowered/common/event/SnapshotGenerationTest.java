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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.event.listener.NonPreListener;
import org.spongepowered.common.event.listener.PreListener;
import org.spongepowered.common.launch.SpongeExtension;
import org.spongepowered.plugin.PluginContainer;

@ExtendWith(SpongeExtension.class)
public class SnapshotGenerationTest {
    private Entity entity;
    private SpawnEntityEvent event;

    @BeforeEach
    public void prepare() {
        this.entity = Mockito.mock(Entity.class, withSettings().defaultAnswer(Mockito.RETURNS_MOCKS));
        final Cause cause = Cause.of(EventContext.builder().add(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN.get()).build(), this);
        this.event = SpongeEventFactory.createSpawnEntityEvent(cause, Lists.newArrayList(this.entity));
    }

    @Test
    public void testPreListener() {
        final EventManager eventManager = Sponge.game().eventManager();
        final PluginContainer plugin = mock(PluginContainer.class);
        final Object[] listeners = {new PreListener(), new NonPreListener()};

        for (Object listener : listeners) {
            eventManager.registerListeners(plugin, listener);
        }

        eventManager.post(this.event);

        verify(this.entity).createSnapshot();
        this.event.entitySnapshots();

        // Cleanup registered listeners to not interfere with other tests
        for (Object listener : listeners) {
            eventManager.unregisterListeners(listener);
        }
    }

    @Test
    public void testNonPreListener() {
        final EventManager eventManager = Sponge.game().eventManager();
        eventManager.post(this.event);

        assertThrows(IllegalStateException.class, () -> this.event.entitySnapshots());
    }

}

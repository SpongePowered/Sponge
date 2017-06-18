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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import co.aikar.timings.Timings;
import co.aikar.timings.TimingsFactory;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.InjectedTest;
import org.spongepowered.common.event.listener.NonPreListener;
import org.spongepowered.lwts.runner.LaunchWrapperTestRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

@RunWith(LaunchWrapperTestRunner.class)
public class InternalStateGenerationTest extends InjectedTest {

    private Entity entity;
    private SpawnEntityEvent event;
    private EventManager eventManager;
    private Object plugin;

    @Before
    public void init() {
        PluginManager manager = Mockito.mock(PluginManager.class);
        this.eventManager = new SpongeEventManager(this.logger, manager);

        try {
            Field field = Timings.class.getDeclaredField("factory");
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, Mockito.mock(TimingsFactory.class));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        this.plugin = new Object();
        PluginContainer container = Mockito.mock(PluginContainer.class);
        Mockito.when(manager.fromInstance(this.plugin)).thenReturn(Optional.of(container));

        Cause cause = Cause.of(EventContext.empty(), this);
        this.entity = Mockito.mock(Entity.class, withSettings().defaultAnswer(Mockito.RETURNS_MOCKS));

        this.event = SpongeEventFactory.createSpawnEntityEvent(cause, Lists.newArrayList(this.entity));

        Game game = mock(Game.class);
        CauseStackManager csm = mock(CauseStackManager.class);
        Mockito.when(game.getCauseStackManager()).thenReturn(csm);
    }

    @Test
    public void testPreListener() {
        this.eventManager.registerListeners(this.plugin, new PreListener());
        this.eventManager.registerListeners(this.plugin, new NonPreListener());
        this.eventManager.post(this.event);

        verify(this.entity).createSnapshot();
        this.event.getEntitySnapshots();
    }

    @Test(expected = IllegalStateException.class)
    public void testNonPreListener() {
        this.eventManager.post(this.event);
        this.event.getEntitySnapshots();
    }

}

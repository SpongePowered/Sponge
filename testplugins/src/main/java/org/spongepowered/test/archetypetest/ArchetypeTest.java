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
package org.spongepowered.test.archetypetest;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

// TODO other Archetypes
@Plugin("archetypetest")
public class ArchetypeTest implements LoadableModule {

    final PluginContainer plugin;

    @Inject
    public ArchetypeTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Command.builder()
                .child(Command.builder().setExecutor(this::testEntityArchetype).build(), "entity")
                .build(), "testarchetypes");
    }

    private CommandResult testEntityArchetype(CommandContext context) {
        context.getCause().first(ServerPlayer.class).ifPresent(p -> {
            final Sheep entity = p.getWorld().createEntity(EntityTypes.SHEEP, p.getPosition());
            this.testEntityArchetype(entity);
        });
        return CommandResult.success();
    }

    @Override
    public void enable(CommandContext ctx) {
        Sponge.getEventManager().registerListeners(this.plugin, new ArchetypeTestListener());
    }


    public class ArchetypeTestListener {
        // TODO we're missing InteractEntityEvent implementation so we cannot test with this yet
        @Listener
        public void onRightClickEntity(InteractEntityEvent.Secondary event) {
            final Entity entity = event.getEntity();
            testEntityArchetype(entity);
        }
    }


    public void testEntityArchetype(Entity entity) {
        Sponge.getServer().getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN); // Need that for restoring

        final EntityArchetype archetype = entity.createArchetype();
        // Restore same entity from archetype
        archetype.apply(entity.getServerLocation());
        // Test restoring from serialized archetype
        final EntityArchetype rebuiltArchetype = EntityArchetype.builder().build(archetype.toContainer()).get();
        rebuiltArchetype.apply(entity.getServerLocation());

        final EntitySnapshot rebuiltSnapshot = rebuiltArchetype.toSnapshot(entity.getServerLocation());
        rebuiltSnapshot.restore();
    }
}

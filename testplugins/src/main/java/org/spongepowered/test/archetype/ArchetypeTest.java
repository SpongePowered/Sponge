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
package org.spongepowered.test.archetype;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Key;
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
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

// TODO other Archetypes
@SuppressWarnings({"unchecked", "rawtypes"})
@Plugin("archetypetest")
public final class ArchetypeTest implements LoadableModule {

    final PluginContainer plugin;

    @Inject
    public ArchetypeTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    private void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.plugin, Command.builder()
                .addChild(Command.builder().executor(this::testEntityArchetype).build(), "entity")
                .build(), "testarchetypes");

        event.register(this.plugin, Command.builder()
                .addChild(this.queryBlockArchetype(), "block")
                .build(), "queryArchetype");
    }

    private CommandResult testEntityArchetype(CommandContext context) {
        context.cause().first(ServerPlayer.class).ifPresent(p -> {
            final Sheep entity = p.world().createEntity(EntityTypes.SHEEP, p.position());
            this.testEntityArchetype(entity);
        });
        return CommandResult.success();
    }

    private Command.Parameterized queryBlockArchetype() {
        final Parameter.Value<ServerLocation> serverLocationParameter = Parameter.location().key("location").build();
        final Parameter.Value<ResourceKey> resourceKeyValue = Parameter.resourceKey().key("resourceKey").build();

        return Command.builder()
                .addParameter(serverLocationParameter)
                .addParameter(resourceKeyValue)
                .executor(context -> {
                    final ServerLocation location = context.requireOne(serverLocationParameter);
                    final ResourceKey resourceKey = context.requireOne(resourceKeyValue);
                    location.createSnapshot().createArchetype().ifPresentOrElse(a ->
                            a.getKeys()
                                    .stream()
                                    .filter(k -> k.key().equals(resourceKey))
                                    .findFirst()
                                    .ifPresentOrElse(k -> context.sendMessage(Component.text("Value: " +  a.get((Key) k).orElse(null))),
                                            () -> context.sendMessage(Component.text("Not valid key"))),
                            () -> context.sendMessage(Component.text("No valid archetype could be created")));
                    return CommandResult.success();
                })
                .build();
    }

    @Override
    public void enable(CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.plugin, new ArchetypeTestListener());
    }


    class ArchetypeTestListener {
        // TODO we're missing InteractEntityEvent implementation so we cannot test with this yet
        @Listener
        private void onRightClickEntity(InteractEntityEvent.Secondary event) {
            final Entity entity = event.entity();
            ArchetypeTest.this.testEntityArchetype(entity);
        }
    }


    public void testEntityArchetype(Entity entity) {
        Sponge.server().causeStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN); // Need that for restoring

        final EntityArchetype archetype = entity.createArchetype();
        // Restore same entity from archetype
        archetype.apply(entity.serverLocation());
        // Test restoring from serialized archetype
        final EntityArchetype rebuiltArchetype = EntityArchetype.builder().build(archetype.toContainer()).get();
        rebuiltArchetype.apply(entity.serverLocation());

        final EntitySnapshot rebuiltSnapshot = rebuiltArchetype.toSnapshot(entity.serverLocation());
        rebuiltSnapshot.restore();
    }
}

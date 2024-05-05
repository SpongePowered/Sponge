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
package org.spongepowered.test.blockstate;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.BlockStateKeys;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.state.BooleanStateProperties;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Map;

@Plugin("blockstate_test")
public class BlockStateTest implements LoadableModule {

    private final PluginContainer plugin;

    @Inject
    public BlockStateTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(final CommandContext ctx) {
        final ItemStack stick = ItemStack.of(ItemTypes.STICK);
        stick.offer(Keys.CUSTOM_NAME, Component.text("State Stick"));
        ctx.cause().first(ServerPlayer.class).ifPresent(player -> player.inventory().offer(stick));
        Sponge.eventManager().registerListeners(this.plugin, new BlockStateTestListener());
    }

    public static class BlockStateTestListener {

        @Listener
        public void onSpawnEntity(final InteractBlockEvent.Secondary event, @First final ServerPlayer player) {
            if (event.context().get(EventContextKeys.USED_HAND).map(hand -> hand == HandTypes.MAIN_HAND.get()).orElse(false) &&
                event.context().get(EventContextKeys.USED_ITEM).flatMap(item -> item.get(Keys.CUSTOM_NAME)).map(t -> t.equals(Component.text("State Stick"))).orElse(false)
            ) {
                final BlockState state = event.block().state();
                player.sendMessage(Component.text("Interacted block is " + state.asString() + " and has the following properties:").color(NamedTextColor.GREEN));
                final ServerLocation loc = event.block().location().get();
                for (final Value.Immutable<?> value : state.getValues()) {
                    if (value.key().key().value().startsWith("property/")) {
                        player.sendMessage(Component.text(value.key().key().toString()).append(Component.text(": ")).append(Component.text(value.get().toString())));
                    }
                    if (value.key().equals(BlockStateKeys.NORTH_WALL)) {
                        final BlockState newState = BlockTypes.BLACKSTONE_WALL.get().defaultState().mergeWith(state);
                        loc.setBlock(newState);
                    }
                }
                state.statePropertyMap().forEach((prop, value) -> player.sendMessage(Component.text(prop.name() + ": " + value.toString())));
                for (Map.Entry<StateProperty<?>, ?> entry : state.statePropertyMap().entrySet()) {
                    if (entry.getKey().equals(BooleanStateProperties.property_SNOWY())) {
                        player.sendMessage(Component.text(entry.getKey().name()));
                        loc.setBlock(state.withStateProperty(BooleanStateProperties.property_SNOWY(), !(Boolean) entry.getValue()).get());
                    }
                }
            }
        }

    }
}

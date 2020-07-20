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
package org.spongepowered.common.registry.builtin.sponge;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.common.event.SpongeEventContextKey;

import java.util.stream.Stream;

public final class EventContextKeyStreamGenerator {

    public static Stream<EventContextKey<?>> stream() {
        // @formatter:off
        return Stream.of(
            new SpongeEventContextKey<>(ResourceKey.sponge("audience"), TypeTokens.AUDIENCE),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_event_process"), TypeTokens.LOCATABLE_BLOCK_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_event_queue"), TypeTokens.LOCATABLE_BLOCK_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_hit"), TypeTokens.BLOCK_SNAPSHOT_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_target"), TypeTokens.BLOCK_SNAPSHOT_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("break_event"), TypeTokens.CHANGE_BLOCK_EVENT_BREAK_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("command"), TypeTokens.STRING_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("creator"), TypeTokens.USER_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("damage_type"), TypeTokens.DAMAGE_TYPE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("decay_event"), TypeTokens.CHANGE_BLOCK_EVENT_DECAY_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("dismount_type"), TypeTokens.DISMOUNT_TYPE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("entity_hit"), TypeTokens.ENTITY_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("fake_player"), TypeTokens.PLAYER_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("fire_spread"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("growth_origin"), TypeTokens.BLOCK_SNAPSHOT_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("grow_event"), TypeTokens.CHANGE_BLOCK_EVENT_GROW_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("igniter"), TypeTokens.LIVING_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("last_damage_source"), TypeTokens.DAMAGE_SOURCE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("leaves_decay"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_break"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_flow"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_mix"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("location"), TypeTokens.SERVER_LOCATION_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("modify_event"), TypeTokens.CHANGE_BLOCK_EVENT_MODIFY_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("neighbor_notify_source"), TypeTokens.BLOCK_SNAPSHOT_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("notifier"), TypeTokens.USER_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("piston_extend"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("piston_retract"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("place_event"), TypeTokens.CHANGE_BLOCK_EVENT_PLACE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("player"), TypeTokens.PLAYER_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("player_break"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("player_place"), TypeTokens.SERVER_WORLD_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("plugin"), TypeTokens.PLUGIN_CONTAINER_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("projectile_source"), TypeTokens.PROJECTILE_SOURCE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("rotation"), TypeTokens.VECTOR_3D_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("simulated_player"), TypeTokens.GAME_PROFILE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("spawn_type"), TypeTokens.SPAWN_TYPE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("subject"), TypeTokens.SUBJECT_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("teleport_type"), TypeTokens.TELEPORT_TYPE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("used_hand"), TypeTokens.HAND_TYPE_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("used_item"), TypeTokens.ITEM_STACK_SNAPSHOT_TOKEN),
            new SpongeEventContextKey<>(ResourceKey.sponge("weapon"), TypeTokens.ITEM_STACK_SNAPSHOT_TOKEN)
        );
        // @formatter:on
    }
}

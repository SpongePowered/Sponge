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

import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.DismountType;
import org.spongepowered.api.event.cause.entity.MovementType;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.event.SpongeEventContextKey;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;

import java.util.stream.Stream;

public final class EventContextKeyStreamGenerator {

    public static Stream<EventContextKey<?>> stream() {
        // @formatter:off
        return Stream.of(
            new SpongeEventContextKey<>(ResourceKey.sponge("audience"), Audience.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_event_process"), LocatableBlock.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_event_queue"), LocatableBlock.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_hit"), BlockSnapshot.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("block_target"), BlockSnapshot.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("break_event"), ChangeBlockEvent.Break.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("command"), String.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("creator"), User.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("damage_type"), DamageType.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("decay_event"), ChangeBlockEvent.Decay.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("dismount_type"), DismountType.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("entity_hit"), Entity.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("fake_player"), Player.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("fire_spread"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("growth_origin"), BlockSnapshot.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("grow_event"), ChangeBlockEvent.Grow.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("igniter"), Living.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("last_damage_source"), DamageSource.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("leaves_decay"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_break"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_flow"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("liquid_mix"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("location"), ServerLocation.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("modify_event"), ChangeBlockEvent.Modify.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("movement_type"), MovementType.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("neighbor_notify_source"), BlockSnapshot.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("notifier"), User.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("piston_extend"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("piston_retract"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("place_event"), ChangeBlockEvent.Place.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("player"), Player.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("player_break"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("player_place"), ServerWorld.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("plugin"), PluginContainer.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("projectile_source"), ProjectileSource.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("rotation"), Vector3d.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("simulated_player"), GameProfile.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("spawn_type"), SpawnType.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("subject"), Subject.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("used_hand"), HandType.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("used_item"), ItemStackSnapshot.class),
            new SpongeEventContextKey<>(ResourceKey.sponge("weapon"), ItemStackSnapshot.class)
        );
        // @formatter:on
    }
}

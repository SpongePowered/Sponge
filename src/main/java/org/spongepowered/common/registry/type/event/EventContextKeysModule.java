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
package org.spongepowered.common.registry.type.event;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.SpongeEventContextKey;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.Locale;

@SuppressWarnings("rawtypes")
@RegisterCatalog(EventContextKeys.class)
public final class EventContextKeysModule
    extends AbstractPrefixAlternateCatalogTypeRegistryModule<EventContextKey>
    implements AdditionalCatalogRegistryModule<EventContextKey> {

    private static final EventContextKeysModule INSTANCE = new EventContextKeysModule();

    public static EventContextKeysModule getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerAdditionalCatalog(EventContextKey extraCatalog) {
        final String id = checkNotNull(extraCatalog).getId();
        final String key = id.toLowerCase(Locale.ENGLISH);
        checkArgument(!key.contains("sponge:"), "Cannot register spoofed event context key!");
        checkArgument(!key.contains("minecraft:"), "Cannot register spoofed event context key!");
        checkArgument(!this.catalogTypeMap.containsKey(key), "Cannot register an already registered EventContextKey: %s", key);
        this.catalogTypeMap.put(key, extraCatalog);

    }

    @Override
    public void registerDefaults() {
        this.createKey("sponge:block_event_queue", "Block Event Queue", LocatableBlock.class);
        this.createKey("sponge:block_event_process", "Block Event Process", LocatableBlock.class);
        this.createKey("sponge:creator", "Creator", User.class);
        this.createKey("sponge:damage_type", "Damage Type", DamageType.class);
        this.createKey("sponge:dismount_type", "Dimension Type", DismountType.class);
        this.createKey("sponge:igniter", "Igniter", User.class);
        this.createKey("sponge:last_damage_source", "Last Damage Source", DamageSource.class);
        this.createKey("sponge:liquid_break", "Liquid Break", World.class);
        this.createKey("sponge:liquid_flow", "Liquid Flow", World.class);
        this.createKey("sponge:liquid_mix", "Liquid Mix", World.class);
        this.createKey("sponge:neighbor_notify_source", "Neighbor Notify Source", BlockSnapshot.class);
        this.createKey("sponge:notifier", "Notifier", User.class);
        this.createKey("sponge:owner", "Owner", User.class);
        this.createKey("sponge:player", "Player", Player.class);
        this.createKey("sponge:player_simulated", "Game Profile", GameProfile.class);
        this.createKey("sponge:projectile_source", "Projectile Source", ProjectileSource.class);
        this.createKey("sponge:service_manager", "Service Manager", ServiceManager.class);
        this.createKey("sponge:spawn_type", "Spawn Type", SpawnType.class);
        this.createKey("sponge:teleport_type", "Teleport Type", TeleportType.class);
        this.createKey("sponge:thrower", "Thrower", User.class);
        this.createKey("sponge:weapon", "Weapon", ItemStackSnapshot.class);
        this.createKey("sponge:fake_player", "Fake Player", Player.class);
        this.createKey("sponge:player_break", "Player Break", World.class);
        this.createKey("sponge:player_place", "Player Place", World.class);
        this.createKey("sponge:fire_spread", "Fire Spread", World.class);
        this.createKey("sponge:leaves_decay", "Leaves Decay", World.class);
        this.createKey("sponge:piston_retract", "Piston Retract", World.class);
        this.createKey("sponge:piston_extend", "Piston Extend", World.class);
        this.createKey("sponge:block_hit", "Block Hit", BlockSnapshot.class);
        this.createKey("sponge:entity_hit", "Entity Hit", BlockSnapshot.class);
        this.createKey("sponge:used_item", "Used Item", ItemStackSnapshot.class);
        this.createKey("sponge:used_hand", "Used Hand", HandType.class);
        this.createKey("sponge:plugin", "Plugin", PluginContainer.class);
        this.createKey("sponge:break_event", "Break Event", ChangeBlockEvent.Break.class);
        this.createKey("sponge:place_event", "Place Event", ChangeBlockEvent.Place.class);
        this.createKey("sponge:modify_event", "Modify Event", ChangeBlockEvent.Modify.class);
        this.createKey("sponge:decay_event", "Decay Event", ChangeBlockEvent.Decay.class);
        this.createKey("sponge:grow_event", "Decay Event", ChangeBlockEvent.Grow.class);
        this.createKey("sponge:growth_origin", "Growth Origin", BlockSnapshot.class);
    }

    private void createKey(String id, String name, Class<?> usedClass) {
        this.catalogTypeMap.put(id, new SpongeEventContextKey<>(id, name, usedClass));
    }

    private EventContextKeysModule() {
        super("sponge");
    }
}

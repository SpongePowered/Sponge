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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.SpongeEventContextKey;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.Locale;
import java.util.UUID;

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
        createKey("sponge:creator", "Creator", UUID.class);
        createKey("sponge:damage_type", "Damage Type", DamageType.class);
        createKey("sponge:dismount_type", "Dimension Type", DismountType.class);
        createKey("sponge:igniter", "Igniter", User.class);
        createKey("sponge:last_damage_source", "Last Damage Source", DamageSource.class);
        createKey("sponge:liquid_mix", "Liquid Mix", World.class);
        createKey("sponge:notifier", "Notifier", User.class);
        createKey("sponge:owner", "Owner", User.class);
        createKey("sponge:player", "Player", Player.class);
        createKey("sponge:player_simulated", "Game Profile", GameProfile.class);
        createKey("sponge:projectile_source", "Projectile Source", ProjectileSource.class);
        createKey("sponge:service_manager", "Service Manager", ServiceManager.class);
        createKey("sponge:spawn_type", "Spawn Type", SpawnType.class);
        createKey("sponge:teleport_type", "Teleport Type", TeleportType.class);
        createKey("sponge:thrower", "Thrower", User.class);
        createKey("sponge:weapon", "Weapon", ItemStackSnapshot.class);
        createKey("sponge:fake_player", "Fake Player", Player.class);
        createKey("sponge:player_break", "Player Break", World.class);
        createKey("sponge:player_place", "Player Place", World.class);
        createKey("sponge:fire_spread", "Fire Spread", World.class);
        createKey("sponge:leaves_decay", "Leaves Decay", World.class);
        createKey("sponge:piston_retract", "Piston Retract", World.class);
        createKey("sponge:piston_extend", "Piston Extend", World.class);
        createKey("sponge:block_hit", "Block Hit", BlockSnapshot.class);
        createKey("sponge:entity_hit", "Entity Hit", BlockSnapshot.class);
    }

    private void createKey(String id, String name, Class<?> usedClass) {
        this.catalogTypeMap.put(id, new SpongeEventContextKey<>(id, name, usedClass));
    }

    private EventContextKeysModule() {
        super("sponge");
    }
}

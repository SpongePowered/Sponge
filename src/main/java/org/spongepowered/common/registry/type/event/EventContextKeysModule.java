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

import com.google.common.collect.ImmutableList;
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
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.common.event.SpongeEventContextKey;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixCheckCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
        this.catalogTypeMap.put("sponge:creator", new SpongeEventContextKey<>("sponge:creator", "Creator", UUID.class));
        this.catalogTypeMap.put("sponge:damage_type", new SpongeEventContextKey<>("sponge:damage_type", "Damage Type", DamageType.class));
        this.catalogTypeMap.put("sponge:dismount_type", new SpongeEventContextKey<>("sponge:dismount_type", "Dimension Type", DismountType.class));
        this.catalogTypeMap.put("sponge:igniter", new SpongeEventContextKey<>("sponge:igniter", "Igniter", User.class));
        this.catalogTypeMap.put("sponge:last_damage_source", new SpongeEventContextKey<>("sponge:last_damage_source", "Last Damage Source", DamageSource.class));
        this.catalogTypeMap.put("sponge:notifier", new SpongeEventContextKey<>("sponge:notifier", "Notifier", User.class));
        this.catalogTypeMap.put("sponge:owner", new SpongeEventContextKey<>("sponge:owner", "Owner", User.class));
        this.catalogTypeMap.put("sponge:player", new SpongeEventContextKey<>("sponge:player", "Player", Player.class));
        this.catalogTypeMap.put("sponge:player_simulated", new SpongeEventContextKey<>("sponge:player_simulated", "Game Profile", GameProfile.class));
        this.catalogTypeMap.put("sponge:projectile_source", new SpongeEventContextKey<>("sponge:projectile_source", "Projectile Source", ProjectileSource.class));
        this.catalogTypeMap.put("sponge:service_manager", new SpongeEventContextKey<>("sponge:service_manager", "Service Manager", ServiceManager.class));
        this.catalogTypeMap.put("sponge:spawn_type", new SpongeEventContextKey<>("sponge:spawn_type", "Spawn Type", SpawnType.class));
        this.catalogTypeMap.put("sponge:teleport_type", new SpongeEventContextKey<>("sponge:teleport_type", "Teleport Type", TeleportType.class));
        this.catalogTypeMap.put("sponge:thrower", new SpongeEventContextKey<>("sponge:thrower", "Thrower", User.class));
        this.catalogTypeMap.put("sponge:weapon", new SpongeEventContextKey<>("sponge:weapon", "Weapon", ItemStackSnapshot.class));
    }

    private EventContextKeysModule() {
        super("sponge");
    }
}

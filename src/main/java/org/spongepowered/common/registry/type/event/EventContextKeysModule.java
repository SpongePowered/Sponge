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
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.common.event.SpongeEventContextKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("rawtypes")
public final class EventContextKeysModule implements AlternateCatalogRegistryModule<EventContextKey>, AdditionalCatalogRegistryModule<EventContextKey> {

    private static final EventContextKeysModule INSTANCE = new EventContextKeysModule();

    public static EventContextKeysModule getInstance() {
        return INSTANCE;
    }

    @RegisterCatalog(EventContextKeys.class)
    private final Map<String, EventContextKey> keyMappings = new HashMap<>();

    @Override
    public Optional<EventContextKey> getById(String id) {
        return Optional.ofNullable(this.keyMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<EventContextKey> getAll() {
        return ImmutableList.copyOf(this.keyMappings.values());
    }

    @Override
    public void registerAdditionalCatalog(EventContextKey extraCatalog) {
        final String id = checkNotNull(extraCatalog).getId();
        final String key = id.toLowerCase(Locale.ENGLISH);
        checkArgument(!key.contains("sponge:"), "Cannot register spoofed teleport type!");
        checkArgument(!key.contains("minecraft:"), "Cannot register spoofed teleport type!");
        checkArgument(!this.keyMappings.containsKey(key), "Cannot register an already registered TeleportType: %s", key);
        this.keyMappings.put(key, extraCatalog);

    }

    @Override
    public void registerDefaults() {
        this.keyMappings.put("sponge:creator", new SpongeEventContextKey<>("sponge:creator", UUID.class));
        this.keyMappings.put("sponge:damage_type", new SpongeEventContextKey<>("sponge:damage_type", DamageType.class));
        this.keyMappings.put("sponge:igniter", new SpongeEventContextKey<>("sponge:igniter", User.class));
        this.keyMappings.put("sponge:last_damage_source", new SpongeEventContextKey<>("sponge:last_damage_source", DamageSource.class));
        this.keyMappings.put("sponge:notifier", new SpongeEventContextKey<>("sponge:notifier", User.class));
        this.keyMappings.put("sponge:owner", new SpongeEventContextKey<>("sponge:owner", User.class));
        this.keyMappings.put("sponge:player", new SpongeEventContextKey<>("sponge:player", Player.class));
        this.keyMappings.put("sponge:player_simulated", new SpongeEventContextKey<>("sponge:player_simulated", GameProfile.class));
        this.keyMappings.put("sponge:projectile_source", new SpongeEventContextKey<>("sponge:projectile_source", ProjectileSource.class));
        this.keyMappings.put("sponge:service_manager", new SpongeEventContextKey<>("sponge:service_manager", ServiceManager.class));
        this.keyMappings.put("sponge:spawn_type", new SpongeEventContextKey<>("sponge:spawn_type", SpawnType.class));
        this.keyMappings.put("sponge:teleport_type", new SpongeEventContextKey<>("sponge:teleport_type", TeleportType.class));
        this.keyMappings.put("sponge:thrower", new SpongeEventContextKey<>("sponge:thrower", User.class));
        this.keyMappings.put("sponge:weapon", new SpongeEventContextKey<>("sponge:weapon", ItemStackSnapshot.class));
    }

    @Override
    public Map<String, EventContextKey> provideCatalogMap() {
        final HashMap<String, EventContextKey> map = new HashMap<>();
        for (Map.Entry<String, EventContextKey> entry : this.keyMappings.entrySet()) {
            map.put(entry.getKey().replace("minecraft:", "").replace("sponge:", ""), entry.getValue());
        }
        return map;
    }

    EventContextKeysModule() {
    }
}

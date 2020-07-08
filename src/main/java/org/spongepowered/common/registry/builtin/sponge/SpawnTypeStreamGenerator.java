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
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.common.data.type.SpongeSpawnType;

import java.util.stream.Stream;

public final class SpawnTypeStreamGenerator {

    public static final SpongeSpawnType ENTITY_DEATH = new SpongeSpawnType(ResourceKey.sponge("entity_death"));
    public static final SpawnType FORCED = new SpongeSpawnType(ResourceKey.sponge("forced")).forced();

    public static Stream<SpawnType> stream() {
        return Stream.of(
            new SpongeSpawnType(ResourceKey.sponge("dispense")),
            new SpongeSpawnType(ResourceKey.sponge("block_spawning")),
            new SpongeSpawnType(ResourceKey.sponge("breeding")),
            new SpongeSpawnType(ResourceKey.sponge("dropped_item")),
            new SpongeSpawnType(ResourceKey.sponge("experience")),
            new SpongeSpawnType(ResourceKey.sponge("falling_block")),
            new SpongeSpawnType(ResourceKey.sponge("mob_spawner")),
            new SpongeSpawnType(ResourceKey.sponge("passive")),
            new SpongeSpawnType(ResourceKey.sponge("placement")),
            new SpongeSpawnType(ResourceKey.sponge("projectile")),
            new SpongeSpawnType(ResourceKey.sponge("spawn_egg")),
            new SpongeSpawnType(ResourceKey.sponge("structure")),
            new SpongeSpawnType(ResourceKey.sponge("tnt_ignite")),
            new SpongeSpawnType(ResourceKey.sponge("weather")),
            new SpongeSpawnType(ResourceKey.sponge("custom")),
            new SpongeSpawnType(ResourceKey.sponge("chunk_load")),
            new SpongeSpawnType(ResourceKey.sponge("world_spawner")),
            new SpongeSpawnType(ResourceKey.sponge("plugin")),
            ENTITY_DEATH,
            FORCED
        );
    }
}

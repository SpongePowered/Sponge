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

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.common.data.type.SpongeSpawnType;

import java.util.stream.Stream;

public final class SpawnTypeStreamGenerator {

    public static SpawnType FORCED = new SpongeSpawnType(CatalogKey.sponge("forced")).forced();

    public static Stream<SpawnType> stream() {
        return Stream.of(
            new SpongeSpawnType(CatalogKey.sponge("dispense")),
            new SpongeSpawnType(CatalogKey.sponge("block_spawning")),
            new SpongeSpawnType(CatalogKey.sponge("breeding")),
            new SpongeSpawnType(CatalogKey.sponge("dropped_item")),
            new SpongeSpawnType(CatalogKey.sponge("experience")),
            new SpongeSpawnType(CatalogKey.sponge("falling_block")),
            new SpongeSpawnType(CatalogKey.sponge("mob_spawner")),
            new SpongeSpawnType(CatalogKey.sponge("passive")),
            new SpongeSpawnType(CatalogKey.sponge("placement")),
            new SpongeSpawnType(CatalogKey.sponge("projectile")),
            new SpongeSpawnType(CatalogKey.sponge("spawn_egg")),
            new SpongeSpawnType(CatalogKey.sponge("structure")),
            new SpongeSpawnType(CatalogKey.sponge("tnt_ignite")),
            new SpongeSpawnType(CatalogKey.sponge("weather")),
            new SpongeSpawnType(CatalogKey.sponge("custom")),
            new SpongeSpawnType(CatalogKey.sponge("chunk_load")),
            new SpongeSpawnType(CatalogKey.sponge("world_spawner")),
            new SpongeSpawnType(CatalogKey.sponge("plugin")),
            new SpongeSpawnType(CatalogKey.sponge("entity_death")),
            FORCED
        );
    }
}

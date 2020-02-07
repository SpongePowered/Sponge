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
package org.spongepowered.common.registry.builtin.supplier;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.common.data.type.SpongeSpawnType;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class SpawnTypeSupplier {

    public static SpawnType FORCED = new SpongeSpawnType(CatalogKey.sponge("forced"), "Forced").forced();

    public static Stream<SpawnType> stream() {
        return Stream.of(
                new SpongeSpawnType(CatalogKey.sponge("dispense"), "Dispense"),
                new SpongeSpawnType(CatalogKey.sponge("dispense"), "Dispense"),
                new SpongeSpawnType(CatalogKey.sponge("block_spawning"), "BlockSpawning"),
                new SpongeSpawnType(CatalogKey.sponge("breeding"), "Breeding"),
                new SpongeSpawnType(CatalogKey.sponge("dropped_item"), "DroppedItem"),
                new SpongeSpawnType(CatalogKey.sponge("experience"), "Experience"),
                new SpongeSpawnType(CatalogKey.sponge("falling_block"), "FallingBlock"),
                new SpongeSpawnType(CatalogKey.sponge("mob_spawner"), "MobSpawner"),
                new SpongeSpawnType(CatalogKey.sponge("passive"), "Passive"),
                new SpongeSpawnType(CatalogKey.sponge("placement"), "Placement"),
                new SpongeSpawnType(CatalogKey.sponge("projectile"), "Projectile"),
                new SpongeSpawnType(CatalogKey.sponge("spawn_egg"), "SpawnEgg"),
                new SpongeSpawnType(CatalogKey.sponge("structure"), "Structure"),
                new SpongeSpawnType(CatalogKey.sponge("tnt_ignite"), "TNTIgnite"),
                new SpongeSpawnType(CatalogKey.sponge("weather"), "Weather"),
                new SpongeSpawnType(CatalogKey.sponge("custom"), "Custom"),
                new SpongeSpawnType(CatalogKey.sponge("chunk_load"), "ChunkLoad"),
                new SpongeSpawnType(CatalogKey.sponge("world_spawner"), "WorldSpawner"),
                new SpongeSpawnType(CatalogKey.sponge("plugin"), "Plugin"),
                FORCED,
                new SpongeSpawnType(CatalogKey.sponge("entity_death"), "EntityDeath")
        );
    }
}

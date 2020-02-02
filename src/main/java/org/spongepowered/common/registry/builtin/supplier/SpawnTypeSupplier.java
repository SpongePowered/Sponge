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

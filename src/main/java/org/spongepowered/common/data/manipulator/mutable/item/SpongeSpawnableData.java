package org.spongepowered.common.data.manipulator.mutable.item;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableSpawnableData;
import org.spongepowered.api.data.manipulator.mutable.item.SpawnableData;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeSpawnableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleCatalogData;

public class SpongeSpawnableData extends AbstractSingleCatalogData<EntityType, SpawnableData, ImmutableSpawnableData> implements SpawnableData {

    public SpongeSpawnableData(EntityType type) {
        super(SpawnableData.class, type, Keys.SPAWNABLE_ENTITY_TYPE, ImmutableSpongeSpawnableData.class);
    }

    public SpongeSpawnableData() {
        this(EntityTypes.CREEPER);
    }
}

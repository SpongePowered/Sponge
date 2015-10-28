package org.spongepowered.common.data.manipulator.immutable.item;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableSpawnableData;
import org.spongepowered.api.data.manipulator.mutable.item.SpawnableData;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeSpawnableData;

public class ImmutableSpongeSpawnableData extends AbstractImmutableSingleCatalogData<EntityType, ImmutableSpawnableData, SpawnableData>
        implements ImmutableSpawnableData {

    public ImmutableSpongeSpawnableData(EntityType type) {
        super(ImmutableSpawnableData.class, type, EntityTypes.CREEPER, Keys.SPAWNABLE_ENTITY_TYPE, SpongeSpawnableData.class);
    }

    public ImmutableSpongeSpawnableData() {
        this(EntityTypes.CREEPER);
    }
}

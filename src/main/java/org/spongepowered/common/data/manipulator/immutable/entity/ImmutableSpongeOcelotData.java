package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableOcelotData;
import org.spongepowered.api.data.manipulator.mutable.entity.OcelotData;
import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.data.type.OcelotTypes;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeOcelotData;

public class ImmutableSpongeOcelotData extends AbstractImmutableSingleCatalogData<OcelotType, ImmutableOcelotData, OcelotData> implements ImmutableOcelotData {

    public ImmutableSpongeOcelotData(OcelotType type) {
        super(ImmutableOcelotData.class, type, OcelotTypes.WILD_OCELOT, Keys.OCELOT_TYPE, SpongeOcelotData.class);
    }

}

package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableOcelotData;
import org.spongepowered.api.data.manipulator.mutable.entity.OcelotData;
import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.data.type.OcelotTypes;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleCatalogData;

public class SpongeOcelotData extends AbstractSingleCatalogData<OcelotType, OcelotData, ImmutableOcelotData> implements OcelotData {

    public SpongeOcelotData(OcelotType value) {
        super(OcelotData.class, value, Keys.OCELOT_TYPE, null);
    }

    public SpongeOcelotData() {
        this(OcelotTypes.WILD_OCELOT);
    }

}

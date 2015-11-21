package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkeletonData;
import org.spongepowered.api.data.manipulator.mutable.entity.SkeletonData;
import org.spongepowered.api.data.type.SkeletonType;
import org.spongepowered.api.data.type.SkeletonTypes;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSkeletonData;

public class ImmutableSpongeSkeletonData extends AbstractImmutableSingleCatalogData<SkeletonType, ImmutableSkeletonData, SkeletonData> implements ImmutableSkeletonData {

    public ImmutableSpongeSkeletonData(SkeletonType type) {
        super(ImmutableSkeletonData.class, type, SkeletonTypes.NORMAL, Keys.SKELETON_TYPE, SpongeSkeletonData.class);
    }

}

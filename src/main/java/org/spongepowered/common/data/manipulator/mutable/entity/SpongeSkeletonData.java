package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkeletonData;
import org.spongepowered.api.data.manipulator.mutable.entity.SkeletonData;
import org.spongepowered.api.data.type.SkeletonType;
import org.spongepowered.api.data.type.SkeletonTypes;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleCatalogData;

public class SpongeSkeletonData extends AbstractSingleCatalogData<SkeletonType, SkeletonData, ImmutableSkeletonData> implements SkeletonData {

    public SpongeSkeletonData(SkeletonType value) {
        super(SkeletonData.class, value, Keys.SKELETON_TYPE, null);
    }

    public SpongeSkeletonData() {
        this(SkeletonTypes.NORMAL);
    }

}

package org.spongepowered.common.data.manipulator.mutable.item;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.GoldenApples;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeGoldenAppleData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleCatalogData;

public class SpongeGoldenAppleData extends AbstractSingleCatalogData<GoldenApple, GoldenAppleData, ImmutableGoldenAppleData> implements GoldenAppleData {

    public SpongeGoldenAppleData() {
        this(GoldenApples.GOLDEN_APPLE);
    }

    public SpongeGoldenAppleData(GoldenApple value) {
        super(GoldenAppleData.class, value, Keys.GOLDEN_APPLE_TYPE, ImmutableSpongeGoldenAppleData.class);
    }
}

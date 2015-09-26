package org.spongepowered.common.data.manipulator.immutable.item;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.GoldenApples;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGoldenAppleData;

public class ImmutableSpongeGoldenAppleData extends AbstractImmutableSingleCatalogData<GoldenApple, ImmutableGoldenAppleData, GoldenAppleData> implements ImmutableGoldenAppleData {

    public ImmutableSpongeGoldenAppleData() {
        this(GoldenApples.GOLDEN_APPLE);
    }

    public ImmutableSpongeGoldenAppleData(GoldenApple value) {
        super(ImmutableGoldenAppleData.class, value, Keys.GOLDEN_APPLE_TYPE, SpongeGoldenAppleData.class);
    }
}

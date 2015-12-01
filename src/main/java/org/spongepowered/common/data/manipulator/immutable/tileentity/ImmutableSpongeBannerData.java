package org.spongepowered.common.data.manipulator.immutable.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.immutable.ImmutablePatternListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongePatternListValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.List;

public class ImmutableSpongeBannerData extends AbstractImmutableData<ImmutableBannerData, BannerData> implements ImmutableBannerData {

    private final DyeColor base;
    private final List<PatternLayer> layers;

    public ImmutableSpongeBannerData(DyeColor base, List<PatternLayer> layers) {
        super(ImmutableBannerData.class);
        this.base = checkNotNull(base, "Null base!");
        this.layers = ImmutableList.copyOf(checkNotNull(layers, "Null pattern list!"));
    }

    public DyeColor getBase() {
        return this.base;
    }

    public List<PatternLayer> getLayers() {
        return this.layers;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.BANNER_BASE_COLOR, this::getBase);
        registerKeyValue(Keys.BANNER_BASE_COLOR, this::baseColor);

        registerFieldGetter(Keys.BANNER_PATTERNS, this::getLayers);
        registerKeyValue(Keys.BANNER_PATTERNS, this::patterns);
    }

    @Override
    public ImmutableValue<DyeColor> baseColor() {
        return ImmutableSpongeValue.cachedOf(Keys.BANNER_BASE_COLOR, DataConstants.DEFAULT_BANNER_BASE, this.base);
    }

    @Override
    public ImmutablePatternListValue patterns() {
        return new ImmutableSpongePatternListValue(Keys.BANNER_PATTERNS, this.layers);
    }

    @Override
    public BannerData asMutable() {
        return new SpongeBannerData(this.base, this.layers);
    }

    @Override
    public int compareTo(ImmutableBannerData o) {
        return ComparisonChain.start()
            .compare(o.baseColor().get().getId(), this.base.getId())
            .compare(o.patterns().get().containsAll(this.layers), this.layers.containsAll(o.patterns().get()))
            .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.BANNER_BASE_COLOR.getQuery(), this.base.getId())
            .set(Keys.BANNER_PATTERNS, this.layers);
    }
}

package org.spongepowered.common.data.processor.value.tileentity;

import net.minecraft.tileentity.TileEntityBanner;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongePatternListValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongePatternListValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.block.tile.IMixinBanner;

import java.util.List;
import java.util.Optional;

public class TileBannerPatternLayersValueProcessor extends AbstractSpongeValueProcessor<TileEntityBanner, List<PatternLayer>, PatternListValue> {

    public TileBannerPatternLayersValueProcessor() {
        super(TileEntityBanner.class, Keys.BANNER_PATTERNS);
    }

    @Override
    protected PatternListValue constructValue(List<PatternLayer> actualValue) {
        return new SpongePatternListValue(Keys.BANNER_PATTERNS, actualValue);
    }

    @Override
    protected boolean set(TileEntityBanner container, List<PatternLayer> value) {
        if (!container.getWorld().isRemote) { // This avoids a client crash because clientside.
            ((IMixinBanner) container).setLayers(value);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<List<PatternLayer>> getVal(TileEntityBanner container) {
        return Optional.of(((IMixinBanner) container).getLayers());
    }

    @Override
    protected ImmutableValue<List<PatternLayer>> constructImmutableValue(List<PatternLayer> value) {
        return new ImmutableSpongePatternListValue(Keys.BANNER_PATTERNS, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}

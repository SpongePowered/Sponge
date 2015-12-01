package org.spongepowered.common.data.processor.value.tileentity;

import net.minecraft.tileentity.TileEntityBanner;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.block.tile.IMixinBanner;

import java.util.Optional;

public class TileBannerBaseColorValueProcessor extends AbstractSpongeValueProcessor<TileEntityBanner, DyeColor, Value<DyeColor>> {

    public TileBannerBaseColorValueProcessor() {
        super(TileEntityBanner.class, Keys.BANNER_BASE_COLOR);
    }

    @Override
    protected Value<DyeColor> constructValue(DyeColor actualValue) {
        return new SpongeValue<>(Keys.BANNER_BASE_COLOR, DataConstants.DEFAULT_BANNER_BASE, actualValue);
    }

    @Override
    protected boolean set(TileEntityBanner container, DyeColor value) {
        if (!container.getWorld().isRemote) {
            ((IMixinBanner) container).setBaseColor(value);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<DyeColor> getVal(TileEntityBanner container) {
        return Optional.of(((IMixinBanner) container).getBaseColor());
    }

    @Override
    protected ImmutableValue<DyeColor> constructImmutableValue(DyeColor value) {
        return ImmutableSpongeValue.cachedOf(Keys.BANNER_BASE_COLOR, DataConstants.DEFAULT_BANNER_BASE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}

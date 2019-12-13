package org.spongepowered.common.data.provider.block.entity;

import net.minecraft.tileentity.BannerTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.common.bridge.tileentity.BannerTileEntityBridge;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class BannerTileEntityBaseColorProvider extends GenericMutableDataProvider<BannerTileEntity, DyeColor> {

    public BannerTileEntityBaseColorProvider() {
        super(Keys.BANNER_BASE_COLOR);
    }

    @Override
    protected Optional<DyeColor> getFrom(BannerTileEntity dataHolder) {
        return Optional.of(((BannerTileEntityBridge) dataHolder).bridge$getBaseColor());
    }

    @Override
    protected boolean set(BannerTileEntity dataHolder, DyeColor value) {
        if (!dataHolder.getWorld().isRemote) {
            ((BannerTileEntityBridge) dataHolder).bridge$setBaseColor(value);
            return true;
        }
        return false;
    }
}

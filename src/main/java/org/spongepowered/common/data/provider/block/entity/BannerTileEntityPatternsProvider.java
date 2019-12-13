package org.spongepowered.common.data.provider.block.entity;

import net.minecraft.tileentity.BannerTileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.common.bridge.tileentity.BannerTileEntityBridge;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.List;
import java.util.Optional;

public class BannerTileEntityPatternsProvider extends GenericMutableDataProvider<BannerTileEntity, List<PatternLayer>> {

    public BannerTileEntityPatternsProvider() {
        super(Keys.BANNER_PATTERNS);
    }

    @Override
    protected Optional<List<PatternLayer>> getFrom(BannerTileEntity dataHolder) {
        return Optional.of(((BannerTileEntityBridge) dataHolder).bridge$getLayers());
    }

    @Override
    protected boolean set(BannerTileEntity dataHolder, List<PatternLayer> value) {
        if (!dataHolder.getWorld().isRemote) { // This avoids a client crash because clientside.
            ((BannerTileEntityBridge) dataHolder).bridge$setLayers(value);
            return true;
        }
        return false;
    }
}

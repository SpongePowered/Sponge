package org.spongepowered.common.data.processor.data.tileentity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.tileentity.TileEntityBanner;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;
import org.spongepowered.common.interfaces.block.tile.IMixinBanner;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TileEntityBannerDataProcessor extends AbstractTileEntityDataProcessor<TileEntityBanner, BannerData, ImmutableBannerData> {

    public TileEntityBannerDataProcessor() {
        super(TileEntityBanner.class);
    }

    @Override
    protected boolean doesDataExist(TileEntityBanner entity) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(TileEntityBanner entity, Map<Key<?>, Object> keyValues) {
        if (!entity.getWorld().isRemote) {
            List<PatternLayer> layers = (List<PatternLayer>) keyValues.get(Keys.BANNER_PATTERNS);
            DyeColor baseColor = (DyeColor) keyValues.get(Keys.BANNER_BASE_COLOR);
            ((IMixinBanner) entity).setLayers(layers);
            ((IMixinBanner) entity).setBaseColor(baseColor);
            return true;
        }
        return false;
    }

    @Override
    protected Map<Key<?>, ?> getValues(TileEntityBanner entity) {
        List<PatternLayer> layers = ((IMixinBanner) entity).getLayers();
        DyeColor color = ((IMixinBanner) entity).getBaseColor();
        return ImmutableMap.of(Keys.BANNER_BASE_COLOR, color, Keys.BANNER_PATTERNS, layers);
    }

    @Override
    protected BannerData createManipulator() {
        return new SpongeBannerData();
    }

    @Override
    public Optional<BannerData> fill(DataContainer container, BannerData bannerData) {
        if (container.contains(Keys.BANNER_PATTERNS.getQuery()) || container.contains(Keys.BANNER_BASE_COLOR.getQuery())) {
            List<PatternLayer> layers = container.getSerializableList(Keys.BANNER_PATTERNS.getQuery(), PatternLayer.class, Sponge.getSerializationService()).get();
            String colorId = container.getString(Keys.BANNER_BASE_COLOR.getQuery()).get();
            DyeColor color = Sponge.getRegistry().getType(DyeColor.class, colorId).get();
            bannerData.set(Keys.BANNER_BASE_COLOR, color);
            bannerData.set(Keys.BANNER_PATTERNS, layers);
            return Optional.of(bannerData);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}

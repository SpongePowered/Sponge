package org.spongepowered.common.data.builder.manipulator.mutable.tileentity;

import java.awt.Color;
import java.util.List;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntityBanner;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.util.DataUtil;

import com.google.common.base.Optional;

public class BannerDataBuilder implements DataManipulatorBuilder<BannerData, ImmutableBannerData> {

    @SuppressWarnings("unchecked")
    @Override
    public Optional<BannerData> build(DataView container) throws InvalidDataException {
        DataUtil.checkDataExists(container, Keys.BANNER_BASE_COLOR.getQuery());
        DataUtil.checkDataExists(container, Keys.BANNER_PATTERNS.getQuery());
        final DyeColor baseColor = (DyeColor) container.get(Keys.BANNER_BASE_COLOR.getQuery()).get();
        final List<PatternLayer> patterns = (List<PatternLayer>) container.getList(Keys.BANNER_PATTERNS.getQuery()).get();
        return Optional.<BannerData>of(new SpongeBannerData(baseColor, patterns));
    }

    @Override
    public BannerData create() {
        return new SpongeBannerData();
    }

    @Override
    public Optional<BannerData> createFrom(DataHolder dataHolder) {
        // TODO 
        if (dataHolder instanceof TileEntityBanner) {
            final Color baseColor;
            final List<TileEntityBanner.EnumBannerPattern> mojangBannerPattern;
            final List<EnumDyeColor> mojangEnumDyeColor;
        }
        return null;
    }
    
    public Color getColor(int id) {
        switch (id) {
        case 0: 
            return new Color(0x191919);    
        case 1:
            return new Color(0x993333);
        case 2:
            return new Color(0x334CB2);
        case 3:
            return new Color(0x664C33);
        case 4:
            return new Color(0x4C7F99);
        case 5:
            return new Color(0x4C4C4C);
        case 6:
            return new Color(0x667F33);
        case 7:
            return new Color(0x6699D8);
        case 8:
            return new Color(0x7FCC19);
        case 9:
            return new Color(0xB24CD8);
        case 10:
            return new Color(0xD87F33);
        case 11:
            return new Color(0xF27FA5);
        case 12:
            return new Color(0x7F3FB2);
        case 13:
            return new Color(0x993333);
        case 14:
            return new Color(0x999999);
        case 15:
            return new Color(0xE5E533);
        default:
            return new Color(0xFFFFFF);
        }
    }

}

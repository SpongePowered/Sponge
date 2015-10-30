package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.tileentity.TileEntityBanner;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BannerPatternShapes;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.common.data.type.SpongeNotePitch;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;
import org.spongepowered.common.registry.RegistryHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BannerPatternShapeRegistryModule implements CatalogRegistryModule<BannerPatternShape> {

    @RegisterCatalog(BannerPatternShapes.class)
    private final Map<String, BannerPatternShape> bannerPatternShapeMappings = Maps.newHashMap();

    @Override
    public Optional<BannerPatternShape> getById(String id) {
        return Optional.ofNullable(this.bannerPatternShapeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<BannerPatternShape> getAll() {
        return ImmutableList.copyOf(this.bannerPatternShapeMappings.values());
    }

    @Override
    public void registerDefaults() {
        for (TileEntityBanner.EnumBannerPattern pattern : TileEntityBanner.EnumBannerPattern.values()) {
            this.bannerPatternShapeMappings.put(pattern.name().toLowerCase(), (BannerPatternShape) (Object) pattern);
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (TileEntityBanner.EnumBannerPattern pattern : TileEntityBanner.EnumBannerPattern.values()) {
            if (!this.bannerPatternShapeMappings.containsKey(pattern.name())) {
                this.bannerPatternShapeMappings.put(pattern.name().toLowerCase(), (BannerPatternShape) (Object) pattern);
            }
        }
    }

}

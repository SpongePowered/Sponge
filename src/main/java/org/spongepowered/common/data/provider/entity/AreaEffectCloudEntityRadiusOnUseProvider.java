package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.AreaEffectCloudEntityAccessor;

import java.util.Optional;

public class AreaEffectCloudEntityRadiusOnUseProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, Double> {

    public AreaEffectCloudEntityRadiusOnUseProvider() {
        super(Keys.AREA_EFFECT_CLOUD_RADIUS_ON_USE);
    }

    @Override
    protected Optional<Double> getFrom(AreaEffectCloudEntity dataHolder) {
        return Optional.of((double) ((AreaEffectCloudEntityAccessor) dataHolder).accessor$getRadiusOnUse());
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, Double value) {
        dataHolder.setRadiusOnUse(value.floatValue());
        return true;
    }
}

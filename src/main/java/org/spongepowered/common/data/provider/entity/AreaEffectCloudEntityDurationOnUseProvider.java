package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.AreaEffectCloudEntityAccessor;

import java.util.Optional;

public class AreaEffectCloudEntityDurationOnUseProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, Integer> {

    public AreaEffectCloudEntityDurationOnUseProvider() {
        super(Keys.AREA_EFFECT_CLOUD_DURATION_ON_USE);
    }

    @Override
    protected Optional<Integer> getFrom(AreaEffectCloudEntity dataHolder) {
        return Optional.of(((AreaEffectCloudEntityAccessor) dataHolder).accessor$getDurationOnUse());
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, Integer value) {
        ((AreaEffectCloudEntityAccessor) dataHolder).accessor$setDurationOnUse(value);
        return true;
    }
}

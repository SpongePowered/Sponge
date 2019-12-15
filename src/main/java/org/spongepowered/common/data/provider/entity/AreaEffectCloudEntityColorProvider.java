package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class AreaEffectCloudEntityColorProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, Color> {

    public AreaEffectCloudEntityColorProvider() {
        super(Keys.AREA_EFFECT_CLOUD_COLOR);
    }

    @Override
    protected Optional<Color> getFrom(AreaEffectCloudEntity dataHolder) {
        return Optional.of(Color.ofRgb(dataHolder.getColor()));
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, Color value) {
        dataHolder.setColor(value.getRgb());
        return true;
    }
}

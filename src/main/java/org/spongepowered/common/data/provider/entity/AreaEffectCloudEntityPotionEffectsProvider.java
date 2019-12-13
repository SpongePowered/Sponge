package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.AreaEffectCloudEntityAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class AreaEffectCloudEntityPotionEffectsProvider extends GenericMutableDataProvider<AreaEffectCloudEntity, List<PotionEffect>> {

    public AreaEffectCloudEntityPotionEffectsProvider() {
        super(Keys.POTION_EFFECTS);
    }

    @Override
    protected Optional<List<PotionEffect>> getFrom(AreaEffectCloudEntity dataHolder) {
        return Optional.of(((List<PotionEffect>) (List<?>) ((AreaEffectCloudEntityAccessor) dataHolder).accessor$getEffects()));
    }

    @Override
    protected boolean set(AreaEffectCloudEntity dataHolder, List<PotionEffect> value) {
        ((AreaEffectCloudEntityAccessor) dataHolder).accessor$setEffects(new ArrayList(value));
        return true;
    }
}

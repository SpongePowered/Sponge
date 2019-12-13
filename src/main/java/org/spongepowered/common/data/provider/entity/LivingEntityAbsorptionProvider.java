package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class LivingEntityAbsorptionProvider extends GenericMutableDataProvider<LivingEntity, Double> {

    public LivingEntityAbsorptionProvider() {
        super(Keys.ABSORPTION);
    }

    @Override
    protected Optional<Double> getFrom(LivingEntity dataHolder) {
        return Optional.of((double) dataHolder.getAbsorptionAmount());
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Double value) {
        dataHolder.setAbsorptionAmount(value.floatValue());
        return false;
    }
}

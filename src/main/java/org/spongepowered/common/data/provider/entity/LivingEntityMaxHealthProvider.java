package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class LivingEntityMaxHealthProvider extends GenericMutableDataProvider<LivingEntity, Double> {

    public LivingEntityMaxHealthProvider() {
        super(Keys.MAX_HEALTH);
    }

    @Override
    protected Optional<Double> getFrom(LivingEntity dataHolder) {
        return Optional.of((double) dataHolder.getMaxHealth());
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Double value) {
        dataHolder.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(value);
        return true;
    }
}

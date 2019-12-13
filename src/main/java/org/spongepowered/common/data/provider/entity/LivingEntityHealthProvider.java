package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.bridge.entity.LivingEntityBridge;
import org.spongepowered.common.data.provider.GenericMutableBoundedDataProvider;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;

import java.util.Optional;

public class LivingEntityHealthProvider extends GenericMutableBoundedDataProvider<LivingEntity, Double> {

    public LivingEntityHealthProvider() {
        super(Keys.HEALTH);
    }

    @Override
    protected BoundedValue<Double> constructValue(LivingEntity dataHolder, Double element) {
        return BoundedValue.immutableOf(this.getKey(), element, 0.0, (double) dataHolder.getMaxHealth());
    }

    @Override
    protected Optional<Double> getFrom(LivingEntity dataHolder) {
        return Optional.of((double) dataHolder.getHealth());
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Double value) {
        final double maxHealth = value;
        // Check bounds
        if (value > maxHealth || value < 0) {
            return false;
        }

        dataHolder.setHealth(value.floatValue());
        if (value == 0) {
            dataHolder.attackEntityFrom(DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE, 1000F);
        } else {
            ((LivingEntityBridge) dataHolder).bridge$resetDeathEventsPosted();
        }
        return true;
    }
}

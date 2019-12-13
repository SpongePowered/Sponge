package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.EnderCrystalEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.provider.GenericMutableBoundedDataProvider;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;

import java.util.Optional;

public class EnderCrystalEntityHealthProvider extends GenericMutableBoundedDataProvider<EnderCrystalEntity, Double> {

    private static final double ALIVE_HEALTH = 1.0;

    public EnderCrystalEntityHealthProvider() {
        super(Keys.HEALTH);
    }

    @Override
    protected BoundedValue<Double> constructValue(EnderCrystalEntity dataHolder, Double element) {
        return BoundedValue.immutableOf(this.getKey(), element, 0.0, ALIVE_HEALTH);
    }

    @Override
    protected Optional<Double> getFrom(EnderCrystalEntity dataHolder) {
        return Optional.of(dataHolder.removed ? 0.0 : ALIVE_HEALTH);
    }

    @Override
    protected boolean set(EnderCrystalEntity dataHolder, Double value) {
        if (value < 0 || value > ALIVE_HEALTH) {
            return false;
        }
        if (value == 0) {
            dataHolder.attackEntityFrom(DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE, 1000F);
        } else {
            dataHolder.removed = false;
        }
        return true;
    }
}

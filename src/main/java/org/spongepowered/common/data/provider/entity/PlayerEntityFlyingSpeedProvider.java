package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.player.PlayerCapabilitiesAccessor;

import java.util.Optional;

public class PlayerEntityFlyingSpeedProvider extends GenericMutableDataProvider<PlayerEntity, Double> {

    public PlayerEntityFlyingSpeedProvider() {
        super(Keys.FLYING_SPEED);
    }

    @Override
    protected Optional<Double> getFrom(PlayerEntity dataHolder) {
        return Optional.of((double) dataHolder.abilities.getFlySpeed());
    }

    @Override
    protected boolean set(PlayerEntity dataHolder, Double value) {
        ((PlayerCapabilitiesAccessor) dataHolder.abilities).accessor$setFlySpeed(value.floatValue());
        dataHolder.sendPlayerAbilities();
        return true;
    }
}

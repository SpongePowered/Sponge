package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.player.PlayerCapabilitiesAccessor;

import java.util.Optional;

public class PlayerEntityWalkingSpeedProvider extends GenericMutableDataProvider<PlayerEntity, Double> {

    public PlayerEntityWalkingSpeedProvider() {
        super(Keys.WALKING_SPEED);
    }

    @Override
    protected Optional<Double> getFrom(PlayerEntity dataHolder) {
        return Optional.of(((double) dataHolder.abilities.getWalkSpeed()));
    }

    @Override
    protected boolean set(PlayerEntity dataHolder, Double value) {
        ((PlayerCapabilitiesAccessor) dataHolder.abilities).accessor$setWalkSpeed(value.floatValue());
        final IAttributeInstance attribute = dataHolder.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        attribute.setBaseValue(value);
        dataHolder.sendPlayerAbilities();
        return false;
    }
}

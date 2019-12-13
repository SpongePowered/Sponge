package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class LivingEntityLastAttackerProvider extends GenericMutableDataProvider<LivingEntity, Entity> {

    public LivingEntityLastAttackerProvider() {
        super(Keys.LAST_ATTACKER);
    }

    @Override
    protected Optional<Entity> getFrom(LivingEntity dataHolder) {
        // TODO
        return Optional.empty();
    }

    @Override
    protected boolean set(LivingEntity dataHolder, Entity value) {
        // TODO
        return false;
    }
}

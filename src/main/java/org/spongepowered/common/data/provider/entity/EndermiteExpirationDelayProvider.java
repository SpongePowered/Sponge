package org.spongepowered.common.data.provider.entity;

import static com.google.common.base.Preconditions.checkArgument;

import net.minecraft.entity.monster.EndermiteEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.TemporalUnits;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.monster.EndermiteEntityAccessor;

import java.time.Duration;
import java.util.Optional;

public class EndermiteExpirationDelayProvider extends GenericMutableDataProvider<EndermiteEntity, Duration> {

    public EndermiteExpirationDelayProvider() {
        super(Keys.EXPIRATION_DELAY);
    }

    @Override
    protected Optional<Duration> getFrom(EndermiteEntity dataHolder) {
        if (dataHolder.isNoDespawnRequired()) {
            return Optional.empty();
        }
        int ticks = ((EndermiteEntityAccessor) dataHolder).accessor$getLifetime();
        Duration duration = Duration.of(ticks, TemporalUnits.MINECRAFT_TICKS);
        return Optional.of(duration);
    }

    @Override
    protected boolean set(EndermiteEntity dataHolder, Duration value) {
        if (dataHolder.isNoDespawnRequired()) {
            return false;
        }
        long ticks = value.get(TemporalUnits.MINECRAFT_TICKS);
        checkArgument(ticks >= 0);
        checkArgument(ticks <= 2400);
        ((EndermiteEntityAccessor) dataHolder).accessor$setLifetime((int)ticks);
        return true;
    }

}

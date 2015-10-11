package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;
import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayingData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayingData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePlayingData;

import java.util.Optional;

public class PlayingDataBuilder implements DataManipulatorBuilder<PlayingData, ImmutablePlayingData> {

    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityVillager;
    }

    @Override
    public PlayingData create() {
        return new SpongePlayingData();
    }

    @Override
    public Optional<PlayingData> createFrom(DataHolder dataHolder) {
        final boolean isPlaying;
        if(supports(dataHolder)) {
            isPlaying = ((EntityVillager) dataHolder).isPlaying();
            return Optional.of(new SpongePlayingData(isPlaying));
        }
        return Optional.empty();
    }

    @Override
    public Optional<PlayingData> build(DataView container) throws InvalidDataException {
        if(container.contains(Keys.IS_PLAYING.getQuery())) {
            PlayingData playingData = create();
            playingData.set(Keys.IS_PLAYING, getData(container, Keys.IS_PLAYING));
            return Optional.of(playingData);
        }
        return Optional.empty();
    }
}

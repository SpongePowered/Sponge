package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.processor.data.entity.HorseDataProcessor;

public class HorseDataBuilder implements DataManipulatorBuilder<HorseData, ImmutableHorseData> {

    private HorseDataProcessor processor;

    public HorseDataBuilder(HorseDataProcessor processor) {
        this.processor = processor;
    }

    @Override
    public HorseData create() {
        return new SpongeHorseData();
    }

    @Override
    public Optional<HorseData> createFrom(DataHolder dataHolder) {
        return this.processor.createFrom(dataHolder);
    }

    @Override
    public Optional<HorseData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.HORSE_COLOR.getQuery()) && container.contains(Keys.HORSE_STYLE.getQuery()) && container.contains(Keys.HORSE_VARIANT.getQuery())) {
            HorseColor color = getData(container, Keys.HORSE_COLOR);
            HorseStyle style = getData(container, Keys.HORSE_STYLE);
            HorseVariant variant = getData(container, Keys.HORSE_VARIANT);

            return Optional.<HorseData>of(new SpongeHorseData(color, style, variant));
        }
        return Optional.absent();
    }
}

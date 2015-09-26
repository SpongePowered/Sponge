package org.spongepowered.common.data.builder.manipulator.mutable.item;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGoldenAppleData;
import org.spongepowered.common.data.processor.data.item.GoldenAppleDataProcessor;
import org.spongepowered.common.data.util.DataUtil;

public class ItemGoldenAppleDataBuilder implements DataManipulatorBuilder<GoldenAppleData, ImmutableGoldenAppleData> {

    private GoldenAppleDataProcessor processor;

    public ItemGoldenAppleDataBuilder(GoldenAppleDataProcessor processor) {
        this.processor = processor;
    }

    @Override
    public GoldenAppleData create() {
        return new SpongeGoldenAppleData();
    }

    @Override
    public Optional<GoldenAppleData> createFrom(DataHolder dataHolder) {
        return this.processor.createFrom(dataHolder);
    }

    @Override
    public Optional<GoldenAppleData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.GOLDEN_APPLE_TYPE.getQuery())) {
            return Optional.<GoldenAppleData>of(new SpongeGoldenAppleData(Sponge.getSpongeRegistry().getType(GoldenApple.class, DataUtil.getData(container, Keys.GOLDEN_APPLE_TYPE, String.class)).get()));
        }
        return Optional.absent();
    }
}

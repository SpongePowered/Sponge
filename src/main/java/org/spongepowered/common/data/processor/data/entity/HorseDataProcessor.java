package org.spongepowered.common.data.processor.data.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHorseData;
import org.spongepowered.api.data.manipulator.mutable.entity.HorseData;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHorseData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.entity.SpongeHorseStyle;
import org.spongepowered.common.entity.SpongeHorseVariant;

import java.util.Map;

public class HorseDataProcessor extends AbstractEntityDataProcessor<EntityHorse, HorseData, ImmutableHorseData> {

    public HorseDataProcessor() {
        super(EntityHorse.class);
    }

    @Override
    protected HorseData createManipulator() {
        return new SpongeHorseData();
    }

    @Override
    protected boolean doesDataExist(EntityHorse entity) {
        return true;
    }

    @Override
    protected boolean set(EntityHorse entity, Map<Key<?>, Object> keyValues) {
        SpongeHorseColor horseColor = (SpongeHorseColor) keyValues.get(Keys.HORSE_COLOR);
        SpongeHorseStyle horseStyle = (SpongeHorseStyle) keyValues.get(Keys.HORSE_STYLE);
        SpongeHorseVariant horseVariant = (SpongeHorseVariant) keyValues.get(Keys.HORSE_VARIANT);

        int type = horseVariant.type;
        int variant = HorseUtils.getInternalVariant(horseColor, horseStyle);

        entity.setHorseType(type);
        entity.setHorseVariant(variant);

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityHorse entity) {
        return ImmutableMap.<Key<?>, Object>of(
                Keys.HORSE_COLOR, HorseUtils.getHorseColor(entity),
                Keys.HORSE_STYLE, HorseUtils.getHorseStyle(entity),
                Keys.HORSE_VARIANT, HorseUtils.getHorseVariant(entity.getHorseType())
        );
    }

    @Override
    public Optional<HorseData> fill(DataContainer container, HorseData horseData) {
        horseData.set(Keys.HORSE_COLOR, getData(container, Keys.HORSE_COLOR));
        horseData.set(Keys.HORSE_STYLE, getData(container, Keys.HORSE_STYLE));
        horseData.set(Keys.HORSE_VARIANT, getData(container, Keys.HORSE_VARIANT));

        return Optional.of(horseData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}

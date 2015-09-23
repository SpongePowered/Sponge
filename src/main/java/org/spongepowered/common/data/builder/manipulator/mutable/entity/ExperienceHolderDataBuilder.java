package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import org.spongepowered.common.data.util.DataUtil;

import org.spongepowered.api.data.key.Keys;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExperienceHolderData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;

public class ExperienceHolderDataBuilder implements DataManipulatorBuilder<ExperienceHolderData, ImmutableExperienceHolderData> {

    @Override
    public Optional<ExperienceHolderData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.EXPERIENCE_LEVEL.getQuery()) && container.contains(Keys.TOTAL_EXPERIENCE.getQuery())
                && container.contains(Keys.EXPERIENCE_SINCE_LEVEL.getQuery()) && container.contains(Keys.EXPERIENCE_FROM_START_OF_LEVEL.getQuery())) {
            final int level = DataUtil.getData(container, Keys.EXPERIENCE_LEVEL, Integer.class);
            final int totalExp = DataUtil.getData(container, Keys.TOTAL_EXPERIENCE, Integer.class);
            final int expSinceLevel = DataUtil.getData(container, Keys.EXPERIENCE_SINCE_LEVEL, Integer.class);
            final int expBetweenLevels = DataUtil.getData(container, Keys.EXPERIENCE_FROM_START_OF_LEVEL, Integer.class);
            return Optional.<ExperienceHolderData>of(new SpongeExperienceHolderData(level, totalExp, expSinceLevel, expBetweenLevels));
        }
        return Optional.absent();
    }

    @Override
    public ExperienceHolderData create() {
        return new SpongeExperienceHolderData();
    }

    @Override
    public Optional<ExperienceHolderData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) dataHolder;
            final int level = player.experienceLevel;
            final int totalExp = player.experienceTotal;
            final int expSinceLevel = (int) player.experience;
            final int expBetweenLevels = level >= 30 ? 112 + (level - 30) * 9 : (level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2);
            return Optional.<ExperienceHolderData>of(new SpongeExperienceHolderData(level, totalExp, expSinceLevel, expBetweenLevels));
        }
        return Optional.absent();
    }
}

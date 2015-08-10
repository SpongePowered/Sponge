package org.spongepowered.common.data.processor.data.entity;

import com.google.common.base.Optional;
import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeCareerData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCareerData;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.interfaces.entity.IMixinVillager;

public class CareerDataProcessor implements DataProcessor<CareerData, ImmutableCareerData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityVillager;
    }

    @Override
    public Optional<CareerData> from(DataHolder dataHolder) {
        if (dataHolder instanceof IMixinVillager) {
            return Optional.<CareerData>of(new SpongeCareerData(((IMixinVillager) dataHolder).getCareer()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<CareerData> fill(DataHolder dataHolder, CareerData manipulator) {
        if (dataHolder instanceof IMixinVillager) {
            return Optional.of(manipulator.set(Keys.CAREER, ((IMixinVillager) dataHolder).getCareer()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<CareerData> fill(DataHolder dataHolder, CareerData manipulator, MergeFunction overlap) {
        if (dataHolder instanceof IMixinVillager) {
            final CareerData original = from(dataHolder).get();
            return Optional.of(manipulator.set(Keys.CAREER, overlap.merge(manipulator, original).career().get()));
        }
        return Optional.absent();
    }

    @Override
    public Optional<CareerData> fill(DataContainer container, CareerData careerData) {
        final String careerId = DataUtil.getData(container, Keys.CAREER, String.class);
        final Optional<Career> optional = Sponge.getSpongeRegistry().getType(Career.class, careerId);
        if (optional.isPresent()) {
            careerData.set(Keys.CAREER, optional.get());
        }
        return Optional.absent();
    }

    @Override
    public Optional<ImmutableCareerData> fillImmutable(DataHolder dataHolder, ImmutableCareerData immutable) {
        return null;
    }

    @Override
    public Optional<ImmutableCareerData> fillImmutable(DataHolder dataHolder, ImmutableCareerData immutable, MergeFunction overlap) {
        return null;
    }

    @Override
    public Optional<ImmutableCareerData> fillImmutable(DataContainer container, ImmutableCareerData immutableManipulator) {
        return null;
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, CareerData manipulator) {
        if (dataHolder instanceof IMixinVillager) {
            final Career oldCareer = ((IMixinVillager) dataHolder).getCareer();
            final ImmutableValue<Career> newCareer = manipulator.career().asImmutable();
            try {
                ((IMixinVillager) dataHolder).setCareer(manipulator.career().get());
                return DataTransactionBuilder.builder()
                    .replace(ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.CAREER, oldCareer, oldCareer))
                    .success(newCareer)
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newCareer);
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, CareerData manipulator, MergeFunction function) {
        if (dataHolder instanceof IMixinVillager) {
            final Career oldCareer = ((IMixinVillager) dataHolder).getCareer();
            final CareerData oldData = from(dataHolder).get();
            final ImmutableValue<Career> newCareer = function.merge(oldData, manipulator).career().asImmutable();
            try {
                ((IMixinVillager) dataHolder).setCareer(newCareer.get());
                return DataTransactionBuilder.builder()
                    .replace(ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.CAREER, oldCareer, oldCareer))
                    .success(newCareer)
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newCareer);
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableCareerData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableCareerData immutable) {
        return null;
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public CareerData create() {
        return new SpongeCareerData();
    }

    @Override
    public ImmutableCareerData createImmutable() {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeCareerData.class, Careers.ARMORER);
    }

    @Override
    public Optional<CareerData> createFrom(DataHolder dataHolder) {
        return from(dataHolder);
    }

    @Override
    public Optional<CareerData> build(DataView container) throws InvalidDataException {
        final String careerId = DataUtil.getData(container, Keys.CAREER, String.class);
        final Optional<Career> optional = Sponge.getSpongeRegistry().getType(Career.class, careerId);
        if (optional.isPresent()) {
            return Optional.<CareerData>of(new SpongeCareerData(optional.get()));
        }
        return Optional.absent();
    }
}

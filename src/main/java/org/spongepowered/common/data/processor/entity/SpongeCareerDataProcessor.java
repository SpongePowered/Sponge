/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.processor.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;

import com.google.common.base.Optional;
import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.CareerData;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.entity.SpongeCareerData;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.entity.IMixinVillager;

public class SpongeCareerDataProcessor implements SpongeDataProcessor<CareerData> {

    @Override
    public Optional<CareerData> fillData(DataHolder dataHolder, CareerData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof IMixinVillager)) {
            return Optional.absent();
        } else {
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                case PRE_MERGE:
                    final Career career = ((IMixinVillager) dataHolder).getCareer();
                    return Optional.of(manipulator.setValue(career));
                default:
                    return Optional.absent();
            }
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, CareerData manipulator, DataPriority priority) {
        if (dataHolder instanceof EntityVillager) {
            switch (checkNotNull(priority)) {
                case DATA_MANIPULATOR:
                case POST_MERGE:
                    final CareerData oldCareer = getFrom(dataHolder).get();
                    final Career career = manipulator.getCareer();
                    ((IMixinVillager) dataHolder).setCareer(career);
                    return successReplaceData(oldCareer);
                default:
                    return successNoData();
            }
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }


    @Override
    public Optional<CareerData> build(DataView container) throws InvalidDataException {
        final String careerId = DataUtil.getData(container, SpongeCareerData.CAREER, String.class);
        final Optional<Career> careerOptional = Sponge.getGame().getRegistry().getType(Career.class, careerId);
        if (careerOptional.isPresent()) {
            return Optional.of(create().setValue(careerOptional.get()));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public CareerData create() {
        return new SpongeCareerData();
    }

    @Override
    public Optional<CareerData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityVillager) {
            final Career career = ((IMixinVillager) dataHolder).getCareer();
            final CareerData careerData = create();
            careerData.setCareer(career);
            return Optional.of(careerData);
        }
        return Optional.absent();
    }

    @Override
    public Optional<CareerData> getFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityVillager)) {
            return Optional.absent();
        } else {
            return createFrom(dataHolder);
        }
    }
}

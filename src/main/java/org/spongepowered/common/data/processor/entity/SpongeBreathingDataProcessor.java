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
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.entity.BreathingComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.entity.SpongeBreathingComponent;
import org.spongepowered.common.data.util.DataUtil;

public class SpongeBreathingDataProcessor implements SpongeDataProcessor<BreathingComponent> {

    @Override
    public Optional<BreathingComponent> getFrom(DataHolder dataHolder) {
        if (!(checkNotNull(dataHolder) instanceof EntityLivingBase)) {
            return Optional.absent();
        }
        final int air = ((EntityLivingBase) dataHolder).getAir();
        final int maxAir = 300; // todo for now
        return Optional.of(create().setValue(air));
    }

    @Override
    public Optional<BreathingComponent> fillData(DataHolder dataHolder, BreathingComponent manipulator, DataPriority priority) {
        if (!(dataHolder instanceof EntityLivingBase)) {
            return Optional.absent();
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                final int air = ((EntityLivingBase) dataHolder).getAir();
                return Optional.of(manipulator.setMaxAir(300).setValue(air));
            default:
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, BreathingComponent manipulator, DataPriority priority) {
        if (!(checkNotNull(dataHolder) instanceof EntityLivingBase)) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case COMPONENT:
            case POST_MERGE:
                final BreathingComponent previous = getFrom(dataHolder).get();
                ((EntityLivingBase) dataHolder).setAir(checkNotNull(manipulator).getValue());
                return successReplaceData(previous);
            default:
                return successNoData();
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<BreathingComponent> build(DataView container) throws InvalidDataException {
        final int air = DataUtil.getData(container, Tokens.AIR.getQuery(), Integer.class);
        return Optional.of(create().setMaxAir(300).setMaxAir(air));
    }

    @Override
    public BreathingComponent create() {
        return new SpongeBreathingComponent(300);
    }

    @Override
    public Optional<BreathingComponent> createFrom(DataHolder dataHolder) {
        return getFrom(dataHolder);
    }
}

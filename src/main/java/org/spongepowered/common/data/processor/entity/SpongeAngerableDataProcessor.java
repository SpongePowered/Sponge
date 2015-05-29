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
import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.entity.AngerableComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.entity.SpongeAngerableComponent;
import org.spongepowered.common.interfaces.entity.IMixinAnger;

public class SpongeAngerableDataProcessor implements SpongeDataProcessor<AngerableComponent> {

    @Override
    public Optional<AngerableComponent> getFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof IMixinAnger)) {
            return Optional.absent();
        }
        final int anger = ((IMixinAnger) dataHolder).getAngerLevel();
        return Optional.of(create().setValue(anger));
    }

    @Override
    public Optional<AngerableComponent> fillData(DataHolder dataHolder, AngerableComponent manipulator, DataPriority priority) {
        if (!(checkNotNull(dataHolder) instanceof IMixinAnger)) {
            return Optional.absent();
        }
        final int anger = ((IMixinAnger) dataHolder).getAngerLevel();
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                return Optional.of(manipulator.setValue(anger));
            default:
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, AngerableComponent manipulator, DataPriority priority) {
        if (!(dataHolder instanceof IMixinAnger)) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case COMPONENT:
            case POST_MERGE:
                final int previous = ((IMixinAnger) dataHolder).getAngerLevel();
                ((IMixinAnger) dataHolder).setAngerLevel(manipulator.getValue());
                final AngerableComponent previousData = create().setValue(previous);
                return builder().replace(previousData).result(DataTransactionResult.Type.SUCCESS).build();
            default:
                return fail(manipulator);
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof IMixinAnger)) {
            return false;
        } else {
            ((IMixinAnger) dataHolder).setAngerLevel(0);
            return true;
        }
    }

    @Override
    public Optional<AngerableComponent> build(DataView container) throws InvalidDataException {
        final int anger = getData(container, Tokens.ANGER.getQuery(), Integer.TYPE);
        return Optional.of(create().setValue(anger));
    }

    @Override
    public AngerableComponent create() {
        return new SpongeAngerableComponent();
    }

    @Override
    public Optional<AngerableComponent> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof IMixinAnger)) {
            return Optional.absent();
        }
        final int anger = ((IMixinAnger) dataHolder).getAngerLevel();
        if (anger < 0) {
            return Optional.of(create().setValue(0));
        } else {
            return Optional.of(create().setValue(anger));
        }
    }
}

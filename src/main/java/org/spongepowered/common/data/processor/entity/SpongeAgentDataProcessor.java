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
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLiving;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.AgentData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.entity.SpongeAgentData;

public class SpongeAgentDataProcessor implements SpongeDataProcessor<AgentData> {

    @Override
    public Optional<AgentData> getFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityLiving)) {
            return Optional.absent();
        }
        final boolean aiDisabled = ((EntityLiving) dataHolder).isAIDisabled();
        return aiDisabled ?  Optional.<AgentData>absent() : Optional.of(create());
    }

    @Override
    public Optional<AgentData> fillData(DataHolder dataHolder, AgentData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof EntityLiving)) {
            return Optional.absent();
        }
        final boolean aiDisabled = ((EntityLiving) dataHolder).isAIDisabled();
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE: {
                if (aiDisabled) {
                    return Optional.absent();
                } else {
                    return Optional.of(manipulator);
                }
            }
            default:
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, AgentData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof EntityLiving)) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                ((EntityLiving) dataHolder).setNoAI(false);
                return successNoData();
            default:
                return successNoData();
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityLiving)) {
            return false;
        }
        ((EntityLiving) dataHolder).setNoAI(true);
        return true;
    }

    @Override
    public Optional<AgentData> build(DataView container) throws InvalidDataException {
        checkDataExists(container, SpongeAgentData.AI_ENABLED);
        final boolean aiEnabled = container.getBoolean(SpongeAgentData.AI_ENABLED).get();
        if (aiEnabled) {
            return Optional.of(create());
        }
        return Optional.absent();
    }

    @Override
    public AgentData create() {
        return new SpongeAgentData();
    }

    @Override
    public Optional<AgentData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityLiving)) {
            return Optional.absent();
        }
        // we create it regardless whether the entity has ai enabled or not.
        return Optional.of(create());
    }
}

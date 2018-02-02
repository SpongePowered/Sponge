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
package org.spongepowered.common.data.processor.data.entity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgentData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgentData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgentData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.util.DataUtil;

import java.util.Map;
import java.util.Optional;

public class AgentDataProcessor extends AbstractEntityDataProcessor<EntityLiving, AgentData, ImmutableAgentData> {

    public AgentDataProcessor() {
        super(EntityLiving.class);
    }

    @Override
    protected boolean doesDataExist(EntityLiving dataHolder) {
        return true;
    }

    @Override
    protected boolean set(EntityLiving dataHolder, Map<Key<?>, Object> keyValues) {
        dataHolder.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue((Double) keyValues.get(Keys.FOLLOW_RANGE));
        dataHolder.setNoAI(!((boolean) keyValues.get(Keys.AI_ENABLED)));
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(EntityLiving dataHolder) {
        return ImmutableMap.of(Keys.AI_ENABLED, !dataHolder.isAIDisabled(), Keys.FOLLOW_RANGE,
            dataHolder.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue());
    }

    @Override
    public Optional<AgentData> fill(DataContainer container, AgentData agentData) {
        if (container.contains(Keys.AI_ENABLED.getQuery()) && container.contains(Keys.FOLLOW_RANGE.getQuery())) {
            agentData.set(Keys.AI_ENABLED, DataUtil.getData(container, Keys.AI_ENABLED));
            agentData.set(Keys.FOLLOW_RANGE, DataUtil.getData(container, Keys.FOLLOW_RANGE));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected AgentData createManipulator() {
        return new SpongeAgentData();
    }
}

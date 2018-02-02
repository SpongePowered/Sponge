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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgentData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgentData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeAgentData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Comparator;

public class SpongeAgentData extends AbstractData<AgentData, ImmutableAgentData> implements AgentData {

    private boolean aiEnabled;
    private double followRange;

    public SpongeAgentData(boolean aiEnabled, double followRange) {
        super(AgentData.class);
        this.aiEnabled = aiEnabled;
        this.followRange = followRange;
    }

    public SpongeAgentData() {
        this(true, 32.0D);
    }

    @Override
    public Value<Boolean> aiEnabled() {
        return new SpongeValue<>(Keys.AI_ENABLED, this.aiEnabled);
    }

    @Override
    public MutableBoundedValue<Double> followRange() {
        return new SpongeBoundedValue<>(Keys.FOLLOW_RANGE, 32.0D, Comparator.naturalOrder(), 0D, 2048D, this.followRange);
    }

    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public boolean isAiEnabled() {
        return this.aiEnabled;
    }

    public void setFollowRange(double followRange) {
        this.followRange = followRange;
    }

    public double getFollowRange() {
        return this.followRange;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.AI_ENABLED, this::isAiEnabled);
        registerFieldSetter(Keys.AI_ENABLED, this::setAiEnabled);
        registerKeyValue(Keys.AI_ENABLED, this::aiEnabled);

        registerFieldGetter(Keys.FOLLOW_RANGE, this::getFollowRange);
        registerFieldSetter(Keys.FOLLOW_RANGE, this::setFollowRange);
        registerKeyValue(Keys.FOLLOW_RANGE, this::followRange);
    }

    @Override
    public AgentData copy() {
        return new SpongeAgentData(this.aiEnabled, this.followRange);
    }

    @Override
    public ImmutableAgentData asImmutable() {
        return new ImmutableSpongeAgentData(this.aiEnabled, this.followRange);
    }
}

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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableAgentData;
import org.spongepowered.api.data.manipulator.mutable.entity.AgentData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeAgentData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Comparator;

public final class ImmutableSpongeAgentData extends AbstractImmutableData<ImmutableAgentData, AgentData> implements ImmutableAgentData {

    private final boolean aiEnabled;
    private final double followRange;

    public ImmutableSpongeAgentData(boolean aiEnabled, double followRange) {
        super(ImmutableAgentData.class);
        this.aiEnabled = aiEnabled;
        this.followRange = followRange;
    }

    public ImmutableSpongeAgentData() {
        this(true, 32.0D);
    }

    @Override
    public ImmutableValue<Boolean> aiEnabled() {
        return ImmutableSpongeValue.cachedOf(Keys.AI_ENABLED, true, this.aiEnabled);
    }

    @Override
    public ImmutableBoundedValue<Double> followRange() {
        return ImmutableSpongeBoundedValue.cachedOf(Keys.FOLLOW_RANGE, 32.0, this.followRange, Comparator.naturalOrder(), 0D, 2048D);
    }

    public boolean isAiEnabled() {
        return this.aiEnabled;
    }

    public double getFollowRange() {
        return this.followRange;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.AI_ENABLED, this::isAiEnabled);
        registerKeyValue(Keys.AI_ENABLED, this::aiEnabled);

        registerFieldGetter(Keys.FOLLOW_RANGE, this::getFollowRange);
        registerKeyValue(Keys.FOLLOW_RANGE, this::followRange);
    }

    @Override
    public AgentData asMutable() {
        return new SpongeAgentData(this.aiEnabled, this.followRange);
    }
}

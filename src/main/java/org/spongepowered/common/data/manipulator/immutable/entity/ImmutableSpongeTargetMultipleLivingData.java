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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetMultipleLivingData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetMultipleLivingData;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.manipulator.immutable.common.collection.AbstractImmutableSingleListData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTargetMultipleLivingData;

import java.util.List;

public class ImmutableSpongeTargetMultipleLivingData extends AbstractImmutableSingleListData<Living, ImmutableTargetMultipleLivingData, TargetMultipleLivingData>
        implements ImmutableTargetMultipleLivingData {

    public ImmutableSpongeTargetMultipleLivingData(List<Living> targets, int targetCount) {
        super(ImmutableTargetMultipleLivingData.class, targets, Keys.TARGETS, SpongeTargetMultipleLivingData.class);

        checkArgument(targets.size() <= targetCount,
                "The amount of targets is limited to " + String.valueOf(targetCount));
    }

    @Override
    public ImmutableListValue<Living> targets() {
        return getValueGetter();
    }
}

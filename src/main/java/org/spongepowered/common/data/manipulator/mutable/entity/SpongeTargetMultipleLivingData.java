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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetMultipleLivingData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetMultipleLivingData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTargetMultipleLivingData;
import org.spongepowered.common.data.manipulator.mutable.common.collection.AbstractSingleListData;
import org.spongepowered.common.data.value.mutable.SpongeListValue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpongeTargetMultipleLivingData extends AbstractSingleListData<Living, TargetMultipleLivingData, ImmutableTargetMultipleLivingData> implements
        TargetMultipleLivingData {

    public SpongeTargetMultipleLivingData(List<Living> targets, int targetCount) {
        super(TargetMultipleLivingData.class, targets, Keys.TARGETS, ImmutableSpongeTargetMultipleLivingData.class);

        checkArgument(targets.size() <= targetCount,
                "The amount of targets is limited to " + String.valueOf(targetCount));
    }

    public SpongeTargetMultipleLivingData() {
        this(new ArrayList<>(), 1);
    }

    @Override
    public int compareTo(TargetMultipleLivingData o) {
        return 0;
    }

    @Override
    public ListValue<Living> targets() {
        return new SpongeListValue<>(Keys.TARGETS, ImmutableList.copyOf(this.getValue()));
    }

    @Override
    public DataContainer toContainer() {
        List<String> entityUuids = getValue().stream().map(living -> living.getUniqueId().toString()).collect(Collectors.toList());
        return new MemoryDataContainer()
                .set(Keys.TARGETS.getQuery(), entityUuids);
    }
}

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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePassengerData;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePassengerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractListData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpongePassengerData extends AbstractListData<UUID, PassengerData, ImmutablePassengerData> implements PassengerData {

    public SpongePassengerData() {
        this(new ArrayList<>());
    }

    public SpongePassengerData(List<UUID> passengers) {
        super(PassengerData.class, passengers, Keys.PASSENGERS, ImmutableSpongePassengerData.class);
    }

    @Override
    public ListValue<UUID> passengers() {
        return getValueGetter();
    }

    @Override
    public PassengerData copy() {
        return new SpongePassengerData(this.getValue());
    }

    @Override
    public ImmutablePassengerData asImmutable() {
        return new ImmutableSpongePassengerData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.PASSENGERS, this.getValue());
    }
}

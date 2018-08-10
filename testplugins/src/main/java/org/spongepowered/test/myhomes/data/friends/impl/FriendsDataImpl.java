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
package org.spongepowered.test.myhomes.data.friends.impl;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractListData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.test.myhomes.MyHomes;
import org.spongepowered.test.myhomes.data.friends.FriendsData;
import org.spongepowered.test.myhomes.data.friends.ImmutableFriendsData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FriendsDataImpl extends AbstractListData<UUID, FriendsData, ImmutableFriendsData> implements FriendsData {

    public FriendsDataImpl(List<UUID> value) {
        super(value, MyHomes.FRIENDS);
    }

    public FriendsDataImpl() {
        this(ImmutableList.of());
    }

    @Override
    public ListValue<UUID> friends() {
        return getListValue();
    }

    @Override
    public ImmutableFriendsDataImpl asImmutable() {
        return new ImmutableFriendsDataImpl(getValue());
    }

    @Override
    public Optional<FriendsData> fill(DataHolder dataHolder, MergeFunction overlap) {
        FriendsData merged = overlap.merge(this, dataHolder.get(FriendsData.class).orElse(null));
        setValue(merged.friends().get());

        return Optional.of(this);
    }

    @Override
    public Optional<FriendsData> from(DataContainer container) {
        if (container.contains(MyHomes.FRIENDS)) {
            List<UUID> friends = container.getObjectList(MyHomes.FRIENDS.getQuery(), UUID.class).get();
            return Optional.of(setValue(friends));
        }

        return Optional.empty();
    }

    @Override
    protected DataContainer fillContainer(DataContainer dataContainer) {
        return dataContainer.set(MyHomes.FRIENDS, this.getValue());
    }

    @Override
    public FriendsData copy() {
        return new FriendsDataImpl(getValue());
    }

    @Override
    public int getContentVersion() {
        return FriendsDataBuilder.CONTENT_VERSION;
    }
}

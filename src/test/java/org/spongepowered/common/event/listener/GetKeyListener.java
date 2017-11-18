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
package org.spongepowered.common.event.listener;

import static org.spongepowered.api.data.DataTransactionResult.DataCategory.REPLACED;
import static org.spongepowered.api.data.DataTransactionResult.DataCategory.SUCCESSFUL;

import org.junit.Assert;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataTransactionResult.DataCategory;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.api.data.type.RailDirections;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.data.GetKey;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.util.DataVersions;

public class GetKeyListener {

    public boolean getKeyListenerCalled;
    public boolean dataHolderListenerCalled;
    public boolean rejectedDataListenerCalled;
    public boolean multipleCategoriesCalled;

    @Listener
    public void getKeyListener(ChangeDataHolderEvent.ValueChange event, @GetKey("FOOD_LEVEL") int foodLevel, @GetKey("FOOD_LEVEL") MutableBoundedValue<Integer> foodValue, @GetKey("FOOD_LEVEL") ImmutableValue<Integer> foodImmut) {
        this.getKeyListenerCalled = true;
        Assert.assertEquals(foodLevel, foodValue.get().intValue());
        Assert.assertEquals(foodLevel, foodImmut.get().intValue());
        Assert.assertEquals((long) foodLevel, 15);
    }

    @Listener
    public void dataHolderListener(ChangeDataHolderEvent.ValueChange event,
            @GetKey(value = "FOOD_LEVEL", from = REPLACED) int oldFood,
            @GetKey(value = "FOOD_LEVEL", from = SUCCESSFUL) MutableBoundedValue<Integer> newFood,
            @First(tag = "a") Player player,
            @GetKey(value = "FOOD_LEVEL", tag = "a") int playerFood) {

        this.dataHolderListenerCalled = true;

        Assert.assertEquals(playerFood, player.get(Keys.FOOD_LEVEL).get().intValue());
        Assert.assertEquals((long) newFood.get(), 15);
        Assert.assertEquals(oldFood, 16);
    }

    @Listener
    public void rejectedDataListener(ChangeDataHolderEvent.ValueChange event, @GetKey(value = "RAIL_DIRECTION", from = {DataCategory.REJECTED}) RailDirection rail) {
        this.rejectedDataListenerCalled = true;
        Assert.assertEquals(RailDirections.ASCENDING_EAST, rail);
    }

    @Listener
    public void multipleCategories(ChangeDataHolderEvent.ValueChange event,
            @GetKey(value = "RAIL_DIRECTION", from = { DataCategory.REPLACED, DataCategory.REJECTED}) RailDirection direction,
            @GetKey(value = "RABBIT_TYPE", from = { DataCategory.SUCCESSFUL, DataCategory.REPLACED}) RabbitType rabbitType) {

        this.multipleCategoriesCalled = true;
        Assert.assertEquals(RailDirections.ASCENDING_EAST, direction);
        Assert.assertEquals(RabbitTypes.BLACK_AND_WHITE, rabbitType);
    }

    @Listener
    public void invalidKeyListener(ChangeDataHolderEvent.ValueChange event, @GetKey("NOT_A_REAL_KEY") Integer blah) {
    }

    @Listener
    public void invalidImmutableParameterType(ChangeDataHolderEvent.ValueChange event, @GetKey("REPRESENTED_BLOCK") ImmutableBoundedValue<DataVersions.BlockState> val) {

    }

    @Listener
    public void invalidMutableParameterType(ChangeDataHolderEvent.ValueChange event, @GetKey("REPRESENTED_BLOCK") MutableBoundedValue<DataVersions.BlockState> val) {

    }

    @Listener
    public void invalidParamType(ChangeDataHolderEvent.ValueChange event, @GetKey("REPRESENTED_BLOCK") String val) {

    }

    @Listener
    public void invalidTagType(InteractBlockEvent event, @First Text text, @GetKey("REPRESENTED_BLOCK") BlockState block) {

    }

    @Listener
    public void multipleMatches(ChangeDataHolderEvent.ValueChange event, @First Text text, @GetKey("REPRESENTED_BLOCK") BlockState block) {

    }

    @Listener
    public void noMatches(ChangeDataHolderEvent.ValueChange event, @GetKey(value = "REPRESENTED_BLOCK", tag = "foo") BlockState block) {

    }

}

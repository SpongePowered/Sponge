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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityCreeper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFuseData;
import org.spongepowered.api.data.manipulator.mutable.entity.FuseData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFuseData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFuseData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.processor.common.AbstractSingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class FuseDataProcessor extends AbstractEntitySingleDataProcessor<Entity, Integer, MutableBoundedValue<Integer>, FuseData, ImmutableFuseData> {

    public FuseDataProcessor() {
        super(Entity.class, Keys.FUSE_DURATION);
    }

    @Override
    protected FuseData createManipulator() {
        return new SpongeFuseData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.FUSE_DURATION)
                .minimum(-1)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(0)
                .actualValue(actualValue)
                .build();
    }

    @Override
    protected boolean set(Entity container, Integer value) {
        if (this.supports(container)) {
            if (container instanceof EntityCreeper) {
                EntityCreeper creeper = (EntityCreeper) container;
                
                creeper.fuseTime += value;
                creeper.timeSinceIgnited = value;
            } else if (container instanceof EntityFireworkRocket) {
                EntityFireworkRocket fireworkRocket = (EntityFireworkRocket) container;
                
                fireworkRocket.lifetime += value;
                fireworkRocket.fireworkAge = value;
            } else if (container instanceof EntityMinecartTNT) {
                ((EntityMinecartTNT) container).minecartTNTFuse = value;
            } else if (container instanceof EntityTNTPrimed) {
                ((EntityTNTPrimed) container).fuse = value;
            }
            
            return true;
        }
        return false;
    }

    @Override
    protected Optional<Integer> getVal(Entity container) {
        if (container instanceof EntityCreeper) {
            EntityCreeper creeper = (EntityCreeper) container;
            
            return Optional.of(creeper.fuseTime - creeper.timeSinceIgnited);
        } else if (container instanceof EntityFireworkRocket) {
            EntityFireworkRocket fireworkRocket = (EntityFireworkRocket) container;
            
            return Optional.of(fireworkRocket.lifetime - fireworkRocket.fireworkAge);
        } else if (container instanceof EntityMinecartTNT) {
            EntityMinecartTNT tntMinecart = (EntityMinecartTNT) container;
            
            return Optional.of(tntMinecart.minecartTNTFuse);
        } else if (container instanceof EntityTNTPrimed) {
            EntityTNTPrimed primedTNT = (EntityTNTPrimed) container;
            
            return Optional.of(primedTNT.fuse);
        }
        return Optional.empty();
    }

    @Override
    protected boolean supports(Entity container) {
        return container instanceof EntityCreeper ||
               container instanceof EntityFireworkRocket ||
               container instanceof EntityMinecartTNT ||
               container instanceof EntityTNTPrimed;
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return constructValue(value).asImmutable();
    }

}

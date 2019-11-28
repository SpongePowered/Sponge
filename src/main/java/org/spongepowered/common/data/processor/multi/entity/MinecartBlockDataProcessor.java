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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMinecartBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.MinecartBlockData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMinecartBlockData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;

public class MinecartBlockDataProcessor extends AbstractEntityDataProcessor<AbstractMinecartEntity, MinecartBlockData, ImmutableMinecartBlockData> {

    public MinecartBlockDataProcessor() {
        super(AbstractMinecartEntity.class);
    }

    @Override
    protected boolean doesDataExist(AbstractMinecartEntity entity) {
        return entity.hasDisplayTile();
    }

    @Override
    protected boolean set(AbstractMinecartEntity entity, Map<Key<?>, Object> keyValues) {
        BlockState type = (BlockState) keyValues.get(Keys.REPRESENTED_BLOCK);
        int offset = (Integer) keyValues.get(Keys.OFFSET);

        entity.setDisplayTileOffset(offset);
        entity.setDisplayTile((net.minecraft.block.BlockState) type);

        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(AbstractMinecartEntity entity) {
        BlockState state = (BlockState) entity.getDisplayTile();
        int offset = entity.getDisplayTileOffset();
        return ImmutableMap.of(Keys.REPRESENTED_BLOCK, state, Keys.OFFSET, offset);
    }

    @Override
    protected MinecartBlockData createManipulator() {
        return new SpongeMinecartBlockData();
    }

    @Override
    public Optional<MinecartBlockData> fill(DataContainer container, MinecartBlockData data) {
        if(!container.contains(Keys.REPRESENTED_BLOCK.getQuery())
            || !container.contains(Keys.OFFSET.getQuery())) {
            return Optional.empty();
        }

        BlockState block = container.getSerializable(Keys.REPRESENTED_BLOCK.getQuery(), BlockState.class).get();
        int offset = container.getInt(Keys.OFFSET.getQuery()).get();

        data.set(Keys.REPRESENTED_BLOCK, block);
        data.set(Keys.OFFSET, offset);

        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if(dataHolder instanceof AbstractMinecartEntity) {
            AbstractMinecartEntity cart = (AbstractMinecartEntity) dataHolder;
            DataTransactionResult.Builder builder = DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS);
            if(cart.hasDisplayTile()) {
                ImmutableValue<BlockState> block = new ImmutableSpongeValue<>(Keys.REPRESENTED_BLOCK, (BlockState) cart.getDisplayTile());
                ImmutableValue<Integer> offset = new ImmutableSpongeValue<>(Keys.OFFSET, cart.getDisplayTileOffset());
                cart.setHasDisplayTile(false);
                builder.replace(block).replace(offset);
            }
            return builder.build();
        }
        return DataTransactionResult.failNoData();
    }
}

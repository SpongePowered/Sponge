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
package org.spongepowered.common.data.processor.data;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSkullData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeSkullData;
import org.spongepowered.common.data.manipulator.mutable.SpongeSkullData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class SkullDataProcessor extends AbstractSpongeDataProcessor<SkullData, ImmutableSkullData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return SkullUtils.supportsObject(dataHolder);
    }

    @Override
    public Optional<SkullData> from(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntitySkull) {
            return Optional.<SkullData>of(new SpongeSkullData(SkullUtils.getSkullType((TileEntitySkull) dataHolder)));
        } else if (SkullUtils.isValidItemStack(dataHolder)) {
            return Optional.<SkullData>of(new SpongeSkullData(SkullUtils.getSkullType((ItemStack) dataHolder)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<SkullData> fill(DataHolder dataHolder, SkullData manipulator, MergeFunction overlap) {
        if (this.supports(dataHolder)) {
            SkullData merged = overlap.merge(checkNotNull(manipulator.copy()), this.from(dataHolder).get());
            return Optional.of(manipulator.set(Keys.SKULL_TYPE, merged.type().get()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<SkullData> fill(DataContainer container, SkullData skullData) {
        return Optional.of(skullData.set(Keys.SKULL_TYPE, Sponge.getGame().getRegistry()
            .getType(SkullType.class, DataUtil.getData(container, Keys.SKULL_TYPE, String.class)).get()));
    }

    private DataTransactionResult setImpl(DataHolder dataHolder, SkullData manipulator, SpongeSkullType newType) {
        if (!this.supports(dataHolder)) {
            return DataTransactionBuilder.failResult(manipulator.getValues());
        }

        DataTransactionBuilder builder = DataTransactionBuilder.builder();

        SpongeSkullType oldType = null;

        if (dataHolder instanceof TileEntitySkull) {
            TileEntitySkull teSkull = (TileEntitySkull) dataHolder;

            oldType = (SpongeSkullType) SkullUtils.getSkullType(teSkull);
            SkullUtils.setSkullType(teSkull, newType.getByteId());
        } else if (SkullUtils.isValidItemStack(dataHolder)) {
            ItemStack itemStack = (ItemStack) dataHolder;

            oldType = (SpongeSkullType) SkullUtils.getSkullType(itemStack);
            itemStack.setItemDamage(newType.getByteId());
        }

        return DataTransactionBuilder.successReplaceResult(new ImmutableSpongeValue<SkullType>(Keys.SKULL_TYPE, newType),
                                                           new ImmutableSpongeValue<SkullType>(Keys.SKULL_TYPE, oldType));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, SkullData manipulator, MergeFunction function) {
        // Avoid calling the merge function and this.from() if the holder is not supported
        if (this.supports(dataHolder)) {
            return this.setImpl(dataHolder, manipulator, (SpongeSkullType) function.merge(this.from(dataHolder).get(), manipulator).type().get());
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableSkullData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableSkullData immutable) {
        if (key.equals(Keys.SKULL_TYPE)) {
            return Optional.<ImmutableSkullData>of(new ImmutableSpongeSkullData((SkullType) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
    }

    @Override
    public Optional<SkullData> createFrom(DataHolder dataHolder) {
        return this.from(dataHolder);
    }

}

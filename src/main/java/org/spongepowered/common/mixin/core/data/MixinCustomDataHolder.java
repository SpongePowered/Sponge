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
package org.spongepowered.common.mixin.core.data;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;

import java.util.List;

import javax.annotation.Nullable;

// TODO don't enable this until we want to enable custom data! Also, needs a lot of testing
@Mixin({TileEntity.class, ItemStack.class, Entity.class})
public abstract class MixinCustomDataHolder implements IMixinCustomDataHolder {

    private List<DataManipulator<?, ?>> manipulators = Lists.newArrayList();

    @Override
    public DataTransactionResult offerCustom(DataManipulator<?, ?> manipulator, MergeFunction function) {
        @Nullable DataManipulator<?, ?> existingManipulator = null;
        for (DataManipulator<?, ?> existing : this.manipulators) {
            if (manipulator.getClass().isInstance(existing)) {
                existingManipulator = existing;
                break;
            }
        }
        final DataManipulator<?, ?> newManipulator = function.merge(existingManipulator, (DataManipulator) manipulator.copy());

        return null;
    }

    @Override
    public <T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>> Optional<T> getCustom(Class<T> customClass) {
        return null;
    }

    @Override
    public DataTransactionResult removeCustom(Class<? extends DataManipulator<?, ?>> customClass) {
        return null;
    }

    @Override
    public List<DataManipulator<?, ?>> getCustomManipulators() {
        return null;
    }

}

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
package org.spongepowered.common.data.builder.manipulator.mutable;

import static org.spongepowered.common.data.util.DataUtil.getData;

import java.util.Optional;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.ImmutableWetData;
import org.spongepowered.api.data.manipulator.mutable.WetData;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.SpongeWetData;
import org.spongepowered.common.data.util.DataUtil;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;

public class WetDataBuilder implements DataManipulatorBuilder<WetData, ImmutableWetData> {

    @Override
    public Optional<WetData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.IS_WET.getQuery())) {
            final boolean isWet = getData(container, Keys.IS_WET);
            return Optional.of(new SpongeWetData(isWet));
        }
        return Optional.empty();
    }

    @Override
    public WetData create() {
        return new SpongeWetData(false);
    }

    @Override
    public Optional<WetData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            ItemStack stack = (ItemStack) dataHolder;

            if (stack.getItem().equals(ItemTypes.SPONGE)) {
                final boolean isWet = stack.getItemDamage() == 1;
                return Optional.of(new SpongeWetData(isWet));
            } else {
                return Optional.empty();
            }
        } else if (dataHolder instanceof EntityWolf) {
            final boolean isWet = ((EntityWolf) dataHolder).isWet || ((EntityWolf) dataHolder).isShaking;
            SpongeWetData data = new SpongeWetData(isWet);
            return Optional.of(data);
        }

        return Optional.empty();
    }

}

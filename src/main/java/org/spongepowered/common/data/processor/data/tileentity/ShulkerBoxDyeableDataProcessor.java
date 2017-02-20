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
package org.spongepowered.common.data.processor.data.tileentity;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntityShulkerBox;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDyeableData;
import org.spongepowered.api.data.manipulator.mutable.DyeableData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.registry.RegistryException;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.mutable.SpongeDyeableData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntityShulkerBox;

import java.util.Optional;

public class ShulkerBoxDyeableDataProcessor extends AbstractSingleDataProcessor<DyeColor, Value<DyeColor>, DyeableData, ImmutableDyeableData> {

    public ShulkerBoxDyeableDataProcessor() {
        super(Keys.DYE_COLOR);
    }

    @Override
    protected DyeableData createManipulator() {
        return new SpongeDyeableData(DyeColors.PURPLE);
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof TileEntityShulkerBox;
    }

    @Override
    public Optional<DyeableData> from(DataHolder dataHolder) {
        EnumDyeColor enumType = ((IMixinTileEntityShulkerBox) dataHolder).getColor();
        if (enumType != null) {
            return Optional.of(new SpongeDyeableData((DyeColor) (Object) enumType));
        }
        return Optional.empty();
    }

    @Override
    public Optional<DyeableData> fill(DataContainer container, DyeableData dyeableData) {
        Optional<String> id = container.getString(Keys.DYE_COLOR.getQuery());
        if (id.isPresent()) {
            Optional<DyeColor> color = Sponge.getGame().getRegistry().getType(DyeColor.class, id.get());
            dyeableData.set(Keys.DYE_COLOR,
                color.orElseThrow(() -> new RegistryException("Corresponding color " + id.get() + " missing in registry")));
            return Optional.of(dyeableData);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, DyeableData manipulator, MergeFunction function) {
        DyeableData original = dataHolder.require(DyeableData.class);
        DyeableData merged = function.merge(original, manipulator);
        Value<DyeColor> mergedValue = merged.type();
        ((IMixinTileEntityShulkerBox) dataHolder).setColor((EnumDyeColor) (Object) mergedValue.get());
        return DataTransactionResult.successReplaceResult(original.type().asImmutable(), mergedValue.asImmutable());
    }

    @Override
    public Optional<ImmutableDyeableData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableDyeableData immutable) {
        if (key.equals(Keys.DYE_COLOR)) {
            return Optional.of(ImmutableDataCachingUtil.getManipulator(ImmutableDyeableData.class, value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        IMixinTileEntityShulkerBox box = (IMixinTileEntityShulkerBox) dataHolder;
        DyeColor color = (DyeColor) (Object) box.getColor();
        box.setColor(null);
        if (color == null) {
            return DataTransactionResult.failNoData();
        }
        return DataTransactionResult.successRemove(ImmutableSpongeValue.cachedOf(Keys.DYE_COLOR, DyeColors.PURPLE, color));
    }

}

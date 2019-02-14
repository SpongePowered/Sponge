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

import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.PaintingType;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableArtData;
import org.spongepowered.api.data.manipulator.mutable.ArtData;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArtData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;

import java.util.Optional;

public class ArtDataProcessor extends AbstractEntitySingleDataProcessor<EntityPainting, Art, ArtData, ImmutableArtData> {

    public ArtDataProcessor() {
        super(EntityPainting.class, Keys.ART);
    }

    @Override
    protected boolean set(EntityPainting entity, Art value) {
        if (!entity.world.isRemote) {
            return EntityUtil.refreshPainting(entity, (PaintingType) (Object) value);
        }
        return true;
    }

    @Override
    protected Optional<Art> getVal(EntityPainting entity) {
        return Optional.of((Art) (Object) entity.art);
    }

    @Override
    protected Value.Immutable<Art> constructImmutableValue(Art value) {
        return ImmutableDataCachingUtil.getValue(SpongeImmutableValue.class, this.key, value, DataConstants.Catalog.DEFAULT_ART);
    }

    @Override
    protected ArtData createManipulator() {
        return new SpongeArtData();
    }

    @Override
    protected Value.Mutable<Art> constructMutableValue(Art actualValue) {
        return new SpongeMutableValue<>(Keys.ART, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}

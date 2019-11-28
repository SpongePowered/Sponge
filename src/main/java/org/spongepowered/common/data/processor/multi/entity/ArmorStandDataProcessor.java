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
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableArmorStandData;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArmorStandData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.mixin.core.entity.item.EntityArmorStandAccessor;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.item.ArmorStandEntity;

public class ArmorStandDataProcessor extends AbstractEntityDataProcessor<ArmorStandEntity, ArmorStandData, ImmutableArmorStandData> {

    public ArmorStandDataProcessor() {
        super(ArmorStandEntity.class);
    }

    @Override
    protected boolean doesDataExist(final ArmorStandEntity dataHolder) {
        return true;
    }

    @Override
    protected boolean set(final ArmorStandEntity dataHolder, final Map<Key<?>, Object> keyValues) {
        final boolean hasArms = (boolean) keyValues.get(Keys.ARMOR_STAND_HAS_ARMS);
        final boolean hasBasePlate = (boolean) keyValues.get(Keys.ARMOR_STAND_HAS_BASE_PLATE);
        final boolean isSmall = (boolean) keyValues.get(Keys.ARMOR_STAND_IS_SMALL);
        final boolean isMarker = (boolean) keyValues.get(Keys.ARMOR_STAND_MARKER);
        ((EntityArmorStandAccessor) dataHolder).accessor$setSmall(isSmall);
        ((EntityArmorStandAccessor) dataHolder).accessor$setMarker(isMarker);
        ((EntityArmorStandAccessor) dataHolder).accessor$setNoBasePlate(!hasBasePlate);
        ((EntityArmorStandAccessor) dataHolder).accessor$setShowArms(hasArms);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final ArmorStandEntity dataHolder) {
        return ImmutableMap.<Key<?>, Object>builder()
                .put(Keys.ARMOR_STAND_HAS_ARMS, dataHolder.getShowArms())
                .put(Keys.ARMOR_STAND_HAS_BASE_PLATE, !dataHolder.hasNoBasePlate())
                .put(Keys.ARMOR_STAND_MARKER, dataHolder.hasMarker())
                .put(Keys.ARMOR_STAND_IS_SMALL, dataHolder.isSmall())
                .build();
    }

    @Override
    protected ArmorStandData createManipulator() {
        return new SpongeArmorStandData();
    }

    @Override
    public Optional<ArmorStandData> fill(final DataContainer container, final ArmorStandData armorStandData) {
        if (container.contains(Keys.ARMOR_STAND_HAS_ARMS)) {
            armorStandData.set(Keys.ARMOR_STAND_HAS_ARMS, container.getBoolean(Keys.ARMOR_STAND_HAS_ARMS.getQuery()).get());
        }
        if (container.contains(Keys.ARMOR_STAND_MARKER)) {
            armorStandData.set(Keys.ARMOR_STAND_MARKER, container.getBoolean(Keys.ARMOR_STAND_MARKER.getQuery()).get());
        }
        if (container.contains(Keys.ARMOR_STAND_HAS_BASE_PLATE)) {
            armorStandData.set(Keys.ARMOR_STAND_HAS_BASE_PLATE, container.getBoolean(Keys.ARMOR_STAND_HAS_BASE_PLATE.getQuery()).get());
        }
        if (container.contains(Keys.ARMOR_STAND_IS_SMALL)) {
            armorStandData.set(Keys.ARMOR_STAND_IS_SMALL, container.getBoolean(Keys.ARMOR_STAND_IS_SMALL.getQuery()).get());
        }
        return Optional.of(armorStandData);
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}

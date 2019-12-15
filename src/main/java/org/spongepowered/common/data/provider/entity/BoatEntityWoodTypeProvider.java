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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.BoatEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.api.data.type.WoodTypes;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class BoatEntityWoodTypeProvider extends GenericMutableDataProvider<BoatEntity, WoodType> {

    public BoatEntityWoodTypeProvider() {
        super(Keys.WOOD_TYPE);
    }

    @Override
    protected Optional<WoodType> getFrom(BoatEntity dataHolder) {
        return Optional.ofNullable(getFor(dataHolder.getBoatType()));
    }

    @Override
    protected boolean set(BoatEntity dataHolder, WoodType value) {
        final BoatEntity.@Nullable Type type = getFor(value);
        if (type != null) {
            dataHolder.setBoatType(type);
            return true;
        }
        return false;
    }

    private static BoatEntity.@Nullable Type getFor(WoodType type) {
        if (type == WoodTypes.OAK) {
            return BoatEntity.Type.OAK;
        } else if (type == WoodTypes.SPRUCE) {
            return BoatEntity.Type.SPRUCE;
        } else if (type == WoodTypes.JUNGLE) {
            return BoatEntity.Type.JUNGLE;
        } else if (type == WoodTypes.DARK_OAK) {
            return BoatEntity.Type.DARK_OAK;
        } else if (type == WoodTypes.BIRCH) {
            return BoatEntity.Type.BIRCH;
        } else if (type == WoodTypes.ACACIA) {
            return BoatEntity.Type.ACACIA;
        }
        // TODO: Modded types?
        return null;
    }

    private static @Nullable WoodType getFor(BoatEntity.Type type) {
        switch (type) {
            case OAK:
                return WoodTypes.OAK;
            case SPRUCE:
                return WoodTypes.SPRUCE;
            case BIRCH:
                return WoodTypes.BIRCH;
            case JUNGLE:
                return WoodTypes.JUNGLE;
            case ACACIA:
                return WoodTypes.ACACIA;
            case DARK_OAK:
                return WoodTypes.DARK_OAK;
        }
        // TODO: Modded types?
        return null;
    }
}

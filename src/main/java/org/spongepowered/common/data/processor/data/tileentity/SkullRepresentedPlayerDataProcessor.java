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

import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedPlayerData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Collections;
import java.util.Optional;

public class SkullRepresentedPlayerDataProcessor extends
AbstractTileEntitySingleDataProcessor<TileEntitySkull, GameProfile, Value<GameProfile>, RepresentedPlayerData, ImmutableRepresentedPlayerData> {

    public SkullRepresentedPlayerDataProcessor() {
        super(TileEntitySkull.class, Keys.REPRESENTED_PLAYER);
    }

    @Override
    public boolean supports(TileEntitySkull skull) {
        return SkullUtils.getSkullType(skull).equals(SkullTypes.PLAYER);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (this.supports(dataHolder)) {
            final TileEntitySkull skull = (TileEntitySkull) dataHolder;
            final Optional<GameProfile> oldData = getVal(skull);
            if (SkullUtils.setProfile(skull, null)) {
                if (oldData.isPresent()) {
                    return DataTransactionBuilder.successReplaceResult(Collections.emptySet(),
                            Collections.singleton(constructImmutableValue(oldData.get())));
                } else {
                    return DataTransactionBuilder.successNoData();
                }
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected boolean set(TileEntitySkull skull, GameProfile profile) {
        return SkullUtils.setProfile(skull, profile);
    }

    @Override
    protected Optional<GameProfile> getVal(TileEntitySkull entity) {
        return SkullUtils.getProfile(entity);
    }

    @Override
    protected ImmutableValue<GameProfile> constructImmutableValue(GameProfile value) {
        return new ImmutableSpongeValue<GameProfile>(Keys.REPRESENTED_PLAYER,  SpongeRepresentedPlayerData.NULL_PROFILE, value);
    }

    @Override
    protected RepresentedPlayerData createManipulator() {
        return new SpongeRepresentedPlayerData();
    }

}

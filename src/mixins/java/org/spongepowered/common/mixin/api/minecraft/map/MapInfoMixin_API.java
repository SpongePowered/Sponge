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
package org.spongepowered.common.mixin.api.minecraft.map;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.storage.MapItemSavedDataBridge;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(MapItemSavedData.class)
public abstract class MapInfoMixin_API extends SavedData implements MapInfo, SpongeMutableDataHolder {

    public MapInfoMixin_API(String name) {
        super(name);
    }

    // @formatter:off
    @Shadow public abstract void toggleBanner(LevelAccessor p_204269_1_, BlockPos p_204269_2_);
    // @formatter:on

    @Override
    public boolean isLinked(final ItemStack itemStack) {
        return itemStack.type() == ItemTypes.FILLED_MAP.get()
                && itemStack.get(Keys.MAP_INFO).isPresent() && itemStack.get(Keys.MAP_INFO).get() == this;
    }

    @Override
    public void addBannerDecoration(final ServerLocation bannerLocation) throws IllegalArgumentException {
        final BlockType bannerType = bannerLocation.block().type();

        Preconditions.checkArgument(bannerType instanceof BannerBlock, "Location must have a banner!");
        this.toggleBanner((LevelAccessor) bannerLocation.world(), new BlockPos(bannerLocation.blockX(), bannerLocation.blockY(), bannerLocation.blockZ()));

        this.setDirty();
    }

    @Override
    public UUID uniqueId() {
        return ((MapItemSavedDataBridge) this).bridge$getUniqueId();
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final List<org.spongepowered.api.map.decoration.MapDecoration> decorationList = new ArrayList<>();
        this.require(Keys.MAP_DECORATIONS).stream()
                .filter(org.spongepowered.api.map.decoration.MapDecoration::isPersistent)
                .forEach(decorationList::add);

        return DataContainer.createNew()
                .set(Constants.Map.MAP_UNSAFE_ID, ((MapItemSavedDataBridge) this).bridge$getMapId())
                .set(Constants.Map.MAP_DATA,
                        DataContainer.createNew()
                                .set(Constants.Map.MAP_LOCATION,            this.require(Keys.MAP_LOCATION)))
                                .set(Constants.Map.MAP_WORLD,               this.require(Keys.MAP_WORLD))
                                .set(Constants.Map.MAP_TRACKS_PLAYERS,      this.require(Keys.MAP_TRACKS_PLAYERS))
                                .set(Constants.Map.MAP_UNLIMITED_TRACKING,  this.require(Keys.MAP_UNLIMITED_TRACKING))
                                .set(Constants.Map.MAP_SCALE,               this.require(Keys.MAP_SCALE))
                                .set(Constants.Map.MAP_CANVAS,              this.require(Keys.MAP_CANVAS))
                                .set(Constants.Map.MAP_LOCKED,              this.require(Keys.MAP_LOCKED))
                                .set(Constants.Map.MAP_DECORATIONS,         decorationList);
    }
}

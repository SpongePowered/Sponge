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
package org.spongepowered.common.mixin.api.mcp.map;

import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.map.SpongeMapInfo;
import org.spongepowered.common.map.decoration.SpongeMapDecoration;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(MapData.class)
public abstract class MapInfoMixin_API implements SpongeMapInfo {
    /*
     * Lots of possible String formats:
     *  - Player name
     *  - '+' for villager treasure maps
     *  - 'frame-' + item frame entity id
     *
     * No decorations are saved to disk. Player/frames are recalculated and treasure maps have nbt on their
     * ItemStacks, therefore, due to our adding of the ability to make maps and add decorations, it could
     * get out of sync quite easily. Therefore we opt to save the ones that would be saved on ItemStacks
     * (but also leave them there)
     */
    @Shadow public Map<String, MapDecoration> mapDecorations;
    private int mapId;
    // Turns out Minecraft 1.14 actually has exactly what we want
    // Locked = stops terrain updates
    // So we're just adding it early, and it should be directly compatible
    // https://minecraft.gamepedia.com/Map#Locking
    private boolean locked = Constants.Map.DEFAULT_MAP_LOCKED;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void setMapId(String mapname, CallbackInfo ci) {
        String id = mapname.substring(Constants.Map.MAP_PREFIX.length());
        try {
            this.mapId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            SpongeImpl.getLogger().error("Map id could not be got from map name, (" + mapname + ")", e);
        }
    }

    // Save to disk if we don't want to auto explore the map
    // No point saving additional data if its default.
    @Inject(method = "writeToNBT", at = @At("RETURN"))
    public void writeNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (locked != Constants.Map.DEFAULT_MAP_LOCKED) {
            compound.setBoolean(Constants.Map.LOCKED_KEY, this.locked);
        }
        NBTTagList nbtList = new NBTTagList();
        for (Map.Entry<String, MapDecoration> entry : mapDecorations.entrySet()) {
            SpongeMapDecoration mapDecoration = (SpongeMapDecoration)entry.getValue();
            if (!mapDecoration.isPersistent()) {
                continue;
            }
            NBTTagCompound nbt = mapDecoration.getMCNBT();
            nbt.setString("id", entry.getKey());
            nbtList.appendTag(nbt);
        }
        compound.setTag(Constants.Map.DECORATIONS_KEY, nbtList);
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    public void readNBT(NBTTagCompound nbt, CallbackInfo ci) {
        if (nbt.hasKey(Constants.Map.LOCKED_KEY)) {
            this.locked = nbt.getBoolean(Constants.Map.LOCKED_KEY);
        }
        NBTTagList decorationsList = ((NBTTagList)nbt.getTag(Constants.Map.DECORATIONS_KEY));
        if (decorationsList != null) {
            for (int i = 0; i < decorationsList.tagCount(); i++) {
                NBTTagCompound decorationNbt = (NBTTagCompound) decorationsList.get(i);
                addDecoration(SpongeMapDecoration.fromNBT(decorationNbt));
            }
        }
    }

    // We could technically Inject after, but that would mean
    // searching the map for the value we just had, seems like a waste.
    @Nullable
    @SuppressWarnings("unchecked")
    @Redirect(method = "updateDecorations", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    public Object redirectPut(Map map, Object key, Object value) {
        ((SpongeMapDecoration)value).setKey((String) key);
        return map.put(key, value);
    }

    @Override
    public void setDecorations(Set<org.spongepowered.api.map.decoration.MapDecoration> newDecorations) {
        this.mapDecorations.clear();
        newDecorations.stream()
                .map(decoration -> (SpongeMapDecoration)decoration)
                .forEach(this::addDecoration);
    }

    @Override
    public Set<org.spongepowered.api.map.decoration.MapDecoration> getDecorations() {
        return this.mapDecorations.values().stream()
                .map(mapDecoration -> (org.spongepowered.api.map.decoration.MapDecoration)mapDecoration)
                .collect(Collectors.toSet());
    }

    public void addDecoration(SpongeMapDecoration mapDecoration) {
        this.mapDecorations.put(mapDecoration.getKey(), (MapDecoration) mapDecoration);
    }

    private MapInfoData getMapInfoData() {
        return new SpongeMapInfoData((MapData)(Object)this);
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public int getMapId() {
        return this.mapId;
    }

    @Override
    public boolean isLinked(MapInfo other) {
        return this.mapId == ((SpongeMapInfo)other).getMapId();
    }

    @Override
    public boolean isLinked(ItemStack itemStack) {
        return itemStack.getType() == ItemTypes.FILLED_MAP
                && this.mapId == ((net.minecraft.item.ItemStack) itemStack).getMetadata();
    }

    @Override
    public boolean validateRawData(DataView container) {
        if (!container.getInt(Constants.Map.MAP_ID).isPresent()) {
            return false;
        }
        Optional<DataView> manipulator = container.getView(Constants.Map.MAP_DATA);
        return manipulator.filter(dataView -> new SpongeMapInfoData().from((DataContainer) dataView).isPresent()).isPresent();
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {
        Optional<DataView> manipulator = container.getView(Constants.Map.MAP_DATA);
        manipulator.flatMap(dataView -> new SpongeMapInfoData().from(dataView.copy()))
            .map(this::offer)
            .orElseThrow(() -> new InvalidDataException("Manipulator data given to MapInfo was invalid!"));
        this.mapId = container.getInt(Constants.Map.MAP_ID).orElseThrow(() -> new InvalidDataException("Could not get the MapId from the container given to MapInfo"));
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Constants.Map.MAP_ID, this.mapId)
                .set(Constants.Map.MAP_DATA, getMapInfoData());
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return Sets.newHashSet(getMapInfoData());
    }

}

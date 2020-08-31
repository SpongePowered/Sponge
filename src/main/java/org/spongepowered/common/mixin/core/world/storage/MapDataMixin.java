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
package org.spongepowered.common.mixin.core.world.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.MapDecoration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(net.minecraft.world.storage.MapData.class)
public abstract class MapDataMixin implements MapDataBridge {
    /*
     * Lots of possible String formats:
     *  - Player name
     *  - '+' for villager treasure maps
     *  - 'frame-' + item frame entity id
     *
     * No decorations are saved to disk. Player/frames are recalculated and treasure maps have nbt on their
     * ItemStacks, therefore, due to our adding of the ability to make maps and add decorations, it could
     * get out of sync quite easily. Therefore we opt to save the ones that would be saved on ItemStacks
     * that aren't ones that are depend on the world surroundings (Players, item frames)
     * (but also leave them there)
     */
    @Shadow public Map<String, MapDecoration> mapDecorations;

    @Shadow public abstract void updateMapData(int x, int y);

    private int bridge$mapId = 0;
    private UUID uuid = UUID.randomUUID();
    // Turns out Minecraft 1.14 actually has exactly what we want
    // Locked = stops terrain updates
    // So we're just adding it early, and it should be directly compatible
    // https://minecraft.gamepedia.com/Map#Locking
    private boolean locked = Constants.Map.DEFAULT_MAP_LOCKED;

    @Override
    public void bridge$updateWholeMap() {
        updateMapData(0,0);
        updateMapData(Constants.Map.MAP_MAX_INDEX, Constants.Map.MAP_MAX_INDEX);
    }

    @Override
    public void bridge$setDecorations(Set<org.spongepowered.api.map.decoration.MapDecoration> newDecorations) {
        this.mapDecorations.clear();
        newDecorations.stream()
                .map(decoration -> (MapDecoration)decoration)
                .forEach(this::addDecoration);
    }

    @Override
    public Set<org.spongepowered.api.map.decoration.MapDecoration> bridge$getDecorations() {
        return this.mapDecorations.values().stream()
                .map(mapDecoration -> (org.spongepowered.api.map.decoration.MapDecoration)mapDecoration)
                .collect(Collectors.toSet());
    }

    @Override
    public int bridge$getMapId() {
        return this.bridge$mapId;
    }

    @Override
    public void bridge$setMapId(int mapId) {
        this.bridge$mapId = mapId;
    }

    @Override
    public boolean bridge$isLocked() {
        return locked;
    }

    @Override
    public void bridge$setLocked(boolean locked) {
        this.locked = locked;
    }

    @Nonnull
    @Override
    public UUID bridge$getUniqueId() {
        return this.uuid;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void setMapId(String mapname, CallbackInfo ci) {
        String id = mapname.substring(Constants.Map.MAP_PREFIX.length());
        try {
            this.bridge$mapId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            SpongeImpl.getLogger().error("Map id could not be got from map name, (" + mapname + ")", e);
        }
    }

    // Save to disk if we don't want to auto explore the map
    // No point saving additional data if its default.
    @Inject(method = "writeToNBT", at = @At("RETURN"))
    public void writeNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        if (this.locked != Constants.Map.DEFAULT_MAP_LOCKED) {
            compound.setBoolean(Constants.Map.LOCKED_KEY, this.locked);
        }
        compound.setUniqueId(Constants.Map.SPONGE_UUID_KEY, this.uuid);
        NBTTagList nbtList = new NBTTagList();
        for (Map.Entry<String, MapDecoration> entry : mapDecorations.entrySet()) {
            org.spongepowered.api.map.decoration.MapDecoration mapDecoration = (org.spongepowered.api.map.decoration.MapDecoration)entry.getValue();
            if (!((MapDecorationBridge)mapDecoration).bridge$isPersistent()) {
                continue;
            }
            NBTTagCompound nbt = MapUtil.mapDecorationToNBT(mapDecoration);
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
        if (nbt.hasKey(Constants.Map.SPONGE_UUID_KEY)) {
            this.uuid = nbt.getUniqueId(Constants.Map.SPONGE_UUID_KEY);
        }
        NBTTagList decorationsList = ((NBTTagList)nbt.getTag(Constants.Map.DECORATIONS_KEY));
        if (decorationsList != null) {
            for (int i = 0; i < decorationsList.tagCount(); i++) {
                NBTTagCompound decorationNbt = (NBTTagCompound) decorationsList.get(i);
                addDecoration(MapUtil.mapDecorationFromNBT(decorationNbt));
            }
        }
    }

    public void addDecoration(MapDecoration mapDecoration) {
        this.mapDecorations.put(((MapDecorationBridge)mapDecoration).bridge$getKey(), mapDecoration);
    }

    // We could technically Inject after, but that would mean
    // searching the map for the value we just had, seems like a waste.
    @Nullable
    @SuppressWarnings("unchecked")
    @Redirect(method = "updateDecorations", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    public Object redirectPut(Map map, Object key, Object value) {
        ((MapDecorationBridge)value).bridge$setKey((String) key);
        return map.put(key, value);
    }
}

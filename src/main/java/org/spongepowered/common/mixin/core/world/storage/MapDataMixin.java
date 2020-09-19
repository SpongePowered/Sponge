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

    @Shadow public abstract void shadow$updateMapData(int x, int y);

    private int impl$mapId = 0;
    private UUID impl$uuid = UUID.randomUUID();
    // Turns out Minecraft 1.14 actually has exactly what we want
    // Locked = stops terrain updates
    // So we're just adding it early, and it should be directly compatible
    // https://minecraft.gamepedia.com/Map#Locking
    private boolean impl$locked = Constants.Map.DEFAULT_MAP_LOCKED;

    @Override
    public void bridge$updateWholeMap() {
        this.shadow$updateMapData(0,0);
        this.shadow$updateMapData(Constants.Map.MAP_MAX_INDEX, Constants.Map.MAP_MAX_INDEX);
    }

    @Override
    public void bridge$setDecorations(final Set<org.spongepowered.api.map.decoration.MapDecoration> newDecorations) {
        this.mapDecorations.clear();
        newDecorations.stream()
                .map(decoration -> (MapDecoration)decoration)
                .forEach(this::impl$addDecorationToDecorationsMap);
    }

    @Override
    public Set<org.spongepowered.api.map.decoration.MapDecoration> bridge$getDecorations() {
        return this.mapDecorations.values().stream()
                .map(mapDecoration -> (org.spongepowered.api.map.decoration.MapDecoration)mapDecoration)
                .collect(Collectors.toSet());
    }

    @Override
    public int bridge$getMapId() {
        return this.impl$mapId;
    }

    @Override
    public void bridge$setMapId(final int mapId) {
        this.impl$mapId = mapId;
    }

    @Override
    public boolean bridge$isLocked() {
        return this.impl$locked;
    }

    @Override
    public void bridge$setLocked(final boolean locked) {
        this.impl$locked = locked;
    }

    @Nonnull
    @Override
    public UUID bridge$getUniqueId() {
        return this.impl$uuid;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void impl$setMapId(final String mapname, final CallbackInfo ci) {
        final String id = mapname.substring(Constants.Map.MAP_PREFIX.length());
        try {
            this.impl$mapId = Integer.parseInt(id);
        } catch (final NumberFormatException e) {
            SpongeImpl.getLogger().error("Map id could not be got from map name, (" + mapname + ")", e);
        }
    }

    // Save to disk if we don't want to auto explore the map
    // No point saving additional data if its default.
    @Inject(method = "writeToNBT", at = @At("RETURN"))
    public void impl$writeAdditionalNBT(final NBTTagCompound compound, final CallbackInfoReturnable<NBTTagCompound> cir) {
        if (this.impl$locked != Constants.Map.DEFAULT_MAP_LOCKED) {
            compound.setBoolean(Constants.Map.LOCKED_KEY, this.impl$locked);
        }
        compound.setUniqueId(Constants.Map.SPONGE_UUID_KEY, this.impl$uuid);
        final NBTTagList nbtList = new NBTTagList();
        for (final Map.Entry<String, MapDecoration> entry : this.mapDecorations.entrySet()) {
            final org.spongepowered.api.map.decoration.MapDecoration mapDecoration = (org.spongepowered.api.map.decoration.MapDecoration)entry.getValue();
            if (!((MapDecorationBridge) mapDecoration).bridge$isPersistent()) {
                continue;
            }
            final NBTTagCompound nbt = MapUtil.mapDecorationToNBT(mapDecoration);
            nbt.setString("id", entry.getKey());
            nbtList.appendTag(nbt);
        }
        compound.setTag(Constants.Map.DECORATIONS_KEY, nbtList);
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    public void impl$readAdditionalNBT(final NBTTagCompound nbt, final CallbackInfo ci) {
        if (nbt.hasKey(Constants.Map.LOCKED_KEY)) {
            this.impl$locked = nbt.getBoolean(Constants.Map.LOCKED_KEY);
        }
        if (nbt.hasKey(Constants.Map.SPONGE_UUID_KEY)) {
            this.impl$uuid = nbt.getUniqueId(Constants.Map.SPONGE_UUID_KEY);
        }
        final NBTTagList decorationsList = ((NBTTagList)nbt.getTag(Constants.Map.DECORATIONS_KEY));
        if (decorationsList != null) {
            for (int i = 0; i < decorationsList.tagCount(); i++) {
                final NBTTagCompound decorationNbt = (NBTTagCompound) decorationsList.get(i);
                this.impl$addDecorationToDecorationsMap(MapUtil.mapDecorationFromNBT(decorationNbt));
            }
        }
    }

    public void impl$addDecorationToDecorationsMap(final MapDecoration mapDecoration) {
        this.mapDecorations.put(((MapDecorationBridge)mapDecoration).bridge$getKey(), mapDecoration);
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "updateDecorations", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    public Object impl$setKeyOnValue(final Map map, final Object key, final Object value) {
        ((MapDecorationBridge) value).bridge$setKey((String) key);
        return map.put(key, value);
    }
}

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
package org.spongepowered.common.mixin.optimization.world.storage;

import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.optimization.OptimizedMapDataBridge;
import org.spongepowered.common.bridge.optimization.OptimizedMapInfoBridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(MapData.class)
public abstract class MapDataMixin_MapOptimization extends WorldSavedData implements OptimizedMapDataBridge {


    public MapDataMixin_MapOptimization(final String name) {
        super(name);
    }

    @Shadow @Final @Mutable private Map<PlayerEntity, MapData.MapInfo> playersHashMap;
    @Shadow public Map<String, MapDecoration> mapDecorations;
    @Shadow public List<MapData.MapInfo> playersArrayList;

    @Shadow public boolean trackingPosition;


    @Shadow protected abstract void updateDecorations(MapDecoration.Type type, World worldIn, String decorationName, double worldX, double worldZ,
            double rotationIn);

    @Shadow public byte scale;
    @Shadow public int xCenter;
    @Shadow public int zCenter;
    @Shadow public boolean unlimitedTracking;

    private Set<UUID> mapOptimizationImpl$activeWorlds = new HashSet<>();
    // Used
    private ItemStack mapOptimizationImpl$dummyItemStack = new ItemStack(Items.field_151098_aY, 1, this.mapOptimizationImpl$getMapId());

    private static Constructor<MapData.MapInfo> mapOptimizationImpl$mapInfoConstructor;
    // Forge changes the type of this field from 'byte' to 'integer'
    // To support both SpongeVanilla and SpongeForge, we use reflection to access it
    private static Field mapOptimizationImpl$dimensionField;



    static {
        try {
            mapOptimizationImpl$mapInfoConstructor = MapData.MapInfo.class.getDeclaredConstructor(MapData.class, PlayerEntity.class);
            if (SpongeImplHooks.isDeobfuscatedEnvironment()) {
                mapOptimizationImpl$dimensionField = MapData.class.getDeclaredField("dimension");
            } else {
                mapOptimizationImpl$dimensionField = MapData.class.getDeclaredField("field_76200_c");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private Integer mapOptimizationImpl$getMapId() {
        return Integer.valueOf(this.field_76190_i.split("map_")[1]);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void mapOptimization$initPlayerHashmap(final CallbackInfo ci) {
        this.playersHashMap = new LinkedHashMap<>();
    }

    /**
     * @reason This method is absurdly inefficient. We completely replace
     * its funcitonality with bridge$tickMap, which produces identical results with
     * thousands fewer calls to InventoryPlayer#hasItemStack
     * @author Aaron1011 - August 8th, 2018
     */
    @Overwrite
    public void updateVisiblePlayers(final PlayerEntity player, final ItemStack mapStack) {
    }

    /**
     * This method performs the important logic from updateVisiblePlayers, while
     * skipping all of the unecessary InventoryPlayer#hasItemStack checks
     *
     * Before this method is called, all players have their inventories
     * (and therefore map items) ticked by the server. When a map item is ticked,
     * our mixin makes it call MixinMapData#bridge$updatePlayer. This method
     * marks it as valid within MapData, and updates the decorations
     * from the 'Decorations' tag.
     *
     * Inside bridge$tickMap(), we use the flag set by bridge$updatePlayer to
     * determine whether or not to update or remove a player
     * from our map decorations. This eliminates the need
     * to do any InventoryPlayer#hasItemStack calls. If a player's
     * inventory contains a map corresponding to this MapData,
     * the flag will have been updated when it ticked. If the item
     * is removed from the player's inventory, the flag will
     * no longer be set before bridge$tickMap() runs.
     *
     * MixinMapData#bridge$updateItemFrameDecoration is run from
     * EntityTrackerEntryMixin - once per Itemframe, not once
     * per player per itemframe. bridge$updateItemFrameDecoration just
     * updates the frame's position (in case it teleported) in our
     * decorations. We skip running all of the unecessary logic that
     * Vanilla does (it calls updateVisiblePlayers), since that will
     * be handled in bridge$tickMap(), which runs after all entities and items
     * have ticked.
     *
     **/
    @Override
    public void mapOptimizationBridge$tickMap() {
        final List<OptimizedMapInfoBridge> mapInfosToUpdate = new ArrayList<>(this.playersHashMap.size());
        try {
            final Iterator<Map.Entry<PlayerEntity, MapData.MapInfo>> it = this.playersHashMap.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<PlayerEntity, MapData.MapInfo> entry = it.next();
                final PlayerEntity player = entry.getKey();
                final MapData.MapInfo mapInfo = entry.getValue();
                final OptimizedMapInfoBridge mixinMapInfo = (OptimizedMapInfoBridge) mapInfo;
                if (player.field_70128_L) {
                    it.remove();
                    continue;
                }

                if (!mixinMapInfo.mapOptimizationBridge$isValid()) {
                    this.mapDecorations.remove(player.func_70005_c_());
                } else {
                    if (this.trackingPosition && mapOptimizationImpl$dimensionField.get(this).equals(player.field_71093_bK)) {
                        this.updateDecorations(MapDecoration.Type.PLAYER, player.field_70170_p, player.func_70005_c_(), player.field_70165_t, player.field_70161_v,
                                (double) player.field_70177_z);
                    }
                    // We invalidate the player's map info every tick.
                    // If the map item is still in the player's hand, the MapInfo
                    // will have mapOptimizationBridge$setValid(true) called when the item ticks.
                    // Otherwise, it will remain invalid
                    mapInfosToUpdate.add(mixinMapInfo);
                }
            }

            this.mapOptimizationImpl$updatePlayersInWorld();

            // We only invalidate the MapInfos after calling mapOptimizationImpl$updatePlayersInWorld
            // This allows mapOptimizationImpl$updatePlayersInWorld to skip sending a duplicate packet
            // to players with a valid entry
            for (final OptimizedMapInfoBridge mapInfo: mapInfosToUpdate) {
                mapInfo.mapOptimizationBridge$setValid(false);
            }

        } catch (final Exception e) {
            SpongeImpl.getLogger().error("Exception ticking map data!", e);
        }
    }

    // In EntityPlayerMP#onUpdateEntity, players have map data packets
    // sent to them for each map in their inventory.
    // In this method, we send map data packets to players in the same world
    // as an ItemFrame containing a map. In Vanilla, this is done in EntityTrackerEntry,
    // for every single ItemFrame in the game. This is completely unecessary - we only
    // need to send update packets once per player per unique MapData.

    // To further improve performance, we skip sending map update packets
    // to players who already have the same map in their inventory
    private void mapOptimizationImpl$updatePlayersInWorld() {
        // Copied from EntityTrackerEntry#updatePlayerList
        if (Sponge.getServer().getRunningTimeTicks() % 10 == 0) {
            for (final org.spongepowered.api.world.World world: Sponge.getServer().getWorlds()) {
                if (!this.mapOptimizationImpl$activeWorlds.contains(world.getUniqueId())) {
                    continue;
                }
                for (final Player player: world.getPlayers()) {
                    // Copied from EntityTrackerEntry#updatePlayerList

                    final ServerPlayerEntity entityplayermp = (ServerPlayerEntity) player;
                    OptimizedMapInfoBridge mapInfo = (OptimizedMapInfoBridge) this.playersHashMap.get(player);
                    if (mapInfo != null && mapInfo.mapOptimizationBridge$isValid()) {
                        continue; // We've already sent the player a map data packet for this map
                    }

                    // Create a MapInfo for use by createMapDataPacket
                    if (mapInfo == null) {
                        mapInfo = (OptimizedMapInfoBridge) this.constructMapInfo(entityplayermp);
                        this.playersHashMap.put(entityplayermp, (MapData.MapInfo) mapInfo);
                    }

                    //mapdata.updateVisiblePlayers(entityplayermp, itemstack); - Sponge - this is handled above in bridge$tickMap
                    final IPacket<?> packet = Items.field_151098_aY.func_150911_c(this.mapOptimizationImpl$dummyItemStack, (World) world, entityplayermp);

                    if (packet != null)
                    {
                        entityplayermp.field_71135_a.func_147359_a(packet);
                    }
                }
            }
        }
    }

    // Use playersHashMap instead of playersArrayList, since we skip updating playersArrayList
    @Redirect(method = "updateMapData", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", remap = false))
    private Iterator<?> mapOptimization$GetIteratorFromPlayerHashMap(final List<?> this$0) {
        return this.playersHashMap.values().iterator();
    }



    // MapInfo is a non-static inner class, so we need to use reflection to call
    // the constructor
    private MapData.MapInfo constructMapInfo(final PlayerEntity player) {
        try {
            return mapOptimizationImpl$mapInfoConstructor.newInstance(this, player);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to construct MapInfo for player " + player, e);
        }
    }

    @Override
    public void mapOptimizationBridge$updatePlayer(final PlayerEntity player, final ItemStack mapStack) {
        MapData.MapInfo info = this.playersHashMap.get(player);
        if (info == null) {
            info = this.constructMapInfo(player);
            this.playersHashMap.put(player, info);
        }
        ((OptimizedMapInfoBridge) info).mapOptimizationBridge$setValid(true);

        if (mapStack.func_77942_o() && mapStack.func_77978_p().func_150297_b("Decorations", 9))
        {
            final ListNBT nbttaglist = mapStack.func_77978_p().func_150295_c("Decorations", 10);

            for (int j = 0; j < nbttaglist.func_74745_c(); ++j)
            {
                final CompoundNBT nbttagcompound = nbttaglist.func_150305_b(j);

                if (!this.mapDecorations.containsKey(nbttagcompound.func_74779_i("id")))
                {
                    this.updateDecorations(MapDecoration.Type.func_191159_a(nbttagcompound.func_74771_c("type")), player.field_70170_p, nbttagcompound.func_74779_i("id"), nbttagcompound.func_74769_h("x"), nbttagcompound.func_74769_h("z"), nbttagcompound.func_74769_h("rot"));
                }
            }
        }
    }

    @Override
    public void mapOptimizationBridge$updateItemFrameDecoration(final ItemFrameEntity frame) {
        this.mapOptimizationImpl$activeWorlds.add(((Entity) frame).getWorld().getUniqueId());
        if (this.trackingPosition) {
            final BlockPos blockpos = frame.func_174857_n();
            if (blockpos == null || frame.field_174860_b == null) {
                return;
            }
            this.updateDecorations(MapDecoration.Type.FRAME, frame.field_70170_p, "frame-" + frame.func_145782_y(), (double)blockpos.func_177958_n(), (double)blockpos.func_177952_p(), (double)(frame.field_174860_b.func_176736_b() * 90));
        }
    }

    @Override
    public void mapOptimizationBridge$removeItemFrame(final ItemFrameEntity frame) {
        this.mapOptimizationImpl$activeWorlds.remove(((Entity) frame).getWorld().getUniqueId());
        this.mapDecorations.remove("frame-" + frame.func_145782_y());
    }

}

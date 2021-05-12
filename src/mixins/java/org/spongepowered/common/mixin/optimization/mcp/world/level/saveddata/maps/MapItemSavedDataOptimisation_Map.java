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
package org.spongepowered.common.mixin.optimization.mcp.world.level.saveddata.maps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.optimization.OptimizedMapDataBridge;
import org.spongepowered.common.bridge.optimization.OptimizedMapInfoBridge;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(MapItemSavedData.class)
public abstract class MapItemSavedDataOptimisation_Map extends SavedData implements OptimizedMapDataBridge {

    public MapItemSavedDataOptimisation_Map(String name) {
        super(name);
    }

    @Shadow
    @Final
    @Mutable
    private Map<Player, MapItemSavedData.HoldingPlayer> carriedByPlayers;
    @Final
    @Shadow public Map<String, MapDecoration> decorations;
    @Final
    @Shadow public List<MapItemSavedData.HoldingPlayer> carriedBy;

    @Shadow public boolean trackingPosition;

    @Shadow public byte scale;
    @Shadow public int x;
    @Shadow public int z;
    @Shadow public boolean unlimitedTracking;

    @Shadow public ResourceKey<Level> dimension;

    @Shadow protected abstract void addDecoration(MapDecoration.Type var1, @Nullable LevelAccessor var2, String var3, double var4, double var6, double var8, @Nullable Component var10);

    private Set<org.spongepowered.api.ResourceKey> mapOptimizationImpl$activeWorlds = new HashSet<>();
    // Used
    private ItemStack mapOptimizationImpl$dummyItemStack;

    // Randomise tick offset. (Set in <init> inject)
    // Normally minecraft gets these distributed by when they are loaded in,
    // but in sponge they are all ticked at once due to the optimisation. Giving
    // this a random offset stops client lag. Also randomised in MapItemSavedData_HoldingPlayerMixin_Optimization_Map
    private int randomTickOffset;

    private static Constructor<MapItemSavedData.HoldingPlayer> mapOptimizationImpl$mapInfoConstructor;

    static {
        try {
            mapOptimizationImpl$mapInfoConstructor = MapItemSavedData.HoldingPlayer.class.getDeclaredConstructor(MapItemSavedData.class, Player.class);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private Integer mapOptimizationImpl$getMapId() {
        return Integer.valueOf(this.getId().split("map_")[1]);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void mapOptimization$initPlayerHashmap(final CallbackInfo ci) {
        this.carriedByPlayers = new LinkedHashMap<>();
        this.randomTickOffset = SpongeCommon.getServer().getLevel(Level.OVERWORLD).random.nextInt(10);
        this.mapOptimizationImpl$dummyItemStack = new ItemStack(Items.FILLED_MAP, 1);
        CompoundTag tag = new CompoundTag();
        tag.putInt("map", this.mapOptimizationImpl$getMapId());
        this.mapOptimizationImpl$dummyItemStack.setTag(tag);
    }

    /**
     * @reason This method is absurdly inefficient. We completely replace
     * its funcitonality with bridge$tickMap, which produces identical results with
     * thousands fewer calls to InventoryPlayer#hasItemStack
     * @author Aaron1011 - August 8th, 2018
     */
    @Overwrite
    public void tickCarriedBy(final Player player, final ItemStack mapStack) {
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
        final List<OptimizedMapInfoBridge> mapInfosToUpdate = new ArrayList<>(this.carriedByPlayers.size());
        try {
            final Iterator<Map.Entry<Player, MapItemSavedData.HoldingPlayer>> it = this.carriedByPlayers.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<Player, MapItemSavedData.HoldingPlayer> entry = it.next();
                final Player player = entry.getKey();
                final MapItemSavedData.HoldingPlayer holdingPlayer = entry.getValue();
                final OptimizedMapInfoBridge mixinMapInfo = (OptimizedMapInfoBridge) holdingPlayer;
                if (player.isDeadOrDying()) {
                    it.remove();
                    continue;
                }

                if (!mixinMapInfo.mapOptimizationBridge$isValid()) {
                    this.decorations.remove(player.getName());
                } else {
                    if (this.trackingPosition && this.dimension.equals(player.level.dimension())) {
                        this.addDecoration(MapDecoration.Type.PLAYER, player.level, player.getName().getString(), player.getX(), player.getZ(),
                                (double) player.yHeadRot, null);
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
            SpongeCommon.getLogger().error("Exception ticking map data!", e);
        }
    }

    // In EntityPlayerMP#onUpdateEntity, players have map data packets
    // sent to them for each map in their inventory.
    // In this method, we send map data packets to players in the same world
    // as an ItemFrame containing a map. In Vanilla, this is done in ServerEntity,
    // for every single ItemFrame in the game. This is completely unecessary - we only
    // need to send update packets once per player per unique MapData.

    // To further improve performance, we skip sending map update packets
    // to players who already have the same map in their inventory
    private void mapOptimizationImpl$updatePlayersInWorld() {
        // Copied from EntityTrackerEntry#updatePlayerList
        if (Sponge.server().runningTimeTicks() % 10 == this.randomTickOffset) {
            for (final ServerWorld world : Sponge.server().worldManager().worlds()) {
                if (!this.mapOptimizationImpl$activeWorlds.contains(world.key())) {
                    continue;
                }
                for (final Object p : world.players()) {
                    // Copied from EntityTrackerEntry#updatePlayerList

                    final ServerPlayer player = (ServerPlayer) p;
                    OptimizedMapInfoBridge mapInfo = (OptimizedMapInfoBridge) this.carriedByPlayers.get(player);
                    if (mapInfo != null && mapInfo.mapOptimizationBridge$isValid()) {
                        continue; // We've already sent the player a map data packet for this map
                    }

                    // Create a MapInfo for use by createMapDataPacket
                    if (mapInfo == null) {
                        mapInfo = (OptimizedMapInfoBridge) this.constructHoldingPlayer(player);
                        this.carriedByPlayers.put(player, (MapItemSavedData.HoldingPlayer) mapInfo);
                    }

                    //mapdata.updateVisiblePlayers(entityplayermp, itemstack); - Sponge - this is handled above in bridge$tickMap
                    final Packet<?> packet = ((MapItemSavedData.HoldingPlayer)mapInfo).nextUpdatePacket(this.mapOptimizationImpl$dummyItemStack);

                    if (packet != null)
                    {
                        player.connection.send(packet);
                    }
                }
            }
        }
    }

    // Use playersHashMap instead of playersArrayList, since we skip updating playersArrayList
    @Redirect(method = "setDirty", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", remap = false))
    private Iterator<?> mapOptimization$GetIteratorFromCarriedByMap(final List<?> this$0) {
        return this.carriedByPlayers.values().iterator();
    }



    // MapInfo is a non-static inner class, so we need to use reflection to call
    // the constructor
    private MapItemSavedData.HoldingPlayer constructHoldingPlayer(final net.minecraft.world.entity.player.Player player) {
        try {
            return mapOptimizationImpl$mapInfoConstructor.newInstance(this, player);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to construct MapInfo for player " + player, e);
        }
    }

    @Override
    public void mapOptimizationBridge$updatePlayer(final net.minecraft.world.entity.player.Player player, final ItemStack mapStack) {
        MapItemSavedData.HoldingPlayer info = this.carriedByPlayers.get(player);
        if (info == null) {
            info = this.constructHoldingPlayer(player);
            this.carriedByPlayers.put(player, info);
        }
        ((OptimizedMapInfoBridge) info).mapOptimizationBridge$setValid(true);

        if (mapStack.hasTag() && mapStack.getTag().contains("Decorations", 9))
        {
            final ListTag nbttaglist = mapStack.getTag().getList("Decorations", 10);

            for (int j = 0; j < nbttaglist.size(); ++j)
            {
                final CompoundTag nbttagcompound = nbttaglist.getCompound(j);

                if (!this.decorations.containsKey(nbttagcompound.getString("id")))
                {
                    this.addDecoration(MapDecoration.Type.byIcon(nbttagcompound.getByte("type")), player.level, nbttagcompound.getString("id"), nbttagcompound.getDouble("x"), nbttagcompound.getDouble("z"), nbttagcompound.getDouble("rot"), null);
                }
            }
        }
    }

    @Override
    public void mapOptimizationBridge$updateItemFrameDecoration(final ItemFrame frame) {
        this.mapOptimizationImpl$activeWorlds.add(((Entity) frame).serverLocation().worldKey());
        if (this.trackingPosition) {
            final BlockPos blockpos = frame.getPos();
            if (blockpos == null || frame.getDirection() == null) {
                return;
            }
            this.addDecoration(MapDecoration.Type.FRAME, frame.level, "frame-" + frame.getUUID(), (double)blockpos.getX(), (double)blockpos.getZ(), (double)(frame.getDirection().get2DDataValue() * 90), null);
        }
    }

    @Override
    public void mapOptimizationBridge$removeItemFrame(final ItemFrame frame) {
        this.mapOptimizationImpl$activeWorlds.remove(((Entity) frame).serverLocation().worldKey());
        this.decorations.remove("frame-" + frame.getUUID());
    }
}
